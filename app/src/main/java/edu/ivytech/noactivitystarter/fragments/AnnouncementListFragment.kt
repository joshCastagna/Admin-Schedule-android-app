package edu.ivytech.noactivitystarter.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.ivytech.noactivitystarter.R
import edu.ivytech.noactivitystarter.databinding.AdminListCardBinding
import edu.ivytech.noactivitystarter.databinding.DialogAddMessageBinding
import edu.ivytech.noactivitystarter.databinding.FragmentAnnouncementCardBinding
import edu.ivytech.noactivitystarter.databinding.FragmentAnnouncementListBinding
import edu.ivytech.noactivitystarter.firestore.AdminUser
import edu.ivytech.noactivitystarter.firestore.Announcement
import edu.ivytech.noactivitystarter.firestore.FirestoreUtil
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// TODO: ADD CONTEXT MENU

class AnnouncementListFragment  : Fragment(){
    private var _binding: FragmentAnnouncementListBinding? = null
    private val binding get() = _binding!!
    private var adapter : AnnouncementAdapter? = AnnouncementAdapter(emptyList())
    var annList: MutableList<Announcement> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAnnouncementListBinding.inflate(inflater,container,false)
        binding.announcementList.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.floatingActionButton.setOnClickListener {
            sendAnnouncementDialog()
        }
        getAnnouncementList()
    }

    private fun getAnnouncementList(){
        FirestoreUtil.getAnnouncements().addOnSuccessListener {
                announcements->
            annList = announcements.toObjects(Announcement::class.java)
            //adds user if id is valid
            for(a in annList){ //.. this is a HORRIBLE idea
                var admin : AdminUser
                FirestoreUtil.loadOtherUserData(a.creatorId).addOnSuccessListener {
                    if(it.toObject(AdminUser::class.java) != null) {
                        admin = it.toObject(AdminUser::class.java)!!
                        a.creator = admin
                    }
                    setUpRecyclerView()
                }
            }

        }
    }


    private fun sendAnnouncementDialog(){
        var dialog : Dialog? = activity?.let { Dialog(it,R.style.DialogRoundedCorner) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(true)
        val dialogBinding : DialogAddMessageBinding = DialogAddMessageBinding.inflate(layoutInflater)
        dialog?.setContentView(dialogBinding.root)
        dialogBinding.cancelBttn.setOnClickListener {
            dialog?.dismiss()
        }
        dialogBinding.sendBttn.setOnClickListener {
            //sending notification
            Toast.makeText(context,"CLICKED SEND",Toast.LENGTH_SHORT).show()
            var announcement = Announcement()
            announcement.timeMade = Date()
            announcement.timeExpire = Date(Date().time + TimeUnit.MILLISECONDS.convert(14, TimeUnit.DAYS))
            announcement.subject = dialogBinding.subjectTextEdittext.text.toString()
            announcement.body = dialogBinding.bodyTextEdittext.text.toString()
            announcement.creatorId = FirestoreUtil.getCurrentUser()?.uid.toString()
            FirestoreUtil.addAnnouncement(announcement).addOnSuccessListener {
                Toast.makeText(context,"Announcement Sent!",Toast.LENGTH_SHORT).show()
                dialog?.dismiss()
                getAnnouncementList()
            }
        }
        //setup button binding and stuff
        dialog?.show()
        setUpRecyclerView() //to refresh
    }

    private fun setUpRecyclerView(){
        adapter = AnnouncementAdapter(annList)
        binding.announcementList.adapter = adapter
    }

    private inner class AnnouncementAdapter(var announcements : List<Announcement>) : RecyclerView.Adapter<AnnouncementListFragment.NotifHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementListFragment.NotifHolder {
            val view = FragmentAnnouncementCardBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            return NotifHolder(view)
        }
        override fun onBindViewHolder(holder: NotifHolder, position: Int) {
            val event = announcements[position]
            holder.bind(event)
        }
        override fun getItemCount(): Int {
            return announcements.size
        }
    }
    private inner class NotifHolder (val itemBinding: FragmentAnnouncementCardBinding) :
        RecyclerView.ViewHolder(itemBinding.root),
        View.OnClickListener, View.OnCreateContextMenuListener {
        private lateinit var notification : Announcement
        init {
            itemBinding.root.setOnClickListener(this)
            itemBinding.root.setOnCreateContextMenuListener(this)
        }
        fun bind(notif: Announcement){
            notification = notif
            itemBinding.announcementCardTitle.text= notif.subject
            itemBinding.announcementCardBodyprev.text = notif.body
            itemBinding.datePosted.text = SimpleDateFormat("EEE, d MMM yyyy h:mm aaa").format(notif.timeMade)
            if(notif.creator != null){
                itemBinding.annnouncementCardName.text = notif.creator!!.fname + " " + notif.creator!!.lname
            }
        }
        override fun onClick(v: View?) {
            Toast.makeText(context, "hit announcement", Toast.LENGTH_SHORT).show()
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu?.add("Delete")?.setOnMenuItemClickListener {
                FirestoreUtil.deleteAnnouncement(notification)
                getAnnouncementList()
                true
            }
        }
    }
}