import { createApp } from 'vue'
import App from './App.vue'
import VueMapkit from 'vue-mapkit'

const MAPKIT_TOKEN = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IkpMVE0zTTIyQ0oifQ.eyJpc3MiOiJENTRGOUoyOTNTIiwiaWF0IjoxNjUyMTI3ODkwLCJleHAiOjE2NzI2MTc2MDB9.0R6_G3uGOUtC_X4KUi7apj4nxLdFMjpFgFpgznOm8r_175h8FDlwAwrrt90E_kEeq0_He98gnFMxbxHcqR5mqw";
const picketfence = createApp(App)

picketfence.use(VueMapkit, {
    authorizationCallback(done) {
        // provide mapkit jwt here
        done(MAPKIT_TOKEN)
    },
    // you can use options from mapkit documentation as well
    language: 'en',
})

picketfence.mount('#app')
