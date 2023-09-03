package edu.ivytech.noactivitystarter.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.*

interface CalendarApi {

    @GET("ivytechopenlabcalendar@gmail.com/events?key=AIzaSyAhjKPQIu3s4JoGBnBasX-iolfkqkxnIRw")
    fun downloadCalendarEvents(@Query("orderBy") order_by : String,
                               @Query("singleEvents") single_events : String,
                               @Query("timeMin") timeMin : String
    ) : Call<GoogleCalendarResponse>
}


//https://www.googleapis.com/calendar/v3/calendars/ivytechopenlabcalendar@gmail.com/events?key=AIzaSyAhjKPQIu3s4JoGBnBasX-iolfkqkxnIRw
//https://www.googleapis.com/calendar/v3/calendars/ivytechopenlabcalendar@gmail.com/events?key=AIzaSyAhjKPQIu3s4JoGBnBasX-iolfkqkxnIRw