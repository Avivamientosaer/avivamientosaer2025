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

                // ✅ 1. Verificación Inicial de Conexión
                if (!NetworkUtils.isHighQualityConnection(applicationContext)) {
                    Log.d("EventSyncWorker", "Esperando conexión estable...")
                    return@withContext Result.retry()
                }

                // 🗑️ 2. Sincronizar Eventos Eliminados
                if (!NetworkUtils.isHighQualityConnection(applicationContext)) {
                    Log.d("EventSyncWorker", "🚫 Conexión perdida. Reintentando sincronización de eliminados...")
                    return@withContext Result.retry()
                }

                val deletedEvents = eventDao.getDeletedEvents()
                Log.d("EventSyncWorker", "🔄 Procesando ${deletedEvents.size} eventos eliminados...")
                for (event in deletedEvents) {
                    try {
                        Log.d("EventSyncWorker", "🗑️ Eliminando evento ${event.id} de Firestore...")
                        val eventRef = firestore.collection("events").document(event.id)
                        eventRef.delete().await()
                        val snapshot = eventRef.get().await()
                        if (!snapshot.exists()) {
                            eventDao.deleteEvent(event.id)
                            Log.d("EventSyncWorker", "✅ Evento ${event.id} eliminado exitosamente.")
                        } else {
                            Log.w("EventSyncWorker", "⚠️ Evento ${event.id} aún existe en Firestore.")
                        }
                    } catch (e: Exception) {
                        Log.e("EventSyncWorker", "❌ Error al eliminar evento ${event.id}: ${e.message}")
                    }
                }

                // 🧹 3. Limpiar Eventos Eliminados Permanentemente
                try {
                    eventDao.deletePermanentlyDeletedEvents()
                    Log.d("EventSyncWorker", "Eventos eliminados permanentemente limpiados.")
                } catch (e: Exception) {
                    Log.e("EventSyncWorker", "Error al limpiar eventos eliminados: ${e.message}")
                }

                // ⏳ 4. Procesar Eliminaciones Pendientes
                val pendingDeletions = eventDao.getPendingDeletions()
                Log.d("EventSyncWorker", "Eliminaciones pendientes: ${pendingDeletions.size}")
                if (!NetworkUtils.isHighQualityConnection(applicationContext)) {
                    Log.d("EventSyncWorker", "Conexión inestable. Reintentando eliminaciones pendientes más tarde.")
                    return@withContext Result.retry()
                }
                Log.d("EventSyncWorker", "Procesando ${pendingDeletions.size} eliminaciones pendientes...")
                for (deletion in pendingDeletions) {
                    try {
                        Log.d("EventSyncWorker", "Eliminando evento pendiente: ${deletion.eventId}")
                        firestore.collection("events").document(deletion.eventId).delete().await()
                        eventDao.deleteEvent(deletion.eventId)
                        eventDao.deletePendingDeletion(deletion.eventId)
                        Log.d("EventSyncWorker", "Evento ${deletion.eventId} eliminado exitosamente.")
                    } catch (e: Exception) {
                        Log.e("EventSyncWorker", "Error al eliminar evento pendiente ${deletion.eventId}: ${e.message}")
                    }
                }

                // ⬆️ 5. Sincronizar Eventos Nuevos/Editados
                val unsyncedEvents = eventDao.getUnsyncedEvents()
                Log.d("EventSyncWorker", "Eventos no sincronizados: ${unsyncedEvents.size}")
                for (event in unsyncedEvents) {
                    try {
                        val eventRef = firestore.collection("events").document(event.id)
                        eventRef.set(event.toMap()).await()
                        val snapshot = eventRef.get().await()
                        if (snapshot.exists()) {
                            eventDao.markEventAsSynced(event.id)
                            Log.d("EventSyncWorker", "Evento ${event.id} confirmado en Firestore.")
                        } else {
                            Log.w("EventSyncWorker", "Evento ${event.id} no se subió correctamente.")
                        }
                    } catch (e: Exception) {
                        Log.e("EventSyncWorker", "Error al verificar evento ${event.id}: ${e.message}")
                    }
                }

                // ✅ 6. Finalización
                Log.d("EventSyncWorker", "Sincronización de eventos completada exitosamente.")
                Result.success()
            } catch (e: Exception) {
                Log.e("EventSyncWorker", "Error en la sincronización: ${e.localizedMessage}")
                Result.retry()
            }
        }
    }
}


// Función de conversión
fun Event.toMap(): Map<String, Any> {
    return mapOf(
        "eventNumber" to eventNumber,
        "menCount" to menCount,
        "womenCount" to womenCount,
        "youthCount" to youthCount,
        "ministrationCount" to ministrationCount,
        "place" to place,
        "department" to department,
        "phoneNumberUser" to phoneNumberUser,
        "createdAt" to createdAt,
        "isSynced" to isSynced
    )
}