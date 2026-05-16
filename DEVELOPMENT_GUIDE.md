# eco-web-api 开发文档

## 1. 项目概览

`eco-web-api` 是一个基于 Spring Boot 的电商后端 API，核心能力包括：
- 用户注册/登录（JWT）
- 商品管理与搜索分页
- 购物车增删改查
- 下单、支付、订单状态流转
- 支付宝回调（当前为 Mock 链路）

响应格式统一为：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

---

## 2. 技术栈

- Java 21
- Spring Boot 4.0.6
- Spring Security + JWT
- Spring Data JPA + MySQL
- Spring Validation
- Springdoc OpenAPI（Swagger UI）
- Lombok

---

## 3. 本地开发环境

### 3.1 必备软件

- JDK 21
- MySQL 8+
- Maven（可直接用 Maven Wrapper）

### 3.2 关键配置（`src/main/resources/application.properties`）

当前支持以下环境变量覆盖：
- `DB_URL`（默认：`jdbc:mysql://127.0.0.1:3306/ecommerce?useSSL=false&serverTimezone=UTC`）
- `DB_USERNAME`（默认：`root`）
- `DB_PASSWORD`（默认：`qqappzWzj.`）
- `ALIPAY_NOTIFY_SIGN`（默认：`demo-sign`）

JWT 密钥通过 `JWT_SECRET` 读取，建议本地显式设置（至少 32 字符）。

---

## 4. 常用命令

```powershell
# 构建
.\mvnw.cmd clean package

# 跳过测试构建
.\mvnw.cmd clean package -DskipTests

# 运行服务
.\mvnw.cmd spring-boot:run

# 全量测试
.\mvnw.cmd test

# 单测（按类）
.\mvnw.cmd -Dtest=EcoWebApiApplicationTests test

# 单测（按方法）
.\mvnw.cmd -Dtest=EcoWebApiApplicationTests#contextLoads test
```

Swagger UI：
- `http://127.0.0.1:8080/swagger-ui.html`

---

## 5. 代码结构

主包：`com.frostsalix.eco_web_api`

- `controller`：接口层
- `service`：业务层
- `repository`：数据访问层
- `model`：实体与枚举
- `dto`：请求/响应数据结构
- `config`：安全与基础配置
- `exception`：全局异常处理
- `common`：统一响应结构

---

## 6. 核心业务设计

### 6.1 鉴权与权限

- 登录成功后返回 JWT。
- `JwtFilter` 从 `Authorization: Bearer xxx` 解析用户身份与角色。
- `/users/login`、`/users/register`、`/payment/webhook` 放行。
- 管理员写接口使用角色控制（含 `@PreAuthorize` 与路由规则）。

### 6.2 订单与库存

- `createOrder` 阶段仅校验库存，不扣减库存。
- `payment success` 阶段才扣减库存（悲观锁）。
- 取消已支付订单时会回滚库存并将支付标记为退款。

### 6.3 支付宝（Mock）

- `POST /payment/{orderId}/alipay`：生成支付单与 `payUrl`。
- `POST /payment/webhook`：接收回调参数，校验 `sign` 后推进支付成功流程。
- 当前为 Mock 版本：签名校验为配置值比对，非官方 SDK 全链路验签。

---

## 7. API 联调建议

推荐顺序：
1. 登录获取 token
2. 浏览商品并加入购物车
3. 创建订单
4. 发起支付宝支付单
5. 回调后查询订单状态是否为 `PAID`

前端详细清单见：
- `FRONTEND_INTEGRATION_CHECKLIST.md`

---

## 8. 测试与质量

当前已有测试覆盖：
- 基础上下文启动测试
- 商品搜索分页服务测试
- 购物车/订单分页服务测试
- 支付宝服务测试

项目目前没有单独的 lint/checkstyle/spotbugs 任务。

---

## 9. 后续建议（面向生产）

### 9.1 支付链路生产化（最高优先级）

1. 接入官方支付宝 SDK
   - 使用正式 `appId`、私钥、公钥证书链。
   - `createAlipayOrder` 改为官方下单接口（而非本地拼接 `payUrl`）。
2. 回调验签升级
   - 以官方验签 API 验证 `sign`，校验 `charset/sign_type`。
   - 验签失败记录安全日志并拒绝处理。
3. 回调幂等与防重放
   - 以 `outTradeNo + tradeStatus` 建立幂等键。
   - 已处理成功回调直接返回成功，避免重复扣库存。
   - 增加回调时间窗口校验与 nonce/replay 防护策略。
4. 交易一致性兜底
   - 增加主动查单任务（按 `outTradeNo` 查询支付宝订单状态）。
   - 对“本地未成功但第三方已成功”的订单做补偿。

### 9.2 安全与配置治理

1. 配置与密钥管理
   - 全部敏感配置改为环境变量或密钥管理服务（如 AWS Secrets Manager）。
   - 禁止在仓库中保留默认口令或默认签名值。
2. 认证体系增强
   - 增加 refresh token 与 token 失效策略。
   - 支持强制登出/黑名单机制（例如 Redis 存储失效 token jti）。
3. 接口防护
   - 增加登录/支付相关接口限流。
   - 对管理端接口增加审计日志（操作人、时间、变更前后值）。

### 9.3 稳定性与可观测性

1. 结构化日志
   - 统一 JSON 日志格式，关键链路增加 `traceId/orderId/paymentId`。
2. 指标与告警
   - 暴露核心指标：下单成功率、支付成功率、回调失败率、库存扣减失败数。
   - 配置告警阈值（如回调失败率超阈触发告警）。
3. 异常追踪
   - 接入 APM/错误追踪（如 OpenTelemetry + Prometheus/Grafana/Sentry）。

### 9.4 测试与发布流程

1. 自动化测试补齐
   - 补充 Controller 层接口测试（权限、参数校验、异常路径）。
   - 增加支付回调幂等测试与库存并发测试。
2. CI/CD 管道
   - PR 阶段：编译 + 测试 + 安全扫描。
   - 主分支：制品构建、镜像发布、分环境部署（dev/staging/prod）。
3. 数据库变更规范
   - 引入 Flyway/Liquibase 管理 schema 变更，避免仅依赖 `ddl-auto=update`。

### 9.5 前后端分离上线准备

1. 增加 CORS 白名单（按环境区分前端域名）。
2. 明确错误码约定（业务码与 HTTP 状态的映射策略）。
3. 固化 API 版本策略（如 `/api/v1`），减少后续升级对前端的破坏性影响。
