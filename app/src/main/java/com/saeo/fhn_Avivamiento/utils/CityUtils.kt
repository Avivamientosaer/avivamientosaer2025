package com.saeo.fhn_Avivamiento.utils

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Data class con name y country
data class CityItem(
    val name: String,      // Internamente llamamos "name"
    val country: String
)

object CityUtils {

    suspend fun fetchCities(): List<CityItem> {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("cities").get().await()

            snapshot.documents.mapNotNull { doc ->
                // Leer "city_name" y "country" de cada doc
                val cityName = doc.getString("city_name") ?: return@mapNotNull null
                val countryName = doc.getString("country") ?: return@mapNotNull null

                CityItem(name = cityName, country = countryName)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addCityIfNotExistsWithCountry(cityName: String, countryName: String) {
        val db = FirebaseFirestore.getInstance()
        val docId = "$cityName $countryName"

        val docRef = db.collection("cities").document(docId)
        val snapshot = docRef.get().await()

        if (!snapshot.exists()) {
            // Guardar "city_name" en vez de "name"
            val data = mapOf(
                "city_name" to cityName,
                "country" to countryName
            )
            docRef.set(data).await()
        }
    }
}
