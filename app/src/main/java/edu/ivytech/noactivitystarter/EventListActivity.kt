package edu.ivytech.noactivitystarter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import edu.ivytech.noactivitystarter.databinding.ActivityEventListBinding
import edu.ivytech.noactivitystarter.fragments.EventListFragment

class EventListActivity : AppCompatActivity() {
    private lateinit var binding : ActivityEventListBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private  var fromMain = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView_full) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.eventListFragment,getData())
        fromMain  = intent.getBooleanExtra(EventListFragment.ARG_FRAGMENT,false)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        if(!fromMain) {
            setupActionBarWithNavController(navController, appBarConfiguration)
        }
        else {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

     private fun getData() : Bundle
    {
        val bundle = Bundle()
        bundle.putBoolean(EventListFragment.ARG_FRAGMENT,intent.getBooleanExtra(EventListFragment.ARG_FRAGMENT,false))
        bundle.putSerializable(EventListFragment.ARG_ITEM_ID,intent.getSerializableExtra(EventListFragment.ARG_ITEM_ID))
        return bundle
    }

    override fun onSupportNavigateUp(): Boolean {
        if(!fromMain) {
            val navController = findNavController(R.id.fragmentContainerView_full)
            return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        }
        else {
            onBackPressed();
            return true;
        }
    }
}



