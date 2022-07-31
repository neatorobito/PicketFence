<template>
  <div class="container flexbox col height-100 no-padding">
    <div class="container no-padding" style="flex: 1 1 60%;">
      <l-map id="fence-map" ref="fenceMap" @ready="handleMapReady()" :zoom="mapZoom" :zoomAnimation=true :options="{zoomControl: false}">
        <l-tile-layer :url="TILE_LAYER" :attribution="MAPS_ATTRIBUTION"></l-tile-layer>
        <l-marker :lat-lng="markerLocat" :visible="markerLocat !== null"></l-marker>
        <l-control-zoom position="bottomleft"></l-control-zoom>
      </l-map>
      <div class="container absolute" id="search-container">
        <places-search @placeClicked="handleSelectedPlace($event)" v-if="isPlacesServerReachable"></places-search>
      </div>
    </div>
    <div class="container flexbox col padding-s" style="flex: 1 1 40%;">
        <h1>PicketFence</h1>
        <p v-if="instructionsText"> {{ instructionsText }} </p>
        <button class="btn button-primary" v-if="actionButtonText" @click="actionForButton">{{ actionButtonText }}</button>
    </div>
  </div>

</template>

<script lang="ts">
import { defineComponent, toRaw } from 'vue'
import { Perimeter, Fence, FenceEvent, PerimeterEvent, TransitionType, LocationPermissionStatus } from '@meld/perimeter'
import { Geolocation, Position } from '@capacitor/geolocation'
import { SplashScreen } from '@capacitor/splash-screen'
import { StateData, StateDataResolver, NamedStates, BasicPlace } from '../StateConstructs';
import { Device, DeviceInfo } from '@capacitor/device';

import "leaflet/dist/leaflet.css"
import { LMap, LTileLayer, LMarker, LControlZoom } from "@vue-leaflet/vue-leaflet"
import PlacesSearch from './PlacesSearch.vue';
import L from 'leaflet';

export default defineComponent({
  name: 'FenceDemo',
  components: {
    LMap,
    LTileLayer,
    LMarker,
    LControlZoom,
    PlacesSearch
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
      lastUserLocat: null as Position | null,
      markerLocat: null as number[] | null,
      selectedPlace: null as BasicPlace | null,
      mapZoom: 1,
      mapObj: null as any | null,
      activeFences: Array<Fence>(),
      permStatus: new LocationPermissionStatus(),
      deviceInfo: null as DeviceInfo | null
    }
  },
  watch: {
    'permStatus' (_updatedPermissions) {
      if(this.hasCorrectPermissions)
      {
        this.setMapToUser()
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

      let newStateData = StateDataResolver[newState] as StateData

      this.actionButtonText = newStateData.actionButtonText
      this.instructionsText = newStateData.instructionsText

      switch (newState) {
        case NamedStates.NEEDS_PERMISSIONS: {
          this.actionForButton = this.requestPerms
          break
        }
        case NamedStates.NOMINATIM_UNAVAILABLE: {
          this.actionForButton = this.getNominatimServerStatus
          break
        }
        case NamedStates.READY_FOR_FENCE: {
          this.actionForButton = this.addNewFence
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

    isPlacesServerReachable() {
      return this.APP_STATE !== NamedStates.NOMINATIM_UNAVAILABLE
    },

    presentUserLocat() {
      return this.$data.lastUserLocat === null ? [0,0] : [this.$data.lastUserLocat.coords.latitude, this.$data.lastUserLocat.coords.longitude]
    }

  },
  methods: {
    async logPerms(perms: LocationPermissionStatus) : Promise<void> {
      console.log(`Foreground result: ${perms.foreground}`)
      console.log(`Background result: ${perms.background}`)
    },

    handleSelectedPlace(place : BasicPlace) { 

      // Convert this from a Proxy
      this.selectedPlace = toRaw(place)

      if(this.APP_STATE === NamedStates.READY_FOR_FENCE) {
        this.actionButtonText = "Add Fence"
        this.instructionsText = "Click the button to begin monitoring for this address." 
      }

      this.markerLocat = [ this.selectedPlace.lat, this.selectedPlace.lng ]
      this.mapObj.setView(this.markerLocat, 13)
    }, 

    handleMapReady() {
      this.mapObj = toRaw((this.$refs.fenceMap as any).leafletObject) // Look at this abomination.
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
      
      if(this.selectedPlace === null) {
        // Set error state.
        return
      }

      let extraData = "Here is some extra data."

      let newFence : Fence = {
        name : "Place",
        uid : this.selectedPlace.id.toString(),
        payload: extraData,
        lat : this.selectedPlace.lat,
        lng : this.selectedPlace.lng,
        radius : 200,
        monitor : TransitionType.Enter
      }

      Perimeter.addFence(newFence).then(() => {
        this.activeFences.push(newFence)
      })
      .catch((e) => {
        console.log(e)
        // Go to error state.
      }).finally(() => {
        
        let newStateData = StateDataResolver[NamedStates.READY_FOR_FENCE] as StateData

        this.actionButtonText = ""
        this.instructionsText = newStateData.instructionsText
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
    
    async setMapToUser() {
      this.lastUserLocat = await Geolocation.getCurrentPosition()
      this.mapObj.setView(this.presentUserLocat, 11)
    },

    async getNominatimServerStatus() { 
      let isServerAvailable = false
      try {
        let serverResponse = await fetch(this.NOMINATIM_API.STATUS)
        if(serverResponse.ok) { isServerAvailable = true }
      }
      finally {
        return isServerAvailable
      }
    }
  },

  async created() {
    await SplashScreen.hide()
  },
  
  async mounted() {
    this.deviceInfo = await Device.getInfo()
    let isServerAvailable = await this.getNominatimServerStatus()

    if(this.deviceInfo?.platform !== "web") {

      Perimeter.addListener("FenceEvent", (data: PerimeterEvent) => { 
        console.log((data as FenceEvent).fences[0].name)
      })

      if(isServerAvailable) {
        this.handlePermissions()
      }
      else {
        this.APP_STATE = NamedStates.NOMINATIM_UNAVAILABLE
      }

    }
  }
})
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

#fence-map { 
  width: 100%;
  z-index: 0;
}

#search-container {
  max-height: 30vh;
  height: initial;
  z-index: 1;
  top: 0;
  margin-top: 3rem;
}

</style>
