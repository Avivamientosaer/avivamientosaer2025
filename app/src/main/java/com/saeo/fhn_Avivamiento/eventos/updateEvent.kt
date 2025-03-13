package com.saeo.fhn_Avivamiento.eventos

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.saeo.fhn_Avivamiento.data.local.AppDatabase
import com.saeo.fhn_Avivamiento.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun updateEvent(
    eventId: String,
    eventNumber: Int,
    menCount: Int,
    womenCount: Int,
    youthCount: Int,
    place: String,
    department: String,
    createdAt: Long, // Nuevo parámetro para la fecha y hora
    context: Context,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val updatedEventData = mapOf(
        "eventNumber" to eventNumber,
        "menCount" to menCount,
        "womenCount" to womenCount,
        "youthCount" to youthCount,
        "place" to place,
        "department" to department,
        "createdAt" to createdAt // Incluir la nueva fecha y hora
    )

    // Verificar si hay conexión a Internet
    if (NetworkUtils.isInternetAvailable(context)) {
        db.collection("events").document(eventId)
            .update(updatedEventData)
            .addOnSuccessListener {
                // Actualizar también en Room
                CoroutineScope(Dispatchers.IO).launch {
                    val roomDb = AppDatabase.getDatabase(context)
                    val event = roomDb.eventDao().getEventById(eventId)
                    if (event != null) {
                        val updatedLocalEvent = event.copy(
                            eventNumber = eventNumber,
                            menCount = menCount,
                            womenCount = womenCount,
                            youthCount = youthCount,
                            place = place,
                            department = department,
                            createdAt = createdAt, // Actualizar la fecha y hora
                            isSynced = true // Marcar como sincronizado
                        )
                        roomDb.eventDao().insertEvent(updatedLocalEvent)
                    }
                }
                onSuccess()
            }
            .addOnFailureListener { e ->
                // Si falla la conexión, guardar localmente
                saveEventLocally(eventId, eventNumber, menCount, womenCount, youthCount, place, department, createdAt, context)
                onFailure("Error: No se pudo actualizar el evento. Se guardó localmente. ${e.localizedMessage}")
            }
    } else {
        // Si no hay conexión, guardar localmente
        saveEventLocally(eventId, eventNumber, menCount, womenCount, youthCount, place, department, createdAt, context)
        onFailure("No hay conexión a Internet. El evento se guardó localmente.")
    }
}

private fun saveEventLocally(
    eventId: String,
    eventNumber: Int,
    menCount: Int,
    womenCount: Int,
    youthCount: Int,
    place: String,
    department: String,
    createdAt: Long, // Nuevo parámetro para la fecha y hora
    context: Context
) {
    CoroutineScope(Dispatchers.IO).launch {
        val roomDb = AppDatabase.getDatabase(context)
        val event = roomDb.eventDao().getEventById(eventId)
        if (event != null) {
            val updatedLocalEvent = event.copy(
                eventNumber = eventNumber,
                menCount = menCount,
                womenCount = womenCount,
                youthCount = youthCount,
                place = place,
                department = department,
                createdAt = createdAt, // Actualizar la fecha y hora
                isSynced = false // Marcar como no sincronizado
            )
            roomDb.eventDao().insertEvent(updatedLocalEvent)
        }
    }
}