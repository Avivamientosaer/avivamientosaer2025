package com.saeo.fhn_Avivamiento.testimonios.data // Corregir paquete

import androidx.room.*
import java.util.*

@Entity(tableName = "testimonies")
@TypeConverters(TestimonyTypeConverter::class)
data class Testimony(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val countryCode: String = "",
    val phone: String,
    val name: String,
    val chapter: String,
    val city: String,
    val testimonyType: TestimonyType,
    val joinedYear: Int,
    val birthYear: Int,
    val notes: String,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
) {
    // Constructor vacío para Room y Firestore
    constructor() : this(
        id = UUID.randomUUID().toString(),
        phone = "",
        name = "",
        chapter = "",
        city = "",
        testimonyType = TestimonyType.INDIVIDUAL,
        joinedYear = 0,
        birthYear = 0,
        notes = "",
        isSynced = false,
        createdAt = 0L,
        lastModified = 0L,
        isDeleted = false
    )
}

// Nuevo enum para tipos de testimonio
enum class TestimonyType {
    INDIVIDUAL,
    PAREJA_JUNTOS_e_INDIVIDUALES,
    PAREJA_INDIVIDUALES
}

// Conversor para Room
class TestimonyTypeConverter {
    @TypeConverter
    fun fromType(type: TestimonyType): String = type.name

    @TypeConverter
    fun toType(value: String): TestimonyType = TestimonyType.valueOf(value)
}