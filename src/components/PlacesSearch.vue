<template>
    <input type="text" v-model.lazy="placeQuery" @change="onEdited" placeholder="Find an address" />
    <div id="places-result-container" v-if="possiblePlaces.length > 0">
	  <p class="place" v-for="place in possiblePlaces" @click="onPlaceClicked(place)">{{ place.label }}</p>
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
      placesInputWatcher: null as any
    }
  },

  methods: {
  
  	async onPlaceClicked(place: any) {
  		const simplified = {} as BasicPlace
  		simplified.name = place.label
  		simplified.id = place.raw.osm_id
  		simplified.lat = place.x
  		simplified.lng = place.y
  		
  		//this.placesInputWatcher() // Stop watching while change the query.
  		this.placeQuery = simplified.name
  		this.possiblePlaces = new Array()
  		console.log(this.placesInputWatcher)
  		//this.placesInputWatcher() // Resume watching
   	},
   	
   	onEdited() {
   		console.log("Edits were made")
   	}
  
  },
  
  async created() {
  
  	this.placesInputWatcher = this.$watch('placeQuery', async (newQuery) => {
  		this.possiblePlaces = await this.placesProvider.search({ query: newQuery })
  		console.log("awaiting")
  	})
  	
  },
  
  async mounted() {
  }
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
