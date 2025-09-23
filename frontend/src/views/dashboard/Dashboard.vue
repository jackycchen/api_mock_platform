<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card>
          <el-statistic
            title="项目总数"
            :value="stats.projectCount"
          />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <el-statistic
            title="接口总数"
            :value="stats.apiCount"
          />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <el-statistic
            title="Mock规则"
            :value="stats.mockCount"
          />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <el-statistic
            title="今日调用"
            :value="stats.todayCallCount"
          />
        </el-card>
      </el-col>
    </el-row>
    
    <el-row
      :gutter="20"
      style="margin-top: 20px"
    >
      <el-col :span="12">
        <el-card>
          <template #header>
            <div>最近项目</div>
          </template>
          <el-table
            :data="recentProjects"
            style="width: 100%"
          >
            <el-table-column
              prop="name"
              label="项目名称"
            />
            <el-table-column
              prop="apiCount"
              label="接口数量"
            />
            <el-table-column
              prop="updateTime"
              label="更新时间"
            />
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <div>调用统计</div>
          </template>
          <div style="height: 300px">
            <!-- 这里可以放图表组件 -->
            <div style="display: flex; align-items: center; justify-content: center; height: 100%; color: #909399">
              图表占位符
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const stats = ref({
  projectCount: 15,
  apiCount: 128,
  mockCount: 89,
  todayCallCount: 1234
})

const recentProjects = ref([
  { name: '用户管理系统', apiCount: 25, updateTime: '2024-01-15 14:30' },
  { name: '订单管理系统', apiCount: 18, updateTime: '2024-01-14 16:45' },
  { name: '商品管理系统', apiCount: 32, updateTime: '2024-01-13 09:20' }
])
</script>