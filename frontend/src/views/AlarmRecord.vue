<template>
  <div class="alarm-container">
    <el-page-header content="告警记录"></el-page-header>

    <!-- 操作栏 -->
    <el-row :gutter="20" class="operation-row">
      <el-col :span="12">
        <el-button type="default" icon="Refresh" @click="fetchAlarms">刷新</el-button>
      </el-col>
      <el-col :span="12" class="search-col">
        <el-input v-model="searchSn" placeholder="输入设备SN查询" style="width: 300px; margin-right: 10px;"></el-input>
        <el-button type="primary" icon="Search" @click="fetchAlarms">查询</el-button>
        <el-button type="default" @click="resetSearch">重置</el-button>
      </el-col>
    </el-row>

    <!-- 告警列表 -->
    <el-table :data="alarmList" border style="width: 100%; margin-top: 10px;">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="deviceSn" label="设备SN"></el-table-column>
      <el-table-column prop="alarmType" label="告警类型">
        <template #default="scope">
          <el-tag type="danger">
            {{ scope.row.alarmType === 'OVERLOAD' ? '功率过载' : '电压异常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="alarmValue" label="告警数值">
        <template #default="scope">
          {{ scope.row.alarmType === 'OVERLOAD' ? scope.row.alarmValue + ' W' : scope.row.alarmValue + ' V' }}
        </template>
      </el-table-column>
      <el-table-column prop="alarmDetail" label="告警详情"></el-table-column>
      <el-table-column prop="triggerTime" label="触发时间"></el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/api/request'
import { ElMessage } from 'element-plus'

// 告警列表数据
const alarmList = ref([])
// 搜索条件
const searchSn = ref('')

// 页面加载时查询告警记录
onMounted(() => {
  fetchAlarms()
})

// 查询告警记录
const fetchAlarms = () => {
  console.log('开始获取告警记录...')
  
  if (searchSn.value) {
    // 根据设备SN查询
    axios.get(`/alarm/search?sn=${searchSn.value}`)
      .then(response => {
        console.log('根据SN查询告警记录响应:', response)
        
        try {
          // 检查响应结构
          if (!response) {
            console.error('告警记录响应为空')
            ElMessage.error('服务器返回空响应')
            return
          }
          
          // 检查数据是否存在于响应中
          let alarms = []
          if (response.data && Array.isArray(response.data)) {
            alarms = response.data
          } else if (response.data && response.data.data && Array.isArray(response.data.data)) {
            alarms = response.data.data
          } else if (response.data && response.data.list && Array.isArray(response.data.list)) {
            alarms = response.data.list
          } else {
            console.warn('告警记录数据结构不符合预期:', response.data)
            alarms = []
          }
          
          // 更新告警列表
          alarmList.value = alarms
          console.log(`成功加载${alarms.length}条告警记录`)
        } catch (innerError) {
          console.error('处理告警记录数据时出错:', innerError)
          alarmList.value = []
        }
      })
      .catch(error => {
        console.error('获取告警记录失败:', error)
        ElMessage.error('查询告警记录失败: ' + (error.response?.data?.msg || '网络错误'))
        alarmList.value = []
      })
  } else {
    // 查询所有告警记录
    axios.get('/alarm/list')
      .then(response => {
        console.log('查询所有告警记录响应:', response)
        
        try {
          // 检查响应结构
          if (!response) {
            console.error('告警记录响应为空')
            ElMessage.error('服务器返回空响应')
            return
          }
          
          // 检查数据是否存在于响应中
          let alarms = []
          if (response.data && Array.isArray(response.data)) {
            alarms = response.data
          } else if (response.data && response.data.data && Array.isArray(response.data.data)) {
            alarms = response.data.data
          } else if (response.data && response.data.list && Array.isArray(response.data.list)) {
            alarms = response.data.list
          } else {
            console.warn('告警记录数据结构不符合预期:', response.data)
            alarms = []
          }
          
          // 更新告警列表
          alarmList.value = alarms
          console.log(`成功加载${alarms.length}条告警记录`)
        } catch (innerError) {
          console.error('处理告警记录数据时出错:', innerError)
          alarmList.value = []
        }
      })
      .catch(error => {
        console.error('获取告警记录失败:', error)
        ElMessage.error('查询告警记录失败: ' + (error.response?.data?.msg || '网络错误'))
        alarmList.value = []
      })
  }
}

// 重置搜索条件
const resetSearch = () => {
  searchSn.value = ''
  fetchAlarms()
}
</script>

<style scoped>
.alarm-container {
  padding: 20px;
}

.operation-row {
  margin-bottom: 10px;
}

.search-col {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}
</style>