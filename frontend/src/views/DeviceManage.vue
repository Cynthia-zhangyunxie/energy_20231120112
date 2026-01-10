<template>
  <div class="device-container">
    <el-page-header content="设备管理"></el-page-header>

    <!-- 操作栏 -->
    <el-row :gutter="20" class="operation-row">
      <el-col :span="12">
        <el-button type="primary" @click="openAddDialog">新增设备</el-button>
        <el-button type="success" @click="openReplaceDialog" :disabled="!selectedDeviceId">更换设备</el-button>
      </el-col>
      <el-col :span="12" class="search-col">
        <el-input v-model="searchSn" placeholder="输入设备SN查询" style="width: 300px; margin-right: 10px;"></el-input>
        <el-button type="primary" icon="Search" @click="fetchDevices">查询</el-button>
        <el-button type="default" icon="Refresh" @click="resetSearch">重置</el-button>
      </el-col>
    </el-row>

    <!-- 设备列表 -->
    <el-table :data="deviceList" @selection-change="handleSelectionChange" border style="width: 100%; margin-top: 10px;">
      <el-table-column type="selection" width="55"></el-table-column>
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="name" label="设备名称"></el-table-column>
      <el-table-column prop="sn" label="设备SN"></el-table-column>
      <el-table-column prop="status" label="通讯状态">
        <template #default="scope">
          <el-tag :type="scope.row.status === 'ONLINE' ? 'success' : 'danger'">
            {{ scope.row.status === 'ONLINE' ? '在线' : scope.row.status === 'OFFLINE' ? '离线' : '已停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="ratedPower" label="额定功率(W)"></el-table-column>
      <el-table-column prop="buildingName" label="所属建筑"></el-table-column>
      <el-table-column prop="roomNo" label="所属房间"></el-table-column>
      <el-table-column prop="createTime" label="创建时间"></el-table-column>
    </el-table>

    <!-- 新增设备对话框 -->
    <el-dialog title="新增设备" v-model="addDialogVisible" width="600px">
      <el-form :model="addDeviceForm" :rules="deviceRules" ref="addDeviceFormRef" label-width="120px">
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="addDeviceForm.name" placeholder="请输入设备名称"></el-input>
        </el-form-item>
        <el-form-item label="设备SN" prop="sn">
          <el-input v-model="addDeviceForm.sn" placeholder="请输入唯一设备SN"></el-input>
        </el-form-item>
        <el-form-item label="通讯状态" prop="status">
          <el-select v-model="addDeviceForm.status" placeholder="请选择通讯状态">
            <el-option label="在线" value="ONLINE"></el-option>
            <el-option label="离线" value="OFFLINE"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="额定功率(W)" prop="ratedPower">
          <el-input v-model="addDeviceForm.ratedPower" type="number" placeholder="请输入额定功率"></el-input>
        </el-form-item>
        <el-form-item label="所属建筑" prop="buildingId">
          <el-select v-model="addDeviceForm.buildingId" placeholder="请选择所属建筑">
            <el-option v-for="building in buildingList" :key="building.id" :label="building.name" :value="building.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="所属房间" prop="roomNo">
          <el-input v-model="addDeviceForm.roomNo" placeholder="请输入所属房间号"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddDevice">确定</el-button>
      </template>
    </el-dialog>

    <!-- 更换设备对话框 -->
    <el-dialog title="更换设备" v-model="replaceDialogVisible" width="600px">
      <el-form :model="replaceDeviceForm" :rules="deviceRules" ref="replaceDeviceFormRef" label-width="120px">
        <el-form-item label="旧设备名称" disabled>
          <el-input v-model="oldDeviceName" placeholder="旧设备名称"></el-input>
        </el-form-item>
        <el-form-item label="新设备名称" prop="name">
          <el-input v-model="replaceDeviceForm.name" placeholder="请输入新设备名称"></el-input>
        </el-form-item>
        <el-form-item label="新设备SN" prop="sn">
          <el-input v-model="replaceDeviceForm.sn" placeholder="请输入新设备唯一SN"></el-input>
        </el-form-item>
        <el-form-item label="通讯状态" prop="status">
          <el-select v-model="replaceDeviceForm.status" placeholder="请选择通讯状态">
            <el-option label="在线" value="ONLINE"></el-option>
            <el-option label="离线" value="OFFLINE"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="额定功率(W)" prop="ratedPower">
          <el-input v-model="replaceDeviceForm.ratedPower" type="number" placeholder="请输入额定功率"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replaceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReplaceDevice">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/api/request'
import { ElMessage } from 'element-plus'

// 设备列表数据
const deviceList = ref([])
// 建筑列表（用于下拉选择）
const buildingList = ref([])
// 搜索条件
const searchSn = ref('')
// 选中的设备ID（用于更换设备）
const selectedDeviceId = ref('')
// 旧设备名称（更换设备时显示）
const oldDeviceName = ref('')

// 新增设备对话框
const addDialogVisible = ref(false)
const addDeviceFormRef = ref(null)
const addDeviceForm = ref({
  name: '',
  sn: '',
  status: 'ONLINE',
  ratedPower: '',
  buildingId: '',
  roomNo: ''
})

// 更换设备对话框
const replaceDialogVisible = ref(false)
const replaceDeviceFormRef = ref(null)
const replaceDeviceForm = ref({
  name: '',
  sn: '',
  status: 'ONLINE',
  ratedPower: ''
})

// 表单校验规则
const deviceRules = ref({
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  sn: [{ required: true, message: '请输入设备SN', trigger: 'blur' }],
  status: [{ required: true, message: '请选择通讯状态', trigger: 'change' }],
  ratedPower: [{ required: true, message: '请输入额定功率', trigger: 'blur', type: 'number' }],
  buildingId: [{ required: true, message: '请选择所属建筑', trigger: 'change' }],
  roomNo: [{ required: true, message: '请输入所属房间号', trigger: 'blur' }]
})

// 页面加载时初始化数据
onMounted(async () => {
  // 先获取建筑列表，再获取设备列表
  await fetchBuildings()
  await fetchDevices()
})

// 查询设备列表
const fetchDevices = async () => {
  console.log('开始获取设备列表...')
  
  try {
    const response = await axios.get('/device/list')
    console.log('获取设备列表响应:', response)
    
    // 检查响应结构
    if (!response) {
      console.error('设备列表响应为空')
      ElMessage.error('服务器返回空响应')
      return
    }
    
    // 检查数据是否存在于响应中
    let devices = []
    if (response.data && Array.isArray(response.data)) {
      devices = response.data
    } else if (response.data && response.data.data && Array.isArray(response.data.data)) {
      devices = response.data.data
    } else if (response.data && response.data.list && Array.isArray(response.data.list)) {
      devices = response.data.list
    } else {
      console.warn('设备列表数据结构不符合预期:', response.data)
      devices = []
    }
    
    // 根据SN筛选
    if (searchSn.value) {
      devices = devices.filter(device => {
        // 增加空值检查
        return device && device.sn && device.sn.includes(searchSn.value)
      })
    }
    
    // 为每个设备添加建筑名称（根据buildingId匹配建筑列表）
    devices = devices.map(device => {
      const building = buildingList.value.find(b => b.id === device.buildingId)
      
      return {
        ...device,
        buildingName: building ? building.name : '未知建筑'
      }
    })
    
    // 更新设备列表
    deviceList.value = devices
    console.log(`成功加载${devices.length}条设备记录`)
  } catch (error) {
    console.error('获取设备列表失败:', error)
    ElMessage.error('查询设备列表失败: ' + (error.response?.data?.msg || '网络错误'))
    deviceList.value = []
  }
}

// 查询建筑列表
const fetchBuildings = async () => {
  try {
    const response = await axios.get('/building/list')
    
    // 处理不同的响应结构
    if (response.data && Array.isArray(response.data)) {
      buildingList.value = response.data
    } else if (response.data && response.data.data && Array.isArray(response.data.data)) {
      buildingList.value = response.data.data
    } else if (response.data && response.data.list && Array.isArray(response.data.list)) {
      buildingList.value = response.data.list
    } else {
      console.warn('建筑列表数据结构不符合预期:', response.data)
      buildingList.value = []
    }
  } catch (error) {
    ElMessage.error('查询建筑列表失败')
    console.error('获取建筑列表失败:', error)
    buildingList.value = []
  }
}

// 表格选择事件
const handleSelectionChange = (val) => {
  if (val.length === 1) {
    selectedDeviceId.value = val[0].id
    oldDeviceName.value = val[0].name
  } else {
    selectedDeviceId.value = ''
    oldDeviceName.value = ''
  }
}

// 重置搜索条件
const resetSearch = () => {
  searchSn.value = ''
  fetchDevices()
}

// 打开新增设备对话框
const openAddDialog = () => {
  addDeviceFormRef.value?.resetFields()
  addDialogVisible.value = true
}

// 提交新增设备
const submitAddDevice = () => {
  addDeviceFormRef.value.validate((isValid) => {
    if (isValid) {
      axios.post('/device/add', addDeviceForm.value)
          .then(response => {
            ElMessage.success('新增设备成功')
            addDialogVisible.value = false
            fetchDevices()
          })
          .catch(error => {
            ElMessage.error('新增设备失败：' + (error.response?.data?.msg || '网络错误'))
          })
    }
  })
}

// 打开更换设备对话框
const openReplaceDialog = () => {
  replaceDeviceFormRef.value?.resetFields()
  replaceDialogVisible.value = true
}

// 提交更换设备
const submitReplaceDevice = () => {
  replaceDeviceFormRef.value.validate((isValid) => {
    if (isValid) {
      axios.post(`/device/replace/${selectedDeviceId.value}`, replaceDeviceForm.value)
          .then(response => {
            ElMessage.success('更换设备成功')
            replaceDialogVisible.value = false
            fetchDevices()
          })
          .catch(error => {
            ElMessage.error('更换设备失败：' + (error.response?.data?.msg || '网络错误'))
          })
    }
  })
}
</script>

<style scoped>
.device-container {
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