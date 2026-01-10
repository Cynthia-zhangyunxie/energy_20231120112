<template>
  <div class="dashboard-container">
    <el-page-header content="数据大屏"></el-page-header>
    
    <!-- 统计数据卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ totalDevices }}</div>
            <div class="stat-label">设备总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ onlineDevices }}</div>
            <div class="stat-label">在线设备</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ totalBuildings }}</div>
            <div class="stat-label">建筑总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ currentTime }}</div>
            <div class="stat-label">当前时间</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 设备状态表格 -->
    <el-row :gutter="20" class="data-row">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>设备状态监控</span>
              <el-button type="primary" @click="refreshData" :loading="loading">刷新数据</el-button>
            </div>
          </template>
          <div v-if="deviceList.length > 0">
            <el-table :data="deviceList" style="width: 100%" v-loading="loading">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="设备名称" />
              <el-table-column prop="sn" label="设备SN" />
              <el-table-column prop="building_name" label="所属建筑" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 'ONLINE' ? 'success' : 'danger'">
                    {{ scope.row.status === 'ONLINE' ? '在线' : '离线' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="last_update" label="最后更新" width="180" />
            </el-table>
          </div>
          <div v-else>
            <el-empty description="暂无设备数据" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 能耗概览 -->
    <el-row :gutter="20" class="energy-row">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>今日能耗概览</span>
          </template>
          <div class="energy-overview">
            <div class="energy-item">
              <span class="energy-label">总用电量</span>
              <span class="energy-value">{{ totalEnergy }} kWh</span>
            </div>
            <div class="energy-item">
              <span class="energy-label">平均功率</span>
              <span class="energy-value">{{ avgPower }} kW</span>
            </div>
            <div class="energy-item">
              <span class="energy-label">在线设备占比</span>
              <span class="energy-value">{{ onlineRate }}%</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/store/index'
import axios from '@/api/request'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

// 响应式数据
const currentTime = ref('')
const deviceList = ref([])
const buildingList = ref([])
const loading = ref(false)

// 统计数据
const totalDevices = ref(0)
const onlineDevices = ref(0)
const totalBuildings = ref(0)
const totalEnergy = ref(0)
const avgPower = ref(0)
const onlineRate = ref(0)

let timeInterval = null

// 更新当前时间
const updateTime = () => {
  currentTime.value = new Date().toLocaleString()
}

// 获取设备列表
const fetchDeviceList = async () => {
  try {
    const response = await axios.get('/device/list')
    if (response.data) {
      let dataArray = null;
      
      if (Array.isArray(response.data)) {
        dataArray = response.data;
      } else if (response.data.data && Array.isArray(response.data.data)) {
        dataArray = response.data.data;
      } else if (response.data.list && Array.isArray(response.data.list)) {
        dataArray = response.data.list;
      }
      
      if (dataArray) {
        deviceList.value = dataArray.map(item => {
          // 根据buildingId匹配建筑名称
          const building = buildingList.value.find(b => b.id === item.buildingId)
          const buildingName = building ? building.name : '未知建筑'
          
          return {
            ...item,
            building_name: buildingName,
            last_update: new Date().toLocaleString()
          }
        })
      }
    }
  } catch (error) {
    console.error('获取设备列表失败:', error)
    ElMessage.error('获取设备列表失败')
  }
}

// 获取建筑列表
const fetchBuildingList = async () => {
  try {
    const response = await axios.get('/building/list')
    if (response.data) {
      let dataArray = null;
      
      if (Array.isArray(response.data)) {
        dataArray = response.data;
      } else if (response.data.data && Array.isArray(response.data.data)) {
        dataArray = response.data.data;
      } else if (response.data.list && Array.isArray(response.data.list)) {
        dataArray = response.data.list;
      }
      
      if (dataArray) {
        buildingList.value = dataArray;
      }
    }
  } catch (error) {
    console.error('获取建筑列表失败:', error)
  }
}

// 获取能耗数据
const fetchEnergyData = async () => {
  try {
    // 构建今日日期参数
    const today = new Date().toISOString().split('T')[0]
    
    let totalEnergyData = 0
    let totalPowerData = 0
    let deviceCount = 0
    
    // 获取所有设备的能耗数据
    for (const device of deviceList.value) {
      try {
        const response = await axios.get(`/energy/recent/${device.id}`, {
          params: {
            startDate: today,
            endDate: today
          }
        })
        
        if (response.data) {
          let energyArray = []
          if (Array.isArray(response.data)) {
            energyArray = response.data
          } else if (response.data.data && Array.isArray(response.data.data)) {
            energyArray = response.data.data
          } else if (response.data.list && Array.isArray(response.data.list)) {
            energyArray = response.data.list
          }
          
          if (energyArray.length > 0) {
            // 计算该设备的数据
            const deviceTotalEnergy = energyArray.reduce((sum, item) => {
              return sum + (item.total_energy || 0)
            }, 0)
            
            const deviceAvgPower = energyArray.reduce((sum, item) => {
              return sum + (item.real_power || 0)
            }, 0) / energyArray.length
            
            totalEnergyData += deviceTotalEnergy
            totalPowerData += deviceAvgPower
            deviceCount++
          }
        }
      } catch (deviceError) {
        console.warn(`获取设备 ${device.id} 能耗数据失败:`, deviceError)
      }
    }
    
    // 更新统计数据
    totalEnergy.value = Math.round(totalEnergyData * 100) / 100
    avgPower.value = deviceCount > 0 ? Math.round((totalPowerData / deviceCount) * 100) / 100 : 0
    
    console.log(`今日能耗统计 - 总用电量: ${totalEnergy.value} kWh, 平均功率: ${avgPower.value} kW, 有数据设备数: ${deviceCount}`)
    
  } catch (error) {
    console.error('获取能耗数据失败:', error)
    totalEnergy.value = 0
    avgPower.value = 0
  }
}

// 计算统计数据
const calculateStatistics = () => {
  totalDevices.value = deviceList.value.length;
  onlineDevices.value = deviceList.value.filter(device => device.status === 'ONLINE').length;
  totalBuildings.value = buildingList.value.length;
  
  if (totalDevices.value > 0) {
    onlineRate.value = Math.round((onlineDevices.value / totalDevices.value) * 100);
  } else {
    onlineRate.value = 0;
  }
}

// 刷新数据
const refreshData = async () => {
  loading.value = true
  try {
    // 先获取建筑列表，再获取设备列表（确保建筑数据可用）
    await fetchBuildingList()
    await fetchDeviceList()
    await fetchEnergyData()
    
    calculateStatistics()
    ElMessage.success('数据刷新成功')
  } catch (error) {
    console.error('刷新数据失败:', error)
    ElMessage.error('数据刷新失败')
  } finally {
    loading.value = false
  }
}

// 页面加载时初始化
onMounted(() => {
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
  
  // 初始加载数据
  refreshData()
})

// 组件卸载时清理
onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.stats-row, .data-row, .energy-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-content {
  padding: 20px;
}

.stat-value {
  font-size: 2.5rem;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.energy-overview {
  display: flex;
  justify-content: space-around;
  padding: 20px 0;
}

.energy-item {
  text-align: center;
}

.energy-label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.energy-value {
  display: block;
  font-size: 1.5rem;
  font-weight: bold;
  color: #67C23A;
}

.el-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.el-table {
  border-radius: 8px;
}
</style>