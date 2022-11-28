package cz.cvut.fel.activityfinder.model

import android.content.ContentValues.TAG
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

class ActivityManager: ViewModel() {

    companion object {
        private var init = ActivityManager()

        fun getInstance(): ActivityManager {
            return init
        }
    }

    private val activitiesDB = FirebaseDatabase.getInstance().getReference("activities")

    private val _result = MutableLiveData<Exception?>()
    val result: LiveData<Exception?> get() = _result

    private val _activities = MutableLiveData<ArrayList<Activity>>()
    val activities: LiveData<ArrayList<Activity>> get() = _activities

    var location: Location? = null
    var minDistance: Float = 1000000.0F
    var maxDistance: Float = 0.0F


    fun addActivity(a: Activity) {
        a.id = activitiesDB.push().key
        a.participated = ArrayList()
        a.participated!!.add(FirebaseAuth.getInstance().currentUser!!.uid)
        a.authorId = FirebaseAuth.getInstance().currentUser!!.uid
        a.authorName = FirebaseAuth.getInstance().currentUser!!.displayName

        activitiesDB.child(a.id!!).setValue(a).addOnCompleteListener {
            if(it.isSuccessful) {
                _result.value = null
            } else {
                _result.value = it.exception
            }
        }
    }

    fun updateActivity(a: Activity) {
        this._result.value = null
        activitiesDB.child(a.id!!).setValue(a).addOnCompleteListener{
            if(it.isSuccessful) {
                this._result.value = null
            } else {
                this._result.value = it.exception
            }
        }
    }

    fun participateActivity(a: Activity, value: Boolean) {
        this._result.value = null
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        if(value) {
            if(!a.participated!!.contains(uid)) {
                a.participated!!.add(uid)
                this.updateActivity(a)
            }
        } else {
            if(a.authorId == uid) {
                this._result.value = Exception("own activity")
            } else {
                if(a.participated!!.contains(uid)) {
                    a.participated!!.remove(uid)
                    this.updateActivity(a)
                }
            }
        }
    }

    fun deleteActivity(a: Activity) {
        this._result.value = null
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        if(a.id != null) {
            if(a.authorId == uid) {
                this.activitiesDB.child(a.id!!).removeValue().addOnCompleteListener {
                    if(it.isSuccessful) {
                        this._result.value = null
                    } else {
                        this._result.value = it.exception
                    }
                }
            } else {
                this._result.value = Exception("you are not author")
            }
        } else {
            this._result.value = Exception("null id")
        }
    }

    fun getActivity(id: String): Task<DataSnapshot> {
        return this.activitiesDB.child(id).get()
    }

    fun getRealtimeUpdate() {
        activitiesDB.addValueEventListener(valueDataListener)
    }

    override fun onCleared() {
        super.onCleared()
        activitiesDB.removeEventListener(valueDataListener)
    }

    private val valueDataListener = object: ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val list = ArrayList<Activity>()
            for(item in snapshot.children) {
                val activity = item.getValue(Activity::class.java)
                if(activity!!.dateTo?.after(Date()) == true) {
                    activity.id = item.key
                    list.add(activity)
                }
                if(location != null) {
                    val res = FloatArray(1)
                    Location.distanceBetween(location!!.latitude, location!!.longitude,
                        activity.position?.lat?:0.0, activity.position?.lng?:0.0, res)
                    activity.distance = res[0]
                    if(res[0]/1000.0 > maxDistance) maxDistance = res[0]/1000
                    if(res[0]/1000.0 < minDistance) minDistance = res[0]/1000
                }
            }
            _activities.value = list
            if(maxDistance > 100.0F) maxDistance = 100.0F
            if(minDistance < 0) minDistance = 0.0F
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w(TAG, "loadPost:onCancelled", error.toException())
        }

    }

}

