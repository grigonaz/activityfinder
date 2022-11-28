package cz.cvut.fel.activityfinder.model

import com.google.android.gms.maps.model.LatLng

class LatitudeLongitude {
    var lat: Double = 0.0
    var lng: Double = 0.0

    fun toLatLng(): LatLng {
        return LatLng(lat, lng)
    }

    // must stay there
    constructor()

    constructor(lat: Double, lng: Double) {
        this.lat = lat
        this.lng = lng
    }
}
