<template>
  <div class="container flexbox col height-100 no-padding">
    <div class="container no-padding" style="flex: 1 1 60%;">
      <l-map id="fence-map" ref="fenceMap" @ready="handleMapReady()" :zoom="mapZoom" :zoomAnimation=true :options="{zoomControl: false}">
        <l-tile-layer :url="TILE_LAYER" :attribution="MAPS_ATTRIBUTION"></l-tile-layer>
        <template v-for="fence in activeFences">
          <l-circle color="green" :latLng="[ fence.lat, fence.lng ]" :radius="fence.radius"></l-circle>
        </template>
        <l-marker :lat-lng="markerLocat" :visible="markerLocat !== null"></l-marker>
        <l-control-zoom position="bottomleft"></l-control-zoom>
      </l-map>
      <div class="container absolute" id="search-container">
        <places-search @placeClicked="handleSelectedPlace($event)" v-if="isPlacesServerReachable"></places-search>
      </div>
    </div>
          
    <div class="container flexbox col padding-s" style="flex: 1 1 34%; overflow-y: auto">
        <div class="margin-bottom-s">
          <h1 class="no-margin">{{ activeFences.length }} active {{ activeFences.length == 1 ? 'fence' : 'fences' }}</h1>
          <p class="no-margin" v-if="statusText">{{ statusText }}</p>
        </div>
        <button class="button margin-bottom-m" v-if="actionButtonText" @click="actionForButton">{{ actionButtonText }}</button>

        <div v-if="isReady" :class="`flexbox col ${activeFences.length === 0 ? 'border' : ''}`" :style="activeFences.length === 0 ? 'border-style: dashed;' : ''">
          <div v-if="activeFences.length === 0" class="flexbox col">
            <fence-card class="opacity-75"></fence-card>
          </div>
          <template v-else v-for="fence in activeFences">
            <fence-card :name="fence.name" :address="fence.payload" :radius="fence.radius" :uid="fence.uid" @deleteClicked="handleDeletedFence($event)"></fence-card>
          </template>
        </div>
    </div>
    <div class="flexbox col" style="flex: 0 0 6%;">
      <p class="centered text-align-center">{{ new Date().getFullYear() }} © Made by Mark | Icons by <a href="https://creativemarket.com/BomSymbols">BomSymbols</a></p>
    </div>
  </div>

</template>

<script lang="ts">
import { defineComponent, toRaw } from 'vue'
import { 
  Perimeter, 
  Fence,
  FenceEvent,
  LocationPermissionStatus,
  PlatformEvent,
  iOSPlatformEvents,
  TransitionTypes } from '@meld/perimeter'
import { Geolocation, Position } from '@capacitor/geolocation'
import { SplashScreen } from '@capacitor/splash-screen'
import { StateData, StateDataResolver, NamedStates, BasicPlace } from '../StateConstructs'
import { Device, DeviceInfo } from '@capacitor/device'
import { LocalNotifications } from '@capacitor/local-notifications'
import "leaflet/dist/leaflet.css"
import { LMap, LTileLayer, LMarker, LControlZoom, LCircle } from "@vue-leaflet/vue-leaflet"
import PlacesSearch from './PlacesSearch.vue'
import FenceCard from './FenceCard.vue'

export default defineComponent({
  name: 'FenceDemo',
  components: {
    LMap,
    LTileLayer,
    LMarker,
    LControlZoom,
    LCircle,
    PlacesSearch,
    FenceCard
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
      statusText: null as string | null,
      lastUserLocat: null as Position | null,
      lastPlatformError: null as any | null,
      markerLocat: null as number[] | null,
      selectedPlace: null as BasicPlace | null,
      mapZoom: 2,
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
        this.APP_STATE = NamedStates.IDLE
      }
      else
      {
        this.APP_STATE = NamedStates.NEED_PERMISSIONS
      }
    },

    'APP_STATE' (newState) {

      this.actionButtonText = null
      this.statusText = null

      let newStateData = StateDataResolver[newState] as StateData

      this.actionButtonText = newStateData.actionButtonText
      this.statusText = newStateData.statusText

      switch (newState) {
        case NamedStates.NEED_PERMISSIONS: {
          this.actionForButton = this.requestPerms
          break
        }
        case NamedStates.NOMINATIM_UNAVAILABLE: {
          this.actionForButton = this.getNominatimServerStatus
          break
        }
        case NamedStates.IDLE: {
          this.actionForButton = this.addNewFence
          break
        }
        case NamedStates.FENCE_SUCCESS: {
          this.actionForButton = () => { 
            this.selectedPlace = null 
            this.APP_STATE = NamedStates.IDLE
          }
          break
        }
        case NamedStates.PLATFORM_ERROR: {
          this.actionForButton = this.continueAfterError
          this.statusText = 'A platform error occured. ' + this.lastPlatformError.message
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
      return (this.$data.permStatus.background === 'granted' && this.$data.permStatus.foreground === 'granted')
    },

    isPlacesServerReachable() {
      return this.APP_STATE !== NamedStates.NOMINATIM_UNAVAILABLE
    },

    presentUserLocat() {
      return this.$data.lastUserLocat === null ? [0,0] : [this.$data.lastUserLocat.coords.latitude, this.$data.lastUserLocat.coords.longitude]
    },

    isReady() {
      return this.APP_STATE === NamedStates.IDLE
    },

    hasErrorOccured() {
      return this.APP_STATE === NamedStates.NOMINATIM_UNAVAILABLE || NamedStates.PLATFORM_ERROR
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

      if(this.APP_STATE === NamedStates.IDLE) {
        // This should probably be another state.
        this.actionButtonText = "Add Fence"
        this.statusText = "Click the button to begin monitoring for this address. A notification will be sent when this device enters within 200 meters of this address." 
      }

      this.markerLocat = [ this.selectedPlace.lat, this.selectedPlace.lng ]
      this.mapObj.setView(this.markerLocat, 13)
    },

    handleDeletedFence(uid : string) {
      this.removeOldFence(uid)
    },

    handleMapReady() {
      this.mapObj = toRaw((this.$refs.fenceMap as any).leafletObject) // Look at this abomination.
    },

    async requestPerms(e: MouseEvent) : Promise<void> {
      let notifPerms = await LocalNotifications.checkPermissions()
      this.permStatus = await Perimeter.checkPermissions()

      if(notifPerms.display !== 'granted') {
        await LocalNotifications.requestPermissions()
      }

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

      let newFence : Fence = {
        name : "Place " + (this.activeFences.length + 1),
        uid : this.selectedPlace.id.toString(),
        payload: this.selectedPlace.address, // This is actually the address from Open Street Maps.
        lat : this.selectedPlace.lat,
        lng : this.selectedPlace.lng,
        radius : 200,
        monitor : TransitionTypes.Enter
      }

      Perimeter.addFence(newFence).then(() => {
        this.activeFences.push(newFence)
        this.APP_STATE = NamedStates.FENCE_SUCCESS
      })
      .catch((e) => {
        this.lastPlatformError = e
        this.APP_STATE = NamedStates.PLATFORM_ERROR
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

    continueAfterError() {
      this.lastPlatformError = null
      this.APP_STATE = NamedStates.BLANK
      
      this.handlePermissions()
    },

    handleiOSReturnToForeground(event: PlatformEvent) {
      if(event.data !== null) {
        let activeFencesBeforePaused = event.data as Array<Fence>
        this.activeFences = activeFencesBeforePaused
      }
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

      Perimeter.addListener("FenceEvent", (event: any) => { 
        let fenceEvent = (event as FenceEvent)
        let fenceNames = ""
        for(let fence of fenceEvent.fences) {
          fenceNames += fence.name + ' '
        }

        console.log("Here is raw event")
        console.log(fenceEvent)

        LocalNotifications.schedule({ 
          notifications : [{ 
            id: 123,
            title: 'Geofencing Event',
            body : `Did you ${ fenceEvent.transitionType === TransitionTypes.Enter ? 'enter' : 'exit' } ${ fenceNames.trimEnd() }?`}]})
      })

      Perimeter.addListener("PlatformEvent", async (event: any) => {
        let platformEvent = (event as PlatformEvent)
        switch(platformEvent.code) {
          case iOSPlatformEvents.FOREGROUND_WITH_EXISTING_FENCES:
            this.handleiOSReturnToForeground(platformEvent)
            break
          default:
            break
        }

      })

      if(isServerAvailable) {
        this.handlePermissions()
        console.log(await Perimeter.getActiveFences())
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
