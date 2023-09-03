package edu.ivytech.noactivitystarter

import android.app.Application

class AdminApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        CalendarRepository.initialize(this)
        CalendarRepository.get().fetchEvents()




    }
}