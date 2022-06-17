<template>
  <div class="container">
    <div class="columns">
      <div class="column pb-2" style="display: flex; flex-direction: column; align-items: flex-start;">
          <h1 class="my-2">PicketFence</h1>
          <p style="margin: 0;" >Simple and solid cross platform geofencing for Capacitor</p>
          <button class="btn mt-2" @click="requestPerms()">Request Permissions</button>
      </div>
    </div>
    <div class="columns">
      <div class="column col-6">
        <div id="map"></div>
          <div class="columns">
            <div class="pt-2 column" style="display: flex; justify-content: space-between;">
              <button class="btn" @click="addNewFence()">Add Fence</button>
              <button class="btn" @click="removeAllFences()">Remove All Fences</button>
            </div>
          </div>
      </div>
    </div>
    <div class="columns">
      <div class="column col-6">
        <h2>Overview</h2>
        <h4>Subheading</h4>
        <h4>Subheading</h4>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { Perimeter, Fence, TransitionType, LocationPermissionStatus } from '@meld/perimeter';

export default defineComponent({
  name: 'HelloWorld',
  props: {
    },
  data() {
    return {
      activeFences: Array<Fence>(),
      permStatus: new LocationPermissionStatus(),
      tokenID: "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IkpMVE0zTTIyQ0oifQ.eyJpc3MiOiJENTRGOUoyOTNTIiwiaWF0IjoxNjUyMTI3ODkwLCJleHAiOjE2NzI2MTc2MDB9.0R6_G3uGOUtC_X4KUi7apj4nxLdFMjpFgFpgznOm8r_175h8FDlwAwrrt90E_kEeq0_He98gnFMxbxHcqR5mqw",
    }
  },
  methods: {
    async logPerms(perms: LocationPermissionStatus) : Promise<void> {
      console.log(`Foreground result: ${perms.foreground}`);
      console.log(`Background result: ${perms.background}`);
    },

    async requestPerms() : Promise<void> {
      this.permStatus = await Perimeter.checkPermissions();

      if(this.permStatus.foreground != "granted")
      {
        this.permStatus = await Perimeter.requestForegroundPermissions();
      }

      if (this.permStatus.background != "granted") {
        this.permStatus = await Perimeter.requestBackgroundPermissions();
      }
    },

    async addNewFence() : Promise<void> {

      let extraData = "dooterino burgino";

      let newFence : Fence = {
        name : "Mark's Apartment",
        uid : '123idguy',
        payload: extraData,
        lat : 47.5982517,
        lng : -122.302551,
        radius : 200,
        monitor : TransitionType.Enter
      };

      try {
        Perimeter.addFence(newFence);
        this.activeFences.push(newFence);
      }
      catch(e) {
        "We were able to catch the reject call."
        console.log(e);
      }
    },

    async removeOldFence(uid: string) : Promise<void> {
      try {
        Perimeter.removeFence({ fenceUID: uid });
        this.activeFences = this.activeFences.filter(fence => fence.uid != uid);
      }
      catch(e) {
        "We were able to catch the reject call."
        console.log(e);
      }
    },
    
    removeAllFences() {
      Perimeter.removeAllFences();
    }
  },
  
  async mounted() : Promise<void> {
    console.log("Welcome");
    let currentStatus = await Perimeter.checkPermissions();
    this.logPerms(currentStatus);

    Perimeter.addListener("FenceEvent", (fenceEvent) => { 
      console.log(fenceEvent.transitionType) 
    });

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
#map { 
  width: 100%;
  height: max(30vh, 300px);
}
</style>
