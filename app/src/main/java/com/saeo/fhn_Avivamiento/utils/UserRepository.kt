package com.saeo.fhn_Avivamiento.utils

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.saeo.fhn_Avivamiento.data.local.AppDatabase
import com.saeo.fhn_Avivamiento.data.local.UserEntity

object UserRepository {

    suspend fun saveUserLocally(context: Context, user: UserEntity) {
        val db = AppDatabase.getDatabase(context)
        db.userDao().insertUser(user)
    }

    suspend fun getUserFromLocal(context: Context, phoneNumber: String): UserEntity? {
        return try {
            val db = AppDatabase.getDatabase(context)
            db.userDao().getUserByPhone(phoneNumber)
        } catch (e: Exception) {
            Log.e("Database", "Error al obtener usuario de Room: ${e.localizedMessage}")
            null
        }
    }


    // Nueva función para autenticar offline
    suspend fun authenticateOffline(context: Context, phoneNumber: String, password: String): Boolean {
        val db = AppDatabase.getDatabase(context)
        val user = db.userDao().verifyCredentials(phoneNumber, password)
        return user != null
    }



    // Función existente para registro
    fun signUpUser(
        context: Context,
        phoneNumber: String,
        password: String,
        names: String,
        lastName: String,
        city: String,
        country: String,
        chapter: String,
        gender: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val documentId = "${names.trim().lowercase()}$phoneNumber"

        db.collection("users").document(documentId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onFailure("Este usuario ya está registrado. Intenta iniciar sesión.")
                } else {
                    val userData = hashMapOf(
                        "phoneNumber" to phoneNumber,
                        "password" to password,
                        "names" to names,
                        "lastName" to lastName,
                        "city" to city,
                        "country" to country,
                        "chapter" to chapter,
                        "gender" to gender,
                        "dateCreated" to Timestamp.now()
                    )

                    db.collection("users").document(documentId)
                        .set(userData)
                        .addOnSuccessListener {
                            val userEntity = UserEntity(
                                phoneNumber = phoneNumber,
                                countryCode = country,
                                password = password,
                                names = names,
                                lastName = lastName,
                                city = city,
                                country = country,
                                chapter = chapter,
                                gender = gender
                            )
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            onFailure("Error al guardar usuario: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                onFailure("Error al verificar cuenta en Firestore: ${e.localizedMessage}")
            }
    }
}