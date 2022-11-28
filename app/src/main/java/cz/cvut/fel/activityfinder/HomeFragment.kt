package cz.cvut.fel.activityfinder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Spinner
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.yahoo.mobile.client.android.util.rangeseekbar.RangeSeekBar
import cz.cvut.fel.activityfinder.model.ActivityManager
import kotlinx.android.synthetic.main.activity_new_activity.*
import kotlinx.android.synthetic.main.dialog_filter.*
import kotlinx.android.synthetic.main.dialog_filter.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*


class HomeFragment : Fragment() {

    private lateinit var recyclerAdapter: AllActivitiesRecyclerAdapter
    private lateinit var viewModel: ActivityManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        this.viewModel = ActivityManager.getInstance()
        this.recyclerAdapter = AllActivitiesRecyclerAdapter()
        viewModel.activities.observe(viewLifecycleOwner) {
            recyclerAdapter.addActivities(it)
        }
        viewModel.getRealtimeUpdate()
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        home_activity_list.adapter = recyclerAdapter
        home_add_activity_button.setOnClickListener{
            var ap = it.context
            if(ap.javaClass.simpleName == "MainActivity") {
                ap = ap as MainActivity
                ap.openNewActivityPopup()
            }
        }
        home_filter_button.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(requireContext())
            var datetime: OffsetDateTime? = null
            val now = OffsetDateTime.now()
            val popup = layoutInflater.inflate(R.layout.dialog_filter, null, false)
            (popup.filter_distance_range as RangeSeekBar<Float>).setRangeValues(0.0F, 200F)
            (popup.filter_distance_range as RangeSeekBar<Float>).setOnRangeSeekBarChangeListener{ _, min, max ->
                popup.distance_range.text = "Od $min km do $max km"
            }
            val spinner: Spinner = popup.filter_activity_choose
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item)
            val adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.types_with_all,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                spinner.adapter = adapter
            }
            popup.filter_time.setOnClickListener {
                DatePickerDialog(requireContext(), { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                    TimePickerDialog(requireContext(), { _: TimePicker, hour: Int, minute: Int ->
                        datetime = OffsetDateTime.of(year, month, dayOfMonth, hour, minute, 0, 0, ZoneOffset.UTC)
                    }, datetime?.hour?:now.hour, datetime?.minute?:now.minute, true).show()
                }, datetime?.year?:now.year, datetime?.monthValue?:now.monthValue, datetime?.dayOfMonth?:now.dayOfMonth ).show()
            }

            dialogBuilder.setView(popup)
            var dialog = dialogBuilder.create()
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_design)

            popup.filter.setOnClickListener {
                val ff = (popup.filter_distance_range as RangeSeekBar<Float>)
                var str: String? = spinner.selectedItem.toString()
                str = if (str == "Vše") null else str
                recyclerAdapter.setFilters(ff.selectedMinValue, ff.selectedMaxValue, str, datetime)
                dialog.hide()
            }

            popup.restore_default.setOnClickListener {
                recyclerAdapter.setFilters(0.0F, Float.MAX_VALUE, null, null)
                dialog.hide()
            }

            dialog.show()


        }
    }


}