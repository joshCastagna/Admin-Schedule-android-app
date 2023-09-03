package edu.ivytech.noactivitystarter.api

import android.content.ClipDescription
import com.google.gson.annotations.SerializedName
import java.util.*

data class GoogleCalendarResponse(var items : List<GCalEvent>)

data class GCalEvent(
    @SerializedName("summary") val title : String,
    @SerializedName("description") val description: String,
    @SerializedName("creator") val creator : GCalCreator,
    @SerializedName("start") val startTime : GCalTime,
    @SerializedName("status") val status : String,
    @SerializedName("end") val endTime : GCalTime,
    @SerializedName("location") val location : String?,
    @SerializedName("recurringEventId") val recurId : String?,
    @SerializedName("recurrence") val freq : List<String>?
    )

data class GCalCreator(
    @SerializedName("email") val email : String,
    @SerializedName("displayName") val displayName : String? = ""
)

data class GCalTime(
    @SerializedName("dateTime") val dateTime : String,
    @SerializedName("date") val date : String? = ""
)