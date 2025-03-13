package com.saeo.fhn_Avivamiento.data.local

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import java.util.Date

class Converters {
    // Convertir de Long a Date
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    // Convertir de Date a Long
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // Convertir de Long a Timestamp
    @TypeConverter
    fun fromFirebaseTimestamp(value: Long?): Timestamp? {
        return value?.let { Timestamp(it / 1000, ((it % 1000) * 1_000_000).toInt()) }
    }

    // Convertir de Timestamp a Long
    @TypeConverter
    fun timestampToFirebase(timestamp: Timestamp?): Long? {
        return timestamp?.toDate()?.time
    }
}