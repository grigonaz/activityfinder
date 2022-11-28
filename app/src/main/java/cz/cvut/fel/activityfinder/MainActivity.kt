package cz.cvut.fel.activityfinder

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import cz.cvut.fel.activityfinder.model.Activity
import cz.cvut.fel.activityfinder.model.ActivityManager
import cz.cvut.fel.activityfinder.model.DateTimeTools
import kotlinx.android.synthetic.main.dialog_activity_popup.view.*
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private lateinit var dialogBuilder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()

        val mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        this.enableMyLocation()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
            ActivityManager.getInstance().location = location
        }
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {

        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }


        // 2. If if a permission rationale dialog should be shown
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ||
            ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), 1)
            return
        }

        // 3. Otherwise, request permission
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ), 1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navigationBottom)
        val navController = findNavController(R.id.fragmentContainerView)

        bottomNavigationView.setupWithNavController(navController)
    }

    fun openDetailPopup(act: Activity) {
        this.dialogBuilder = AlertDialog.Builder(this)
        val popup = layoutInflater.inflate(R.layout.dialog_activity_popup, null, false)
        popup.name.text = act.name
        val from = OffsetDateTime.ofInstant(act.dateFrom?.toInstant(), ZoneId.systemDefault())
        val to = OffsetDateTime.ofInstant(act.dateTo?.toInstant(), ZoneId.systemDefault())
        val date = DateTimeTools.fmt.format(from) + " - " + DateTimeTools.fmt.format(to)
        popup.date.text = "Datum a čas: $date"
        val distance = "${((act.distance/100.0)).roundToInt()/10.0}"
        popup.distance.text = "${getString(R.string.distance)}: $distance km"
        popup.type.text = "${getString(R.string.type)}: ${act.type}"
        popup.signed_participants.text = "${getString(R.string.activity_detail_number_of_particiants_label)}: ${act.participated?.size?:0}"
        popup.max_participants.text = "${getString(R.string.activity_detail_maximum_of_particiants_label)}: ${act.maximumCapacity?:0}"
        popup.author.text = "${getString(R.string.author)}: ${act.authorName}"
        popup.details.text = "${getString(R.string.description)}: ${act.description}"
        popup.date.text = "${getString(R.string.date_and_time)}: $date"

        val participate = act.participated!!.contains(FirebaseAuth.getInstance().currentUser!!.uid)

        popup.participate.text = if(participate) getString(R.string.sign_out_label) else getString(R.string.participate_label)


        dialogBuilder.setView(popup)
        dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_design)
        popup.participate.setOnClickListener {
            ActivityManager.getInstance().participateActivity(act, !participate)
            if(ActivityManager.getInstance().result.value!= null) {
                if(ActivityManager.getInstance().result.value!!.message == getString(R.string.own_activity_label)) {
                    Toast.makeText(this, getString(R.string.activity_detail_warn_sign_out_of_own_activity), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "${getString(R.string.error_occurred)}: ${ActivityManager.getInstance().result.value!!.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, if(participate) getString(R.string.sign_out_info) else getString(R.string.sign_in_info), Toast.LENGTH_LONG).show()
            }
            dialog.dismiss()
        }

        popup.open_detail.setOnClickListener {
            this.openDetailActivity(act.id!!)
        }

        dialog.show()
    }

    fun openNewActivityPopup() {
        val intent = Intent(this, NewActivityActivity::class.java)
        startActivity(intent)
    }

    fun openDetailActivity(id: String) {
        val intent = Intent(this, ActivityDetailActivity::class.java)
        intent.putExtra("ACTIVITY_ID", id)
        startActivity(intent)
    }

    fun openDeleteActivityPopup(act: Activity) {
        val dialog = AlertDialog.Builder(this).setTitle(getString(R.string.remove_own_activity)).setMessage(getString(R.string.remove_own_activity_warning) + act.name)
            .setPositiveButton(getString(R.string.remove)) { _, _ ->
                ActivityManager.getInstance().deleteActivity(act)
                if(ActivityManager.getInstance().result.value!= null) {
                    if(ActivityManager.getInstance().result.value!!.message.equals("you are not author")) {
                        Toast.makeText(this, getString(R.string.not_author_info), Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "${getString(R.string.error_occurred)}: ${ActivityManager.getInstance().result.value!!.message}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.removed_info), Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton(getString(R.string.back_label), null)
        dialog.show()
    }
}
