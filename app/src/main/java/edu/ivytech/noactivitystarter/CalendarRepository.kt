package edu.ivytech.noactivitystarter

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import edu.ivytech.noactivitystarter.api.CalendarApi
import edu.ivytech.noactivitystarter.api.GCalEvent
//import edu.ivytech.noactivitystarter.api.GCalendarEventList
import edu.ivytech.noactivitystarter.api.GoogleCalendarResponse
import edu.ivytech.noactivitystarter.database.Event
import edu.ivytech.noactivitystarter.database.EventDatabase
import edu.ivytech.noactivitystarter.firestore.AdminUser
import edu.ivytech.noactivitystarter.firestore.FirestoreUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


private const val DATABASE_NAME = "AdminCalendar.db"
private const val TAG = "Calendar Repo"
class CalendarRepository private constructor(private val context : Context){
    private val database : EventDatabase = Room
        .databaseBuilder(context,EventDatabase::class.java,
        DATABASE_NAME)
        .build()
    private val eventDao = database.eventDao()
    private val executor : Executor = Executors.newSingleThreadExecutor()
    private val calApi : CalendarApi
    init{
        val retrofitGCal : Retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/calendar/v3/calendars/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        calApi = retrofitGCal.create(CalendarApi::class.java)
    }

    fun fetchEvents(){
        val responseData : MutableList<Event> = mutableListOf()
        //making date for query
        val date_today = Date()
        val date_timeMin = Date(date_today.time - TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS))
        val d = Date(date_today.time + TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS))
        val timeMin = SimpleDateFormat("yyyy-MM-dd'T'h:m:ssZZZZZ").format(date_timeMin)
        val gcalRequest : Call<GoogleCalendarResponse> = calApi.downloadCalendarEvents("startTime","true",timeMin)
        gcalRequest.enqueue(object : Callback<GoogleCalendarResponse> {
            override fun onResponse(
                call: Call<GoogleCalendarResponse>,
                response: Response<GoogleCalendarResponse>
            ) {
               Log.d(TAG,"Response Recieved")
                val gcalResponse : GoogleCalendarResponse? = response.body()
                val calendar: List<GCalEvent>? = gcalResponse?.items
                if(calendar != null ){
                    var counter = 0
                    for(e in calendar){
                        counter ++
                        //MAKING INTO EVENT
                      if(e.status == "confirmed" ) {
                          val singleDateFormat: SimpleDateFormat =
                              SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.US)
                          val multiDateFormat: SimpleDateFormat =
                              SimpleDateFormat("yyyy-MM-dd", Locale.US)
                          singleDateFormat.timeZone = TimeZone.getDefault()
                          multiDateFormat.timeZone = TimeZone.getDefault()

                          val start = if (e.startTime.dateTime == null) {
                              multiDateFormat.parse(e.startTime.date) as Date
                          } else {
                              singleDateFormat.parse(e.startTime.dateTime) as Date
                          }

                          val end = if (e.endTime.dateTime == null) {
                              multiDateFormat.parse(e.endTime.date) as Date
                          } else {
                              singleDateFormat.parse(e.endTime.dateTime) as Date
                          }

                          val event = Event()
                          event.startTime = start
                          event.endTime = end
                          if (e.creator.displayName != null)
                              event.creatorName = e.creator.displayName
                          event.creatorEmail = e.creator.email
                          if (e.description != null)
                              event.description = e.description
                          if (e.title != null)
                              event.title = e.title
                          if(e.location != null){
                              event.location = e.location
                          }
                          if(e.recurId != null){
                              event.recurId = e.recurId
                          }
                          //println("event: " + event.id.toString() + "\n" + event.startTime.toString())
                          responseData += event
                          //println("event after add: " + event.id.toString() + "\n" + event.startTime.toString())
                      }
                    }
                    insertEvents(responseData)
                }
                else{
                    Log.e(TAG,"Response is coming back null")
                }
            }
            override fun onFailure(call: Call<GoogleCalendarResponse>, t: Throwable) {
                Log.e(TAG,"Failed to fetch events",t)
            }
        })
    }

    fun getEvents() : LiveData<List<Event>> = eventDao.getAllEvents()
    fun getEvent(id : UUID) : LiveData<Event> = eventDao.getEvent(id)

    private fun insertEvents(responseData: List<Event>) {
        executor.execute{
            eventDao.deleteEvents()
            for (a in responseData){
                insertEvent(a)
            }
        }
    }
    private fun insertEvent(e : Event)
    {
        executor.execute{
            eventDao.insertEvent(e)
        }
    }
    companion object {
        private var INSTANCE : CalendarRepository? = null

        fun initialize(context: Context) {
            INSTANCE = CalendarRepository(context)
        }
        fun get() : CalendarRepository {
            return INSTANCE?: throw IllegalStateException("Calendar Repository must be initialized.")
        }
    }

    // <<<<<<<<<<<<<<< FIREBASE REPOSITORY ACCESS <<<<<<<<<<<<<<<<<<<<<<<

    //GETTING LIST OF USERS


}