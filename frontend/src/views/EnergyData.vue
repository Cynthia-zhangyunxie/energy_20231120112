<template>
  <div class="energy-data-container">
    <el-page-header content="能耗数据"></el-page-header>
    
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select v-model="selectedDeviceId" placeholder="请选择设备" style="width: 200px; margin-right: 20px" @change="fetchEnergyData">
        <el-option label="全部设备" value="" />
        <el-option v-for="item in deviceList" :key="item.id" :label="item.name + ' (' + item.sn + ')'" :value="item.id" />
      </el-select>
      <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 300px"
          @change="fetchEnergyData"
      />
    </div>

    <!-- 统计数据卡片 -->
    <div class="card-group">
      <el-card class="stat-card">
        <div class="card-title">当前电压</div>
        <div class="card-value">{{ currentVoltage }} V</div>
      </el-card>
      <el-card class="stat-card">
        <div class="card-title">当前电流</div>
        <div class="card-value">{{ currentCurrent }} A</div>
      </el-card>
      <el-card class="stat-card">
        <div class="card-title">当前功率</div>
        <div class="card-value">{{ currentPower }} kW</div>
      </el-card>
      <el-card class="stat-card">
        <div class="card-title">总能耗</div>
        <div class="card-value">{{ totalEnergy.toFixed(2) }} kWh</div>
      </el-card>
    </div>
    
    <!-- 平均数据统计卡片 -->
    <div class="card-group">
      <el-card class="stat-card">
        <div class="card-title">平均电压</div>
        <div class="card-value">{{ avgVoltage }} V</div>
      </el-card>
      <el-card class="stat-card">
        <div class="card-title">平均电流</div>
        <div class="card-value">{{ avgCurrent }} A</div>
      </el-card>
      <el-card class="stat-card">
        <div class="card-title">平均功率</div>
        <div class="card-value">{{ avgPower }} kW</div>
      </el-card>
      <el-card class="stat-card">
        <div class="card-title">平均能耗</div>
        <div class="card-value">{{ avgEnergy }} kWh</div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <!-- 1. 设备最近10次功率变化折线图 -->
    <div class="chart-container">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>设备最近10次功率变化</span>
          </div>
        </template>
        <div ref="recentPowerChartRef" class="chart" style="height: 300px;"></div>
      </el-card>
    </div>

    <!-- 2. 能耗趋势图 -->
    <div class="chart-container">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>能耗趋势图</span>
            <el-radio-group v-model="chartType" size="small" @change="updateChart">
              <el-radio-button label="line">折线图</el-radio-button>
              <el-radio-button label="area">面积图</el-radio-button>
              <el-radio-button label="bar">柱状图</el-radio-button>
            </el-radio-group>
            <el-radio-group v-model="xAxisType" size="small" @change="updateChart">
              <el-radio-button label="index">按序号</el-radio-button>
              <el-radio-button label="time">按时间</el-radio-button>
            </el-radio-group>
          </div>
        </template>
        <div ref="chartRef" class="chart" style="height: 400px;"></div>
      </el-card>
    </div>

    <!-- 3. 不同建筑今日用电量饼状图 -->
    <div class="chart-container">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>今日建筑用电量占比</span>
          </div>
        </template>
        <div ref="buildingPieChartRef" class="chart" style="height: 300px;"></div>
      </el-card>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table :data="energyData" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="device_id" label="设备ID" width="100"></el-table-column>
        <el-table-column label="电压(V)" width="100">
          <template #default="scope">
            {{ scope.row.voltage !== undefined ? scope.row.voltage : (scope.row.v || '-') }}
          </template>
        </el-table-column>
        <el-table-column label="电流(A)" width="100">
          <template #default="scope">
            {{ scope.row.current !== undefined ? scope.row.current : (scope.row.c || '-') }}
          </template>
        </el-table-column>
        <el-table-column label="功率(kW)" width="120">
          <template #default="scope">
            {{ scope.row.real_power !== undefined ? scope.row.real_power : 
               (scope.row.power !== undefined ? scope.row.power : 
               (scope.row.voltage !== undefined && scope.row.current !== undefined ? 
                 (scope.row.voltage * scope.row.current).toFixed(2) : '-')) }}
          </template>
        </el-table-column>
        <el-table-column label="能耗(kWh)" width="120">
          <template #default="scope">
            <!-- 优先显示total_energy，如果没有则显示energy，都没有则显示'-' -->
            {{ 
              scope.row.total_energy !== undefined && scope.row.total_energy !== null && scope.row.total_energy !== '' 
                ? Number(scope.row.total_energy).toFixed(2)
                : (scope.row.energy !== undefined && scope.row.energy !== null && scope.row.energy !== '' 
                  ? Number(scope.row.energy).toFixed(2)
                  : '-') 
            }}
          </template>
        </el-table-column>
        <el-table-column prop="collect_time" label="采集时间" width="180"></el-table-column>
        <el-table-column prop="create_time" label="创建时间" width="180"></el-table-column>
      </el-table>
      <div class="empty-state" v-if="!loading && energyData.length === 0">
        <el-empty description="暂无数据"></el-empty>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import axios from '@/api/request';
import * as echarts from 'echarts';

// 设备列表
const deviceList = ref([]);

// 能耗数据
const energyData = ref([]);

// 加载状态
const loading = ref(false);

// 筛选条件
const selectedDeviceId = ref('');
const dateRange = ref<[Date, Date]>([new Date('2025-01-01'), new Date()]);

// 统计数据
const totalEnergy = ref(0);
const avgVoltage = ref(0);
const avgCurrent = ref(0);
const avgPower = ref(0);
const avgEnergy = ref(0);

// 当前数据
const currentVoltage = ref(0);
const currentCurrent = ref(0);
const currentPower = ref(0);

// 图表相关数据
const chartRef = ref(null);
const recentPowerChartRef = ref(null);
const buildingPieChartRef = ref(null);
const chartType = ref('line');
const xAxisType = ref('index');
let chartInstance = null;
let recentPowerChartInstance = null;
let buildingPieChartInstance = null;

// 建筑能耗数据
const buildingEnergyData = ref([]);

// 获取建筑能耗数据
const fetchBuildingEnergyData = async () => {
  try {
    // 获取今天的日期
    const today = new Date().toISOString().split('T')[0];
    
    const response = await axios.get('/energy/building/today', {
      params: {
        date: today
      }
    });
    
    console.log('建筑能耗数据响应:', response);
    
    let data = [];
    if (response.data && Array.isArray(response.data)) {
      data = response.data;
    } else if (response.data && response.data.data && Array.isArray(response.data.data)) {
      data = response.data.data;
    } else if (response.data && response.data.list && Array.isArray(response.data.list)) {
      data = response.data.list;
    }
    
    buildingEnergyData.value = data;
    
    // 更新建筑用电量饼图
    nextTick(() => {
      updateBuildingPieChart();
    });
  } catch (error) {
    console.error('获取建筑能耗数据失败:', error);
    ElMessage.error('获取建筑能耗数据失败: ' + (error.response?.data?.msg || '网络错误'));
    buildingEnergyData.value = [];
  }
};

// 获取能耗数据
const fetchEnergyData = async () => {
  loading.value = true;
  try {
    console.log('开始获取能耗数据...');
    
    // 格式化日期参数
    const startDate = dateRange.value[0].toISOString().split('T')[0];
    const endDate = dateRange.value[1].toISOString().split('T')[0];
    
    // 构建请求参数
    const params = {
      startDate,
      endDate
    };
    
    // 发起API请求 - 使用 /energy/energy 接口
    let response = null;
    
    // 根据是否选择设备构造不同请求
    if (selectedDeviceId.value) {
      // 如果选择了特定设备，获取该设备的能耗数据
      response = await axios.get(`/energy/recent/${selectedDeviceId.value}`, { params });
    } else {
      // 如果选择"全部设备"，获取所有设备的能耗数据
      // 需要先获取所有设备列表，然后分别获取每个设备的数据
      if (deviceList.value.length === 0) {
        // 如果还没有设备列表，先获取设备列表
        const deviceResponse = await axios.get('/device/devices');
        if (deviceResponse.data) {
          deviceList.value = deviceResponse.data;
        }
      }
      
      // 获取所有设备的能耗数据
      const allDevicesData = [];
      for (const device of deviceList.value) {
        try {
          const deviceResponse = await axios.get(`/energy/recent/${device.id}`, { params });
          if (deviceResponse.data && Array.isArray(deviceResponse.data)) {
            allDevicesData.push(...deviceResponse.data);
          }
        } catch (deviceError) {
          console.error(`获取设备 ${device.id} 能耗数据失败:`, deviceError);
        }
      }
      
      // 直接使用获取到的所有设备数据
      response = { data: allDevicesData };
    }
    
    console.log('获取能耗数据响应:', response);
    
    // 检查响应结构
    if (!response) {
      console.error('能耗数据响应为空');
      ElMessage.error('服务器返回空响应');
      return;
    }
    
    // 提取数据
    let data = [];
    if (response.data && Array.isArray(response.data)) {
      data = response.data;
    } else if (response.data && response.data.data && Array.isArray(response.data.data)) {
      data = response.data.data;
    } else if (response.data && response.data.list && Array.isArray(response.data.list)) {
      data = response.data.list;
    } else {
      console.warn('能耗数据结构不符合预期:', response.data);
      data = [];
    }
    
    // 更新能耗数据
    energyData.value = data;
    
    // 计算统计数据
    calculateStatistics(data);
    
    // 更新图表
    nextTick(() => {
      updateChart();
      updateRecentPowerChart();
    });
    
  } catch (error) {
    console.error('获取能耗数据失败:', error);
    ElMessage.error('获取能耗数据失败: ' + (error.response?.data?.msg || '网络错误'));
    
    // 设置空数据
    energyData.value = [];
    resetStatistics();
  } finally {
    loading.value = false;
  }
};

  // 计算统计数据
  const calculateStatistics = (data) => {
    if (data.length === 0) {
      resetStatistics();
      return;
    }
    
    // 检查数据结构和字段映射
    console.log('当前数据样本:', JSON.stringify(data[0]));
    
    // 提取各字段数据
    const voltageData = data.map(item => Number(item.voltage || item.v || 0));
    const currentData = data.map(item => Number(item.current || item.c || 0));
    
    // 检查功率和能耗字段是否存在
    let powerField = 'real_power';
    let energyField = 'total_energy';
    
    // 如果real_power不存在，检查是否有其他可能的字段名
    if (!data.some(item => item.real_power !== undefined)) {
      console.log('未找到real_power字段，尝试使用替代字段');
      powerField = data.some(item => item.power !== undefined) ? 'power' : null;
    }
    
    // 如果total_energy不存在，检查是否有其他可能的字段名
    if (!data.some(item => item.total_energy !== undefined)) {
      console.log('未找到total_energy字段，尝试使用替代字段');
      energyField = data.some(item => item.energy !== undefined) ? 'energy' : null;
    }
    
    // 处理功率数据
    let powerData = [];
    if (powerField) {
      powerData = data.map(item => Number(item[powerField] || 0));
      // 计算平均功率
      avgPower.value = powerData.length > 0 ? 
        Math.round((powerData.reduce((sum, val) => sum + val, 0) / powerData.length) * 100) / 100 : 0;
      
      // 设置当前功率
      currentPower.value = Number(data[0][powerField] || 0);
    } else {
      // 如果没有功率字段，则使用电压*电流计算
      console.log('使用电压*电流计算功率');
      powerData = data.map(item => 
        Number((item.voltage || 0) * (item.current || 0))
      );
      avgPower.value = powerData.length > 0 ? 
        Math.round((powerData.reduce((sum, val) => sum + val, 0) / powerData.length) * 100) / 100 : 0;
      currentPower.value = powerData.length > 0 ? powerData[0] : 0;
    }
    
    // 处理能耗数据 - 优先使用数据库中的值
    if (energyField && data.some(item => item[energyField] !== undefined)) {
      // 有能耗字段并且至少有一条记录包含这个字段
      const energyData = data.map(item => Number(item[energyField] || 0));
      totalEnergy.value = energyData.reduce((sum, val) => sum + val, 0);
      avgEnergy.value = energyData.length > 0 ? 
        Math.round((energyData.reduce((sum, val) => sum + val, 0) / energyData.length) * 100) / 100 : 0;
    } else {
      // 如果没有能耗字段，则计算总能耗
      console.log('未找到能耗字段，尝试计算能耗');
      let total = 0;
      data.forEach(item => {
        // 假设能耗 = 功率 * 时间，这里假设每个数据点代表1小时
        const power = powerField ? Number(item[powerField] || 0) : 
          Number((item.voltage || 0) * (item.current || 0));
        total += power; // 这里简化计算，实际应考虑时间间隔
      });
      totalEnergy.value = Math.round(total * 100) / 100;
      avgEnergy.value = powerData.length > 0 ? 
        Math.round((powerData.reduce((sum, val) => sum + val, 0) / powerData.length) * 100) / 100 : 0;
    }
    
    // 计算电压和电流的统计数据
    avgVoltage.value = voltageData.length > 0 ? 
      Math.round((voltageData.reduce((sum, val) => sum + val, 0) / voltageData.length) * 100) / 100 : 0;
    
    avgCurrent.value = currentData.length > 0 ? 
      Math.round((currentData.reduce((sum, val) => sum + val, 0) / currentData.length) * 100) / 100 : 0;
    
    // 获取当前电压和电流（最新一条记录）
    if (data.length > 0) {
      currentVoltage.value = Number(data[0].voltage || data[0].v || 0);
      currentCurrent.value = Number(data[0].current || data[0].c || 0);
    }
  };

// 重置统计数据
const resetStatistics = () => {
  totalEnergy.value = 0;
  avgVoltage.value = 0;
  avgCurrent.value = 0;
  avgPower.value = 0;
  avgEnergy.value = 0;
  currentVoltage.value = 0;
  currentCurrent.value = 0;
  currentPower.value = 0;
};

// 获取设备列表
const fetchDevices = async () => {
  try {
    console.log('开始获取设备列表...');
    
    // 发起API请求获取设备列表
    const response = await axios.get('/device/list');
    
    console.log('获取设备列表响应:', response);
    
    // 检查响应结构
    if (!response) {
      console.error('设备列表响应为空');
      ElMessage.error('服务器返回空响应');
      return;
    }
    
    // 提取数据
    let devices = [];
    if (response.data && Array.isArray(response.data)) {
      devices = response.data;
    } else if (response.data && response.data.data && Array.isArray(response.data.data)) {
      devices = response.data.data;
    } else if (response.data && response.data.list && Array.isArray(response.data.list)) {
      devices = response.data.list;
    } else {
      console.warn('设备列表数据结构不符合预期:', response.data);
      devices = [];
    }
    
    // 更新设备列表
    deviceList.value = devices;
    console.log(`成功加载${devices.length}条设备记录`);
    
  } catch (error) {
    console.error('获取设备列表失败:', error);
    ElMessage.error('获取设备列表失败: ' + (error.response?.data?.msg || '网络错误'));
    
    // 设置空数据
    deviceList.value = [];
  }
};

// 监听设备选择变化
watch(selectedDeviceId, () => {
  fetchEnergyData();
});

// 监听日期范围变化
watch(dateRange, () => {
  fetchEnergyData();
});

// 初始化图表
const initChart = () => {
  // 初始化主图表
  if (chartRef.value) {
    chartInstance = echarts.init(chartRef.value);
    updateChart();
  }
  
  // 初始化最近10次功率变化图表
  if (recentPowerChartRef.value) {
    recentPowerChartInstance = echarts.init(recentPowerChartRef.value);
    updateRecentPowerChart();
  }
  
  // 初始化建筑用电量饼图
  if (buildingPieChartRef.value) {
    buildingPieChartInstance = echarts.init(buildingPieChartRef.value);
    updateBuildingPieChart();
  }
};

// 更新图表
const updateChart = () => {
  if (!chartInstance || energyData.value.length === 0) {
    return;
  }

  // 处理x轴数据
  let xData = [];
  if (xAxisType.value === 'index') {
    xData = energyData.value.map((_, index) => `第${index + 1}条`);
  } else {
    // 按时间排序数据
    const sortedData = [...energyData.value].sort((a, b) => {
      const timeA = new Date(a.collect_time || a.create_time || 0).getTime();
      const timeB = new Date(b.collect_time || b.create_time || 0).getTime();
      return timeA - timeB;
    });
    
    xData = sortedData.map(item => {
      const time = item.collect_time || item.create_time;
      if (time) {
        const date = new Date(time);
        return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`;
      }
      return `第${xData.length + 1}条`;
    });
  }

  // 处理y轴数据
  const voltageData = energyData.value.map(item => Number(item.voltage || item.v || 0));
  const currentData = energyData.value.map(item => Number(item.current || item.c || 0));
  
  // 检查功率字段
  let powerField = 'real_power';
  if (!energyData.value.some(item => item.real_power !== undefined)) {
    powerField = energyData.value.some(item => item.power !== undefined) ? 'power' : null;
  }
  
  const powerData = powerField ? 
    energyData.value.map(item => Number(item[powerField] || 0)) :
    energyData.value.map(item => Number((item.voltage || 0) * (item.current || 0)));
  
  // 检查能耗字段
  let energyField = 'total_energy';
  if (!energyData.value.some(item => item.total_energy !== undefined)) {
    energyField = energyData.value.some(item => item.energy !== undefined) ? 'energy' : null;
  }
  
  const energyChartData = energyField ? 
    energyData.value.map(item => Number(item[energyField] || 0)) :
    powerData.map((power, index) => power * (index + 1)); // 模拟累计能耗

  // 构建图表配置
  let series = [];
  let yAxis = [];
  
  if (chartType.value === 'line') {
    series = [
      {
        name: '电压(V)',
        type: 'line',
        data: voltageData,
        smooth: true
      },
      {
        name: '电流(A)',
        type: 'line',
        data: currentData,
        smooth: true
      },
      {
        name: '功率(kW)',
        type: 'line',
        data: powerData,
        smooth: true
      }
    ];
    
    if (energyField) {
      series.push({
        name: '能耗(kWh)',
        type: 'line',
        data: energyChartData,
        smooth: true
      });
    }
    
    yAxis = [
      { type: 'value', name: '数值', position: 'left' }
    ];
  } else if (chartType.value === 'area') {
    series = [
      {
        name: '功率(kW)',
        type: 'line',
        data: powerData,
        smooth: true,
        areaStyle: {}
      }
    ];
    
    if (energyField) {
      series.push({
        name: '能耗(kWh)',
        type: 'line',
        data: energyChartData,
        smooth: true,
        areaStyle: {}
      });
    }
    
    yAxis = [
      { type: 'value', name: '数值', position: 'left' }
    ];
  } else {
    // 柱状图
    series = [
      {
        name: '电压(V)',
        type: 'bar',
        data: voltageData
      },
      {
        name: '电流(A)',
        type: 'bar',
        data: currentData
      },
      {
        name: '功率(kW)',
        type: 'bar',
        data: powerData
      }
    ];
    
    if (energyField) {
      series.push({
        name: '能耗(kWh)',
        type: 'bar',
        data: energyChartData
      });
    }
    
    yAxis = [
      { type: 'value', name: '数值', position: 'left' }
    ];
  }

  const option = {
    title: {
      text: '能耗数据趋势图',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: series.map(s => s.name),
      top: 30
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: chartType.value === 'bar',
      data: xData
    },
    yAxis: yAxis,
    series: series,
    color: ['#5470c6', '#91cc75', '#fac858', '#ee6666']
  };

  chartInstance.setOption(option);
};

// 更新最近10次功率变化图表
const updateRecentPowerChart = () => {
  if (!recentPowerChartInstance || energyData.value.length === 0) {
    return;
  }

  // 获取最近10条数据（如果数据不足10条则取所有）
  const recentData = energyData.value.slice(0, 10).reverse();
  
  // 处理x轴数据
  const xData = recentData.map((_, index) => `第${index + 1}次`);
  
  // 检查功率字段
  let powerField = 'real_power';
  if (!recentData.some(item => item.real_power !== undefined)) {
    powerField = recentData.some(item => item.power !== undefined) ? 'power' : null;
  }
  
  // 处理功率数据
  const powerData = powerField ? 
    recentData.map(item => Number(item[powerField] || 0)) :
    recentData.map(item => Number((item.voltage || 0) * (item.current || 0)));

  const option = {
    title: {
      text: '最近10次功率变化',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        const data = params[0];
        const date = new Date(recentData[data.dataIndex].collect_time || recentData[data.dataIndex].create_time);
        const timeStr = `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`;
        return `时间: ${timeStr}<br/>功率: ${data.value} kW`;
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xData
    },
    yAxis: {
      type: 'value',
      name: '功率(kW)',
      axisLabel: {
        formatter: '{value} kW'
      }
    },
    series: [
      {
        name: '功率',
        type: 'line',
        data: powerData,
        smooth: true,
        itemStyle: {
          color: '#5470c6'
        },
        lineStyle: {
          width: 3
        },
        areaStyle: {
          opacity: 0.1
        }
      }
    ]
  };

  recentPowerChartInstance.setOption(option);
};

// 更新建筑用电量饼图
const updateBuildingPieChart = () => {
  if (!buildingPieChartInstance || buildingEnergyData.value.length === 0) {
    // 如果没有建筑能耗数据，尝试使用当前能耗数据计算
    if (!buildingPieChartInstance) return;
    
    // 从当前能耗数据中按建筑分组计算用电量
    const buildingMap = {};
    
    energyData.value.forEach(item => {
      // 检查建筑信息
      if (item.building_id || item.buildingId) {
        const buildingId = item.building_id || item.buildingId;
        const buildingName = item.building_name || item.buildingName || `建筑${buildingId}`;
        
        // 检查功率和能耗字段
        let powerField = 'real_power';
        if (!item.real_power) {
          powerField = item.power ? 'power' : null;
        }
        
        let energyField = 'total_energy';
        if (!item.total_energy) {
          energyField = item.energy ? 'energy' : null;
        }
        
        // 计算能耗
        let energyValue = 0;
        if (energyField) {
          energyValue = Number(item[energyField] || 0);
        } else if (powerField) {
          energyValue = Number(item[powerField] || 0); // 简化计算，假设每个数据点代表1小时
        } else {
          energyValue = Number((item.voltage || 0) * (item.current || 0));
        }
        
        // 累加建筑能耗
        if (buildingMap[buildingId]) {
          buildingMap[buildingId].energy += energyValue;
        } else {
          buildingMap[buildingId] = {
            name: buildingName,
            energy: energyValue
          };
        }
      }
    });
    
    // 转换为饼图数据格式
    const pieData = Object.values(buildingMap).map(item => ({
      name: item.name,
      value: item.energy
    }));
    
    const option = {
      title: {
        text: '今日建筑用电量占比',
        left: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} kWh ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        data: pieData.map(item => item.name)
      },
      series: [
        {
          name: '用电量',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['50%', '60%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            formatter: '{b}: {d}%'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: true
          },
          data: pieData
        }
      ]
    };

    buildingPieChartInstance.setOption(option);
    return;
  }

  // 使用建筑能耗数据更新饼图
  const pieData = buildingEnergyData.value.map(item => ({
    name: item.building_name || item.buildingName || `建筑${item.building_id || item.buildingId}`,
    value: Number(item.energy || item.total_energy || 0)
  }));

  const option = {
    title: {
      text: '今日建筑用电量占比',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} kWh ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: pieData.map(item => item.name)
    },
    series: [
      {
        name: '用电量',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '60%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {d}%'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: true
        },
        data: pieData
      }
    ]
  };

  buildingPieChartInstance.setOption(option);
};

// 页面加载初始化
onMounted(() => {
  fetchDevices();
  fetchEnergyData();
  fetchBuildingEnergyData();
  
  // 初始化图表
  nextTick(() => {
    initChart();
  });
});
</script>

<style scoped>
.energy-data-container {
  padding: 20px;
}

.filter-bar {
  padding: 10px 0;
  display: flex;
  align-items: center;
  background: #f5f7fa;
  padding: 15px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.card-group {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  flex: 1;
  text-align: center;
}

.card-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.table-container {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-top: 20px;
}

.empty-state {
  margin-top: 50px;
  text-align: center;
}

.chart-container {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.card-header span {
  font-weight: bold;
  font-size: 16px;
}

.card-header .el-radio-group {
  display: flex;
  gap: 10px;
}
</style>