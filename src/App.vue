<template>
  <FenceDemo />
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import FenceDemo from './components/FenceDemo.vue';

export default defineComponent({
  name: 'App',
  components: {
    FenceDemo
  }
});
</script>

<style>
#app {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100vh;
  width: 100vw; 
  display: flex;
  flex-direction: column;
}

.container {
  height: 100%;
}

a {
  color: #42b983;
}

h1, h2, h3, h4, h5 {
  margin: 0;
}

</style>
