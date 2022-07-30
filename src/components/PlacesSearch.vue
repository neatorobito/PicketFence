<template>
    <div>
      <input id="search-input" type="text" @focusin="handleSearchGainFocus" v-model="placeQuery" @keypress="handleSearchKeyPress($event)" @change="" placeholder="🔍 Find an address" />
      <div id="places-result-container" v-if="areResultsVisible">
        <p class="place" v-for="place in possiblePlaces" @click="handlePlaceClick(place)">{{ place.label }}</p>
      </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { OpenStreetMapProvider } from 'leaflet-geosearch';
import { BasicPlace } from '../StateConstructs';

export default defineComponent({
  name: 'PlacesSearch',
  components: {
  },
  data: () => {
    return {
      placeQuery: "",
      possiblePlaces: new Array(),
      areResultsVisible: false,
      placesProvider: new OpenStreetMapProvider(),
      placesInputTimeout: null as any,
      STANDARD_INPUT_TIMEOUT_MS: 350
    }
  },

  methods: {
  
  	async handlePlaceClick(place: any) {
  		const simplified = {} as BasicPlace
  		simplified.name = place.label
  		simplified.id = place.raw.osm_id
  		simplified.lat = parseFloat(place.raw.lat)
  		simplified.lng = parseFloat(place.raw.lon)
  		
  		this.placeQuery = simplified.name
      this.$emit('placeClicked', simplified)
      this.areResultsVisible = false
   	},

    async searchForPlace() {
      this.possiblePlaces = new Array() 
      let places = await this.placesProvider.search({ query: this.placeQuery })

      // Remove any duplicates at the top from OSM.
      if(places[0].label === places[1].label) {
        places.shift()
      }

      this.possiblePlaces = places

      if(document.activeElement?.id === "search-input") {
        this.areResultsVisible = true
      }
    },

    handleSearchKeyPress(e : KeyboardEvent) {

      if(this.placeQuery !== "") {
        if(this.placesInputTimeout !== null) {
          clearTimeout(this.placesInputTimeout)
        }
          let timeout = e.key === "Enter" ? 0 : this.STANDARD_INPUT_TIMEOUT_MS
          this.placesInputTimeout = setTimeout(this.searchForPlace, timeout)
        }
    },

    handleSearchGainFocus() {
      if(this.possiblePlaces.length > 0 && this.placeQuery !== "") {
        this.areResultsVisible = true
      }
    }

  },
})
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

#places-result-container {
	border-radius: 0.25em;
	max-height: 25vh;
	background-color: white;
	overflow-y: scroll;
}

.place {
	border-bottom: 2px solid #D3D3D3;
	padding: 0.75em;
	margin: 0;
}

.place:hover {
	background-color: #D3D3D3;
}

.place:last-child {
	border-bottom: none;
	padding: 1em;
}

</style>
