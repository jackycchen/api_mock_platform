# API Mock Platform

API Mock Platform 是一个自主可控的API Mock平台，支持前后端分离开发模式下的接口模拟和管理。

## 项目特性

- 🚀 **现代化技术栈**：SpringBoot 3 + Vue 3 + Element Plus
- 🔧 **完整功能**：项目管理、接口管理、Mock引擎、代理服务
- ⚙️ **项目设置**：环境配置、成员管理、权限控制、危险操作管理
- 🐳 **容器化部署**：Docker + Docker Compose 一键部署
- 🎯 **自主可控**：支持定制化业务场景
- 📊 **监控统计**：接口调用统计、性能监控
- 🔒 **安全认证**：JWT令牌、角色权限、操作审计

## 技术架构

### 后端技术栈
- SpringBoot 3.2.5
- JDK 17
- Spring Data JPA + MySQL 8.0
- H2 Database (测试环境)
- Spring Data Redis 6.0
- Spring Security + JWT
- Swagger/OpenAPI 3
- iText PDF (报告生成)
- JavaFaker (Mock数据生成)

### 前端技术栈
- Vue 3 + Vite
- Element Plus UI框架
- Vue Router + Pinia状态管理
- Axios HTTP客户端

### 部署方案
- Docker + Docker Compose
- Nginx反向代理
- MySQL + Redis数据存储

## 项目结构

```
api_mock_platform/
├── backend/                 # SpringBoot后端
│   ├── src/main/java/com/apimock/
│   │   ├── controller/     # 控制器层
│   │   │   ├── auth/       # 认证控制器
│   │   │   ├── project/    # 项目控制器
│   │   │   └── user/       # 用户控制器
│   │   ├── service/        # 业务逻辑层
│   │   ├── repository/     # 数据访问层
│   │   ├── entity/         # 实体类
│   │   │   ├── user/       # 用户实体
│   │   │   └── project/    # 项目实体
│   │   ├── dto/           # 数据传输对象
│   │   │   ├── auth/       # 认证DTO
│   │   │   ├── project/    # 项目DTO
│   │   │   └── user/       # 用户DTO
│   │   ├── config/        # 配置类
│   │   ├── security/      # 安全配置
│   │   ├── util/          # 工具类
│   │   └── exception/     # 异常处理
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   ├── application-test.properties
│   │   └── db/migration/  # 数据库迁移脚本
│   ├── pom.xml
│   └── Dockerfile
├── frontend/               # Vue3前端
│   ├── src/
│   │   ├── components/    # 全局组件
│   │   ├── views/         # 页面组件
│   │   │   ├── auth/      # 认证页面
│   │   │   ├── project/   # 项目管理页面
│   │   │   └── user/      # 用户管理页面
│   │   ├── router/        # 路由配置
│   │   ├── store/         # Pinia状态管理
│   │   ├── api/           # API接口
│   │   ├── utils/         # 工具函数
│   │   ├── styles/        # 样式文件
│   │   └── assets/        # 静态资源
│   ├── package.json
│   ├── vite.config.js
│   ├── Dockerfile
│   └── nginx.conf
├── docs/                   # 项目文档
│   ├── PRD.md             # 产品需求文档
│   ├── USER_STORIES.md    # 用户故事
│   ├── USER_JOURNEY.md    # 用户旅程
│   ├── TECH_STACK.md      # 技术栈文档
│   └── TODO.md            # 开发任务清单
├── test-e2e.sh            # 端到端测试脚本
├── docker-compose.yml     # Docker编排
├── CLAUDE.md              # AI开发助手配置
└── README.md              # 项目说明
```

## 快速开始

### 前置要求
- Docker & Docker Compose
- JDK 17+（本地开发）
- Node.js 18+（本地开发）
- MySQL 8.0+（生产环境）
- Redis 6.0+（生产环境）

### 使用Docker部署

1. 克隆项目
```bash
git clone <repository-url>
cd api_mock_platform
```

2. 启动服务
```bash
docker-compose up -d
```

3. 访问应用
- 前端：http://localhost
- 后端API：http://localhost:8080
- API文档：http://localhost:8080/swagger-ui.html

### 本地开发

#### 后端开发
```bash
cd backend
# 使用测试配置（H2数据库）
mvn spring-boot:run -Dspring-boot.run.profiles=test

# 或使用生产配置（MySQL数据库）
mvn spring-boot:run
```

#### 前端开发
```bash
cd frontend
npm install
npm run dev
```

#### 端到端测试
```bash
# 确保后端和前端都已启动
bash test-e2e.sh
```

## 核心功能

### 1. 项目管理
- ✅ 多项目隔离管理
- ✅ 成员权限控制（观察者、开发者、管理员、所有者）
- ✅ 环境配置管理（开发、测试、预发布、生产）
- ✅ 项目基础设置（名称、描述、标签、可见性）
- ✅ 代理配置和CORS设置
- ✅ 性能参数配置（超时、限流、缓存）
- ✅ 危险操作管理（项目转移、删除、恢复）

### 2. 用户认证
- ✅ 用户注册和登录
- ✅ JWT令牌认证
- ✅ 角色权限管理
- ✅ 密码加密存储
- ✅ 登录状态保持

### 3. 接口管理
- 🚧 RESTful API定义
- 🚧 接口文档自动生成
- 🚧 在线接口测试

### 4. Mock引擎
- 🚧 智能数据生成
- 🚧 自定义Mock规则
- 🚧 条件化响应

### 5. 代理服务
- 🚧 请求拦截转发
- 🚧 智能路由配置
- 🚧 访问日志记录

### 6. 数据管理
- 🚧 Mock数据导入导出
- 🚧 数据备份恢复
- 🚧 批量数据操作

### 7. 监控统计
- 🚧 API调用统计
- 🚧 性能监控
- 🚧 使用报告生成

## 开发计划

- [x] 项目初始化和基础架构
- [x] 用户认证模块
- [x] 项目管理基础功能
- [x] 项目设置功能（环境配置、成员管理、权限控制）
- [x] 端到端测试框架
- [ ] 接口定义和管理
- [ ] Mock引擎核心功能
- [ ] 代理服务实现
- [ ] 监控统计功能
- [ ] 性能优化和测试

## 开发状态

### 当前版本：v0.3.0-alpha

#### ✅ 已完成功能
- **用户认证系统**: 注册、登录、JWT认证、角色管理
- **项目基础管理**: 项目CRUD、基础信息维护
- **项目设置功能**:
  - 基础设置（名称、描述、类型、标签、可见性）
  - 环境配置（开发、测试、预发布、生产环境）
  - 成员管理（邀请、角色分配、权限控制）
  - 危险操作（项目转移、删除、恢复）
  - 代理配置和CORS设置
  - 性能参数配置
- **测试基础设施**: 端到端测试脚本、H2测试数据库

#### 🚧 开发中功能
- API接口管理模块
- Mock数据生成引擎
- 请求代理服务

#### 📋 计划功能
- 接口文档生成
- 数据导入导出
- 监控统计面板
- 性能优化

## 贡献指南

1. Fork项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开Pull Request

## 开发规范

### 代码规范
- 后端：遵循Spring Boot最佳实践，使用RESTful API设计
- 前端：遵循Vue 3组合式API，使用Element Plus组件库
- 数据库：合理的表结构设计，规范的字段命名
- 测试：编写单元测试和集成测试，确保代码质量

### 提交规范
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具的变动

## 项目文档

- [产品需求文档](docs/PRD.md)
- [用户故事](docs/USER_STORIES.md)
- [用户旅程](docs/USER_JOURNEY.md)
- [技术栈文档](docs/TECH_STACK.md)
- [开发任务清单](docs/TODO.md)
- [AI开发助手配置](CLAUDE.md)

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

如有问题或建议，请提交 Issue 或联系开发团队。