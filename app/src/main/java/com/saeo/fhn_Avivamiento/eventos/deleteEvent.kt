package com.saeo.fhn_Avivamiento.eventos

import android.content.Context
import android.util.Log
import com.saeo.fhn_Avivamiento.data.local.AppDatabase
import com.saeo.fhn_Avivamiento.data.local.PendingDeletion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun deleteEvent(
    eventId: String,
    context: Context,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val roomDb = AppDatabase.getDatabase(context)

    CoroutineScope(Dispatchers.IO).launch {
        val event = roomDb.eventDao().getEventById(eventId)
        if (event != null) {
            try {
                // 1. Marcar como eliminado y agregar a pending_deletions
                roomDb.eventDao().insertEvent(event.copy(isDeleted = true))
                roomDb.eventDao().insertPendingDeletion(PendingDeletion(eventId))
                Log.d("DELETION_LOGIC", "Evento ${event.id} eliminado localmente")
                onSuccess() // ✅ Notificar éxito para actualizar UI
            } catch (e: Exception) {
                Log.e("DELETION_LOGIC", "Error al eliminar evento: ${e.message}")
                onFailure("Error interno al eliminar")
            }
        } else {
            Log.e("DELETION_LOGIC", "Evento $eventId no existe en Room")
            onFailure("Evento no encontrado")
        }
    }
}