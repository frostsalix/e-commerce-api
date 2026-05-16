# eco-web-api

基于 Spring Boot 的电商后端 API，实现用户认证、商品管理、购物车、订单生命周期、支付宝支付全链路。

## 技术栈

Java 21 · Spring Boot 4.0.6 · Spring Security + JWT · Spring Data JPA · MySQL · Alipay SDK

## 快速开始

```bash
# 确保 MySQL 运行，数据库 ecommerce 已创建
./mvnw spring-boot:run
```

Swagger UI: `http://127.0.0.1:8080/swagger-ui.html`

## API 概览

| 模块 | 端点 |
|------|------|
| 用户 | `POST /users/register` `POST /users/login` |
| 商品 | `GET /products` `POST /products` `PUT /products/{id}` `DELETE /products/{id}` |
| 购物车 | `GET /cart` `POST /cart` `PUT /cart/{id}` `DELETE /cart/{id}` |
| 订单 | `POST /orders` `GET /orders` `GET /orders/{id}` `PUT /orders/{id}/cancel` |
| 管理 | `PUT /orders/{id}/ship` `PUT /orders/{id}/deliver` |
| 支付 | `POST /payment/{orderId}` `POST /payment/{orderId}/alipay` `POST /payment/webhook` |

## 配置

关键环境变量：

| 变量 | 说明 |
|------|------|
| `DB_URL` `DB_USERNAME` `DB_PASSWORD` | 数据库连接 |
| `JWT_SECRET` | JWT 签名密钥 |
| `ALIPAY_APP_ID` `ALIPAY_PRIVATE_KEY` `ALIPAY_PUBLIC_KEY` | 支付宝凭证 |
| `ALIPAY_NOTIFY_URL` | 支付宝异步通知地址 |

## 项目来源

[roadmap.sh E-Commerce API](https://roadmap.sh/projects/ecommerce-api)
