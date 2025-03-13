package com.saeo.fhn_Avivamiento.eventos

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.saeo.fhn_Avivamiento.data.local.AppDatabase
import com.saeo.fhn_Avivamiento.data.local.PendingDeletion
import com.saeo.fhn_Avivamiento.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun deleteEvent(
    eventId: String,
    context: Context,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val roomDb = AppDatabase.getDatabase(context)

    // 1. Marcar como eliminado y agregar a cola de eliminaciones
    CoroutineScope(Dispatchers.IO).launch {
        val event = roomDb.eventDao().getEventById(eventId)
        event?.let {
            Log.d("DELETION_LOGIC", "Marcando evento ${event.id} como isDeleted = 1")
            roomDb.eventDao().insertEvent(it.copy(isDeleted = true))
            roomDb.eventDao().insertPendingDeletion(PendingDeletion(eventId))
            Log.d("DELETION_LOGIC", "Evento ${event.id} marcado y agregado a cola")
        } ?: run {
            Log.e("DELETION_LOGIC", "Evento $eventId no encontrado en Room")
        }
    }

    // 2. Intentar eliminar de Firestore si hay conexión
    if (NetworkUtils.isInternetAvailable(context)) {
        db.collection("events").document(eventId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                // 3. Si falla, guardar en cola de eliminaciones pendientes
                CoroutineScope(Dispatchers.IO).launch {
                    roomDb.eventDao().insertPendingDeletion(PendingDeletion(eventId)) // 🛠️ Corrección aquí
                }
                onFailure("Error al eliminar. Se reintentará automáticamente.")
            }
    } else {
        // 4. Sin conexión: guardar en cola
        CoroutineScope(Dispatchers.IO).launch {
            roomDb.eventDao().insertPendingDeletion(PendingDeletion(eventId)) // 🛠️ Corrección aquí
        }
        onSuccess()
    }
}