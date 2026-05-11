package com.example.saulifeapp.ui.profile

import com.google.firebase.firestore.PropertyName

data class UserProfile(
    var uid: String = "",
    var fullName: String = "",
    var email: String = "",
    var city: String = "",
    var age: Int = 0,
    var gender: String = "",

    @get:PropertyName("isPregnant")
    @set:PropertyName("isPregnant")
    var isPregnant: Boolean? = null,

    var allergies: String = "",
    var chronicDiseases: String = "",
    var currentMedications: String = "",
    var profileCompleted: Boolean = false
)