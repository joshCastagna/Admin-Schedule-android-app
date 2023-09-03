package edu.ivytech.noactivitystarter

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableString
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import edu.ivytech.noactivitystarter.databinding.ActivityBioBinding
import edu.ivytech.noactivitystarter.firestore.AdminUser
import edu.ivytech.noactivitystarter.firestore.FirestoreUtil
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class BioActivity : AppCompatActivity() {
    private lateinit var binding : ActivityBioBinding
    private var user : AdminUser? = null
    private lateinit var photoFile: File
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var IVYTECH_COURSES = arrayOf("CSCI","SDEV","DBMS","ITSP","SVAD","CSIA","NETI","INFM")
    private var pickedCourses = mutableListOf<String>()
    var pickedBool = BooleanArray(IVYTECH_COURSES.size)
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
        binding = ActivityBioBinding.inflate(layoutInflater)
        setContentView(binding.root)
       // supportActionBar?.setDisplayHomeAsUpEnabled(true)
        FirestoreUtil.loadUserData().addOnSuccessListener {
            //accessing document from Firestore
            document ->
            user =  document.toObject(AdminUser::class.java)
            //FirestoreUtil.fNAME = user?.fname
            updateUI()
            if(user != null){
                pickedCourses = user!!.courses
            }
        }
        // IMAGE STU
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                permissions -> permissions.entries.forEach {
            Log.e("DEBUG", "${it.key} = ${it.value}")
        }
        }
        cameraLauncher =  registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = FileProvider.getUriForFile(
                    this,
                    "edu.ivytech.noactivitystarter.fileprovider",
                    photoFile
                )
                this.revokeUriPermission(
                    uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                updatePhotoView()
            }
        }
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
            val data:Intent? = result.data
            if(result.resultCode == Activity.RESULT_OK && data != null) {
                val inputStream = this.contentResolver.openInputStream(data.data!!)
                val fileOutputStream = FileOutputStream(photoFile)
                inputStream?.copyTo(fileOutputStream)
                inputStream!!.close()
                fileOutputStream.close()
                updatePhotoView()
            }
        }
        binding.profileImageBio.setOnClickListener {
         Toast.makeText(this,"CLICKED PROFILE PIC!",Toast.LENGTH_LONG).show()
            selectImage()
        }
        binding.editCoursesBttn2.setOnClickListener {
            coursePicker()
        }
        binding.profileSaveButton.setOnClickListener{
            user!!.fname = binding.fnameEditText.text.toString()
            user!!.lname = binding.lnameEditText.text.toString()
            user!!.bio = binding.profileBioEditText.text.toString()
            user!!.courses = pickedCourses
            uploadImg()
            //FirestoreUtil.fNAME = user?.fname
            FirestoreUtil.saveUserData(user!!).addOnSuccessListener {
                startActivity(Intent(this,MainActivity::class.java))
            }
        }
    }

    private fun coursePicker(){
        //establishing boolarray values
        for(i in pickedCourses){
            pickedBool[courseMap[i]!!] = true
        }

        val builder = AlertDialog.Builder(this)
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
            binding.courselist.text = setCourseList(pickedCourses)
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
                pickedBool[p] = false
                pickedCourses.clear()
                binding.courselist.text = ""
            }
        }
        builder.show()
    }

    private fun updateUI(){
        binding.fnameEditText.setText(user!!.fname!!)
        binding.lnameEditText.setText(user!!.lname!!)
        binding.profileBioEditText.setText(user!!.bio!!)
        binding.courselist.text = setCourseList(user!!.courses)
        val filesDir = this.applicationContext.filesDir
        //should it search online? hmm
            photoFile = File(filesDir, user!!.photoFileName)
            if(!photoFile.exists()){
             //search online if for some reason we cant get it

            }
        updatePhotoView()
    }

    private fun setCourseList(spstr : List<String>) : SpannableString{
        var courseListString : String = ""
        for(c in spstr){
            courseListString +=  c + "\n"
        }
        return SpannableString(courseListString)
    }

    private fun selectImage(){
        val options = arrayOf<CharSequence>("Take Photo","Select from Gallery","Exit")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Profile Picture")

        builder.setItems(options){
            dialog, item ->
            if(options[item] == "Take Photo"){
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                    val uri = FileProvider.getUriForFile(
                        this,
                        "edu.ivytech.noactivitystarter.fileprovider",
                        photoFile
                    )
                    val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri)

                    val cameraActivities: List<ResolveInfo> =
                        this
                            .packageManager.queryIntentActivities(
                                captureImage,
                                PackageManager.MATCH_DEFAULT_ONLY
                            )
                    for (activity in cameraActivities) {
                        this.grantUriPermission(
                            activity.activityInfo.packageName,
                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    }
                    cameraLauncher.launch(captureImage)
                } else {
                    permissionLauncher.launch(arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE))
                }
            }
            else if(options[item] == "Select from Gallery"){
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE))
                } else {
                    val pickPhoto = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI
                    )
                    galleryLauncher.launch(pickPhoto)
                }
            }
            else if(options[item] == "Exit"){
                dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun updatePhotoView(){
        if(!photoFile.exists()){
            return
        } else {
            var bitmap : Bitmap? = getScaledBitmap(photoFile.path,this)
            bitmap = cropToSquare(bitmap!!)
          binding.profileImageBio.setImageBitmap(bitmap)
            downloadImg()
        }
    }

    private fun uploadImg(){
        val storage = FirebaseStorage.getInstance()
        var path : String = "profileImg/" + user?.id + ".png"
        var profileRef = storage.getReference(path)
        var bitmap : Bitmap? = BitmapFactory.decodeFile(photoFile.path)
        var outputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.PNG,100,outputStream)
        var data = outputStream.toByteArray()
            profileRef.putBytes(data).onSuccessTask {
                profileRef.downloadUrl.addOnSuccessListener {
                    user?.profilePic  = it.toString()
                }
            }
    }

    private fun downloadImg() {

        val storage = FirebaseStorage.getInstance().getReference().child("profileImg/" + user?.id + ".png")
        var file : File = File.createTempFile("profile","png")
        storage.getFile(file)
            .addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

//*********************************************************************


