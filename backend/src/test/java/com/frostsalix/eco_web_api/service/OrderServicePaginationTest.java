package com.frostsalix.eco_web_api.service;

import com.frostsalix.eco_web_api.model.Order;
import com.frostsalix.eco_web_api.model.User;
import com.frostsalix.eco_web_api.repository.CartItemRepository;
import com.frostsalix.eco_web_api.repository.OrderRepository;
import com.frostsalix.eco_web_api.repository.PaymentRepository;
import com.frostsalix.eco_web_api.repository.ProductRepository;
import com.frostsalix.eco_web_api.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServicePaginationTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private OrderService orderService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnPagedOrdersForCurrentUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice", null)
        );

        User user = new User();
        user.setUsername("alice");

        Order order = new Order();
        Page<Order> orderPage = new PageImpl<>(
                List.of(order),
                PageRequest.of(1, 5),
                11
        );

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(orderRepository.findByUser(user, PageRequest.of(1, 5))).thenReturn(orderPage);

        Page<Order> result = orderService.getMyOrders(1, 5);

        assertThat(result.getTotalElements()).isEqualTo(11);
        assertThat(result.getContent()).hasSize(1);
    }
}
