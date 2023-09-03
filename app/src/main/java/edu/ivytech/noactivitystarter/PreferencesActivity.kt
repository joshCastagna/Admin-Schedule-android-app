package edu.ivytech.noactivitystarter

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import edu.ivytech.noactivitystarter.database.Event
import edu.ivytech.noactivitystarter.databinding.ActivityMainBinding
import edu.ivytech.noactivitystarter.fragments.EventListViewModel
import edu.ivytech.noactivitystarter.notification.AlarmReceiver
import java.util.*
import java.util.concurrent.TimeUnit


class PreferencesActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var binding: ActivityMainBinding
    private val viewModel : EventListViewModel by viewModels()
    private var alarmMgr: AlarmManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        alarmMgr = this.getSystemService(Context.ALARM_SERVICE) as? AlarmManager?
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, s: String?) {
      //notification changes
        if(s == NOTIF_BOOL_KEY && sharedPreferences!!.getBoolean(NOTIF_BOOL_KEY,false)){ // IS TURNING ON NOTIFICATIONS
            println("turning on notifications")
            checkReminderTimes(sharedPreferences)
        }
        if(s == NOTIF_BOOL_KEY && !sharedPreferences!!.getBoolean(NOTIF_BOOL_KEY,false)){ //IS TURNING OFF NOTIFICICATIONS
            cancelReminders(sharedPreferences)
        }
        if(s == NOTIF_TIMES_KEY || s == NOTIF_DETAIL_KEY){ //CHANGE TO ANY NOTIFICATION SETTING SUCH AS
                                                           // TIMES OR DETAIL AMOUNT
            println("changed time or detail notif settings")
            checkReminderTimes(sharedPreferences)
        }
    }



    fun checkReminderTimes(sharedPreferences: SharedPreferences?){
        val reminder_times = sharedPreferences?.getStringSet(NOTIF_TIMES_KEY, emptySet())
        if (reminder_times != null) {
            if(reminder_times.size == 0){
                //is the same as cancelling
                println("no times set")
                cancelReminders(sharedPreferences)
            } else {
                makeReminders(sharedPreferences)
            }
        }
    }

    fun cancelReminders(sharedPreferences: SharedPreferences?){
        //GET REQUEST CODE LIST FROM SAVEPREFERENCES
        if (sharedPreferences != null) {
            val requestCodesStr = sharedPreferences.getStringSet(ALARM_ID_KEY, emptySet())
            val requestCodes : MutableSet<Int> = mutableSetOf()
            if (requestCodesStr != null) {
                for(i in requestCodesStr){
                    requestCodes += i.toInt()
                }
            }
            requestCodes.forEach { //CANCELS FOR EVERY REQUEST CODE
                val intent = Intent(this, AlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getService(
                    this,
                    it,
                    intent,
                    PendingIntent.FLAG_NO_CREATE
                )
                pendingIntent?.let{
                    alarmMgr?.cancel(it)
                }
            }
        }
    }

    fun makeReminders(sharedPreferences: SharedPreferences?){
        val requestCodes : MutableSet<Int> = mutableSetOf()
        var counter = 0
        val reminder_times = sharedPreferences?.getStringSet(NOTIF_TIMES_KEY, emptySet())

        viewModel.eventListLiveData.observe(this, Observer {
            //getting all of the events
            var i = 0
            for(e in it) {
                while (i < 60){
                    for (t in reminder_times!!) { //SEEING WHICH ALARMS TO MAKE
                        if (t == "15") { // 15 MINS SELECTED
                            val alarmIntent = makeAlarmIntent(e, 15)
                            val pendingIntent = PendingIntent.getBroadcast(
                                this,
                                counter,
                                alarmIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )
                            alarmMgr?.set(
                                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                e.startTime.time - TimeUnit.MILLISECONDS.convert(
                                    15,
                                    TimeUnit.MINUTES
                                ),
                                pendingIntent
                            )
                            requestCodes += counter
                            counter++
                        }
                        if (t == "30") { // 30 MINS SELECTED
                            val alarmIntent = makeAlarmIntent(e, 30)
                            val pendingIntent = PendingIntent.getBroadcast(
                                this,
                                counter,
                                alarmIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )
                            alarmMgr?.set(
                                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                e.startTime.time - TimeUnit.MILLISECONDS.convert(
                                    30,
                                    TimeUnit.MINUTES
                                ),
                                pendingIntent
                            )
                            requestCodes += counter
                            counter++
                        }
                        if (t == "60") { // HOUR SELECTED
                            val alarmIntent = makeAlarmIntent(e, 60)
                            val pendingIntent = PendingIntent.getBroadcast(
                                this,
                                counter,
                                alarmIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )
                            alarmMgr?.set(
                                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                e.startTime.time - TimeUnit.MILLISECONDS.convert(
                                    60,
                                    TimeUnit.MINUTES
                                ),
                                pendingIntent
                            )
                            requestCodes += counter
                            counter++
                        }
                        if (t == "1440") { // 1 DAY SELECTED
                            val alarmIntent = makeAlarmIntent(e, 1440)
                            val pendingIntent = PendingIntent.getBroadcast(
                                this,
                                counter,
                                alarmIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )
                            alarmMgr?.set(
                                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                e.startTime.time - TimeUnit.MILLISECONDS.convert(
                                    1440,
                                    TimeUnit.MINUTES
                                ),
                                pendingIntent
                            )
                            requestCodes += counter
                            counter++
                        }
                    }
                    i++
            }
            }
            if (sharedPreferences != null) {
                val requestCodes_str : MutableSet<String> = mutableSetOf()
                requestCodes.forEach {
                    requestCodes_str += it.toString()
                }
                sharedPreferences.edit().putStringSet(ALARM_ID_KEY, requestCodes_str) // adding list of requestIds to preferences, so they can be cancelled later
            }
        })
    }

    fun makeAlarmIntent(event : Event, time_before : Int) : Intent {
        val alarmIntent = Intent(this,AlarmReceiver::class.java)
        alarmIntent.putExtra("title",event.title)
        when(time_before){
            15 ->{ alarmIntent.putExtra("timeUntil","starting in 15 minutes")}
            30 ->{alarmIntent.putExtra("timeUntil","starting in 30 minutes")}
            60->{alarmIntent.putExtra("timeUntil","starting in 1 hour")}
            1440->{alarmIntent.putExtra("timeUntil","starting in 1 day")}
        }
        return alarmIntent
    }

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }



    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

        }
        override fun onStart() {
            super.onStart()
        }
    }

    companion object {
        const val ALARM_ID_KEY = "alarms"
        const val NOTIF_BOOL_KEY = "reminders"
        const val NOTIF_TIMES_KEY = "reminder_times"
        const val NOTIF_DETAIL_KEY = "detailed_reminder"
    }
}