package cz.cvut.fel.activityfinder.model

import com.google.firebase.database.Exclude
import java.util.*

data class Activity(
    @get:Exclude
    var id: String?=null,
    var type: String?=null,
    var name: String?=null,
    var description: String?=null,
    var dateFrom: Date?=null,
    var dateTo: Date?=null,
    var maximumCapacity: Int?=null,
    var position: LatitudeLongitude?=null,
    var authorName: String?=null,
    var authorId: String?=null,
    var participated: ArrayList<String>?=null
    ) {
    var distance: Float = 0.0F
}
