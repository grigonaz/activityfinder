package cz.cvut.fel.activityfinder

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import cz.cvut.fel.activityfinder.model.Activity
import cz.cvut.fel.activityfinder.model.ActivityManager
import cz.cvut.fel.activityfinder.model.DateTimeTools
import kotlinx.android.synthetic.main.activity_detail_activity.*
import kotlinx.android.synthetic.main.dialog_activity_popup.view.*
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlin.math.roundToInt


class ActivityDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private var activity: Activity? = null

    private var map: GoogleMap? = null
    private var mapView: MapView? = null
    private var savedInstanceState: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityId = intent.getStringExtra("ACTIVITY_ID")
        val url: Uri? = intent?.data
        //Toast.makeText(this, url?.encodedPath?:"", Toast.LENGTH_SHORT).show()
        if(activityId == null) {
            Toast.makeText(this, getString(R.string.activity_detail_not_exist), Toast.LENGTH_SHORT).show()
            this.finish()
        }
        this.loadActivity(activityId!!)
        setContentView(R.layout.activity_detail_activity)
        this.mapView = this.findViewById(R.id.activity_detail_map_location)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
        activity_detail_back_button.setOnClickListener{
            this.finish()
        }
    }

    override fun onMapReady(g_map: GoogleMap) {
        map = g_map
        map?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(50.1, 14.3),
                4F
            )
        )
        map?.mapType = GoogleMap.MAP_TYPE_NORMAL
    }


    private fun loadActivity(id: String) {
        ActivityManager.getInstance().getActivity(id).addOnCompleteListener{
            if(it.isSuccessful) {
                this.activity = it.result.getValue(Activity::class.java)
                activity!!.id = it.result.key
                this.fillData()
            } else {
                Toast.makeText(this, getString(R.string.activity_detail_not_exist), Toast.LENGTH_SHORT).show()
                this.finish()
            }
        }
    }

    private fun fillData() {
        if(this.activity == null) return
        val participate = this.activity!!.participated!!.contains(FirebaseAuth.getInstance().currentUser!!.uid)
        activity_detail_name.text = this.activity!!.name?:getString(R.string.activity_detail_name_not_found)
        val from = OffsetDateTime.ofInstant(this.activity!!.dateFrom?.toInstant(), ZoneId.systemDefault())
        val to = OffsetDateTime.ofInstant(this.activity!!.dateTo?.toInstant(), ZoneId.systemDefault())
        val date = DateTimeTools.fmt.format(from) + " - " + DateTimeTools.fmt.format(to)
        activity_detail_datetime.text = "${getString(R.string.date)}: $date"
        if(ActivityManager.getInstance().location != null) {
            val location = ActivityManager.getInstance().location
            var res = FloatArray(1)
            Location.distanceBetween(location!!.latitude, location!!.longitude,
                activity!!.position?.lat?:0.0, activity!!.position?.lng?:0.0, res)
            activity!!.distance = res[0]
        }
        val distance = "Vzdálenost ${((this.activity!!.distance/100.0)).roundToInt()/10.0} km"
        activity_detail_distance.text = distance
        activity_detail_type.text = "${getString(R.string.type)}: ${this.activity!!.type}"
        activity_detail_participans.text = "${getString(R.string.activity_detail_number_of_particiants_label)}: ${this.activity!!.participated?.size?:0}"
        activity_detail_maximum_participans.text = "${getString(R.string.activity_detail_maximum_of_particiants_label)}: ${this.activity!!.maximumCapacity?:0}"
        activity_detail_author.text = "${getString(R.string.author)}: ${this.activity!!.authorName}"
        activity_detail_description.text = "${getString(R.string.description)}: ${this.activity!!.description?.take(200)?:"${getString(R.string.activity_detail_no_description_label)}"}"
        activity_detail_participate_button.text = if(participate) getString(R.string.sign_out_label) else getString(
            R.string.participate_label)
        activity_detail_participate_button.setOnClickListener {
            ActivityManager.getInstance().participateActivity(this.activity!!, !this.activity!!.participated!!.contains(FirebaseAuth.getInstance().currentUser!!.uid))
            if(ActivityManager.getInstance().result.value!= null) {
                if(ActivityManager.getInstance().result.value!!.message == getString(R.string.own_activity_label)) {
                    Toast.makeText(this, getString(R.string.activity_detail_warn_sign_out_of_own_activity), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "${getString(R.string.error_occurred)}: ${ActivityManager.getInstance().result.value!!.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, if(participate) getString(R.string.sign_out_info) else getString(
                                    R.string.sign_in_info), Toast.LENGTH_LONG).show()
            }
            this.loadActivity(this.activity!!.id!!)
        }
        activity_detail_link.setOnClickListener {
            val clipboard: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Activity url", getString(R.string.app_url_path)+this.activity!!.id)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, getString(R.string.copy_buffer_info), Toast.LENGTH_LONG).show()
        }
        // set
        this.mapView?.getMapAsync {
            map!!.clear()
            val mark = map!!.addMarker(MarkerOptions().title(this.activity!!.name).position(this.activity!!.position?.toLatLng() ?: LatLng(50.0, 14.0)))
            map?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    mark!!.position,
                    10F
                )
            )
        }
    }

    override fun onResume() {
        mapView?.onResume()
        super.onResume()
    }

    override fun onPause() {
        mapView?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView?.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        mapView?.onLowMemory()
        super.onLowMemory()
    }
}