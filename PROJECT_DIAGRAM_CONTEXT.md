# Chat Service 项目图表建模资料

## 1. 项目识别

- 项目名称：`chat-service`
- 前端技术栈（桌面端）：`Electron` + `Vue` + `Element Plus`
- 后端技术栈：`Java 8` + `Spring Boot 2.6.13` + `Spring Web` + `Spring AOP` + `Spring Validation` + `MyBatis-Plus` + `PageHelper` + `Druid` + `MySQL` + `Redis` + `Redisson` + `Netty` + `Swagger2` + `Knife4j`
- 构建方式：`Maven`（`pom.xml`）
- 启动入口：`com.example.ChatServiceApplication`
- 系统架构形态：`C/S`（Client/Server）
- 客户端形态：桌面端应用（Desktop Client）
- 服务形态：HTTP API + WebSocket 实时通信（双通道）
- 默认运行配置：`dev`
- HTTP 访问前缀：`/api`
- HTTP 端口：`7777`
- WebSocket 端口：`18023`
- WebSocket 路径：`/ws`

## 2. 参与者（Actor）

- 普通用户：注册、登录、通讯录管理、消息发送、群聊操作、申请审核
- 管理员：可打开后台管理端，进行用户管理、群聊管理、系统设置、版本管理、自定义账号管理
- 客户端应用（桌面端 C 端）：调用 HTTP 接口、建立 WebSocket 连接、接收推送
- 外部基础设施：MySQL（持久化）、Redis（缓存/会话/消息辅助）

## 3. 系统角色与功能权限（用于权限矩阵图）

### 3.1 角色定义

- 普通用户：可使用聊天系统前台业务能力（含注册、登录、验证码、登录后业务）
- 管理员（已登录且具备管理权限）：可进入后台管理端并使用全部后台管理能力

### 3.2 权限边界

- 公开权限（无需 token）：`/api/account/code`、`/api/account/login`、`/api/account/register`
- 登录权限（需 token）：聊天、联系人、申请、群聊、个人信息维护等前台业务接口
- 管理权限（需管理员身份）：`/api/admin/**` 下后台管理接口 + 版本管理接口（`/api/version/**`）

### 3.3 角色-功能权限矩阵

| 功能域 | 普通用户 | 管理员 |
|---|---|---|
| 获取验证码/注册/登录 | 允许 | 允许 |
| 个人资料维护/修改密码/退出登录 | 允许 | 允许 |
| 聊天消息与文件收发 | 允许 | 允许 |
| 联系人管理与好友申请 | 允许 | 允许 |
| 群聊创建/管理/入群申请 | 允许 | 允许 |
| 后台管理端访问（打开后台） | 禁止 | 允许 |
| 后台用户管理（`/api/admin/user/**`） | 禁止 | 允许 |
| 后台群聊管理（`/api/admin/group/**`） | 禁止 | 允许 |
| 后台系统设置（`/api/admin/setting/**`） | 禁止 | 允许 |
| 后台自定义账户（`/api/admin/custom/**`） | 禁止 | 允许 |
| 版本管理（`/api/version/**`） | 禁止 | 允许 |

### 3.4 后台入口说明

- 管理员支持打开后台管理端（前端后台页面）
- 后台页面对应核心 API 前缀：`/api/admin/**`
- 推荐在前端路由中以“管理员角色”控制后台菜单与页面可见性
- 推荐在后端对管理员接口增加角色校验（除 token 校验外再做 admin 权限校验）

## 4. 功能域划分（用于功能用例图）

### 3.1 账号与认证

- 获取验证码：`GET /api/account/code`
- 登录：`POST /api/account/login`
- 注册：`POST /api/account/register`
- 更新个人信息：`POST /api/account/update`
- 查询用户信息：`GET /api/account/user/{userId}/{isAdmin}`
- 修改密码：`POST /api/account/changePwd`
- 退出登录：`GET /api/account/logout`

### 3.2 聊天与文件

- 发送消息：`POST /api/chat/send`
- 上传文件：`POST /api/chat/upload`
- 下载文件：`POST /api/chat/download`
- 查询文件限制：`GET /api/chat/fileConfig`

### 3.3 联系人与申请

- 搜索用户/群聊：`GET /api/userContact/search/{id}`
- 查询联系人列表：`GET /api/userContact/users/{id}/{status}`
- 查询联系人详情：`GET /api/userContact/user/{id}`
- 联系人状态变更（删除/拉黑等）：`POST /api/userContact/status`
- 申请加好友：`POST /api/userApply/add`
- 查询好友申请：`GET /api/userApply/all/{applicantUserId}`
- 审核好友申请：`POST /api/userApply/check`

### 3.4 群聊管理

- 新建群聊：`POST /api/group/add`
- 群聊详情：`GET /api/group/info/{groupId}`
- 解散群聊：`POST /api/group/disband`
- 修改群聊：`POST /api/group/save`
- 查询用户群聊列表：`GET /api/groupContact/groups/{userId}`
- 查询群成员：`GET /api/groupContact/group/{groupId}/{userId}`
- 查询群人数：`GET /api/groupContact/count/{groupId}`
- 批量邀请入群：`POST /api/groupContact/batchUsers`
- 申请入群：`POST /api/groupApply/add`
- 查询入群申请：`GET /api/groupApply/all/{applicantUserId}`
- 审核入群申请：`POST /api/groupApply/check`

### 3.5 管理后台

- 用户列表分页查询：`POST /api/admin/user/all`
- 用户状态设置：`GET /api/admin/user/status/{userId}`
- 用户强制下线：`GET /api/admin/user/offLine/{userId}`
- 群聊列表分页查询：`POST /api/admin/group/all`
- 群聊状态/解散：`POST /api/admin/group/status`
- 系统设置查询：`GET /api/admin/setting/all`
- 系统设置保存：`POST /api/admin/setting/save`
- 自定义账户列表：`POST /api/admin/custom/all`
- 添加自定义账户：`POST /api/admin/custom/add`
- 删除自定义账户：`GET /api/admin/custom/del/{customAccountId}`
- 获取最新版本：`GET /api/version/latest/{userId}/{currentVersion}`
- 添加版本：`POST /api/version/add`
- 保存版本：`POST /api/version/save`
- 版本分页查询：`POST /api/version/all`
- 删除版本：`DELETE /api/version/del/{id}`

## 5. 核心架构分层（用于系统架构图）

### 4.1 分层结构

- 客户端层（C）：桌面端应用（UI、联系人、会话、文件、音视频交互）
- 服务端层（S）：`chat-service`（REST + WebSocket + 业务逻辑）
- 接入层：`controller`（REST） + `websocket.netty`（Netty WebSocket）
- 业务层：`service` / `service.impl`
- 数据访问层：`mapper` + `resources/mapper/*.xml`
- 数据层：MySQL（业务数据） + Redis（token、心跳、联系人缓存、离线消息、系统设置）

### 4.2 关键组件

- 认证切面：`TokenValidationAspect` + `@GlobalTokenInterceptor`
- 全局异常：`GlobalExceptionHandler`
- WebSocket 服务：`NettyServer` + `ServerListenerHandler`
- 在线连接管理：`ChannelContextUtils`
- 跨实例消息分发：`MessageHandler`（Redisson Topic）
- 视频通话信令处理：`WebSocketMessageService`

## 6. 实时通信与消息路径（用于时序图）

### 5.1 HTTP 消息发送主路径

1. 客户端调用 `POST /api/chat/send`
2. Controller 从 `authorization` 解析登录态
3. Service 落库消息并封装 `MessageSendDto`
4. 通过 `MessageHandler` 发布到 Redis Topic（`message.topic`）
5. 订阅端收到消息后，经 `ChannelContextUtils` 推送到目标在线用户/群
6. 用户离线时写入 Redis 离线队列，待上线补发

### 5.2 WebSocket 连接与信令路径

1. 客户端连接 `ws://<host>:18023/ws?token=<token>`
2. 握手完成后在 `ServerListenerHandler` 校验 token
3. `ChannelContextUtils` 绑定 `userId -> Channel`
4. 客户端发送信令（offer/answer/candidate/call_request/...）
5. `WebSocketMessageService` 校验并路由到目标用户
6. 在线直发，不在线返回失败/不在线提示

## 7. 数据对象与领域实体（用于ER图/领域模型图）

### 6.1 主要实体（POJO）

- `UserInfo`：用户基础信息
- `UserContact`：用户联系人关系
- `UserApplyInfo`：好友申请
- `GroupInfo`：群信息
- `GroupContact`：群成员关系
- `GroupApplyInfo`：入群申请
- `ChatSession`：会话
- `ChatSessionUser`：用户会话关联
- `ChatMessage`：聊天消息
- `AppVersion`：应用版本
- `CustomAccount`：后台自定义账号

### 6.2 关键关系（抽象）

- 用户 与 用户：通过 `UserContact` 建立多对多关系
- 用户 与 群：通过 `GroupContact` 建立多对多关系
- 用户/群 与 消息：`ChatMessage` 按接收方类型关联
- 用户 与 会话：通过 `ChatSessionUser` 关联 `ChatSession`
- 申请流：`UserApplyInfo`、`GroupApplyInfo` 分别承载好友/入群申请状态

## 8. 安全与边界（用于安全架构图）

- 鉴权机制：大多数业务接口使用 `@GlobalTokenInterceptor`（基于 Redis token 校验）
- Token 来源：HTTP Header `authorization`，WebSocket Query `?token=`
- 会话控制：同用户 token 替换校验（防止旧 token 并发使用）
- 在线状态：Redis 心跳 + Netty IdleState 超时检测
- 返回规范：统一 `ResultVo<T>`

## 9. 外部依赖与部署节点（用于部署图）

- `desktop-client`（C 端桌面应用）
- `chat-service`（Spring Boot 主应用，暴露 HTTP）
- `netty-server`（同进程内启动，暴露 WebSocket）
- `mysql`（业务主库）
- `redis`（缓存/会话/消息辅助）
- 可选：`Knife4j` 文档入口 `/api/doc.html`

## 10. 建图输入模板（给 AI 直接用）

### 9.1 用例图输入

- 系统：Chat Service
- 角色：普通用户、管理员、客户端应用
- 用户核心用例：注册登录、聊天、文件上传下载、联系人管理、好友申请、群聊管理、视频通话信令
- 管理员核心用例：用户管理、群管理、系统设置、版本管理、自定义账户管理

### 9.2 架构图输入

- 架构风格：分层架构 + 事件分发（Redis Topic）
- 关键组件：Controller、Service、Mapper、MySQL、Redis、Netty、ChannelContextUtils、MessageHandler
- 关键链路：HTTP 发消息链路 + WebSocket 信令链路
- 非功能重点：高并发连接、在线状态管理、离线消息补发、鉴权一致性

### 9.3 时序图输入

- 时序 1：用户 A 发送消息给用户 B（含在线/离线分支）
- 时序 2：WebSocket 握手鉴权 + 信令转发（offer/answer/candidate）
- 时序 3：管理员强制下线用户
