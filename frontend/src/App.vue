<script setup>
// 无需导入 HelloWorld 等示例组件，路由会动态渲染内容
</script>

<template>
  <!-- 路由视图：所有匹配的路由组件将在这里渲染 -->
  <router-view />
</template>

<style scoped>
/* 清除默认样式，可根据需要添加全局通用样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  min-height: 100vh;
  background-color: #f5f7fa; /* 浅灰背景，适合管理系统 */
}
</style>