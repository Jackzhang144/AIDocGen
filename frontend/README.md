# SmartCodeEditor 前端

Vite + React 构建的桌面风格界面，提供本地文件浏览/编辑、AI 注释/解释/文档生成，以及用户登录与管理员面板。

## 开发与构建
```bash
npm install         # 首次安装依赖
npm run dev         # 启动本地开发（默认 5173）
npm run build       # 生产构建
npm run lint        # ESLint 检查
```

## 配置
- `VITE_API_URL`：后端根地址，默认 `http://localhost:8080`。在 `.env` 或启动命令中覆盖。
- 其它 UI/业务文案在 `src/i18n.js` 维护，可扩展语言。

## 主要功能
- **文件侧边栏**：使用浏览器 File System Access API 选择目录，展开/折叠子目录并打开文件。
- **Monaco 编辑器**：支持语法高亮与保存到本地；AI 注释会直接替换选区。
- **AI 面板**：右侧持久抽屉显示最新解释/文档结果，支持 Markdown 渲染。
- **身份与权限**：`src/components/AuthPanel.jsx` 负责登录/注册；`AdminPanel.jsx` 仅管理员可见，可管理用户并查看调用总量。
- **多语言**：顶部切换中/英文，影响 UI 与发送给后端的 `language` 字段。

## 关键文件
- `src/App.jsx`：整体布局与路由切换（编辑器/管理员视图）。
- `src/components/Sidebar.jsx`：目录读取与展开逻辑。
- `src/components/CodeEditor.jsx`：编辑、保存与 AI 调用入口。
- `src/components/AiPanel.jsx`：Markdown 展示。
- `src/config.js`：后端地址配置。

## 与后端的交互
所有受保护请求都会带上本地存储的 JWT：`Authorization: Bearer <token>`。AI 请求接口 `/api/ai/process` 需要携带 `type`（comment/explain/document）、`code`、`fileName`、`context`（文档模式使用）与 `language`。
