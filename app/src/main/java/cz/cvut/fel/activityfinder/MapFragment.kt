package cz.cvut.fel.activityfinder

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import cz.cvut.fel.activityfinder.model.ActivityManager


class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener {

    private var map: GoogleMap? = null
    private var mapView: MapView? = null
    private val markers = HashMap<String, String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_map, container, false)
        initMap(view, savedInstanceState)
        return view
    }

    private fun initMap(view: View?, savedInstanceState: Bundle?) {
        mapView = view?.findViewById(R.id.map_view)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(g_map: GoogleMap) {
        map = g_map
        this.enableMyLocation()
        //map?.isMyLocationEnabled = true
        map?.uiSettings?.isCompassEnabled = true
        map?.uiSettings?.isMyLocationButtonEnabled = true
        map?.uiSettings?.isMapToolbarEnabled = true
        map?.setOnMyLocationButtonClickListener(this)
        map?.setOnMyLocationClickListener(this)
        map?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(50.1, 14.3),
                7F
            )
        )
        map?.mapType = GoogleMap.MAP_TYPE_NORMAL
        ActivityManager.getInstance().activities.observe(viewLifecycleOwner) {
            it.forEach{point ->
                val marker = map!!.addMarker(MarkerOptions().title(point.name).position(point.position?.toLatLng() ?: LatLng(50.0, 14.0)))
                this.markers[marker!!.id] = point.id!!
            }
        }
        map?.setOnInfoWindowClickListener {
            (this.context as MainActivity).openDetailActivity(this.markers[it.id]!!)
        }
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {

        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(requireContext(), ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            map?.isMyLocationEnabled = true
            return
        }


        // 2. If if a permission rationale dialog should be shown
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), ACCESS_FINE_LOCATION) ||
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), ACCESS_COARSE_LOCATION))
        {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION), 1)
            return
        }

        // 3. Otherwise, request permission
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION), 1)
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(activity, "${getString(R.string.current_location_label)}:\n$location", Toast.LENGTH_LONG)
            .show()
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(activity, getString(R.string.location_button_clicked_label), Toast.LENGTH_SHORT)
            .show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
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