# eco-web-api

Spring Boot 电商后端 API —— 用户认证、商品管理、购物车、订单生命周期、支付宝支付、管理员面板。

## 技术栈

| 层 | 技术 |
|---|---|
| 运行时 | Java 21 · Spring Boot 4.0.6 |
| 安全 | Spring Security · JWT (stateless) · BCrypt |
| 持久层 | Spring Data JPA · Hibernate · MySQL |
| 支付 | Alipay SDK (page pay + async notify) |
| API 文档 | Swagger / OpenAPI 3 |
| 模板 | Thymeleaf (admin panel) |
| 构建 | Maven Wrapper |

## 快速开始

**前置条件：** JDK 21、MySQL 8+ 运行中、数据库 `ecommerce` 已创建。

```bash
# 克隆并启动
git clone <repo-url> && cd eco-web-api
./mvnw spring-boot:run
```

应用启动后：
- API 服务：`http://127.0.0.1:8080`
- Swagger UI：`http://127.0.0.1:8080/swagger-ui.html`
- 管理面板：`http://127.0.0.1:8080/admin/login`

## 项目结构

```
src/main/java/com/frostsalix/eco_web_api/
├── common/
│   └── ApiResponse.java          # 统一响应包装
├── config/
│   ├── SecurityConfig.java       # Spring Security 配置
│   ├── JwtFilter.java            # JWT 认证过滤器
│   └── PasswordConfig.java       # BCrypt 密码编码器
├── controller/
│   ├── UserController.java       # 注册/登录 (REST)
│   ├── ProductController.java    # 商品 CRUD (REST)
│   ├── CartController.java       # 购物车 (REST)
│   ├── OrderController.java      # 订单 (REST)
│   ├── PaymentController.java    # 支付宝支付 (REST)
│   └── AdminController.java      # 管理面板 (MVC/Thymeleaf)
├── dto/                          # 请求/响应 DTO
├── model/
│   ├── User.java                 # 用户实体
│   ├── Product.java              # 商品实体
│   ├── CartItem.java             # 购物车项
│   ├── Order.java                # 订单
│   ├── OrderItem.java            # 订单明细 (商品快照)
│   ├── Payment.java              # 支付记录
│   ├── OrderStatus.java          # 订单状态枚举
│   ├── PaymentStatus.java        # 支付状态枚举
│   └── PaymentMethod.java        # 支付方式枚举
├── repository/                   # JPA Repository 接口
├── service/
│   ├── UserService.java          # 用户业务逻辑
│   ├── ProductService.java       # 商品业务逻辑
│   ├── CartService.java          # 购物车业务逻辑
│   ├── OrderService.java         # 订单业务逻辑
│   ├── PaymentService.java       # 支付接口
│   ├── PaymentServiceImpl.java   # 支付实现 (含支付宝对接)
│   ├── AlipayService.java        # 支付宝 SDK 封装
│   └── AlipaySignatureVerifier.java  # 支付宝签名验证
├── exception/
│   ├── GlobalExceptionHandler.java   # 全局异常处理
│   └── ResourceNotFoundException.java
├── util/
│   └── JwtUtil.java              # JWT 生成/解析工具
└── vo/
    └── UserVO.java               # 用户视图对象
```

## 架构

```
Client
  │
  ├─ REST API (JSON) ──→ RestController ──→ Service ──→ Repository ──→ MySQL
  │
  └─ Admin Panel (HTML) ──→ AdminController (MVC) ──→ Service ──→ Repository ──→ MySQL
                                │
                                ├─ Thymeleaf templates in resources/templates/admin/
                                └─ Cookie-based JWT (login → set cookie → browser auto-send)
```

**响应格式：** 所有 REST 端点统一返回 `ApiResponse<T>`：
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

HTTP 状态码始终为 200。业务错误通过 JSON `code` 字段传递（400 = 校验失败，500 = 运行时异常）。

**认证流程：**
1. 客户端 `POST /users/login` 获取 JWT token
2. 后续请求在 `Authorization: Bearer <token>` 中携带
3. `JwtFilter` 从 token 解析 username/role 注入 `SecurityContextHolder`
4. 无数据库查询 —— 完全无状态

**授权规则：**

| 操作 | 角色 |
|------|------|
| 注册 / 登录 | 无需认证 |
| 浏览商品 | 无需认证 |
| 购物车 / 下单 / 支付 | USER 或 ADMIN |
| 商品增删改 | ADMIN |
| 订单发货 / 管理面板 | ADMIN |

## API 参考

### 用户

| 方法 | 端点 | 说明 | 认证 |
|------|------|------|------|
| POST | `/users/register` | 注册 | 否 |
| POST | `/users/login` | 登录，返回 JWT | 否 |

### 商品

| 方法 | 端点 | 说明 | 认证 |
|------|------|------|------|
| GET | `/products` | 搜索商品（分页、关键词、价格区间） | 否 |
| GET | `/products/{id}` | 商品详情 | 否 |
| POST | `/products` | 新增商品 | ADMIN |
| PUT | `/products/{id}` | 更新商品 | ADMIN |
| DELETE | `/products/{id}` | 删除商品 | ADMIN |

### 购物车

| 方法 | 端点 | 说明 | 认证 |
|------|------|------|------|
| GET | `/cart` | 我的购物车 | USER+ |
| POST | `/cart` | 添加商品到购物车 | USER+ |
| PUT | `/cart/{id}` | 修改购物车项数量 | USER+ |
| DELETE | `/cart/{id}` | 删除购物车项 | USER+ |

### 订单

| 方法 | 端点 | 说明 | 认证 |
|------|------|------|------|
| POST | `/orders` | 创建订单（从购物车） | USER+ |
| GET | `/orders` | 我的订单列表 | USER+ |
| GET | `/orders/{id}` | 订单详情 | USER+ |
| PUT | `/orders/{id}/cancel` | 取消订单（用户自助） | USER+ |

### 支付

| 方法 | 端点 | 说明 | 认证 |
|------|------|------|------|
| POST | `/payment/{orderId}` | 模拟支付（开发环境） | USER+ |
| POST | `/payment/{orderId}/alipay` | 发起支付宝支付，返回支付表单 | USER+ |
| POST | `/payment/webhook` | 支付宝异步通知回调 | 否 |

### 管理 (Admin Panel)

| 方法 | 端点 | 说明 | 认证 |
|------|------|------|------|
| GET | `/admin` | 仪表板（统计概览） | ADMIN |
| GET/POST | `/admin/products` | 商品管理页 | ADMIN |
| GET | `/admin/orders` | 订单管理页（含状态筛选） | ADMIN |
| POST | `/admin/orders/{id}/ship` | 发货（填物流单号） | ADMIN |
| POST | `/admin/orders/{id}/deliver` | 标记已送达 | ADMIN |
| POST | `/admin/orders/{id}/cancel` | 管理员取消订单 | ADMIN |
| GET | `/admin/users` | 用户列表 | ADMIN |
| POST | `/admin/users/{id}/role` | 修改用户角色 | ADMIN |

## 业务设计

### 订单状态机

```
PENDING ──→ PAID ──→ SHIPPED ──→ DONE
  │          │           │
  └──→ CANCELLED ←──────┘
```

- **PENDING**：订单已创建，等待支付
- **PAID**：支付成功，库存已扣减，等待发货
- **SHIPPED**：已发货，等待确认收货
- **DONE**：已确认收货，订单完成
- **CANCELLED**：已取消（用户自助 / 管理员取消）

### 库存扣减时机

**库存在实际支付成功时扣减，而非下单时。**

- `OrderService.createOrder()` 仅校验库存是否充足，不扣减
- `PaymentServiceImpl.success()` 使用 `SELECT ... FOR UPDATE`（悲观写锁）完成原子扣减
- 两个方法均在 `@Transactional` 中，保证一致性

### 订单快照

`OrderItem` 存储下单时刻的商品名称和价格（`productName`、`productPrice`）。即使后续商品涨价或下架，订单明细不变。

### 支付与订单解耦

`Payment.orderId` 是普通 `Long` 字段，不与 `Order` 建立 JPA 实体关联。支付和订单在 ORM 层面独立。

## 环境变量

| 变量 | 默认值 | 说明 |
|------|------|------|
| `DB_URL` | `jdbc:mysql://127.0.0.1:3306/ecommerce?...` | 数据库连接 |
| `DB_USERNAME` | `root` | 数据库用户 |
| `DB_PASSWORD` | — | 数据库密码 |
| `JWT_SECRET` | (内置) | JWT 签名密钥 |
| `ALIPAY_APP_ID` | — | 支付宝应用 ID |
| `ALIPAY_PRIVATE_KEY` | — | 应用私钥 |
| `ALIPAY_PUBLIC_KEY` | — | 支付宝公钥 |
| `ALIPAY_GATEWAY_URL` | `https://openapi.alipay.com/gateway.do` | 支付宝网关 |
| `ALIPAY_NOTIFY_URL` | — | 支付异步通知地址 |
| `ALIPAY_RETURN_URL` | — | 支付完成同步跳转地址 |

## 构建

```bash
./mvnw clean package              # 完整构建
./mvnw clean package -DskipTests  # 跳过测试
```

## 项目来源

[roadmap.sh — E-Commerce API](https://roadmap.sh/projects/ecommerce-api)
