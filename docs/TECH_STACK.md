# 技术栈文档 (Technology Stack)

## 架构概览

API Mock Platform 采用前后端分离的微服务架构，支持容器化部署和水平扩展。

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端层 (Web)   │    │   网关层 (API)   │    │   服务层 (App)   │
│                 │    │                 │    │                 │
│   Vue 3 SPA     │◄──►│  Nginx Proxy    │◄──►│  SpringBoot API │
│   Element Plus  │    │  Load Balancer  │    │  Business Logic │
│   Pinia Store   │    │  Rate Limiting  │    │  Mock Engine    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                       ┌─────────────────┐    ┌─────────────────┐
                       │   缓存层 (Cache) │    │   存储层 (Data)  │
                       │                 │    │                 │
                       │   Redis Cluster │    │   MySQL Master  │
                       │   Session Store │    │   Read Replicas │
                       │   Mock Data     │    │   Backup Store  │
                       └─────────────────┘    └─────────────────┘
```

---

## 前端技术栈

### 核心框架
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **Vue 3** | 3.3.11 | 前端框架 | 组合式API、TypeScript支持、生态成熟 |
| **Vite** | 5.0.8 | 构建工具 | 开发速度快、HMR、ES模块支持 |
| **TypeScript** | 5.3.0 | 类型系统 | 类型安全、IDE支持、团队协作 |

### UI组件库
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **Element Plus** | 2.4.4 | UI组件库 | Vue 3专用、组件丰富、文档完善 |
| **@element-plus/icons-vue** | 2.3.1 | 图标库 | 与Element Plus配套、SVG图标 |

### 状态管理
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **Pinia** | 2.1.7 | 状态管理 | Vue 3官方推荐、TypeScript友好 |
| **Vue Router** | 4.2.5 | 路由管理 | Vue 3配套、支持组合式API |

### 网络请求
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **Axios** | 1.6.2 | HTTP客户端 | 拦截器支持、取消请求、错误处理 |

### 数据可视化
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **ECharts** | 5.4.3 | 图表库 | 功能强大、性能优秀、可定制性强 |
| **Vue-ECharts** | 6.6.1 | Vue集成 | 官方Vue包装、响应式支持 |

### 开发工具
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **ESLint** | 8.49.0 | 代码检查 | 代码质量保证、团队规范统一 |
| **Prettier** | 3.0.3 | 代码格式化 | 自动格式化、风格一致 |
| **Sass** | 1.69.5 | CSS预处理器 | 变量、嵌套、混入支持 |

### 前端项目结构
```
frontend/
├── src/
│   ├── components/          # 可复用组件
│   │   ├── common/         # 通用组件
│   │   ├── layout/         # 布局组件
│   │   └── business/       # 业务组件
│   ├── views/              # 页面组件
│   │   ├── auth/           # 认证相关
│   │   ├── dashboard/      # 控制台
│   │   ├── project/        # 项目管理
│   │   ├── api/           # 接口管理
│   │   ├── mock/          # Mock管理
│   │   ├── data/          # 数据管理
│   │   └── monitor/       # 监控统计
│   ├── router/            # 路由配置
│   ├── store/             # 状态管理
│   ├── utils/             # 工具函数
│   ├── assets/            # 静态资源
│   └── types/             # TypeScript类型定义
├── public/                # 公共资源
├── tests/                 # 测试文件
└── docs/                  # 组件文档
```

---

## 后端技术栈

### 核心框架
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **Spring Boot** | 3.2.5 | 应用框架 | 自动配置、微服务支持、生态丰富 |
| **JDK** | 1.8 | 运行环境 | 稳定性好、企业级应用标准 |
| **Maven** | 3.9.0 | 项目管理 | 依赖管理、构建自动化 |

### Web层
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **Spring Web MVC** | 6.1.0 | Web框架 | RESTful API、注解驱动 |
| **Spring Security** | 6.2.0 | 安全框架 | 认证授权、CSRF防护 |
| **Spring Validation** | 6.1.0 | 数据校验 | 参数校验、Bean校验 |

### 数据访问层
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **Spring Data JPA** | 3.2.0 | ORM框架 | 简化数据访问、自动查询生成 |
| **Hibernate** | 6.4.0 | JPA实现 | 成熟稳定、性能优秀 |
| **MySQL Connector** | 8.0.33 | 数据库驱动 | 官方驱动、性能稳定 |
| **Spring Data Redis** | 3.2.0 | Redis集成 | 缓存抽象、分布式锁 |

### 安全认证
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **JWT (jjwt)** | 0.11.5 | Token认证 | 无状态、跨域支持 |
| **BCrypt** | - | 密码加密 | 安全性高、防彩虹表攻击 |

### 文档工具
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **SpringDoc OpenAPI** | 2.2.0 | API文档 | 自动生成、Swagger UI集成 |

### 工具库
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **Apache Commons Lang** | 3.13.0 | 工具类库 | 字符串、日期、反射工具 |
| **JavaFaker** | 1.0.2 | 测试数据生成 | Mock数据生成、多语言支持 |
| **Jackson** | 2.16.0 | JSON处理 | 序列化、反序列化 |

### 监控组件
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **Spring Actuator** | 3.2.0 | 应用监控 | 健康检查、指标收集 |
| **Micrometer** | 1.12.0 | 指标库 | 多种监控系统支持 |

### 测试框架
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **JUnit 5** | 5.10.0 | 单元测试 | 现代化测试框架、注解丰富 |
| **Mockito** | 5.7.0 | Mock框架 | 模拟对象、行为验证 |
| **TestContainers** | 1.19.0 | 集成测试 | 真实环境测试、容器支持 |

### 后端项目结构
```
backend/
├── src/main/java/com/apimock/
│   ├── controller/          # 控制器层
│   │   ├── auth/           # 认证控制器
│   │   ├── project/        # 项目管理
│   │   ├── api/           # 接口管理
│   │   ├── mock/          # Mock服务
│   │   ├── proxy/         # 代理服务
│   │   ├── data/          # 数据管理
│   │   └── monitor/       # 监控统计
│   ├── service/            # 业务逻辑层
│   │   └── impl/          # 服务实现
│   ├── repository/         # 数据访问层
│   ├── entity/             # 实体类
│   ├── dto/               # 数据传输对象
│   │   ├── request/       # 请求DTO
│   │   └── response/      # 响应DTO
│   ├── config/            # 配置类
│   │   ├── database/      # 数据库配置
│   │   ├── security/      # 安全配置
│   │   └── redis/         # Redis配置
│   ├── util/              # 工具类
│   ├── exception/         # 异常处理
│   └── constant/          # 常量定义
├── src/main/resources/
│   ├── application.yml    # 应用配置
│   ├── application-dev.yml # 开发环境
│   ├── application-prod.yml # 生产环境
│   └── db/migration/      # 数据库迁移脚本
└── src/test/              # 测试代码
```

---

## 数据存储

### 关系型数据库
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **MySQL** | 8.0 | 主数据库 | 稳定可靠、性能优秀、社区活跃 |
| **MySQL Cluster** | 8.0 | 高可用 | 主从复制、读写分离 |

### 缓存数据库
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **Redis** | 6.2 | 缓存存储 | 高性能、数据结构丰富 |
| **Redis Cluster** | 6.2 | 缓存集群 | 高可用、数据分片 |

### 数据库设计原则
- **主库写入**：所有写操作在主库执行
- **从库读取**：读操作分散到从库
- **缓存优先**：热点数据优先从Redis读取
- **数据一致性**：最终一致性策略

### 核心数据表
```sql
-- 用户表
users (id, username, email, password_hash, role, created_at, updated_at)

-- 项目表
projects (id, name, description, owner_id, created_at, updated_at)

-- 项目成员表
project_members (id, project_id, user_id, role, joined_at)

-- API接口表
apis (id, project_id, name, method, path, description, request_schema, response_schema, created_at, updated_at)

-- Mock规则表
mock_rules (id, api_id, name, conditions, response_data, delay, status, created_at, updated_at)

-- 代理配置表
proxy_configs (id, project_id, path_pattern, target_url, enabled, created_at, updated_at)

-- 访问日志表
access_logs (id, api_id, ip_address, user_agent, request_data, response_data, status_code, response_time, created_at)
```

---

## 基础设施

### 容器化
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **Docker** | 24.0 | 容器运行时 | 环境一致性、资源隔离 |
| **Docker Compose** | 2.21 | 容器编排 | 本地开发、服务编排 |

### Web服务器
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **Nginx** | 1.25 | 反向代理 | 高性能、负载均衡、静态文件服务 |

### 生产环境（可选）
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **Kubernetes** | 1.28 | 容器编排 | 自动扩展、服务发现、滚动更新 |
| **Helm** | 3.13 | 包管理 | 应用部署、配置管理 |

### 监控告警
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **Prometheus** | 2.47 | 指标收集 | 时序数据库、PromQL查询 |
| **Grafana** | 10.2 | 可视化 | 丰富的图表、告警功能 |
| **ELK Stack** | 8.11 | 日志管理 | 日志收集、分析、可视化 |

---

## 开发工具链

### 版本控制
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **Git** | 2.42 | 版本控制 | 分布式、分支管理 |
| **GitHub/GitLab** | - | 代码托管 | 协作开发、CI/CD集成 |

### CI/CD
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **GitHub Actions** | - | 持续集成 | 与GitHub集成、配置简单 |
| **Jenkins** | 2.426 | 持续部署 | 插件丰富、可定制性强 |

### 代码质量
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **SonarQube** | 10.2 | 代码分析 | 质量检查、安全扫描 |
| **Checkstyle** | 10.12 | 代码规范 | Java代码风格检查 |

### 性能测试
| 技术 | 版本 | 用途 | 选择理由 |
|------|------|------|----------|
| **JMeter** | 5.6 | 性能测试 | 压力测试、负载测试 |
| **Artillery** | 2.0 | API测试 | 现代化、脚本简单 |

---

## 安全策略

### 认证授权
- **JWT Token**：无状态认证，支持分布式
- **RBAC权限模型**：基于角色的访问控制
- **OAuth 2.0**：第三方登录支持（可扩展）

### 数据安全
- **数据加密**：敏感数据AES-256加密存储
- **传输加密**：HTTPS/TLS 1.3
- **密码策略**：BCrypt哈希，加盐存储

### 接口安全
- **速率限制**：防止API滥用
- **CORS配置**：跨域访问控制
- **输入验证**：防止SQL注入、XSS攻击
- **输出编码**：防止数据泄露

### 安全审计
- **操作日志**：记录所有关键操作
- **访问日志**：记录API访问情况
- **安全扫描**：定期漏洞扫描

---

## 性能优化

### 前端优化
- **代码分割**：按需加载、路由懒加载
- **资源压缩**：Gzip、图片压缩
- **CDN加速**：静态资源分发
- **浏览器缓存**：合理的缓存策略

### 后端优化
- **连接池**：数据库连接复用
- **查询优化**：索引优化、分页查询
- **缓存策略**：多级缓存、缓存穿透防护
- **异步处理**：耗时操作异步执行

### 数据库优化
- **索引策略**：主键、外键、查询索引
- **读写分离**：主从复制、负载均衡
- **分库分表**：水平扩展（按需）
- **数据归档**：历史数据清理

---

## 部署架构

### 开发环境
```
Developer Machine
├── Frontend (Node.js + Vite Dev Server)
├── Backend (Spring Boot Dev Profile)
├── MySQL (Docker Container)
└── Redis (Docker Container)
```

### 测试环境
```
Test Server
├── Nginx (Reverse Proxy)
├── Frontend Container
├── Backend Container
├── MySQL Container
└── Redis Container
```

### 生产环境
```
Production Cluster
├── Load Balancer (Nginx/ALB)
├── Frontend Pods (3 replicas)
├── Backend Pods (5 replicas)
├── MySQL Cluster (Master + 2 Slaves)
├── Redis Cluster (3 Masters + 3 Slaves)
└── Monitoring Stack (Prometheus + Grafana)
```

---

## 技术选型原则

### 1. 稳定性优先
- 选择成熟的技术栈
- 考虑长期维护成本
- 社区活跃度和文档完整性

### 2. 性能导向
- 高并发支持
- 低延迟响应
- 资源利用效率

### 3. 可扩展性
- 水平扩展能力
- 模块化设计
- 微服务友好

### 4. 开发效率
- 开发工具完善
- 学习曲线平缓
- 团队技能匹配

### 5. 成本控制
- 开源技术优先
- 运维成本可控
- 硬件资源需求合理

---

## 技术演进路线

### Phase 1: MVP版本 (当前)
- 核心功能实现
- 单体部署
- 基础监控

### Phase 2: 功能完善 (3-6个月)
- 高级Mock功能
- 性能优化
- 安全增强

### Phase 3: 平台化 (6-12个月)
- 微服务架构
- 插件系统
- AI辅助功能

### Phase 4: 生态建设 (12+个月)
- 开放API
- 第三方集成
- 行业解决方案