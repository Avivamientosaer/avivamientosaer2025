package com.saeo.fhn_Avivamiento.reportes

import android.content.Context
import com.saeo.fhn_Avivamiento.data.local.AppDatabase
import com.saeo.fhn_Avivamiento.eventos.Event

// Función para obtener eventos del usuario desde Room
suspend fun getEventsForUser(context: Context, phoneNumber: String): List<Event> {
    val db = AppDatabase.getDatabase(context)
    return db.eventDao().getEventsByPhoneNumber(phoneNumber)
}

// Función para filtrar eventos por rango de tiempo
fun filterEventsByTimeRange(events: List<Event>, startTime: Long, endTime: Long): List<Event> {
    return events.filter { it.createdAt in startTime..endTime }
}

// Función para calcular estadísticas (suma de eventos, hombres, mujeres y jóvenes)
fun calculateStatistics(events: List<Event>): ReportData {
    return ReportData(
        totalEvents = events.sumOf { it.eventNumber }, // Suma de eventNumber
        totalMen = events.sumOf { it.menCount },       // Suma de menCount
        totalWomen = events.sumOf { it.womenCount },   // Suma de womenCount
        totalYouth = events.sumOf { it.youthCount },   // Suma de youthCount
        totalMinistrations = events.sumOf { it.ministrationCount },  // Suma de ministrationCount
        places = events.map { it.place }.toSet()       // Lugares únicos
    )
}