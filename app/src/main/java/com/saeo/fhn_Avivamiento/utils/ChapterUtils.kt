package com.saeo.fhn_Avivamiento.utils

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Data class con (name, city, country)
data class ChapterItem(
    val name: String,
    val city: String,
    val country: String
)

object ChapterUtils {

    // 1) Traer todos los capítulos
    suspend fun fetchChapters(): List<ChapterItem> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("chapters").get().await()

            snapshot.documents.mapNotNull { doc ->
                val chapterName = doc.getString("chapter_name") ?: return@mapNotNull null
                val cityName = doc.getString("city_name") ?: return@mapNotNull null
                val countryName = doc.getString("country_name") ?: return@mapNotNull null

                ChapterItem(
                    name = chapterName,
                    city = cityName,
                    country = countryName
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 2) Crear un capítulo si no existe en "chapters" con ID = "chapter_name city_name country_name"
    suspend fun addChapterIfNotExists(chapterName: String, cityName: String, countryName: String) {
        val db = FirebaseFirestore.getInstance()
        val docId = "$chapterName $cityName $countryName"  // ID => "Capítulo Ciudad País"

        val docRef = db.collection("chapters").document(docId)
        val snapshot = docRef.get().await()

        if (!snapshot.exists()) {
            val data = mapOf(
                "chapter_name" to chapterName,
                "city_name" to cityName,
                "country_name" to countryName
            )
            docRef.set(data).await()
        }
    }
}
