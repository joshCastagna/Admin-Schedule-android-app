package edu.ivytech.noactivitystarter.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.storage.FirebaseStorage
import edu.ivytech.noactivitystarter.R
import edu.ivytech.noactivitystarter.cropToSquare
import edu.ivytech.noactivitystarter.databinding.*
import edu.ivytech.noactivitystarter.firestore.AdminUser
import edu.ivytech.noactivitystarter.firestore.FirestoreUtil
import java.io.File

class AdminListFragment : Fragment(){
    private var _binding: FragmentAdminListBinding? = null
    private val binding get() = _binding!!
    private var adapter : AdminAdapter? = AdminAdapter(emptyList())
    var adminList: MutableList<AdminUser> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =  FragmentAdminListBinding.inflate(inflater, container,false)
        binding.adminList.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FirestoreUtil.getUsers().addOnSuccessListener {
            //accessing document from Firestore
                document ->

                adminList = document.toObjects<AdminUser>() as MutableList<AdminUser>

            setupRecyclerView()
        }
    }

    private fun setupRecyclerView() {
            adapter = AdminAdapter(adminList)
            binding.adminList.adapter = adapter
    }

    private inner class AdminAdapter(var admins : List<AdminUser>) : RecyclerView.Adapter<AdminHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminHolder {
            val view = AdminListCardBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            return AdminHolder(view)
        }
        override fun onBindViewHolder(holder: AdminHolder, position: Int) {
            val event = admins[position]
            holder.bind(event)
        }
        override fun getItemCount(): Int {
            return admins.size
        }

    }

    private inner class AdminHolder (val itemBinding: AdminListCardBinding) :
        RecyclerView.ViewHolder(itemBinding.root),
        View.OnClickListener {
        private lateinit var admin : AdminUser
        init {
            itemBinding.root.setOnClickListener(this)
        }
        fun bind(admin: AdminUser){
            this.admin = admin
            //PUT ALL THE INFO HERE
            itemBinding.adminEmail.text = admin.email
            itemBinding.adminName.text = admin.fname + " " + admin.lname
            itemBinding.adminCourseList.text = admin.courses.joinToString()
            downloadImg()
            //need to find way to pull image?
        }
        override fun onClick(v: View?) {
            Toast.makeText(context, "hit admin",Toast.LENGTH_SHORT).show()
            val bundle = Bundle()
            bundle.putSerializable(AdminDetailFragment.ARG_ITEM_ID, admin.id)
            itemView.findNavController().navigate(R.id.action_adminListFragment_to_adminDetailFragment, bundle)
        }

        private fun downloadImg() {
            val storage = FirebaseStorage.getInstance().getReference().child("profileImg/" + admin.id + ".png")
            var file : File = File.createTempFile("profile","png")
            storage.getFile(file)
                .addOnSuccessListener {
                    var bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    bitmap = cropToSquare(bitmap!!)
                    itemBinding.adminProfileImg.setImageBitmap(bitmap)
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}