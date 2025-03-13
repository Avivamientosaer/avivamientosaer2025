package com.saeo.fhn_Avivamiento.eventos

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.concurrent.futures.await
import androidx.work.BackoffPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.firestore.FirebaseFirestore
import com.saeo.fhn_Avivamiento.data.local.AppDatabase
import com.saeo.fhn_Avivamiento.utils.NetworkUtils
import com.saeo.fhn_Avivamiento.utils.UserPreferences
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    context: Context,
    onNavigateToEventRegistration: () -> Unit,
    onNavigateToReports: () -> Unit,
    onBack: () -> Unit,
    onEditEvent: (Event) -> Unit,
    onDeleteEvent: (String, () -> Unit, () -> Unit) -> Unit
) {
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Obtener el número de teléfono del usuario
    val phoneNumber by UserPreferences.getPhoneNumberWithCountryCode(context).collectAsState(initial = "")
    Log.d("EventListScreen", "Número de teléfono: $phoneNumber")

    // Cargar eventos y sincronizar cuando cambie el número de teléfono
    LaunchedEffect(phoneNumber) {
        coroutineScope.launch {
            try {
                // 1. Cargar eventos locales
                val localEvents = AppDatabase.getDatabase(context)
                    .eventDao()
                    .getEventsByPhoneNumber(phoneNumber)

                Log.d("EventListScreen", "Eventos locales cargados: ${localEvents.size}")
                Log.d("EventListScreen", "Número de teléfono: $phoneNumber")
                Log.d("EventListScreen", "Eventos: $localEvents")

                events = localEvents.sortedByDescending { it.createdAt }
                isLoading = false

                // 2. Sincronizar solo si hay Internet de alta calidad (en segundo plano)
                if (NetworkUtils.isHighQualityConnection(context)) {
                    syncEvents(context) // Sincronizar eventos en segundo plano

                    // 3. Comparar cantidad de eventos en Room y Firestore
                    val firestoreEvents = fetchEventsFromFirestore(phoneNumber)
                    val roomEvents = AppDatabase.getDatabase(context)
                        .eventDao()
                        .getEventsByPhoneNumber(phoneNumber)

                    if (firestoreEvents.size > roomEvents.size) {
                        // Reemplazar eventos en Room con los de Firestore
                        AppDatabase.getDatabase(context).eventDao().deleteEventsByPhoneNumber(phoneNumber)
                        AppDatabase.getDatabase(context).eventDao().insertEvents(firestoreEvents)
                        events = firestoreEvents.sortedByDescending { it.createdAt }
                    } else {
                        // Recargar eventos locales tras sincronizar
                        val updatedLocalEvents = AppDatabase.getDatabase(context)
                            .eventDao()
                            .getEventsByPhoneNumber(phoneNumber)
                            .filter { !it.isDeleted }

                        Log.d("EventListScreen", "Eventos actualizados tras sincronización: ${updatedLocalEvents.size}")
                        Log.d("EventListScreen", "Eventos actualizados: $updatedLocalEvents")

                        events = updatedLocalEvents.sortedByDescending { it.createdAt }
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Eventos Realizados") },
                actions = {
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Cerrar Sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Botones en la parte superior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onNavigateToEventRegistration,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Registrar Nuevo Evento")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onNavigateToReports,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Ver Reportes")
                }
            }

            // Lista de eventos
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (errorMessage != null) {
                Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
            } else if (events.isEmpty()) {
                Text("No hay eventos disponibles.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(events) { event ->
                        EventCard(
                            event = event,
                            onEditEvent = { onEditEvent(event) },
                            onDeleteEvent = { eventId ->
                                onDeleteEvent(
                                    eventId,
                                    {
                                        // 1. Filtrar la lista actual
                                        Log.d("DELETION_UI", "Filtrando evento $eventId de la lista local")
                                        events = events.filter { it.id != eventId }
                                        Log.d("DELETION_UI", "Eventos restantes: ${events.size}")

                                        // 2. Recargar desde Room para garantizar consistencia
                                        Log.d("DELETION_UI", "Recargando eventos desde Room")
                                        coroutineScope.launch {
                                            try {
                                                Log.d("DELETION_ROOM", "Recargando eventos desde Room...")
                                                val updatedEvents = AppDatabase.getDatabase(context)
                                                    .eventDao()
                                                    .getEventsByPhoneNumber(phoneNumber)

                                                Log.d("DELETION_ROOM", "Eventos obtenidos: ${updatedEvents.size}")
                                                Log.d("DELETION_ROOM", "IDs: ${updatedEvents.map { it.id }}")

                                                events = updatedEvents.sortedByDescending { it.createdAt }
                                            } catch (e: Exception) {
                                                Log.e("DELETION_ROOM", "Error al recargar: ${e.message}")
                                            }
                                        }
                                    },
                                    {
                                        Log.e("DELETION_ERROR", "Falló la eliminación en Firestore")
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}


private suspend fun syncEvents(context: Context) {
    try {
        val workManager = WorkManager.getInstance(context)
        val syncWorkRequest = OneTimeWorkRequestBuilder<EventSyncWorker>()
            .setBackoffCriteria( // 🛡️ Reintentar hasta 3 veces
                BackoffPolicy.EXPONENTIAL,
                10,
                TimeUnit.MINUTES
            )
            .build()
        workManager.enqueue(syncWorkRequest).result.await()
    } catch (e: Exception) {
        Log.e("Sync", "Error grave: ${e.message}. Reintentando en próxima apertura.")
    }
}

private suspend fun fetchEventsFromFirestore(phoneNumber: String): List<Event> {
    return try {
        val firestore = FirebaseFirestore.getInstance()
        val querySnapshot = firestore.collection("events")
            .whereEqualTo("phoneNumberUser", phoneNumber)
            .get()
            .await()

        querySnapshot.documents.mapNotNull { doc ->
            doc.toObject(Event::class.java)?.copy(id = doc.id)
        }
    } catch (e: Exception) {
        Log.e("EventListScreen", "Error al cargar eventos desde Firestore: ${e.localizedMessage}")
        emptyList()
    }
}