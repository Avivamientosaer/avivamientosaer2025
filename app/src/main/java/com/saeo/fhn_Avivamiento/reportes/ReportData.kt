package com.saeo.fhn_Avivamiento.reportes

data class ReportData(
    val totalEvents: Int = 0,  // Suma de eventNumber
    val totalMen: Int = 0,     // Suma de menCount
    val totalWomen: Int = 0,   // Suma de womenCount
    val totalYouth: Int = 0 ,   // Suma de youthCount
    val places: Set<String> = emptySet() // Lugares únicos
)