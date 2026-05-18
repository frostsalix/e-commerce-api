package com.frostsalix.eco_web_api.controller;

import com.frostsalix.eco_web_api.dto.LoginDTO;
import com.frostsalix.eco_web_api.dto.LoginResponseDTO;
import com.frostsalix.eco_web_api.dto.ProductDTO;
import com.frostsalix.eco_web_api.model.OrderStatus;
import com.frostsalix.eco_web_api.repository.OrderRepository;
import com.frostsalix.eco_web_api.repository.ProductRepository;
import com.frostsalix.eco_web_api.repository.UserRepository;
import com.frostsalix.eco_web_api.service.OrderService;
import com.frostsalix.eco_web_api.service.ProductService;
import com.frostsalix.eco_web_api.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public AdminController(
            UserService userService,
            ProductService productService,
            OrderService orderService,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            UserRepository userRepository
    ) {
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletResponse response,
            RedirectAttributes ra
    ) {
        try {
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setUsername(username);
            loginDTO.setPassword(password);
            LoginResponseDTO result = userService.login(loginDTO);
            Cookie cookie = new Cookie("jwt_token", result.getToken());
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(3600);
            response.addCookie(cookie);
            return "redirect:/admin";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/login";
        }
    }

    @GetMapping("")
    public String dashboard(Model model) {
        model.addAttribute("productCount", productRepository.count());
        model.addAttribute("orderCount", orderRepository.count());
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("pendingOrders", orderRepository.countByStatus(OrderStatus.PENDING));
        model.addAttribute("paidOrders", orderRepository.countByStatus(OrderStatus.PAID));
        model.addAttribute("shippedOrders", orderRepository.countByStatus(OrderStatus.SHIPPED));
        model.addAttribute("doneOrders", orderRepository.countByStatus(OrderStatus.DONE));
        model.addAttribute("cancelledOrders", orderRepository.countByStatus(OrderStatus.CANCELLED));

        model.addAttribute("recentOrders", orderRepository.findAll(
                PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("createdAt").descending())
        ).getContent());

        model.addAttribute("lowStockProducts", productRepository.findByStockLessThan(5));

        return "admin/dashboard";
    }

    @GetMapping("/products")
    public String products(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var productPage = productService.searchProducts(null, null, null, page, size);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        return "admin/products";
    }

    @PostMapping("/products")
    public String addProduct(@Valid @ModelAttribute ProductDTO dto, RedirectAttributes ra) {
        try {
            productService.addProduct(dto);
            ra.addFlashAttribute("success", "Product added successfully");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/update")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductDTO dto,
            RedirectAttributes ra
    ) {
        try {
            productService.updateProduct(id, dto);
            ra.addFlashAttribute("success", "Product updated successfully");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes ra) {
        try {
            productService.deleteProduct(id);
            ra.addFlashAttribute("success", "Product deleted successfully");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt_token", "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/admin/login";
    }

    @GetMapping("/orders")
    public String orders(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status
    ) {
        var pageable = PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by("createdAt").descending());

        var orderPage = (status != null && !status.isBlank())
                ? orderRepository.findByStatus(OrderStatus.valueOf(status), pageable)
                : orderRepository.findAll(pageable);

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("currentStatus", status);
        return "admin/orders";
    }

    @PostMapping("/orders/{id}/ship")
    public String shipOrder(
            @PathVariable Long id,
            @RequestParam String trackingNumber,
            RedirectAttributes ra
    ) {
        try {
            orderService.shipOrder(id, trackingNumber);
            ra.addFlashAttribute("success", "Order shipped");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable Long id, RedirectAttributes ra) {
        try {
            orderService.cancelOrderByAdmin(id);
            ra.addFlashAttribute("success", "Order cancelled");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    @PostMapping("/orders/{id}/deliver")
    public String deliverOrder(@PathVariable Long id, RedirectAttributes ra) {
        try {
            orderService.deliverOrder(id);
            ra.addFlashAttribute("success", "Order delivered");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders";
    }
}
