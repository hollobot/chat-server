# AGENTS.md

本文件提供给在本仓库执行任务的 agent，目标是快速对齐：

- 如何构建、运行、测试（尤其是单测）。
- 如何遵循当前项目代码风格与分层约定。

## 1) 项目速览

- 技术栈：Java 8、Spring Boot 2.6.13、MyBatis/MyBatis-Plus、Redis、Netty。
- 构建工具：Maven（存在 `pom.xml`，未发现 Gradle wrapper）。
- 主代码目录：`src/main/java/com/example`
- 资源目录：`src/main/resources`
- 测试目录：`src/test/java`

核心包结构：

- `controller`：HTTP 接口层（统一返回 `ResultVo`）。
- `service` + `service.impl`：业务接口与实现。
- `mapper` + `resources/mapper/*.xml`：持久层接口与 SQL 映射。
- `entity/pojo|dto|vo|enums|constants`：实体、传输对象、枚举与常量。
- `handler`：全局异常处理。
- `aspect` + `annotation`：鉴权等横切逻辑。
- `websocket`：Netty/WebSocket 相关代码。

---

## 2) Build / Run / Test 命令

默认在仓库根目录执行。

### 2.1 常用构建命令

- 编译（跳过测试）：`mvn -q -DskipTests compile`
- 打包（跳过测试）：`mvn -q -DskipTests package`
- 本地启动：`mvn spring-boot:run`
- 运行 jar：`java -jar target/chat-service-0.0.1-SNAPSHOT.jar`

### 2.2 测试命令（含单测）

- 全量测试：`mvn test`
- 单个测试类：`mvn -Dtest=ChatServiceApplicationTests test`
- 单个测试方法：`mvn -Dtest=ChatServiceApplicationTests#contextLoads test`

### 2.3 单测当前状态（重要）

实测 `mvn -q -Dtest=... test` 在当前仓库可能出现：

- `Tests run: 0`
- `No tests were executed`

原因通常是：`pom.xml` 未显式锁定支持 JUnit 5 的 Surefire 版本（测试类使用 `org.junit.jupiter.api.Test`）。

因此对于“先验证代码无编译错误”的任务，推荐：

- `mvn -q -DskipTests compile`

若必须在 CLI 跑测试且暂不改插件，可临时避免因 0 测试直接失败：

- `mvn -Dtest=ChatServiceApplicationTests#contextLoads test -DfailIfNoTests=false`

### 2.4 Lint / 格式化现状

- 未发现 Checkstyle / Spotless / PMD / SpotBugs 等独立 lint 插件配置。
- 当前可执行的硬性校验以“编译通过 +（可用时）测试执行”为主。
- 若后续引入 lint，请在 `pom.xml` 固定插件版本并定义执行阶段。

---

## 3) 环境前置与配置要点

- Java 目标版本：1.8（见 `maven-compiler-plugin` `source/target=1.8`）。
- Maven：建议 3.6+。
- 依赖服务：MySQL、Redis。
- profile：`src/main/resources/application.yml` 当前激活 `dev`。

执行注意：

- `@SpringBootTest` 场景通常依赖数据库/Redis 可连接。
- 配置文件里含开发样例账号与路径，禁止提交真实敏感信息。

---

## 4) 代码风格与约定

以下基于仓库现有实现提炼，新增代码请尽量保持一致。

### 4.1 命名与分层

- 包名前缀统一 `com.example`。
- Controller：`*Controller`。
- Service 接口：`*Service`；实现：`*ServiceImpl`。
- Mapper 接口：`*Mapper`；XML 与接口同名。
- DTO/VO/POJO 职责分明，不混用。
- 枚举统一 `*Enum`，业务错误码统一走 `ExceptionCodeEnum`。

### 4.2 导入与依赖

- 不使用通配符导入，保持显式 import。
- import 分组清晰：JDK/Javax -> 第三方 -> 项目内。
- 注入方式以 `@Resource` 为主，单类内保持一致。
- 已使用 Lombok 的模型类，优先复用 `@Data`/`@Slf4j` 等。

### 4.3 格式化与可读性

- 使用 4 空格缩进，不使用 Tab。
- 方法尽量短小，按“参数校验 -> 业务处理 -> 返回”组织。
- 仅保留必要注释，避免解释显而易见代码。
- 用空行隔开逻辑阶段，减少大段连续语句。

### 4.4 类型与返回模型

- 对外返回统一 `ResultVo<T>`，优先用工厂方法 `success/error`。
- 同类时间字段优先沿用现有 `Timestamp` 方案。
- 状态语义优先枚举，避免散落魔法数字。
- 分页保持 PageHelper 模式：`PageHelper.startPage` + `PageInfo<T>`。

### 4.5 参数、校验、控制层

- 控制层负责入参接收和轻转换，重业务逻辑放 Service。
- 需要登录态的接口加 `@GlobalTokenInterceptor`。
- 入参校验优先 `@Validated` + `javax.validation` 注解。
- Mapper 多参数方法使用 `@Param`，并确保与 XML 占位符一致。

### 4.6 异常与日志

- 业务异常优先抛 `CustomException(ExceptionCodeEnum)`。
- 统一由 `GlobalExceptionHandler` 映射为 `ResultVo`。
- 记录异常时包含请求路径和堆栈，禁止吞异常。
- 参数错误、鉴权失败、权限不足等应返回明确错误码。

### 4.7 SQL 与映射

- 修改 Mapper 方法时同步修改 XML（方法名、参数名、返回类型）。
- 数据库字段多为下划线命名，Java 属性使用驼峰。
- 修改查询字段后，检查 `resultMap` / VO 是否同步更新。

### 4.8 配置与安全

- 配置采用 `application.yml + application-dev.yml + application-prod.yml`。
- 禁止提交真实密码、token、私钥、生产地址等敏感信息。
- 新增配置项至少在一个 profile 完整落地，并说明默认行为。

---

## 5) Agent 最小自检清单

提交前建议至少执行：

1. `mvn -q -DskipTests compile`
2. 若变更涉及测试，尝试目标测试命令并记录是否受 Surefire/JUnit 问题影响。
3. 检查是否误改配置凭据、二进制文件或本地路径。

提交说明建议包含：

- 变更模块（controller/service/mapper/config）。
- 是否涉及 DB 字段、Redis key 或接口返回结构变化。
- 实际验证命令和结果。

---

## 6) Cursor / Copilot 规则检查结果

已检查以下路径，当前仓库未发现对应规则文件：

- `.cursorrules`
- `.cursor/rules/`
- `.github/copilot-instructions.md`

若后续新增上述文件，应将其视为高优先级约束，并同步更新本 AGENTS 文档。
