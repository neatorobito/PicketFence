<template>
  <div class="hello">
    <h1>{{ msg }}</h1>
    <p>
      For a guide and recipes on how to configure / customize this project,<br>
      check out the
      <a href="https://cli.vuejs.org" target="_blank" rel="noopener">vue-cli documentation</a>.
    </p>
    <h3>Installed CLI Plugins</h3>
    <button @click="requestPerms()">Request Permissions</button>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { Perimeter, PermissionStatus } from 'perimeter';

export default defineComponent({
  name: 'HelloWorld',
  props: {
    msg: String,
  },
  data() {
    "world"
  },
  methods: {
    async logPerms(perms: PermissionStatus) : Promise<void> {
      console.log(`Foreground result: ${perms.foreground}`);
      console.log(`Background result: ${perms.background}`);
    },

    async requestPerms() : Promise<void> {
      let currentStatus = await Perimeter.checkPermissions();

      if(currentStatus.foreground != "granted")
      {
        currentStatus = await Perimeter.requestForegroundPermissions();
        this.logPerms(currentStatus);
      }

      if (currentStatus.background != "granted") {
        currentStatus = await Perimeter.requestBackgroundPermissions();
        this.logPerms(currentStatus);
      }
    }
  },
  async mounted() : Promise<void> {
    console.log("Welcome");
    let currentStatus = await Perimeter.checkPermissions();
    this.logPerms(currentStatus);
  }
});
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h3 {
  margin: 40px 0 0;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}
</style>
