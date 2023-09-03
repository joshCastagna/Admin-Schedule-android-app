package edu.ivytech.noactivitystarter.fragments

import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import edu.ivytech.noactivitystarter.database.Event
import edu.ivytech.noactivitystarter.databinding.FragmentEventDetailviewBinding
import java.text.DateFormat
import java.util.*

class DetailFragment : Fragment() {
    private var _binding: FragmentEventDetailviewBinding? = null
    private val binding get() = _binding!!
    private var item : Event? = null
    private val eventDetailVM : EventDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let{
            if(it.containsKey(ARG_ITEM_ID)) {
                val eventId = it.getSerializable(ARG_ITEM_ID) as UUID
                eventDetailVM.loadEvent(eventId)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEventDetailviewBinding.inflate(inflater,container,false)
        //update UI
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.buttonSavetocalendar.setOnClickListener {
            saveToCalendar()
        }
    }

    fun saveToCalendar(){
        val intent = Intent(Intent.ACTION_INSERT).apply {
            val pref = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
            val detailBool = pref?.getBoolean("cal_detail",false)
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, item!!.title)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,item!!.startTime.time)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME,item!!.endTime.time)
            if(detailBool == true) {
                putExtra(CalendarContract.Events.DESCRIPTION, item!!.description)
                putExtra(CalendarContract.Events.EVENT_LOCATION,item!!.location)
            }
        }
        startActivity(intent)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventDetailVM.eventLiveDate.observe(viewLifecycleOwner){
            item = it
            updateUI()
        }
    }

    private fun updateUI(){
        binding.detailviewEventTitle.text = item?.title
        binding.detailviewDescription.text = item?.description
        binding.detailviewDate.text = java.text.SimpleDateFormat("EEEE, MMM dd, yyyy").format(item?.startTime)
        binding.detailviewTimeframe.text = DateFormat.getTimeInstance(DateFormat.SHORT)
            .format(item?.startTime) + " - " + DateFormat.getTimeInstance(DateFormat.SHORT).format(item?.endTime)
        binding.detailviewLocation.text = item?.location
            if(item?.location == null || item?.location == ""){ //hides location information to not have an empty row
                binding.detailviewLocation.visibility = View.GONE
                binding.descriptionLocationDiv.visibility = View.GONE
            }
        binding.detailviewProfessor.text = item?.creatorName
        binding.detailviewBio.text = item?.creatorName
    }

    companion object {
        const val ARG_ITEM_ID = "item_id"
    }
}