package edu.ivytech.noactivitystarter

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.graphics.scale
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import com.firebase.ui.auth.AuthUI
import com.google.firebase.storage.FirebaseStorage
import edu.ivytech.noactivitystarter.databinding.ActivityMainBinding
import edu.ivytech.noactivitystarter.firestore.AdminUser
import edu.ivytech.noactivitystarter.firestore.FirestoreUtil
import java.io.File
import java.util.*


private const val DETAIL_KEY = "item_id"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration : AppBarConfiguration
    private var user : AdminUser? = null
    private var alarmMgr : AlarmManager? = null

    fun toDetailFromMain(context : Context, id : UUID) : Intent {
            val intent = Intent(context,EventListActivity::class.java)
            intent.putExtra(DETAIL_KEY,id)
            return intent}

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //OPTIONS MENU
        menuInflater.inflate(R.menu.menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.preferences_menu -> {
                Toast.makeText(this, "pref menu", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this,PreferencesActivity::class.java))
                true
            }
            R.id.log_out -> {
                AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener {
                        Toast.makeText(this,"You signed out",Toast.LENGTH_LONG).show()
                    }
                startActivity(Intent(this,FirebaseAuthUIActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alarmMgr = this.getSystemService(Context.ALARM_SERVICE) as? AlarmManager?

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.infobutton.setOnClickListener {
            startActivity(Intent(this,InfoActivity::class.java))
        }
        binding.profileImage.setOnClickListener {
            startActivity(Intent(this,BioActivity::class.java))
        }
        binding.calendarButton.setOnClickListener{
            startActivity(Intent(this,EventListActivity::class.java))
        }
        binding.announcementButton.setOnClickListener{
            startActivity(Intent(this,AnnouncementActivity::class.java))
        }
        binding.adminListbutton.setOnClickListener {
            startActivity(Intent(this,AdminListActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        FirestoreUtil.loadUserData().addOnSuccessListener {
            //accessing document from Firestore
                document ->
            user =  document.toObject(AdminUser::class.java)
            var namestring = user?.fname
            //Toast.makeText(this,"${user?.fname}",Toast.LENGTH_LONG).show()
            if(user?.fname == ""){
                binding.welcomeText.setText(getString(R.string.welcome_string_nullName))
            } else {
                binding.welcomeText.setText(getString(R.string.welcome_string_name,user?.fname.toString()))
            }
            downloadImg()
        }
    }

    private fun downloadImg() {
        val storage = FirebaseStorage.getInstance().getReference().child("profileImg/" + user?.id + ".png")
        var file : File = File.createTempFile("profile","png")
        storage.getFile(file)
            .addOnSuccessListener {
                var bitmap = BitmapFactory.decodeFile(file.absolutePath)
                bitmap = cropToSquare(bitmap!!)
                binding.profileImage.setImageBitmap(bitmap)
            }
    }

}