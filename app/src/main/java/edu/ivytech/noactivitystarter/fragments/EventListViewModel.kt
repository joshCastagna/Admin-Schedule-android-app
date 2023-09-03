package edu.ivytech.noactivitystarter.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import edu.ivytech.noactivitystarter.CalendarRepository
import edu.ivytech.noactivitystarter.database.Event

class EventListViewModel : ViewModel() {
    private val eventRepo : CalendarRepository = CalendarRepository.get()
    val eventListLiveData : LiveData<List<Event>> = eventRepo.getEvents()
}