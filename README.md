# Chat Service

<div align="center">
  <h3>🚀 基于 Spring Boot + Netty 的聊天服务后台</h3>
  <p>一个现代化的高性能聊天服务端，支持实时通信和分布式架构</p>

![License](https://img.shields.io/badge/license-Non--Commercial-red.svg)
![Java](https://img.shields.io/badge/Java-1.8-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.6.13-brightgreen.svg)
![Netty](https://img.shields.io/badge/Netty-4.1.84-blue.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)
![Redis](https://img.shields.io/badge/Redis-6.0+-red.svg)

</div>

## ✨ 项目特性

- 🔥 **高性能通信** - 基于 Netty 实现的高并发 WebSocket 通信
- 🌐 **分布式架构** - 支持 Redis 集群和分布式锁
- 📊 **数据持久化** - MyBatis Plus + MySQL 高效数据操作
- 🔐 **安全验证** - 集成验证码和参数校验
- 📖 **API 文档** - Knife4j (Swagger) 自动生成接口文档
- ⚡ **连接池优化** - Druid 数据库连接池
- 🔄 **分页查询** - PageHelper 分页插件支持

## 🛠️ 技术栈

### 核心框架
- **Spring Boot** `2.6.13` - 应用框架
- **Spring Web** - Web 层框架
- **Spring AOP** - 面向切面编程
- **Spring Validation** - 参数校验

### 数据层
- **MyBatis Plus** `3.5.12` - ORM 框架
- **MySQL Connector** - 数据库驱动
- **Druid** `1.2.14` - 数据库连接池
- **PageHelper** `1.4.7` - 分页插件

### 缓存 & 分布式
- **Spring Data Redis** - Redis 集成
- **Redisson** `3.13.6` - 分布式锁和缓存

### 网络通信
- **Netty** `4.1.84.Final` - 高性能网络框架

### 工具库
- **Lombok** - 代码简化
- **FastJSON** `2.0.34` - JSON 处理
- **Easy Captcha** `1.6.2` - 验证码生成

### 文档工具
- **Knife4j** `2.0.8` - API 文档生成
- **Swagger** `2.9.2` - API 规范

## 📋 系统要求

- **JDK** >= 1.8
- **Maven** >= 3.6.0
- **MySQL** >= 8.0
- **Redis** >= 6.0

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone [your-repo-url]
cd chat-service
```

### 2. 数据库配置

创建 MySQL 数据库：
```sql
CREATE DATABASE chat CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 配置文件

编辑 `application.yml`：
```yaml
server:
  port: 8080

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/chat_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
    type: com.alibaba.druid.pool.DruidDataSource
    
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
    database: 0

# MyBatis Plus 配置
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# Netty 配置
netty:
  websocket:
    port: 9999
    path: /websocket
```

### 4. 运行项目

```bash
# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run

# 或者打包后运行
mvn clean package
java -jar target/chat-service-0.0.1-SNAPSHOT.jar
```

### 5. 访问服务

- **API 文档**: http://localhost:8080/doc.html
- **WebSocket**: ws://localhost:9999/websocket
- **健康检查**: http://localhost:8080/actuator/health

## 📁 项目结构

```
chat-service/
├── src/main/java/com/example/
│   ├── config/             # 配置类
│   │   ├── NettyConfig.java
│   │   ├── RedisConfig.java
│   │   └── SwaggerConfig.java
│   ├── controller/         # 控制器层
│   │   ├── ChatController.java
│   │   └── UserController.java
│   ├── service/           # 业务逻辑层
│   │   ├── ChatService.java
│   │   └── UserService.java
│   ├── mapper/            # 数据访问层
│   │   ├── ChatMapper.java
│   │   └── UserMapper.java
│   ├── entity/            # 实体类
│   │   ├── Chat.java
│   │   └── User.java
│   ├── dto/               # 数据传输对象
│   ├── vo/                # 视图对象
│   ├── netty/             # Netty 相关
│   │   ├── NettyServer.java
│   │   ├── WebSocketHandler.java
│   │   └── ChannelManager.java
│   ├── utils/             # 工具类
│   ├── exception/         # 异常处理
│   └── ChatServiceApplication.java
├── src/main/resources/
│   ├── application.yml    # 配置文件
│   ├── mapper/           # MyBatis 映射文件
│   └── db/               # 数据库脚本
└── pom.xml               # Maven 配置
```

## 🔍 API 文档

项目集成了 Knife4j，启动项目后访问：
- **文档地址**: http://localhost:8080/doc.html
- **JSON 格式**: http://localhost:8080/v2/api-docs

## 🎯 开发计划

- [x] 基础 WebSocket 通信
- [x] 用户认证和授权
- [x] 消息持久化
- [x] 分布式锁支持
- [x] 文件上传功能
- [x] 消息推送
- [x] 聊天室管理
- [ ] 消息加密
- [x] 集群部署
- [ ] 监控告警

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

**⚠️ 重要声明：非商业使用许可**

本项目采用严格的非商业许可证：

### 🚫 禁止商业使用
- ❌ 严禁用于任何商业目的
- ❌ 禁止商业分发和销售
- ❌ 禁止基于此项目开发商业产品

### ✅ 允许的使用
- ✅ 个人学习和技术研究
- ✅ 教育用途和学术研究
- ✅ 开源项目贡献
- ✅ 非营利组织使用

## 👨‍💻 作者信息

**学习目的声明**: 本项目为个人学习 Spring Boot + Netty 技术栈而创建，仅供技术交流和学习使用。

---

<div align="center">
  <p>⭐ 如果这个项目对你有帮助，请给个 Star 支持！</p>
  <p><strong>仅供学习交流，严禁商业使用</strong></p>
</div>