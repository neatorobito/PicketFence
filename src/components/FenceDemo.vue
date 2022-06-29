<template>
  <div class="flexbox col" style="height: 100%;">
    <div class="container" style="padding: 0;">
      <l-map id="fence-map" :center="mapCenter" :zoom="mapZoom" :zoomAnimation=true :options="{zoomControl: false}" style="z-index: 0">
        <l-tile-layer :url="TILE_LAYER" :attribution="MAPS_ATTRIBUTION"></l-tile-layer>
        <l-marker :lat-lng="presentLocat" :visible="shouldShowUserMarker"></l-marker>
        <l-control-zoom position="bottomleft"></l-control-zoom>
      </l-map>
      <div class="container" style="height: auto; z-index: 1; position: absolute; left: calc(50% - 50vw); top: 0; margin-top: 3rem;">
        <input type="text" id="input-text" placeholder="Find an address" />
      </div>
    </div>
    <div class="container flexbox col">
        <h1 style="margin-top: 0.5rem; margin-bottom: 0.5rem;">PicketFence</h1>
        <p>This sample app demonstrates basic geofencing capabilities with Perimeter.</p>
        <p v-if="instructionsText"> {{ instructionsText }} </p>
        <button class="btn button-primary" v-if="actionButtonText" @click="actionForButton">{{ actionButtonText }}</button>
    </div>
    <i id="global_status" v-if="statusText" class="color-info text-align-center position-bottom padding-s">Status: {{ statusText }}</i>
  </div>

</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { Perimeter, Fence, FenceEvent, PerimeterEvent, TransitionType, LocationPermissionStatus } from '@meld/perimeter'
import { Geolocation, Position } from '@capacitor/geolocation'
import { SplashScreen } from '@capacitor/splash-screen'
import { StateData, StateDataResolver, NamedStates } from '../StateConstructs';

import "leaflet/dist/leaflet.css"
import { LMap, LTileLayer, LMarker, LControlZoom } from "@vue-leaflet/vue-leaflet"

export default defineComponent({
  name: 'FenceDemo',
  components: {
    LMap,
    LTileLayer,
    LMarker,
    LControlZoom
  },
  data: () => {
    return {
      APP_STATE: NamedStates.BLANK,
      TILE_LAYER: "https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png",
      MAPS_ATTRIBUTION: `&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>`,
      NOMINATIM_API: {
        'STATUS': 'https://nominatim.openstreetmap.org/status.php'
      },
      actionForButton: ((payload: MouseEvent) => {}) as ((payload: MouseEvent) => void),
      actionButtonText: null as string | null,
      instructionsText: null as string | null,
      statusText: null as string | null,
      lastLocat: null as Position | null,
      mapZoom: 3,
      mapCenter: [47.6053581, -122.336566],
      shouldShowUserMarker: false,
      activeFences: Array<Fence>(),
      permStatus: new LocationPermissionStatus(),
    }
  },
  watch: {
    'permStatus' (_updatedPermissions) {
      if(this.hasCorrectPermissions)
      {
        this.configureMap()
        this.APP_STATE = NamedStates.READY_FOR_FENCE
      }
      else
      {
        this.APP_STATE = NamedStates.NEEDS_PERMISSIONS
      }
    },

    'APP_STATE' (newState) {

      this.actionButtonText = null;
      this.instructionsText = null;
      this.statusText = null;

      let newStateData = StateDataResolver[newState] as StateData

      this.actionButtonText = newStateData.actionButtonText
      this.instructionsText = newStateData.instructionsText
      this.statusText = newStateData.statusText

      switch (newState) {
        case NamedStates.NEEDS_PERMISSIONS: {
          this.actionForButton = this.requestPerms
          break
        }
        case NamedStates.NOMINATIM_UNAVAILABLE: {
          this.actionForButton = this.getNominatimServerStatus
          break
        }
        default: {
          break
        }
      }

    }
  },
  
  computed: {

    hasCorrectPermissions() {
      return (this.$data['permStatus'].background === 'granted' && this.$data['permStatus'].foreground === 'granted')
    },

    presentLocat() {
      return this.$data.lastLocat === null ? [0,0] : [this.$data.lastLocat.coords.latitude, this.$data.lastLocat.coords.longitude]
    }

  },
  methods: {
    async logPerms(perms: LocationPermissionStatus) : Promise<void> {
      console.log(`Foreground result: ${perms.foreground}`)
      console.log(`Background result: ${perms.background}`)
    },

    async requestPerms(e: MouseEvent) : Promise<void> {
      this.permStatus = await Perimeter.checkPermissions()

      if(this.permStatus.foreground != "granted")
      {
        this.permStatus = await Perimeter.requestForegroundPermissions()
      }

      if (this.permStatus.background != "granted") {
        this.permStatus = await Perimeter.requestBackgroundPermissions()
      }
    },

    addNewFence() {

      let extraData = "dooterino burgino"

      let newFence : Fence = {
        name : "Mark's Apartment",
        uid : '123idguy',
        payload: extraData,
        lat : 47.5982517,
        lng : -122.302551,
        radius : 200,
        monitor : TransitionType.Enter
      }

      Perimeter.addFence(newFence).then(() => {
        this.activeFences.push(newFence)
      })
      .catch((e) => {
        console.log(e)
      })

    },

    removeOldFence(uid: string) {
      Perimeter.removeFence({ fenceUID : uid}).then(() => {
        this.activeFences = this.activeFences.filter(fence => fence.uid != uid)
      })
      .catch((e) => {
        console.log(e)
      })
    },
    
    removeAllFences() {
      Perimeter.removeAllFences()
    },

    async handlePermissions() {
      this.permStatus = await Perimeter.checkPermissions()
      this.logPerms(this.permStatus)
    },

    async configureMap() {
      this.lastLocat = await Geolocation.getCurrentPosition()
      this.mapCenter = this.presentLocat
      this.shouldShowUserMarker = true
      this.mapZoom = 12.5
    },

    async getNominatimServerStatus() { 
      fetch(this.NOMINATIM_API.STATUS).then((r) => {
        if(!(r.status === 200)) {
          this.APP_STATE = NamedStates.NOMINATIM_UNAVAILABLE
        }
        else {
          this.handlePermissions()
        }
      })
    }
  },

  async created() {
    await SplashScreen.hide()
  },
  
  async mounted() {

    Perimeter.addListener("FenceEvent", (data: PerimeterEvent) => { 
      console.log((data as FenceEvent).fences[0].name)
    })

    this.getNominatimServerStatus()
  }
})
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
#fence-map { 
  width: 100%;
  height: max(55vh, 500px);
}

h1, h2, h3, h4, h5 {
  margin: 0;
}

</style>
