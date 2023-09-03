package edu.ivytech.noactivitystarter.firestore

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.collections.HashMap

object FirestoreUtil {
    val userCollection = "users"
    val messageCollection = "announcements"

// <<<<<<<< ANNOUNCEMENTS <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    fun addAnnouncement(txt : Announcement) : Task<Void> {
        val firestore = FirebaseFirestore.getInstance()
        return firestore.collection(messageCollection)
            .document(txt.id)
            .set(txt, SetOptions.merge())
    }

    fun deleteAnnouncement(txt: Announcement){
     val firestore = FirebaseFirestore.getInstance()
     firestore.collection(messageCollection)
         .document(txt.id)
         .delete()
    }

    fun getAnnouncements() : Task<QuerySnapshot> {
        val firestore = FirebaseFirestore.getInstance()
        return firestore.collection(messageCollection).get()
    }

//<<<<<<<<<<<<<<< USERS <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    fun getCurrentUser() : FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    //TO GET LIST OF ALL ADMIN BIOS
    fun getUsers() : Task<QuerySnapshot> {
        val firestore = FirebaseFirestore.getInstance()
        return firestore.collection(userCollection).get()
    }

    fun registerUser(userInfo : AdminUser) : Task<Void> {
        val firestore = FirebaseFirestore.getInstance()
        return firestore.collection(userCollection)
            .document(getCurrentUser()!!.uid)
            .set(userInfo, SetOptions.merge())
    }

    fun saveUserData(userInfo : AdminUser) : Task<Void> {
        val data = HashMap<String,Any>()//a map of information.
                                        // the string will be the name of the firebase column.
        data["fname"] = userInfo.fname
        data["lname"] = userInfo.lname
        data["bio"] = userInfo.bio
        data["courses"] = userInfo.courses
        data["profilePic"] = userInfo.profilePic

        return FirebaseFirestore.getInstance()
            .collection(userCollection)//'userCollection' is a constant string made that just says "users",
                                       // so they are saved all in the same folder
            .document(userInfo.id)  // this gives the id for the row,
                                    // from whatever object is being documented
            .update(data)
    }

    fun loadUserData() : Task<DocumentSnapshot> {
     var userID = ""
     if(getCurrentUser() != null)
     {
         userID = getCurrentUser()!!.uid
     }
     return FirebaseFirestore.getInstance()
         .collection(userCollection)
         .document(userID)
         .get()
    }

    fun loadOtherUserData(id : String) : Task<DocumentSnapshot> {
        return FirebaseFirestore.getInstance()
            .collection(userCollection)
            .document(id)
            .get()
    }

    fun uploadImg(){

    }

}