package edu.ivytech.noactivitystarter.fragments

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.CalendarContract
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.storage.FirebaseStorage
import edu.ivytech.noactivitystarter.MainActivity
import edu.ivytech.noactivitystarter.cropToSquare
import edu.ivytech.noactivitystarter.database.Event
import edu.ivytech.noactivitystarter.databinding.FragmentAdminDetailBinding
import edu.ivytech.noactivitystarter.databinding.FragmentEventDetailviewBinding
import edu.ivytech.noactivitystarter.firestore.AdminUser
import edu.ivytech.noactivitystarter.firestore.FirestoreUtil
import edu.ivytech.noactivitystarter.fragments.DetailFragment.Companion.ARG_ITEM_ID
import java.io.File
import java.text.DateFormat
import java.util.*

class AdminDetailFragment : Fragment() {
    private var _binding: FragmentAdminDetailBinding? = null
    private val binding get() = _binding!!
    private var item : Event? = null
    private var IVYTECH_COURSES = arrayOf("CSCI","SDEV","DBMS","ITSP","SVAD","CSIA","NETI","INFM")
    private var isEditable : Boolean = false
    var pickedCourses = mutableListOf<String>()
    var pickedBool = BooleanArray(IVYTECH_COURSES.size)
    private var user : AdminUser? = null
    private var courseMap = mutableMapOf<String,Int>()
    init {
        courseMap["CSCI"] = 0
        courseMap["SDEV"] = 1
        courseMap["DBMS"] = 2
        courseMap["ITSP"] = 3
        courseMap["SVAD"] = 4
        courseMap["CSIA"] = 5
        courseMap["NETI"] = 6
        courseMap["INFM"] = 7
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                val adminId = it.getString(ARG_ITEM_ID) as String
                FirestoreUtil.getUsers().addOnSuccessListener {
                    //accessing document from Firestore
                        document ->
                    val adminList = document.toObjects<AdminUser>() as MutableList<AdminUser>
                    for (u in adminList) {
                        if (u.id == adminId) {
                            user = u
                            updateUI()
                        }
                    }
                }
            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminDetailBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        binding.editbutton.setOnClickListener {
            if(isEditable == false)
                isEditable = true
            else
                isEditable = false
            activateLayout()
        }
        binding.profileSaveButton3.setOnClickListener{
            user!!.fname = binding.fnameEditText.text.toString()
            user!!.lname = binding.lnameEditText.text.toString()
            user!!.bio = binding.profileBioEditText.text.toString()
            user!!.courses = pickedCourses
            //FirestoreUtil.fNAME = user?.fname
            FirestoreUtil.saveUserData(user!!).addOnSuccessListener {
               Toast.makeText(activity,"Saved Profile",Toast.LENGTH_SHORT).show()
            }
        }
        binding.editCoursesBttn.setOnClickListener {
            coursePicker()
        }
        }

    private fun activateLayout(){
        if(isEditable){
            binding.fnameEditText.isFocusable = true
            binding.fnameEditText.isFocusableInTouchMode = true
            binding.fnameEditText.isLongClickable = true
            binding.fnameEditText.isCursorVisible = true

            binding.lnameEditText.isFocusable = true
            binding.lnameEditText.isFocusableInTouchMode = true
            binding.lnameEditText.isLongClickable = true
            binding.lnameEditText.isCursorVisible = true

            binding.profileBioEditText.isFocusableInTouchMode = true
            binding.profileBioEditText.isFocusable = true
            binding.profileBioEditText.isLongClickable = true
            binding.profileBioEditText.isCursorVisible = true

            binding.editCoursesBttn.isEnabled = true
            binding.profileSaveButton3.isEnabled = true
        }
        else{
            binding.fnameEditText.isFocusable = false
            binding.fnameEditText.isFocusableInTouchMode = false
            binding.fnameEditText.isLongClickable = false
            binding.fnameEditText.isCursorVisible = false

            binding.lnameEditText.isFocusable = false
            binding.lnameEditText.isFocusableInTouchMode = false
            binding.lnameEditText.isLongClickable = false
            binding.lnameEditText.isCursorVisible = false

            binding.profileBioEditText.isFocusableInTouchMode = false
            binding.profileBioEditText.isFocusable = false
            binding.profileBioEditText.isLongClickable = false
            binding.profileBioEditText.isCursorVisible = false

            binding.editCoursesBttn.isEnabled = false
            binding.profileSaveButton3.isEnabled = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    private fun coursePicker(){
        for(i in pickedCourses){
            pickedBool[courseMap[i]!!] = true
        }

        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Select Courses")
        builder.setCancelable(false)
        builder.setMultiChoiceItems(
            IVYTECH_COURSES,pickedBool
        ){
                di, i, b ->
            if(b){
                pickedCourses.add(IVYTECH_COURSES[i])
            }else{
                pickedCourses.remove(IVYTECH_COURSES[i])
            }
        }
        builder.setPositiveButton("OKAY"){
                di, i ->
            binding.adminCourselist.text = setCourseList(pickedCourses)
        }
        builder.setNegativeButton(
            "Cancel"
        ) { di, i ->
            di.dismiss()
        }
        builder.setNeutralButton(
            "Clear"
        ) { dialogInterface, i ->
            for (p in pickedCourses.indices) {
                pickedBool[p] = false }
                pickedCourses.clear()
                binding.adminCourselist.text = ""

        }
        builder.show()
    }

    private fun updateUI(){
        pickedCourses = user!!.courses
        binding.emailTextview.text = user!!.email
        binding.fnameEditText.setText(user!!.fname)
        binding.lnameEditText.setText(user!!.lname)
        binding.profileBioEditText.setText(user!!.bio)
        binding.adminCourselist.text = setCourseList(user!!.courses)
        downloadImg()
    }

    private fun downloadImg() {
        val storage = FirebaseStorage.getInstance().getReference().child("profileImg/" + user!!.id + ".png")
        var file : File = File.createTempFile("profile","png")
        storage.getFile(file)
            .addOnSuccessListener {
                var bitmap = BitmapFactory.decodeFile(file.absolutePath)
                bitmap = cropToSquare(bitmap!!)
                binding.profileImageBio3.setImageBitmap(bitmap)
            }
    }

    private fun setCourseList(spstr : List<String>) : SpannableString {
        var courseListString : String = ""
        for(c in spstr){
            courseListString +=  c + "\n"
        }
        return SpannableString(courseListString)
    }

    companion object {
        const val ARG_ITEM_ID = "item_id"
    }
}
