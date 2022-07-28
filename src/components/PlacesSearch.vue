<template>
    <div>
      <input type="text" v-model="placeQuery" @keypress="handleKeyPress($event)" @change="" placeholder="🔍 Find an address" />
      <div id="places-result-container" v-if="possiblePlaces.length > 0">
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
      placesProvider: new OpenStreetMapProvider(),
      placesInputTimeout: null as any,
      STANDARD_INPUT_TIMEOUT_MS: 750
    }
  },

  methods: {
  
  	async handlePlaceClick(place: any) {
  		const simplified = {} as BasicPlace
  		simplified.name = place.label
  		simplified.id = place.raw.osm_id
  		simplified.lat = place.raw.lat
  		simplified.lng = place.raw.lon
  		
  		this.placeQuery = simplified.name
  		this.possiblePlaces = new Array()
      this.$emit('placeClicked', simplified )
   	},

    async searchForPlace() {
      this.possiblePlaces = new Array() 
      this.possiblePlaces = await this.placesProvider.search({ query: this.placeQuery })
    },

    handleKeyPress(e : KeyboardEvent) {

      if(this.placeQuery !== "") {
        if(this.placesInputTimeout !== null) {
          clearTimeout(this.placesInputTimeout)
        }

          let timeout = e.key === "Enter" ? 0 : this.STANDARD_INPUT_TIMEOUT_MS
          this.placesInputTimeout = setTimeout(this.searchForPlace, timeout)
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
