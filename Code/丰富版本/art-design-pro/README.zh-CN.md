# Admin Template Template

这是一个个人使用的后台管理系统前端模板，技术栈为 Vue 3、TypeScript、Vite、Element Plus、Pinia 和 Vue Router。

当前仓库用于后续后端开发的前端基础工程。开发阶段可以使用 Vite middleware 本地 Mock API，后端接口完成后逐步切换到真实接口。

## 快速开始

```bash
pnpm install
pnpm dev
```

## 主要技术栈

- Vue 3
- TypeScript
- Vite
- Element Plus
- Pinia
- Vue Router
- Tailwind CSS 和 SCSS

## 本地 Mock

开发环境 Mock API 位于：

```text
mock/local-mock.ts
```

本地开发可通过 `VITE_USE_LOCAL_MOCK=true` 启用已覆盖的 `/api/*` 接口响应。

## 文档

项目文档统一维护在根目录 `Docs/` 下。
