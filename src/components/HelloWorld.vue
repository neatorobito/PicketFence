<template>
  <div class="flexbox col">
    <div class="container" style="padding: 0;">
      <div id="mapkit_js"></div>
      <div class="container" style="height: auto; z-index: 1; position: absolute; left: calc(50% - 50vw); top: 0; margin-top: 2rem;">
        <input type="text" id="input-text" placeholder="Find an address" />
      </div>
    </div>
    <div class="container">


      <h1 style="margin-top: 0.5rem;">Welcome to PicketFence</h1>
      <p style=";" >This sample app demonstrates basic geofencing capabilities with Perimeter.</p>

      <div class="flexbox col">
        <p class="text-align-center">To get started, tap the button below. </p>
        <button class="centered btn button-primary mt-2" v-if="hasCorrectPermissions" @click="requestPerms()">Request Permissions</button>
      </div>

    </div>
    <i id="global_status" class="color-info text-align-center position-bottom margin-top-s">Status: Waiting for permissions</i>
  </div>

</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { Perimeter, Fence, FenceEvent, PerimeterEvent, TransitionType, LocationPermissionStatus } from '@meld/perimeter';
import { createMapkit, mapkit } from 'vue-mapkit'
import { Geolocation } from '@capacitor/geolocation';

export default defineComponent({
  name: 'HelloWorld',
  props: {
    },
  data() {
    return {
      map: null,
      activeFences: Array<Fence>(),
      permStatus: new LocationPermissionStatus(),
    }
  },
  computed: {

    hasCorrectPermissions() {
      return !(this.$data['permStatus'].background === 'granted' && this.$data['permStatus'].foreground === 'granted');
    },

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

    addNewFence() {

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

      Perimeter.addFence(newFence).then(() => {
        this.activeFences.push(newFence);
      })
      .catch((e) => {
        console.log(e);
      });

    },

    removeOldFence(uid: string) {
      Perimeter.removeFence({ fenceUID : uid}).then(() => {
        this.activeFences = this.activeFences.filter(fence => fence.uid != uid);
      })
      .catch((e) => {
        console.log(e);
      });
    },
    
    removeAllFences() {
      Perimeter.removeAllFences();
    }
  },
  
  async mounted() : Promise<void> {
    // let currentStatus = await Perimeter.checkPermissions();
    // this.logPerms(currentStatus);

    // Perimeter.addListener("FenceEvent", (data: PerimeterEvent) => { 
    //   console.log((data as FenceEvent).fences[0].name);
    // });

    const coordinates = await Geolocation.getCurrentPosition();
    this.map = await createMapkit("mapkit_js", { language: 'en' });
    console.log(coordinates)    
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
#mapkit_js { 
  width: 100%;
  height: max(55vh, 500px);
}
</style>
