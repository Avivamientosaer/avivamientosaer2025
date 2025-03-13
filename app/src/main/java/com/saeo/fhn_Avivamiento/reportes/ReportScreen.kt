package com.saeo.fhn_Avivamiento.reportes

import android.icu.util.Calendar
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.saeo.fhn_Avivamiento.eventos.Event
import com.saeo.fhn_Avivamiento.utils.UserPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    phoneNumberUser: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Obtener el número de teléfono con código de país
    val phoneNumberWithCountryCode by UserPreferences.getPhoneNumberWithCountryCode(context)
        .collectAsState(initial = "")
    Log.d("ReportScreen", "Número de teléfono con código de país: $phoneNumberWithCountryCode")

    // Obtener eventos del usuario
    LaunchedEffect(phoneNumberWithCountryCode) {
        if (phoneNumberWithCountryCode.isNotEmpty()) {
            events = getEventsForUser(context, phoneNumberWithCountryCode)
            Log.d("ReportScreen", "Eventos cargados: ${events.size}")
            isLoading = false
        }
    }

    // Definir rangos de tiempo
    val currentTime = System.currentTimeMillis()
    val startOfDay = getStartOfDay(currentTime)
    val startOfAfternoon = getStartOfAfternoon(currentTime)
    val endOfDay = getEndOfDay(currentTime)

    // Filtrar eventos por rangos de tiempo
    val morningEvents = filterEventsByTimeRange(events, startOfDay, startOfAfternoon - 1)
    val afternoonEvents = filterEventsByTimeRange(events, startOfAfternoon, endOfDay)
    val todayEvents = filterEventsByTimeRange(events, startOfDay, endOfDay)
    val allEvents = events

    // Calcular estadísticas
    val morningStats = calculateStatistics(morningEvents)
    val afternoonStats = calculateStatistics(afternoonEvents)
    val todayStats = calculateStatistics(todayEvents)
    val allTimeStats = calculateStatistics(allEvents)

    // Manejar el botón de retroceso
    BackHandler {
        onBack() // Navegar a la pantalla anterior dentro de la aplicación
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Reportes Personales",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.Blue,
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Bold,
                            fontSize = MaterialTheme.typography.headlineMedium.fontSize * 1.1f
                        )
                    )
                },
                actions = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.height(56.dp), // Altura de la BottomAppBar
                content = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Próximamente podremos ver el Consolidado de todos los eventos de las personas que están usando esta aplicación",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Red, // Color gris para un estilo minimalista
                                textAlign = TextAlign.Center // Texto centrado
                            ),
                            maxLines = 2, // Máximo de líneas para evitar desbordamiento
                            overflow = TextOverflow.Ellipsis // Puntos suspensivos si el texto es demasiado largo
                        )
                    }
                }
            )
        }


    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                // Card para la mañana
                StatisticCard(title = "Hoy en la MAÑANA Antes de las 12 PM", stats = morningStats)

                // Card para la tarde
                StatisticCard(
                    title = "Hoy en la TARDE Despues de las 12 PM",
                    stats = afternoonStats
                )

                // Card para el día completo
                StatisticCard(title = "El Día Completo de HOY", stats = todayStats)

                // Card para todos los eventos
                StatisticCard(
                    title = "Todos los Eventos que has Hecho y Grabado",
                    stats = allTimeStats
                )
            }
        }
    }
}

@Composable
fun StatisticCard(title: String, stats: ReportData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            // Título de la tarjeta
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                textDecoration = TextDecoration.Underline

            )
            Spacer(modifier = Modifier.height(8.dp))

            // Eventos y Hombres en la misma línea
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Eventos: ",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                )
                Text(
                    text = "${stats.totalEvents}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2),
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.5f // 50% más grande
                    )
                )
                Spacer(modifier = Modifier.width(20.dp)) // Espacio entre Eventos y Hombres
                Text(
                    text = "Hombres: ",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                )
                Text(
                    text = "${stats.totalMen}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2),
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.5f // 50% más grande
                    )
                )
            }

            // Mujeres y Jóvenes en la misma línea
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Mujeres: ",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                )
                Text(
                    text = "${stats.totalWomen}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2),
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.5f // 50% más grande
                    )
                )
                Spacer(modifier = Modifier.width(20.dp)) // Espacio entre Mujeres y Jóvenes
                Text(
                    text = "Jóvenes: ",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                )
                Text(
                    text = "${stats.totalYouth}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2),
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.5f // 50% más grande
                    )
                )
            }

            // Lugares únicos
            if (stats.places.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Realizados en: ",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                    )
                    Text(
                        text = stats.places.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF1976D2),
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.2f // 20% más grande
                        )
                    )
                }
            }
        }
    }
}

// Funciones auxiliares para calcular rangos de tiempo
fun getStartOfDay(time: Long): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = time }
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

fun getStartOfAfternoon(time: Long): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = time }
    calendar.set(Calendar.HOUR_OF_DAY, 12)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

fun getEndOfDay(time: Long): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = time }
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.timeInMillis
}