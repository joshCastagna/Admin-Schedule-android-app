package edu.ivytech.noactivitystarter.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.ivytech.noactivitystarter.CalendarRepository
import edu.ivytech.noactivitystarter.database.Event
import java.util.*

class EventDetailViewModel : ViewModel() {

    private val repo = CalendarRepository.get()
    private val eventIdLiveData = MutableLiveData<UUID>()
    var eventLiveDate : LiveData<Event> = Transformations
        .switchMap(eventIdLiveData){
        id -> repo.getEvent(id)
    }

    fun loadEvent(id : UUID){
        eventIdLiveData.value = id
    }

}