<template>
  <div class="building-manage-container">
    <!-- 页面标题和操作按钮 -->
    <div class="header">
      <h2>建筑管理</h2>
      <el-button type="primary" @click="openAddDialog">新增建筑</el-button>
    </div>

    <!-- 建筑列表 -->
    <el-table :data="buildingList" border stripe style="width: 100%; margin-top: 20px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="建筑名称" min-width="150" />
      <el-table-column prop="locationCode" label="地址编码" min-width="150" />
      <el-table-column prop="floorCount" label="层数" width="80" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button type="primary" size="small" @click="openEditDialog(scope.row)">编辑</el-button>
          <el-button type="danger" size="small" @click="deleteBuilding(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑建筑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑建筑' : '新增建筑'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="建筑名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入建筑名称" />
        </el-form-item>
        <el-form-item label="地址编码" prop="locationCode">
          <el-input v-model="form.locationCode" placeholder="请输入地址编码" />
        </el-form-item>
        <el-form-item label="层数" prop="floorCount">
          <el-input v-model.number="form.floorCount" placeholder="请输入层数" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import axios from '@/api/request';

// 建筑列表数据
const buildingList = ref([]);

// 弹窗相关
const dialogVisible = ref(false);
const isEdit = ref(false);
const formRef = ref();

// 表单数据
const form = reactive({
  id: '',
  name: '',
  locationCode: '',
  floorCount: 0,
});

// 表单校验规则
const rules = reactive({
  name: [{ required: true, message: '请输入建筑名称', trigger: 'blur' }],
  locationCode: [{ required: true, message: '请输入地址编码', trigger: 'blur' }],
  floorCount: [{ required: true, message: '请输入层数', trigger: 'blur' }, { type: 'number', min: 1, message: '层数必须大于0' }],
});

// 打开新增弹窗
const openAddDialog = () => {
  resetForm();
  isEdit.value = false;
  dialogVisible.value = true;
};

// 打开编辑弹窗
const openEditDialog = (row: any) => {
  resetForm();
  Object.assign(form, row);
  isEdit.value = true;
  dialogVisible.value = true;
};

// 重置表单
const resetForm = () => {
  form.id = '';
  form.name = '';
  form.locationCode = '';
  form.floorCount = 0;
  formRef.value?.resetFields();
};

// 提交表单（新增/编辑）
const submitForm = async () => {
  try {
    await formRef.value.validate();
    if (isEdit.value) {
      // 编辑逻辑
      try {
        const response = await axios.post(`/building/update/${form.id}`, form);
        if (response.status === 200) {
          // 更新本地数据
          const index = buildingList.value.findIndex(item => item.id === form.id);
          if (index > -1) {
            buildingList.value[index] = { ...form, createTime: buildingList.value[index].createTime };
            ElMessage.success('编辑成功');
          }
        }
      } catch (error) {
        console.error('编辑建筑失败:', error);
        ElMessage.error('编辑建筑失败: ' + (error.response?.data?.msg || '网络错误'));
      }
    } else {
      // 新增逻辑
      try {
        const response = await axios.post('/building/add', form);
        if (response.status === 200) {
          // 更新本地数据
          const newId = Math.max(...buildingList.value.map(item => item.id)) + 1;
          buildingList.value.push({
            ...form,
            id: newId,
            createTime: new Date().toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' }).replace(/\//g, '-'),
          });
          ElMessage.success('新增成功');
        }
      } catch (error) {
        console.error('新增建筑失败:', error);
        ElMessage.error('新增建筑失败: ' + (error.response?.data?.msg || '网络错误'));
      }
    }
    dialogVisible.value = false;
  } catch (error) {
    console.error('表单校验失败:', error);
  }
};

// 删除建筑
const deleteBuilding = (id: number) => {
  ElMessageBox.confirm('确定要删除该建筑吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      const response = await axios.post(`/building/delete/${id}`);
      if (response.status === 200) {
        buildingList.value = buildingList.value.filter(item => item.id !== id);
        ElMessage.success('删除成功');
      }
    } catch (error) {
      console.error('删除建筑失败:', error);
      ElMessage.error('删除建筑失败: ' + (error.response?.data?.msg || '网络错误'));
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

// 获取建筑列表
const fetchBuildings = () => {
  console.log('开始获取建筑列表...')
  
  axios.get('/building/list')
    .then(response => {
      console.log('获取建筑列表响应:', response)
      
      try {
        // 检查响应结构
        if (!response) {
          console.error('建筑列表响应为空')
          ElMessage.error('服务器返回空响应')
          return
        }
        
        // 检查数据是否存在于响应中
        let buildings = []
        if (response.data && Array.isArray(response.data)) {
          buildings = response.data
        } else if (response.data && response.data.data && Array.isArray(response.data.data)) {
          buildings = response.data.data
        } else if (response.data && response.data.list && Array.isArray(response.data.list)) {
          buildings = response.data.list
        } else {
          console.warn('建筑列表数据结构不符合预期:', response.data)
          buildings = []
        }
        
        // 更新建筑列表
        buildingList.value = buildings
        console.log(`成功加载${buildings.length}条建筑记录`)
      } catch (innerError) {
        console.error('处理建筑列表数据时出错:', innerError)
        buildingList.value = []
      }
    })
    .catch(error => {
      console.error('获取建筑列表失败:', error)
      ElMessage.error('获取建筑列表失败: ' + (error.response?.data?.msg || '网络错误'))
      buildingList.value = []
    })
};

// 页面加载时获取建筑列表
onMounted(() => {
  fetchBuildings()
});
</script>

<style scoped>
.building-manage-container {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>