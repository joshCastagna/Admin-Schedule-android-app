package edu.ivytech.noactivitystarter.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.util.*

@Dao
interface EventDAO {

    @Query("select * from event")
    fun getAllEvents() : LiveData<List<Event>>

    @Query("select * from event where id = (:eventId)")
    fun getEvent(eventId : UUID) : LiveData<Event>

    @Query("delete from Event")
    fun deleteEvents()

    @Insert
    fun insertEvent(event : Event)
}