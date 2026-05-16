# 前端对接文档清单

## 0. 通用规范（先完成）
- [ ] Base URL：`http://127.0.0.1:8080`
- [ ] Swagger UI：`http://127.0.0.1:8080/swagger-ui.html`（交互式 API 文档）
- [ ] 所有接口响应统一为：`{ code, message, data }`
- [ ] 业务成功条件：`code === 200`
- [ ] 需要登录的接口统一带：`Authorization: Bearer <token>`
- [ ] JWT 有效期 1 小时，前端需处理过期（401）后跳转登录页
- [ ] 分页参数统一：`page`（从 0 开始）、`size`
- [ ] 分页响应结构：`{ code, message, data: { content: [...], totalPages, totalElements, number, size } }`
- [ ] 环境变量（当前版本为必填）：
  - `JWT_SECRET`（JWT 签名密钥，至少 32 字符）
  - `DB_URL` / `DB_USERNAME` / `DB_PASSWORD`（MySQL 连接）
  - `ALIPAY_NOTIFY_SIGN`（支付宝回调验签值）

## 1. 登录/注册页
- [ ] `POST /users/register`：注册，body：`{ "username": "...", "password": "..." }`
- [ ] `POST /users/login`：登录，body：`{ "username": "...", "password": "..." }`，返回 `{ token }`
- [ ] 前端保存 token（localStorage/sessionStorage，刷新后恢复）

## 2. 商品列表/搜索页
- [ ] `GET /products?keyword=&minPrice=&maxPrice=&page=0&size=20`：分页搜索商品
- [ ] `GET /products/{id}`：商品详情
- [ ] 管理端（需 ADMIN 角色）：
  - [ ] `POST /products`：新增，body：`{ "name": "...", "price": 99.9, "stock": 100 }`，校验 name 非空、price>=0、stock>=0
  - [ ] `PUT /products/{id}`：更新，body 同上
  - [ ] `DELETE /products/{id}`：删除

## 3. 购物车页
- [ ] `POST /cart`：加入购物车，body：`{ "productId": 1, "quantity": 2 }`，校验 productId 非空、quantity>=1
- [ ] `GET /cart?page=0&size=20`：分页获取购物车
- [ ] `PUT /cart/{id}?quantity=1`：修改数量，校验 quantity>=1，无权修改他人购物车
- [ ] `DELETE /cart/{id}`：删除购物车项，校验归属

## 4. 结算页
- [ ] `POST /orders`：从购物车创建订单（仅校验库存，不扣减）
- [ ] 失败提示处理：库存不足 / 购物车为空

## 5. 支付页（支付宝 Mock）
- [ ] `POST /payment/{orderId}/alipay`：创建支付宝支付单，返回 `{ paymentId, outTradeNo, payForm }`
- [ ] 凭证未配置时为 Mock 实现：`payForm` 是本地拼接 URL；配置后为支付宝 SDK `pageExecute` 生成的 HTML 表单
- [ ] 前端渲染 `payForm`（Mock 时跳转 URL，生产时将 HTML 插入页面自动跳转支付宝）
- [ ] 支付后回到前端结果页，通过 `GET /orders/{id}` 轮询判断 `status === "PAID"`

## 6. 订单页
- [ ] `GET /orders?page=0&size=20`：我的订单列表（分页）
- [ ] `GET /orders/{id}`：订单详情（含 items 列表，每项有 productName、productPrice、quantity）
- [ ] `PUT /orders/{id}/cancel`：取消订单（已支付订单自动回滚库存并退款，payment 状态变为 REFUNDED）
- [ ] 管理端（需 ADMIN 角色）：
  - [ ] `PUT /orders/{id}/status?status=...`：修改订单状态，遵守状态机规则
  - [ ] `PUT /orders/{id}/ship?trackingNumber=...`：发货，trackingNumber 非空
  - [ ] `PUT /orders/{id}/deliver`：确认送达

## 7. 订单状态机
```
PENDING ──→ PAID ──→ SHIPPED ──→ DONE
   │          │
   └──→ CANCELLED ←──┘
```
- 只有 PENDING 可支付
- 只有 PAID 可发货
- 只有 SHIPPED 可确认送达
- CANCELLED / DONE 为终态，不可再变更

## 8. 支付结果与状态同步
- [ ] 后端回调接口：`POST /payment/webhook`（后端对后端，无需 Token）
- [ ] 前端不直接调 webhook
- [ ] 前端通过 `GET /orders/{id}` 判断是否 `PAID`
- [ ] 回调幂等注意：同一 `outTradeNo` 仅用于一次成功流程；重复通知可能返回“订单状态不允许支付”

## 9. 错误码与提示策略
- [ ] `code=200`：成功
- [ ] `code=400`：参数校验失败（@Valid 触发），直接提示 `message`
- [ ] `code=500`：运行时异常（含业务失败、权限不足、状态不允许等），直接提示 `message`
- [ ] JWT 无效或缺失时，可能直接返回 HTTP `401`（不一定走 `ApiResponse` 包装）
- [ ] 参数绑定失败（如路径变量格式错误）可能直接返回 HTTP `400`（容器层）

## 10. 最小联调顺序（建议）
1. 登录：`POST /users/login`，拿 `token`
2. 商品：`GET /products` 选择商品
3. 购物车：`POST /cart` 加入商品，`GET /cart` 确认
4. 下单：`POST /orders`，拿 `orderId`
5. 支付下单：`POST /payment/{orderId}/alipay`，拿 `outTradeNo`
6. 支付结果：轮询 `GET /orders/{orderId}`，直到 `status === PAID`

## 11. 字段字典（前端状态映射）
- [ ] `OrderStatus`：`PENDING` / `PAID` / `SHIPPED` / `DONE` / `CANCELLED`
- [ ] `PaymentStatus`：`PENDING` / `SUCCESS` / `REFUNDED`
- [ ] `PaymentMethod`：`ALIPAY` / `COD`

## 12. Postman 环境变量模板
```text
baseUrl=http://127.0.0.1:8080
token=
productId=
orderId=
outTradeNo=
```
