package com.saeo.fhn_Avivamiento.eventos

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.saeo.fhn_Avivamiento.data.local.AppDatabase
import com.saeo.fhn_Avivamiento.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EventSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val eventDao = db.eventDao()
                val firestore = FirebaseFirestore.getInstance()

                // ✅ Permitir sincronización con datos móviles/WiFi
                if (!NetworkUtils.isHighQualityConnection(applicationContext)) {
                    Log.d("EventSyncWorker", "Esperando conexión estable...")
                    return@withContext Result.retry()
                }

                // 1. Sincronizar eventos no sincronizados
                val unsyncedEvents = eventDao.getUnsyncedEvents()
                Log.d("EventSyncWorker", "Eventos no sincronizados: ${unsyncedEvents.size}")

                for (event in unsyncedEvents) {
                    try {
                        val eventRef = firestore.collection("events").document(event.id)
                        eventRef.set(event.toMap()).await()

                        // 🔄 Verificar si realmente existe en Firestore
                        val snapshot = eventRef.get().await()
                        if (snapshot.exists()) {
                            eventDao.markEventAsSynced(event.id) // ✅ Solo si existe
                            Log.d("EventSyncWorker", "Evento ${event.id} confirmado en Firestore.")
                        } else {
                            Log.w("EventSyncWorker", "Evento ${event.id} no se subió correctamente.")
                        }
                    } catch (e: Exception) {
                        Log.e("EventSyncWorker", "Error al verificar evento ${event.id}: ${e.message}")
                    }
                }

                // 2. Sincronizar eventos eliminados
                val deletedEvents = eventDao.getDeletedEvents()
                Log.d("EventSyncWorker", "Eventos eliminados: ${deletedEvents.size}")

                for (event in deletedEvents) {
                    try {
                        val eventRef = firestore.collection("events").document(event.id)
                        eventRef.delete().await()

                        // 🔄 Verificar si fue eliminado de Firestore
                        val snapshot = eventRef.get().await()
                        if (!snapshot.exists()) {
                            eventDao.deleteEvent(event.id) // ✅ Solo si ya no existe
                            Log.d("EventSyncWorker", "Evento ${event.id} eliminado correctamente.")
                        } else {
                            Log.w("EventSyncWorker", "Evento ${event.id} aún existe en Firestore.")
                        }
                    } catch (e: Exception) {
                        Log.e("EventSyncWorker", "Error al verificar eliminación: ${e.message}")
                    }
                }

                // 3. Limpiar eventos eliminados permanentemente
                eventDao.deletePermanentlyDeletedEvents()

                // 4. Sincronizar eliminaciones pendientes
                val pendingDeletions = eventDao.getPendingDeletions()
                Log.d("EventSyncWorker", "Eliminaciones pendientes: ${pendingDeletions.size}")

                // 🔄 Eliminar eventos de Firestore si existen en la cola de eliminaciones pendientes
                for (deletion in pendingDeletions) {
                    try {
                        firestore.collection("events").document(deletion.eventId).delete().await() // Eliminar de Firestore
                        eventDao.deleteEvent(deletion.eventId) // Eliminar de Room
                        eventDao.deletePendingDeletion(deletion.eventId) // Eliminar de la cola
                    } catch (_: Exception) { /* ... */ }
                }


                Log.d("EventSyncWorker", "Sincronización de eventos completada exitosamente.")
                Result.success()
            } catch (e: Exception) {
                Log.e("EventSyncWorker", "Error en la sincronización: ${e.localizedMessage}")
                Result.retry()
            }
        }
    }


}


// Función de conversión (sin cambios)
fun Event.toMap(): Map<String, Any> {
    return mapOf(
        "eventNumber" to eventNumber,
        "menCount" to menCount,
        "womenCount" to womenCount,
        "youthCount" to youthCount,
        "place" to place,
        "department" to department,
        "phoneNumberUser" to phoneNumberUser,
        "createdAt" to createdAt,
        "isSynced" to isSynced
    )
}