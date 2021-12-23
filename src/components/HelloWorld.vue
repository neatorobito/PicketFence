<template>
  <div class="hello">
    <h1>{{ msg }}</h1>
    <p>
      For a guide and recipes on how to configure / customize this project,<br>
      check out the
      <a href="https://cli.vuejs.org" target="_blank" rel="noopener">vue-cli documentation</a>.
    </p>
    <h3>Installed CLI Plugins</h3>
    <button class="btn" @click="requestPerms()">Request Permissions</button>
    <button class="btn" @click="addNewFence()">Add Fence</button>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { Perimeter, Fence, TransitionType, PermissionStatus } from 'perimeter';

export default defineComponent({
  name: 'HelloWorld',
  props: {
    msg: String,
  },
  data() {
    return {
      activeFences: Array<Fence>()
    }
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
    },

    async addNewFence() : Promise<void> {

      let payload = "dooterino burgino";
      let newFence : Fence = {
        fenceName : "Mark's Apartment",
        fenceId : '123idguy',
        interests: payload,
        lat : 47.598270,
        lng : -122.302560,
        radius : 100,
        expires : 5 * 60000,
        transitionType : 0
      };

      try {
        Perimeter.addFence(newFence);
        this.activeFences.push(newFence);
      }
      catch(e) {
        "We were able to catch the reject call."
        console.log(e);
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
