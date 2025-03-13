package com.saeo.fhn_Avivamiento.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.saeo.fhn_Avivamiento.eventos.Event

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<Event>)

    @Query("SELECT * FROM events WHERE phoneNumberUser = :phoneNumberUser AND isDeleted = 0")
    suspend fun getEventsByPhoneNumber(phoneNumberUser: String): List<Event>

    @Query("SELECT * FROM events WHERE id = :eventId LIMIT 1")
    suspend fun getEventById(eventId: String): Event?

    @Query("SELECT * FROM events WHERE isSynced = 0") // Obtener eventos no sincronizados
    suspend fun getUnsyncedEvents(): List<Event>

    @Query("UPDATE events SET isSynced = 1 WHERE id = :eventId") // Marcar evento como sincronizado
    suspend fun markEventAsSynced(eventId: String)

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: String)

    @Query("SELECT * FROM events WHERE isDeleted = 1")
    suspend fun getDeletedEvents(): List<Event>

    @Query("DELETE FROM events WHERE isDeleted = 1")
    suspend fun deletePermanentlyDeletedEvents()

    @Query("DELETE FROM events WHERE phoneNumberUser = :phoneNumberUser")
    suspend fun deleteEventsByPhoneNumber(phoneNumberUser: String)

    // Obtener todos los lugares únicos registrados (sin restricción de fecha)
    @Query("SELECT DISTINCT place FROM events")
    suspend fun getAllPlaces(): List<String>

    // Obtener todos los departamentos únicos registrados (sin restricción de fecha)
    @Query("SELECT DISTINCT department FROM events")
    suspend fun getAllDepartments(): List<String>

    // Obtener el último evento registrado hoy
    @Query("SELECT * FROM events WHERE createdAt >= :startOfDay ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastEventForToday(startOfDay: Long): Event?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingDeletion(deletion: PendingDeletion)

    @Query("DELETE FROM pending_deletions WHERE eventId = :eventId")
    suspend fun deletePendingDeletion(eventId: String)

    @Query("SELECT * FROM pending_deletions")
    suspend fun getPendingDeletions(): List<PendingDeletion>
}