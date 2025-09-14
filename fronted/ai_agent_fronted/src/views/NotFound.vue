<template>
  <div id="svgContainer"></div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import lottie from 'lottie-web'
import animationData from '@/assets/lottie/data.js'

onMounted(() => {
  const svgContainer = document.getElementById('svgContainer')
  if (svgContainer) {
    lottie.loadAnimation({
      container: svgContainer,
      renderer: 'svg',
      loop: true,
      autoplay: true,
      animationData: animationData
    })
  }
})
</script>

<style scoped>
html {
  margin: 0;
  padding: 0;
  background-color: white;
}

body,
html {
  width: 100%;
  height: 100%;
  overflow: hidden;
}

#svgContainer {
  width: 640px;
  height: 512px;
  background-color: white;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  margin: auto;
}
</style>
