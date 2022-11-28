package cz.cvut.fel.activityfinder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cz.cvut.fel.activityfinder.model.ActivityManager
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_my_activities.*

class MyActivitiesFragment : Fragment() {

    private lateinit var recyclerAdapter: MyActivitiesRecyclerAdapter
    private lateinit var viewModel: ActivityManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        this.viewModel = ActivityManager.getInstance()
        this.recyclerAdapter = MyActivitiesRecyclerAdapter()
        viewModel.activities.observe(viewLifecycleOwner) {
            recyclerAdapter.addActivities(it)
        }
        viewModel.getRealtimeUpdate()
        return inflater.inflate(R.layout.fragment_my_activities, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myact_activity_list.adapter = recyclerAdapter
        myact_add_activity_button.setOnClickListener{
            var ap = it.context
            if(ap.javaClass.simpleName == "MainActivity") {
                ap = ap as MainActivity
                ap.openNewActivityPopup()
            }
        }
    }
}