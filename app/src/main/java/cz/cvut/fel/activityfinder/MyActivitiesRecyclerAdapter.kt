package cz.cvut.fel.activityfinder

import android.annotation.SuppressLint
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import cz.cvut.fel.activityfinder.model.Activity
import cz.cvut.fel.activityfinder.model.DateTimeTools
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class MyActivitiesRecyclerAdapter : RecyclerView.Adapter<MyActivitiesRecyclerAdapter.ViewHolder> () {

    private val activities: MutableList<Activity> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.my_activity_card_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val act = this.activities[position]
        holder.name.text = act.name
        val from = OffsetDateTime.ofInstant(act.dateFrom?.toInstant(), ZoneId.systemDefault())
        val to = OffsetDateTime.ofInstant(act.dateTo?.toInstant(), ZoneId.systemDefault())
        val date = DateTimeTools.fmt.format(from) + " - " + DateTimeTools.fmt.format(to)
        holder.date.text = date
        holder.type.text = "Typ: ${act.type}"
        holder.members.text = "Účastníků: ${act.participated?.size?:0}"
        holder.maximumMembers.text = "Maximum účastníků: ${act.maximumCapacity?:0}"
        holder.author.text = "Autor: ${act.authorName}"
        holder.description.text = "${act.description}"
        val dstn = "Vzdálenost: ${((this.activities[position].distance/100.0)).roundToInt()/10.0}"
        holder.distance.text = "Vzdálenost: $dstn km"
        holder.type.text = "${holder.itemView.getContext().getString(R.string.type)}: ${act.type}"
        holder.members.text = "${holder.itemView.getContext().getString(R.string.activity_detail_number_of_particiants_label)}: ${act.participated?.size?:0}"
        holder.maximumMembers.text = "${holder.itemView.getContext().getString(R.string.activity_detail_maximum_of_particiants_label)}: ${act.maximumCapacity?:0}"
        holder.author.text = "${holder.itemView.getContext().getString(R.string.author)}: ${act.authorName}"
        holder.description.text = "${holder.itemView.getContext().getString(R.string.description)}: ${act.description}"
        holder.activityItem.setOnClickListener {
            if(it.context.javaClass.simpleName == "MainActivity") {
                (it.context as MainActivity).openDetailActivity(act.id!!)
            }
        }
        if(act.authorId == FirebaseAuth.getInstance().currentUser!!.uid) {
            holder.delete.setOnClickListener {
                if (it.context.javaClass.simpleName == "MainActivity") {
                    (it.context as MainActivity).openDeleteActivityPopup(act)
                }
            }
        } else {
            holder.delete.isVisible = false
        }
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        var name: TextView
        var date: TextView
        var type: TextView
        var distance: TextView
        var members: TextView
        var maximumMembers: TextView
        var author: TextView
        var description: TextView
        var activityItem: CardView
        var delete: Button

        init {
            name = itemView.findViewById(R.id.my_activity_name)
            date = itemView.findViewById(R.id.my_activity_datetime)
            type = itemView.findViewById(R.id.my_activity_type)
            distance = itemView.findViewById(R.id.my_activity_distance)
            members = itemView.findViewById(R.id.my_activity_members)
            maximumMembers = itemView.findViewById(R.id.my_activity_maximum_members)
            author = itemView.findViewById(R.id.my_activity_author)
            description = itemView.findViewById(R.id.my_activity_description)
            activityItem = itemView.findViewById(R.id.my_activity_list_item)
            delete = itemView.findViewById(R.id.my_activity_delete)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addActivities(list: ArrayList<Activity>) {
        this.activities.clear()
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        this.activities.addAll(list.filter {
            it.authorId == uid || it.participated?.contains(uid)==true
        })
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return this.activities.size
    }

}