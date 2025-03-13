package com.saeo.fhn_Avivamiento.utils

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Objeto para manejar la lógica relacionada con los países
object CountryUtils {

    // Función para obtener la lista de países desde Firebase
    suspend fun fetchCountries(): List<Pair<String, String>> {
        return try {
            // Inicializar FirebaseFirestore dentro de la función (no como campo estático)
            val firestore = FirebaseFirestore.getInstance()

            // Hacer la solicitud a Firebase
            val querySnapshot = firestore.collection("countries").get().await()

            // Mapear los documentos a una lista de pares (código, nombre)
            querySnapshot.documents.map { document ->
                val code = document["code"] as? String ?: ""
                val name = document["name"] as? String ?: ""
                code to name
            }.filter { (code, name) -> code.isNotEmpty() && name.isNotEmpty() } // Filtrar entradas vacías
        } catch (e: Exception) {
            // Manejar errores (por ejemplo, problemas de red o Firebase)
            emptyList() // Devolver una lista vacía en caso de error
        }
    }
}