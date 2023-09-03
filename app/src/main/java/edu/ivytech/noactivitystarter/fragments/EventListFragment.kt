package edu.ivytech.noactivitystarter.fragments

import android.content.Intent.getIntent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.ivytech.noactivitystarter.R
import edu.ivytech.noactivitystarter.database.Event
import edu.ivytech.noactivitystarter.databinding.FragmentEventListBinding
import edu.ivytech.noactivitystarter.databinding.FragmentEventListCardBinding
import java.text.DateFormat
import java.util.*

class EventListFragment : Fragment(){
    private var _binding: FragmentEventListBinding? = null
    private val binding get() = _binding!!
    private val viewModel : EventListViewModel by viewModels()
    private var adapter : EventAdapter? = EventAdapter(emptyList())
    private var disablePast : Boolean = false
    private var goToDetail : Boolean = false
    private lateinit var detailId : UUID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if(it.containsKey(ARG_FRAGMENT) && it.containsKey(ARG_ITEM_ID)){
                if(it.getBoolean(ARG_FRAGMENT)){
                    goToDetail = true
                    detailId = it.getSerializable(ARG_ITEM_ID) as UUID
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =  FragmentEventListBinding.inflate(inflater, container,false)
        binding.eventList.layoutManager = LinearLayoutManager(context,
            LinearLayoutManager.VERTICAL,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pref = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
        disablePast = pref?.getBoolean("disable_past_events",false)!!

        if(goToDetail){
            val bundle = Bundle()
            bundle.putSerializable(DetailFragment.ARG_ITEM_ID,detailId)
            view.findNavController().navigate(R.id.detailFragment, bundle)
        }
        else
            setupRecyclerView()
    }
    //needs to display events only for the current week
    // ... will figure that out later

    private fun setupRecyclerView() {
        viewModel.eventListLiveData.observe(viewLifecycleOwner){
        adapter = EventAdapter(it)
        binding.eventList.adapter = adapter
        }
    }

    private inner class EventAdapter(var events : List<Event>) : RecyclerView.Adapter<EventHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventHolder {
            val view = FragmentEventListCardBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            return EventHolder(view)
        }
        override fun onBindViewHolder(holder: EventHolder, position: Int) {
            val event = events[position]
            holder.bind(event)
        }
        override fun getItemCount(): Int {
            return events.size
        }

    }

    private inner class EventHolder (val itemBinding: FragmentEventListCardBinding) :
        RecyclerView.ViewHolder(itemBinding.root),
        View.OnClickListener {
        private lateinit var event : Event
        init {
            itemBinding.root.setOnClickListener(this)
        }
        fun bind(event: Event){
            this.event = event
            itemBinding.title.text = event.title
            itemBinding.date.text = java.text.SimpleDateFormat("MMM d, yyyy").format(event.startTime)
           // itemBinding.weekDay.text = java.text.SimpleDateFormat("EEE").format(event.startTime.getTime())
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
            }else {
                val bundle = Bundle()
                bundle.putSerializable(DetailFragment.ARG_ITEM_ID, event.id)
                itemView.findNavController()
                    .navigate(R.id.action_eventListFragment_to_detailFragment, bundle)
            }

        }
    }

    companion object {
        const val ARG_ITEM_ID = "item_id"
        const val ARG_FRAGMENT = "fragment_nav"
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}