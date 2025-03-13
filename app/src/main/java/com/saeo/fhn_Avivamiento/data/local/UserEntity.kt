package com.saeo.fhn_Avivamiento.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

@Entity(tableName = "users")
@TypeConverters(Converters::class) // ✅ Agregar TypeConverter aquí
data class UserEntity(
    @PrimaryKey val phoneNumber: String,
    val countryCode: String,
    val password: String,
    val names: String,
    val lastName: String,
    val city: String,
    val country: String,
    val chapter: String,
    val gender: String,
    val lastModified: Date = Date(), // Fecha de última modificación
    val synced: Boolean = false // Indica si el usuario ha sido sincronizado
)
