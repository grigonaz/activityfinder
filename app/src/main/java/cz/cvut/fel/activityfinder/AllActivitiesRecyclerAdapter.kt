package cz.cvut.fel.activityfinder

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import cz.cvut.fel.activityfinder.model.Activity
import cz.cvut.fel.activityfinder.model.ActivityManager
import cz.cvut.fel.activityfinder.model.DateTimeTools
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*
import java.util.stream.Collector
import java.util.stream.Collectors
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class AllActivitiesRecyclerAdapter : RecyclerView.Adapter<AllActivitiesRecyclerAdapter.ViewHolder> (){

    private val activities: MutableList<Activity> = mutableListOf()
    private val activitiesFilter: MutableList<Activity> = mutableListOf()
    private var minDistance: Float = 0.0F
    private var maxDistance: Float = Float.MAX_VALUE
    private var category: String? = null
    private var datetime: Date? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.home_card_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(this.activitiesFilter[position].participated?.contains(FirebaseAuth.getInstance().currentUser?.uid?:0) == true) {
            holder.image.setImageResource(R.drawable.ic_check_yes)
            holder.image.setOnClickListener{
                ActivityManager.getInstance().participateActivity(this.activitiesFilter[position], false)
            }
        } else {
            holder.image.setImageResource(R.drawable.ic_check_no)
            holder.image.setOnClickListener{
                ActivityManager.getInstance().participateActivity(this.activitiesFilter[position], true)
            }
        }
        holder.name.text = this.activitiesFilter[position].name
        val from = OffsetDateTime.ofInstant(this.activitiesFilter[position].dateFrom?.toInstant(), ZoneId.systemDefault())
        val to = OffsetDateTime.ofInstant(this.activitiesFilter[position].dateTo?.toInstant(), ZoneId.systemDefault())
        val date = DateTimeTools.fmt.format(from) + " - " + DateTimeTools.fmt.format(to)
        holder.date.text = date
        val distance = "${((this.activitiesFilter[position].distance/100.0)).roundToInt()/10.0} km"
        holder.distance.text = distance
        holder.activityItem.setOnClickListener {
            var ap = it.context
            if(ap.javaClass.simpleName == "MainActivity") {
                ap = ap as MainActivity
                ap.openDetailPopup(this.activities[position])
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addActivities(list: ArrayList<Activity>) {
        this.activities.clear()
        this.activities.addAll(list)
        this.filtering()
        notifyDataSetChanged()
    }

    private fun filtering() {
        this.activitiesFilter.clear()
        this.activitiesFilter.addAll(this.activities.stream().filter{
            if(this.datetime != null) {
                if(it.dateFrom?.after(this.datetime) == true || it.dateTo?.before(this.datetime) == true) {
                    val date = datetime
                    val from = it.dateFrom?.after(this.datetime)
                    val to = it.dateTo?.before(this.datetime) == true
                    return@filter false
                }
            }
            if(it.distance/1000 > maxDistance || it.distance/1000 < minDistance) {
                return@filter false
            }
            if(this.category != null) {
                if(this.category != it.type) {
                    return@filter false
                }
            }
            true
        }.collect(Collectors.toList()))
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setFilters(min: Float, max: Float, category: String?, datetime: OffsetDateTime?) {
        this.minDistance = min
        this.maxDistance = max
        this.category = category
        this.datetime = datetime?.toInstant()?.toEpochMilli()?.let { Date(it) }
        Log.d(TAG, "setFilters: $min $max $category $datetime")
        this.filtering()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return this.activitiesFilter.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var image: ImageButton
        var name: TextView
        var date: TextView
        var distance: TextView
        var activityItem: CardView

        init {
            image = itemView.findViewById(R.id.check_activity_signed)
            name = itemView.findViewById(R.id.activity_name)
            date = itemView.findViewById(R.id.activity_datetime)
            distance = itemView.findViewById(R.id.activity_distance)
            activityItem = itemView.findViewById(R.id.activity_list_item)
        }
    }

}