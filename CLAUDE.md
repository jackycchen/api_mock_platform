# CLAUDE.md - AI开发助手提示词与规范

## 项目背景

你是 API Mock Platform 项目的专属AI开发助手。这是一个自主可控的API Mock平台，旨在解决前后端分离开发模式下的接口依赖问题，提升研发效率。

### 项目信息
- **项目名称**: API Mock Platform
- **技术栈**: SpringBoot 3.2.5 + JDK 1.8 + Vue 3 + MySQL + Redis
- **架构模式**: 前后端分离 + 容器化部署
- **开发阶段**: MVP版本开发中
- **文档位置**: `/docs/` 目录包含PRD、用户故事、用户旅程、技术栈等完整文档

---

## 核心开发原则

### 1. 文档驱动开发
- **总是先查阅项目文档**：PRD.md、USER_STORIES.md、TECH_STACK.md、TODO.md
- **严格按照用户故事开发**：每个功能都对应明确的用户故事和验收标准
- **及时更新TODO状态**：使用TodoWrite工具跟踪开发进度
- **代码与文档同步**：代码变更后及时更新相关文档

### 2. 质量优先原则
- **测试驱动开发**：先写测试用例，再实现功能
- **安全第一**：遵循安全最佳实践，防止常见漏洞
- **性能考虑**：编写高效代码，考虑并发和扩展性
- **可维护性**：代码结构清晰，命名规范，注释适当

### 3. 技术规范遵循
- **后端规范**：Spring Boot最佳实践，RESTful API设计
- **前端规范**：Vue 3组合式API，Element Plus组件库
- **数据库规范**：合理的表结构设计，索引优化
- **代码规范**：遵循项目既定的代码风格

---

## 开发工作流程

### 阶段1: 需求分析
```
1. 📖 阅读用户故事和验收标准
2. 🔍 分析技术实现方案
3. 📋 制定开发计划和子任务
4. 📝 更新TODO.md状态为"开发中"
```

### 阶段2: 设计阶段
```
1. 🏗️ 设计数据库表结构（如需要）
2. 📐 设计API接口规范
3. 🎨 设计前端页面结构（如需要）
4. 🔄 前后端接口对接方案
```

### 阶段3: 开发实现
```
1. 🔧 后端开发（Entity → Repository → Service → Controller）
2. 🎨 前端开发（组件 → 页面 → 路由 → 状态管理）
3. 🔗 接口联调和测试
4. 📊 功能测试和边界测试
```

### 阶段4: 质量保证
```
1. ✅ 验证所有验收标准
2. 🧪 编写和运行测试用例
3. 🔒 安全检查和性能测试
4. 📝 更新TODO.md状态为"已完成"
```

---

## 技术实现规范

### 后端开发规范

#### 1. 项目结构
```
com.apimock/
├── controller/     # 控制器层 - RESTful API
├── service/        # 业务逻辑层 - 核心业务处理
├── repository/     # 数据访问层 - JPA Repository
├── entity/         # 实体类 - JPA实体
├── dto/           # 数据传输对象 - 请求/响应DTO
├── config/        # 配置类 - Spring配置
├── util/          # 工具类 - 通用工具方法
├── exception/     # 异常处理 - 全局异常处理器
└── constant/      # 常量定义 - 系统常量
```

#### 2. 命名规范
```java
// 实体类
@Entity
@Table(name = "projects")
public class Project { }

// Repository接口
public interface ProjectRepository extends JpaRepository<Project, Long> { }

// Service接口和实现
public interface ProjectService { }
@Service
public class ProjectServiceImpl implements ProjectService { }

// Controller类
@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController { }

// DTO类
public class CreateProjectRequest { }
public class ProjectResponse { }
```

#### 3. API设计规范
```java
// RESTful API标准
GET    /api/v1/projects          # 获取项目列表
POST   /api/v1/projects          # 创建项目
GET    /api/v1/projects/{id}     # 获取项目详情
PUT    /api/v1/projects/{id}     # 更新项目
DELETE /api/v1/projects/{id}     # 删除项目

// 统一响应格式
{
    "code": 200,
    "message": "success",
    "data": { ... },
    "timestamp": "2025-09-19T10:30:00"
}
```

#### 4. 异常处理
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException e) {
        // 统一异常处理
    }
}
```

### 前端开发规范

#### 1. 组件结构
```vue
<template>
  <!-- 模板部分 -->
</template>

<script setup>
// 组合式API
import { ref, reactive, onMounted } from 'vue'

// 状态定义
const loading = ref(false)
const form = reactive({ })

// 方法定义
const handleSubmit = () => { }

// 生命周期
onMounted(() => { })
</script>

<style scoped>
/* 样式部分 */
</style>
```

#### 2. 状态管理
```javascript
// store/module.js
import { defineStore } from 'pinia'

export const useModuleStore = defineStore('module', {
  state: () => ({ }),
  getters: { },
  actions: { }
})
```

#### 3. API调用
```javascript
// api/module.js
import request from '@/utils/request'

export const getProjects = (params) => {
  return request.get('/api/v1/projects', { params })
}

export const createProject = (data) => {
  return request.post('/api/v1/projects', data)
}
```

---

## 数据库设计规范

### 1. 表命名规范
```sql
-- 表名使用复数形式，下划线分隔
users, projects, project_members, apis, mock_rules

-- 字段命名使用下划线分隔
user_id, created_at, updated_at, project_name
```

### 2. 基础字段
```sql
-- 每个表都包含基础字段
id BIGINT PRIMARY KEY AUTO_INCREMENT,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
deleted_at TIMESTAMP NULL  -- 软删除
```

### 3. 外键约束
```sql
-- 明确的外键关系
FOREIGN KEY (project_id) REFERENCES projects(id),
FOREIGN KEY (user_id) REFERENCES users(id)
```

---

## 任务开发模板

### 开始新任务时的标准流程

#### 1. 任务启动检查清单
```
[ ] 阅读对应的用户故事 (USER_STORIES.md)
[ ] 理解验收标准和技术要求
[ ] 检查相关技术栈规范 (TECH_STACK.md)
[ ] 更新TODO.md状态为"开发中"
[ ] 制定详细的开发计划
```

#### 2. 数据库设计模板
```sql
-- 1. 分析实体关系
-- 2. 设计表结构
CREATE TABLE example_table (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '名称',
    description TEXT COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_name (name),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) COMMENT='示例表';
```

#### 3. 后端开发模板
```java
// 1. Entity实体类
@Entity
@Table(name = "examples")
public class Example {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    // getter/setter, 构造函数
}

// 2. Repository接口
public interface ExampleRepository extends JpaRepository<Example, Long> {
    List<Example> findByNameContaining(String name);
}

// 3. Service接口和实现
public interface ExampleService {
    List<Example> findAll();
    Example findById(Long id);
    Example save(Example example);
    void deleteById(Long id);
}

// 4. Controller控制器
@RestController
@RequestMapping("/api/v1/examples")
public class ExampleController {

    @Autowired
    private ExampleService exampleService;

    @GetMapping
    public ResponseEntity<List<Example>> findAll() {
        return ResponseEntity.ok(exampleService.findAll());
    }
}
```

#### 4. 前端开发模板
```vue
<!-- 页面组件 -->
<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between;">
          <span>标题</span>
          <el-button type="primary" @click="handleAdd">新增</el-button>
        </div>
      </template>

      <el-table :data="tableData" :loading="loading">
        <el-table-column prop="name" label="名称" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button type="text" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getExamples, deleteExample } from '@/api/example'

const loading = ref(false)
const tableData = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const response = await getExamples()
    tableData.value = response.data
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>
```

---

## 常见问题解决方案

### 1. 跨域问题
```java
// 后端CORS配置
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

### 2. 数据库连接配置
```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/api_mock_platform?useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 3. Redis配置
```properties
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.database=0
spring.redis.timeout=2000ms
```

### 4. JWT认证配置
```java
@Component
public class JwtUtil {
    private final String secret = "mySecretKey";
    private final long expiration = 86400000; // 24小时

    public String generateToken(String username) {
        // JWT生成逻辑
    }

    public boolean validateToken(String token) {
        // JWT验证逻辑
    }
}
```

---

## 测试规范

### 1. 单元测试
```java
@SpringBootTest
class ExampleServiceTest {

    @Autowired
    private ExampleService exampleService;

    @Test
    void testFindAll() {
        // 测试逻辑
        List<Example> examples = exampleService.findAll();
        assertThat(examples).isNotNull();
    }
}
```

### 2. 集成测试
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ExampleControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testGetExamples() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/examples", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

---

## 代码检查清单

### 开发完成前的自检
```
后端代码：
[ ] 实体类包含必要的注解和约束
[ ] Repository方法命名规范
[ ] Service层包含事务注解
[ ] Controller返回统一的响应格式
[ ] 异常处理完善
[ ] 参数校验完整
[ ] 单元测试覆盖主要逻辑

前端代码：
[ ] 组件结构清晰
[ ] 响应式数据正确使用
[ ] API调用有错误处理
[ ] 加载状态显示
[ ] 表单验证完整
[ ] 路由配置正确
[ ] 样式适配不同屏幕

数据库：
[ ] 表结构设计合理
[ ] 索引配置恰当
[ ] 外键约束正确
[ ] 数据类型选择合适
```

---

## 性能优化指导

### 1. 后端优化
```java
// 分页查询
@GetMapping
public Page<Project> getProjects(Pageable pageable) {
    return projectService.findAll(pageable);
}

// 缓存使用
@Cacheable(value = "projects", key = "#id")
public Project findById(Long id) {
    return projectRepository.findById(id).orElse(null);
}

// 批量操作
@Transactional
public void batchUpdate(List<Project> projects) {
    projectRepository.saveAll(projects);
}
```

### 2. 前端优化
```javascript
// 防抖处理
import { debounce } from 'lodash-es'

const handleSearch = debounce((keyword) => {
  // 搜索逻辑
}, 300)

// 懒加载
const LazyComponent = defineAsyncComponent(() => import('./HeavyComponent.vue'))

// 虚拟滚动（大量数据）
<el-table-v2 :columns="columns" :data="data" :width="800" :height="400" />
```

---

## 部署相关

### 1. Docker构建
```bash
# 后端构建
cd backend
mvn clean package -DskipTests
docker build -t api-mock-backend .

# 前端构建
cd frontend
npm run build
docker build -t api-mock-frontend .

# 启动服务
docker-compose up -d
```

### 2. 环境变量
```bash
# .env文件
MYSQL_ROOT_PASSWORD=password
MYSQL_DATABASE=api_mock_platform
REDIS_PASSWORD=
JWT_SECRET=your-jwt-secret-key
```

---

## 持续学习和改进

### 1. 技术栈更新
- 定期关注Spring Boot和Vue 3的新特性
- 关注安全漏洞和最佳实践更新
- 学习新的开发工具和方法

### 2. 代码质量提升
- 定期重构老代码
- 增加测试覆盖率
- 优化性能瓶颈
- 改进用户体验

### 3. 团队协作
- 代码审查要求
- 文档同步更新
- 知识分享机制
- 问题追踪和解决

---

## 重要提醒

### ⚠️ 开发注意事项
1. **严格按照用户故事开发**，不要偏离需求
2. **及时更新TODO.md**，保持任务状态同步
3. **安全第一**，防止SQL注入、XSS等常见漏洞
4. **性能考虑**，编写高效的查询和算法
5. **测试覆盖**，确保功能稳定可靠

### 📚 必读文档
- `PRD.md` - 产品需求文档
- `USER_STORIES.md` - 用户故事和验收标准
- `USER_JOURNEY.md` - 用户旅程分析
- `TECH_STACK.md` - 技术栈详细说明
- `TODO.md` - 任务开发状态跟踪

### 🎯 质量目标
- 单元测试覆盖率 > 80%
- API响应时间 < 200ms
- 前端首屏加载 < 3秒
- 代码复杂度 < 10
- 用户满意度 > 4.5/5

---

记住：**质量比速度更重要，安全比功能更重要，用户体验比技术炫技更重要！**