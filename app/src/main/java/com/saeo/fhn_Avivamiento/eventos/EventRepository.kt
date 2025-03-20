package com.saeo.fhn_Avivamiento.eventos

import android.content.Context
import com.saeo.fhn_Avivamiento.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Función para crear un evento (GUARDADO LOCAL SIEMPRE)
fun createEvent(
    eventNumber: Int,
    menCount: Int,
    womenCount: Int,
    youthCount: Int,
    ministrationCount: Int,
    place: String,
    department: String,
    country: String,
    phoneNumberUser: String,
    context: Context,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val createdAt = System.currentTimeMillis()
    val eventId = "${phoneNumberUser}_${createdAt}"

    // Guardar siempre localmente sin verificar conexión
    saveEventLocally(
        eventId = eventId,
        eventNumber = eventNumber,
        menCount = menCount,
        womenCount = womenCount,
        youthCount = youthCount,
        ministrationCount = ministrationCount,
        place = place,
        department = department,
        country = country,
        phoneNumberUser = phoneNumberUser,
        createdAt = createdAt,
        context = context
    )

    // Notificar éxito local
    onSuccess()
    onFailure("Evento guardado localmente. Se sincronizará automáticamente.")
}

// Función auxiliar para guardar el evento localmente
private fun saveEventLocally(
    eventId: String,
    eventNumber: Int,
    menCount: Int,
    womenCount: Int,
    youthCount: Int,
    ministrationCount: Int,
    place: String,
    department: String,
    country: String,
    phoneNumberUser: String,
    createdAt: Long,
    context: Context
) {
    val event = Event(
        id = eventId,
        eventNumber = eventNumber,
        menCount = menCount,
        womenCount = womenCount,
        youthCount = youthCount,
        ministrationCount = ministrationCount,
        place = place,
        department = department,
        country = country,
        phoneNumberUser = phoneNumberUser,
        createdAt = createdAt,
        isSynced = false, // Siempre no sincronizado al guardar
        isDeleted = false
    )

    CoroutineScope(Dispatchers.IO).launch {
        val db = AppDatabase.getDatabase(context)
        db.eventDao().insertEvent(event)
    }
}