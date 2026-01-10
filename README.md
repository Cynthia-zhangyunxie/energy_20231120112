# energy_20231120112
2025秋软件设计与体系结构 - 智慧校园能耗监测与管理平台

## 项目介绍
本项目是一套面向校园场景的能耗监测与管理系统，基于前后端分离架构开发，实现校园建筑、能耗设备的统一管理，能耗数据实时采集、可视化展示，以及异常告警自动触发等核心功能，助力校园能耗精细化管理。

## 技术栈
### 前端技术
- 框架：Vue 3
- 状态管理：Pinia
- 路由：Vue Router
- 网络请求：Axios
- 可视化：ECharts
- UI组件：Element Plus（可选，根据实际使用补充）

### 后端技术
- 框架：Spring Boot
- 持久层：MyBatis-Plus
- 安全认证：Shiro + JWT
- 数据库：MySQL
- 工具：Lombok、Spring Validation

## 核心功能模块
### 1. 用户管理
- 基于角色的权限控制（管理员/普通用户）
- 用户状态管理、账号安全验证

### 2. 建筑与设备管理
- 校园建筑信息维护（名称、楼层、位置编码）
- 能耗设备管理（电表/水表等，含设备编号、所属建筑、额定功率）
- 设备状态实时监控（运行/离线/故障）

### 3. 能耗数据监测
- 设备能耗数据实时采集（电压、电流、实际功率、累计能耗）
- 按建筑/设备/时间维度统计能耗（今日/本周/本月能耗）
- 能耗数据可视化展示（折线图、柱状图、占比图）

### 4. 告警管理
- 多类型告警触发（电压异常、电流异常、功率超限）
- 告警记录查询、按设备编号检索
- 告警批量删除、状态标记

## 系统架构
### 整体架构
- 前端：表现层 → 路由层 → 状态管理层 → 网络层 → 工具层
- 后端：接口层（Controller）→ 业务层（Service）→ 数据访问层（Repository）→ 数据库层

### 核心业务类设计
- 能耗数据服务：EnergyDataService + EnergyDataServiceImpl
- 告警服务：AlarmService + AlarmServiceImpl
- 设备服务：DeviceService + DeviceServiceImpl

## 快速运行
### 前置条件
- 本地安装 JDK 1.8+、MySQL 8.0+、Node.js 16+
- 配置数据库连接（修改后端 `application.yml` 中的数据库地址/账号/密码）

### 后端运行
# 克隆仓库


git clone https://github.com/你的用户名/仓库名.git
# 进入后端目录
cd 仓库名/energy-backend
# 编译打包
mvn clean package
# 运行jar包
java -jar target/energy-backend-0.0.1-SNAPSHOT.jar

###前端运行

# 进入前端目录
cd 仓库名/frontend
# 安装依赖
npm install
# 启动开发服务
npm run dev
