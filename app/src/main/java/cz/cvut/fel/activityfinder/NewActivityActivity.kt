package cz.cvut.fel.activityfinder

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import cz.cvut.fel.activityfinder.model.Activity
import cz.cvut.fel.activityfinder.model.ActivityManager
import cz.cvut.fel.activityfinder.model.LatitudeLongitude
import kotlinx.android.synthetic.main.activity_new_activity.*
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*


class NewActivityActivity : AppCompatActivity() {

    private var position: LatLng = LatLng(50.0755, 14.4378)
    private var dateTimeFrom = OffsetDateTime.now()
    private var dateTimeTo = OffsetDateTime.now().plusHours(2)
    private var dateTimeSet: Boolean = false

    private var markerCenter: Marker? = null
    private lateinit var viewModel: ActivityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_activity)
        this.viewModel = ViewModelProviders.of(this).get(ActivityManager::class.java)
    }

    override fun onStart() {
        super.onStart()
        // Spinner initialize
        val spinner: Spinner = new_activity_type_choose
        ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item)
        ArrayAdapter.createFromResource(
            this,
            R.array.types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
        this.initMap()
        // Buttons initialize
        new_activity_back.setOnClickListener{
            finish()
        }
        participate.setOnClickListener {
            var noError = true
            if(new_activity_name.text.length <= 2) {
                new_activity_name.error = getString(R.string.validation_error_new_activity_name)
                noError = false
            }
            if(new_activity_position.text != getString(R.string.validation_info_position_selected)) {
                new_activity_position.error = getString(R.string.validation_error_position_not_selected)
                noError = false
            } else {
                new_activity_position.error = null
            }
            if(!dateTimeSet) {
                new_activity_date_from.error = getString(R.string.validation_error_must_specify_time)
                noError = false
            } else {
                new_activity_date_from.error = null
            }
            if(this.dateTimeFrom.isAfter(this.dateTimeTo)) {
                new_activity_date_to.error = "Musí být později než první datum"
                noError = false
            } else {
                new_activity_date_to.error = null
            }
            if(!this.dateTimeTo.isAfter(OffsetDateTime.now())) {
                if( new_activity_date_to.error != null) {
                    new_activity_date_to.error = "${new_activity_date_to.error}, a musí být později, než je aktuální okamžik"
                } else {
                    new_activity_date_to.error = "Datum musí být později, než je aktuální okamžik"
                }
            }
            if(new_activity_max_count.text.isEmpty()) {
                new_activity_max_count.error = getString(R.string.validation_error_dont_be_empty)
                noError = false
            }
            if(noError) {
                val activity = Activity(null, spinner.selectedItem.toString(), new_activity_name.text.toString(), new_activity_description.text.toString(),
                    convertDate(this.dateTimeFrom), convertDate(this.dateTimeTo), Integer.parseInt(new_activity_max_count.text.toString()),
                LatitudeLongitude(this.position.latitude, this.position.longitude), "Autor", FirebaseAuth.getInstance().currentUser?.uid)
                this.viewModel.addActivity(activity)
                if(this.viewModel.result.value == null) {
                    Toast.makeText(this, getString(R.string.activity_create_info), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "${getString(R.string.error)} ${this.viewModel.result.value}", Toast.LENGTH_LONG).show()
                }
                finish()
            }
        }
        // two datetimes set
        new_activity_date_from.setOnClickListener{
            DatePickerDialog(this, { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                TimePickerDialog(this, { _: TimePicker, hour: Int, minute: Int ->
                    this.dateTimeFrom = OffsetDateTime.of(year, month, dayOfMonth, hour, minute, 0, 0, ZoneOffset.UTC)
                    this.updateDateTimeRangeText()
                }, dateTimeFrom.hour, dateTimeFrom.minute, true).show()
            }, dateTimeFrom.year, dateTimeFrom.monthValue, dateTimeFrom.dayOfMonth ).show()
        }
        new_activity_date_to.setOnClickListener{
            DatePickerDialog(this, { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                TimePickerDialog(this, { _: TimePicker, hour: Int, minute: Int ->
                    this.dateTimeTo = OffsetDateTime.of(year, month, dayOfMonth, hour, minute, 0, 0, ZoneOffset.UTC)
                    this.updateDateTimeRangeText()
                }, dateTimeTo.hour, dateTimeTo.minute, true).show()
            }, dateTimeTo.year, dateTimeTo.monthValue, dateTimeTo.dayOfMonth ).show()
        }

    }

    fun convertDate(d: OffsetDateTime): Date {
        val epochMilli: Long = d.toInstant().toEpochMilli()
        return Date(epochMilli)
    }

    private fun updateDateTimeRangeText() {
        val fmt: DateTimeFormatter = DateTimeFormatter.ofPattern("d. M. y HH:mm")
        this.dateTimeSet = true
        val infoText = fmt.format(this.dateTimeFrom) + " - " + fmt.format(this.dateTimeTo)
        new_activity_date_range_info.text = infoText
    }

    private fun initMap() {
        new_activity_position.setOnClickListener {
            // create dialog for map position
            val dialog = Dialog(this, R.style.DialogStyle)
            dialog.setContentView(R.layout.dialog_map_select_position)

            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_design)

            dialog.findViewById<Button>(R.id.position_select_storno).setOnClickListener {
                dialog.onBackPressed()
            }
            dialog.findViewById<Button>(R.id.position_select_accept).also { button ->
                button.setOnClickListener{
                    dialog.dismiss()
                    new_activity_position.text = getString(R.string.validation_info_position_selected)
                }
            }

            dialog.show()

            dialog.findViewById<MapView>(R.id.position_select_map).also {
                MapsInitializer.initialize(this)
                it.onCreate(dialog.onSaveInstanceState())
                it.onResume()

                it.getMapAsync { map ->
                    map.uiSettings.isCompassEnabled = true
                    map.uiSettings.isMapToolbarEnabled = true
                    map.uiSettings.isZoomControlsEnabled = true
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            this.position,
                            9F
                        )
                    )
                    val markerOptions = MarkerOptions()
                    markerOptions.position(map.cameraPosition.target)
                    markerCenter = map.addMarker(markerOptions)

                    map.mapType = GoogleMap.MAP_TYPE_NORMAL
                    map.setOnCameraMoveListener {
                        markerCenter!!.position = map.cameraPosition.target
                        this.position = map.cameraPosition.target
                    }
                }
            }
        }
    }
}