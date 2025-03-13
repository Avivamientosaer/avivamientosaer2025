package com.saeo.fhn_Avivamiento.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "countries")
data class CountryEntity(
    @PrimaryKey val code: String,  // Código del país (ej: "US")
    val name: String               // Nombre del país (ej: "United States")
)