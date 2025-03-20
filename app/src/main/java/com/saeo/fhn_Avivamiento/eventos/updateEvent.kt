package com.saeo.fhn_Avivamiento.eventos

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.saeo.fhn_Avivamiento.data.local.AppDatabase

fun updateEvent(
    eventId: String,
    eventNumber: Int,
    menCount: Int,
    womenCount: Int,
    youthCount: Int,
    ministrationCount: Int,
    place: String,
    department: String,
    createdAt: Long,
    context: Context,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        val roomDb = AppDatabase.getDatabase(context)
        val event = roomDb.eventDao().getEventById(eventId)

        if (event != null) {
            // 1. Crear copia actualizada del evento con isSynced = false
            val updatedEvent = event.copy(
                eventNumber = eventNumber,
                menCount = menCount,
                womenCount = womenCount,
                youthCount = youthCount,
                ministrationCount = ministrationCount,
                place = place,
                department = department,
                createdAt = createdAt,
                isSynced = false // Siempre no sincronizado al guardar
            )

            // 2. Guardar en Room
            roomDb.eventDao().insertEvent(updatedEvent)

            // 3. Notificar éxito para actualizar UI
            onSuccess()
        } else {
            // 4. Manejar error si el evento no existe
            onFailure("El evento no se encontró en la base de datos local")
        }
    }
}