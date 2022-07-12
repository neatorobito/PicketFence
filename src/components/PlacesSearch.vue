<template>
    <input type="text" v-model="placeQuery" placeholder="Find an address" />
    <table>
      <tbody style="">
        <tr v-for="place in possiblePlaces">
          <td>{{ place.label }}</td>
        </tr>
      </tbody>
    </table>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { OpenStreetMapProvider } from 'leaflet-geosearch';

export default defineComponent({
  name: 'PlacesSearch',
  components: {
  },
  data: () => {
    return {
      placeQuery: "",
      possiblePlaces: new Array(),
      placesProvider: new OpenStreetMapProvider()
    }
  },

  watch: { 
    async 'placeQuery' (newQuery) {
      this.possiblePlaces = await this.placesProvider.search({ query: newQuery })
    }
  },

  methods: {
  },
  
  async mounted() {
  }
})
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
