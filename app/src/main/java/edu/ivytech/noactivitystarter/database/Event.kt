package edu.ivytech.noactivitystarter.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Array.set
import java.util.*
import kotlin.collections.ArrayList

@Entity
data class Event(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var creatorName : String = "",
    var creatorEmail : String = "",
    var title: String = "",
    var startTime: Date = Date(),
    var endTime: Date = Date(),
    var description: String = "",
    var location : String? = "",
    var recurId : String? = "",
    var frequency : Frequency = Frequency()
)


data class Frequency(
    var freq : String = "",
    var wkst : String = "",
    var until : Date= Date(),
    var byday : String = ""
)
