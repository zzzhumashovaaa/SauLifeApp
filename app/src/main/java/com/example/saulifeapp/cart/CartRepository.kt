package com.example.saulifeapp.cart

import com.example.saulifeapp.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun addToCart(product: Product, onResult: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            onResult(false)
            return
        }

        val productId = product.name.lowercase().replace(" ", "_")

        val cartRef = db.collection("users")
            .document(userId)
            .collection("cart")
            .document(productId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(cartRef)

            if (snapshot.exists()) {
                val currentQuantity = snapshot.getLong("quantity") ?: 1
                transaction.update(cartRef, "quantity", currentQuantity + 1)
            } else {
                val item = hashMapOf(
                    "id" to productId,
                    "name" to product.name,
                    "description" to "",
                    "price" to product.price,
                    "quantity" to 1
                )
                transaction.set(cartRef, item)
            }
        }.addOnSuccessListener {
            onResult(true)
        }.addOnFailureListener {
            onResult(false)
        }
    }
}