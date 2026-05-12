package com.example.saulifeapp.ui.medicine

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MedicineCartRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun addMedicineToCart(
        medicine: Medicine,
        onResult: (Boolean) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onResult(false)
            return
        }

        val cartData = hashMapOf(
            "id" to medicine.id,
            "name" to medicine.name,
            "type" to medicine.type,
            "dosage" to medicine.dosage,
            "forWhom" to medicine.forWhom,
            "purpose" to medicine.purpose,
            "whenToTake" to medicine.whenToTake,
            "instruction" to medicine.instruction,
            "contraindications" to medicine.contraindications,
            "price" to medicine.price,
            "quantity" to 1,
            "addedAt" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(uid)
            .collection("cart")
            .document(medicine.id.toString())
            .set(cartData)
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }
}