# Admin Template Template

Personal admin template based on Vue 3, TypeScript, Vite and Element Plus.

This repository is used as a frontend base for later backend development. Runtime data should come from local Mock API during development or real backend APIs after integration.

## Quick Start

```bash
pnpm install
pnpm dev
```

## Main Stack

- Vue 3
- TypeScript
- Vite
- Element Plus
- Pinia
- Vue Router
- Tailwind CSS and SCSS

## Local Mock

Development Mock API is implemented through Vite middleware:

```text
mock/local-mock.ts
```

Use `VITE_USE_LOCAL_MOCK=true` in local development environment to route covered `/api/*` requests to local Mock handlers.

## Documentation

Project documentation is maintained under the root `Docs/` directory.
