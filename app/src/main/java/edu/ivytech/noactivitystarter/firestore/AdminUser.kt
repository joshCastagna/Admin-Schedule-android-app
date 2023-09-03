package edu.ivytech.noactivitystarter.firestore

import java.util.*


data class AdminUser(
    val id : String = "",
    val email : String = "",
    var fname : String = "",
    var lname : String = "",
    var bio : String = "",
    var profilePic : String = "",
    var courses: MutableList<String> = mutableListOf()
){
    val photoFileName
        get() = "IMG_$id.jpg"
}

data class Announcement(
    val id: String = UUID.randomUUID().toString(),
    var subject: String = "",
    var body: String = "",
    var timeMade: Date = Date(),
    var timeExpire: Date = Date(),
    var creatorId: String = "",
    var creator: AdminUser? = null
)
