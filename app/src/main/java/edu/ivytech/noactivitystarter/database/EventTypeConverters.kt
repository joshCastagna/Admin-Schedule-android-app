package edu.ivytech.noactivitystarter.database

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.*

class EventTypeConverters {

    @TypeConverter
    fun fromDate(date : Date?) : Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(dateLong : Long?) : Date? {
       val date = dateLong?.let { Date(it) }
        return date
    }

    @TypeConverter
    fun fromUUID(id : UUID) : String {
        return id.toString()
    }

    @TypeConverter
    fun toUUID(id : String?) : UUID? {
        return UUID.fromString(id)
    }

    @TypeConverter
    fun fromFrequency(freq : Frequency) : String {
        var stringFrequency = ""
        stringFrequency += "FREQ="
        stringFrequency += freq.freq
        stringFrequency += ";WKST="
        stringFrequency += freq.wkst
        stringFrequency += ";UNTIL="
        stringFrequency += freq.until.time
        stringFrequency += ";BYDAY="
        stringFrequency += freq.byday

        return stringFrequency
    }

    @TypeConverter
    fun toFrequency(str : String ) : Frequency {
        //FREQ=WEEKLY;WKST=SU;UNTIL=20200512T035959Z;BYDAY=MO
        var frequency : Frequency = Frequency()
        val x = str.split(";","=")
        for(i in x.indices){
            if(x[i] == "FREQ"){
                frequency.freq = x[i+1]
            }
            if(x[i] == "WKST"){
                frequency.wkst = x[i+1]
            }
            if(x[i] == "UNTIL")
            {
                frequency.until.time = x[i+1].toLong()
            }
            if(x[i] == "BYDAY"){
               frequency.wkst = x[i+1]
            }
        }
        return frequency
    }
}