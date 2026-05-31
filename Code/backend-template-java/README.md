# Backend Template Java

Java 17 + Spring Boot 3.x + MySQL + MyBatis-Plus 后端模板。

## 当前定位

- 工程目录：`Code/backend-template-java`
- Java 包名：`com.template`
- 构建工具：Maven
- 工程形态：单体分模块，后期可升级 Maven 多模块单体
- 接口前缀：`/api/**`
- 认证方案：Bearer JWT，accessToken 30 分钟，暂不做 refreshToken
- 权限模型：RBAC + 组织树数据权限

## 本地命令

```powershell
D:\Coding\Maven\apache-maven-3.9.15\bin\mvn.cmd compile
```

含义：使用本机 Maven 编译后端项目，验证 Java 源码和依赖是否正确。

## 目录说明

```text
src/main/java/com/template/
├── BackendTemplateApplication.java
├── common/      # 通用响应、异常、枚举、配置
├── security/    # JWT、安全配置、认证上下文
├── system/      # 用户、角色、菜单、组织、配置项
├── article/     # 文章、分类、评论
├── file/        # 文件上传、本地存储
└── audit/       # 登录日志、操作日志
```

## Mock SQL

Mock SQL 位于：

```text
src/main/resources/db/mock/
```

每开发一个业务模块，需要同步维护对应 Mock SQL。
