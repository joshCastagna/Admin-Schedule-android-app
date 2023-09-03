package edu.ivytech.noactivitystarter.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities=[Event::class],version = 1)
@TypeConverters(EventTypeConverters::class)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao() : EventDAO
}

