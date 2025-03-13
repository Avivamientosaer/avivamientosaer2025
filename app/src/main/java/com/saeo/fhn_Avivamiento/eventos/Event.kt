package com.saeo.fhn_Avivamiento.eventos

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey val id: String, // Ahora el ID será phoneNumberUser_createdAt
    val eventNumber: Int,
    val menCount: Int,
    val womenCount: Int,
    val youthCount: Int,
    val place: String,
    val department: String,
    val phoneNumberUser: String,
    val createdAt: Long, // Nuevo campo para el timestamp

    val isSynced: Boolean, // Nuevo campo para indicar si el evento está sincronizado
    val isDeleted: Boolean = false, // Nuevo campo para indicar si el evento está eliminado
    val country: String
) {
    // Constructor sin argumentos requerido por Firestore
    constructor() : this("", 0, 0, 0, 0, "", "", "", 0L, false, false, "")
}