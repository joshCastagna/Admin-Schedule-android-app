package edu.ivytech.noactivitystarter

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.format.DateFormat.format

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.ivytech.noactivitystarter.database.Event
import edu.ivytech.noactivitystarter.databinding.FragmentWeekActivitiesBinding
import edu.ivytech.noactivitystarter.databinding.WeekActivityCardBinding
import edu.ivytech.noactivitystarter.fragments.DetailFragment
import edu.ivytech.noactivitystarter.fragments.EventListFragment
import edu.ivytech.noactivitystarter.fragments.EventListViewModel
import java.text.DateFormat
import java.util.*


class WeekEventFragment : Fragment() {

    private var _binding: FragmentWeekActivitiesBinding? = null
    private val binding get() = _binding!!
    private var adapter : EventAdapter? = EventAdapter(emptyList())
    private val viewModel : EventListViewModel by viewModels()
    private var disablePast : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =  FragmentWeekActivitiesBinding.inflate(inflater, container,false)
        binding.weekList.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pref = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
        disablePast = pref?.getBoolean("disable_past_events",false)!!
        setupRecyclerView()
    }

        //needs to display events only for the current week
        // ... will figure that out later

    private fun setupRecyclerView() {

        //only returns current week
        viewModel.eventListLiveData.observe(viewLifecycleOwner) {
            eventListLD ->

        var today = Date()
        val weekOfYear : java.text.SimpleDateFormat =
                java.text.SimpleDateFormat("w", Locale.US)
        val weekOfMonth : java.text.SimpleDateFormat =
                java.text.SimpleDateFormat("W", Locale.US)
        var week_num = weekOfYear.format(today).toInt()
        var mweek_num =  weekOfMonth.format(today).toInt()

        var weeklyList: MutableList<Event> = mutableListOf()

            eventListLD.forEach { //goes through all of live data
           if (weekOfYear.format(it.startTime).toInt() == week_num
               && weekOfMonth.format(it.startTime).toInt() == mweek_num){ //checks if date is the same
                                                                          // week of year & month
                    weeklyList.add(it)
           }
        }
        weeklyList.sortBy { it.startTime.time }
        adapter = EventAdapter(weeklyList)
        binding.weekList.adapter = adapter
    }
    }

    private inner class EventAdapter(var weeklyEvents : List<Event>) : RecyclerView.Adapter<EventHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventHolder {
            val view = WeekActivityCardBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            return EventHolder(view)
        }

        override fun onBindViewHolder(holder: EventHolder, position: Int) {
           val event = weeklyEvents[position]
            holder.bind(event)
        }

        override fun getItemCount(): Int {
            return weeklyEvents.size
        }

    }

    private inner class EventHolder (val itemBinding: WeekActivityCardBinding) :
    RecyclerView.ViewHolder(itemBinding.root),
    View.OnClickListener {

        private lateinit var event : Event

        init {
            itemBinding.root.setOnClickListener(this)
        }

        fun bind(event: Event){
            this.event = event
            itemBinding.eventTitle.text = event.title
            itemBinding.dateNumber.text = java.text.SimpleDateFormat("dd").format(event.startTime) //may have to change this?
            itemBinding.date.text = java.text.SimpleDateFormat("EEE").format(event.startTime)
            itemBinding.teacherName.text = event.creatorName
            itemBinding.category.text = event.description
             itemBinding.timeSpan.text = DateFormat.getTimeInstance(DateFormat.SHORT)
             .format(event.startTime) + " - " + DateFormat.getTimeInstance(DateFormat.SHORT).format(event.endTime)

            val date = Date()
            if(date.after(event.endTime)){
                if(disablePast)
                    activity?.resources?.let { itemBinding.weekCardHeader.setBackgroundColor(it.getColor(R.color.ivyTechGray))
                    }
            }
            else {
                activity?.resources?.let {
                    itemBinding.weekCardHeader.setBackgroundColor(it.getColor(R.color.lightGreen1))
                }
            }

        }

        override fun onClick(v: View?) {

            val date = Date()
            if(date.after(event.endTime) && disablePast) {
                Toast.makeText(activity,"Past events disabled.",Toast.LENGTH_SHORT).show()
            } else {

                val bundle = Bundle()
                bundle.putSerializable(DetailFragment.ARG_ITEM_ID, event.id)
                val intent = Intent(activity, EventListActivity::class.java)
                intent.putExtra(EventListFragment.ARG_FRAGMENT, true)
                intent.putExtra(EventListFragment.ARG_ITEM_ID, event.id)
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}