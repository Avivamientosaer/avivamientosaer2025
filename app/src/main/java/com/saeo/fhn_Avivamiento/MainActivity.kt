package com.saeo.fhn_Avivamiento

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.saeo.fhn_Avivamiento.eventos.*
import com.saeo.fhn_Avivamiento.reportes.*
import androidx.work.*
import com.saeo.fhn_Avivamiento.utils.UserSyncWorker
import java.util.concurrent.TimeUnit
import com.saeo.fhn_Avivamiento.utils.ConnectivityObserver
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saeo.fhn_Avivamiento.inicio.LoginScreen
import com.saeo.fhn_Avivamiento.registro_usuario.SignUpScreen
import com.saeo.fhn_Avivamiento.utils.UserPreferences

class MainActivity : ComponentActivity() {

    private lateinit var connectivityObserver: ConnectivityObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar observador de conexión a Internet
        connectivityObserver = ConnectivityObserver(this)

        // Programar sincronización de usuarios con Firestore solo si hay conexión
        val userSyncWorkRequest = PeriodicWorkRequestBuilder<UserSyncWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        ).setBackoffCriteria(
            BackoffPolicy.EXPONENTIAL, // 🎯 Reintentos progresivos
            WorkRequest.MIN_BACKOFF_MILLIS, // ✅ Tiempo mínimo de espera
            TimeUnit.MILLISECONDS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "UserSyncWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            userSyncWorkRequest
        )

        setContent {
            MaterialTheme {
                var currentScreen by remember { mutableStateOf("login") }
                var selectedEvent by remember { mutableStateOf<Event?>(null) }
                val isConnected by connectivityObserver.isConnected.collectAsState(initial = true)
                val phoneNumberUser = UserPreferences.getPhoneNumber(this@MainActivity).collectAsState(initial = "").value ?: ""

                // Si no hay conexión, mostrar alerta
                var showNoConnectionDialog by remember { mutableStateOf(false) }

                // Mostrar la alerta solo una vez cuando no hay conexión
                LaunchedEffect(isConnected) {
                    if (!isConnected) {
                        showNoConnectionDialog = true
                    }
                }

                // NUEVO BLOQUE PARA SINCRONIZACIÓN AUTOMÁTICA
                LaunchedEffect(isConnected) {
                    if (isConnected) {
                        Log.d("MainActivity", "Conexión recuperada - Forzando sincronización...")
                        val syncWorkRequest = OneTimeWorkRequestBuilder<EventSyncWorker>()
                            .build()

                        WorkManager.getInstance(this@MainActivity).enqueue(syncWorkRequest)
                    }
                }

                // Alerta de conexión
                if (showNoConnectionDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showNoConnectionDialog = false
                        }, // Cerrar la alerta al tocar fuera
                        confirmButton = {
                            Button(
                                onClick = { showNoConnectionDialog = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Text("OK", style = MaterialTheme.typography.bodyLarge)
                            }
                        },
                        title = { Text("Sin Conexión a Internet") },
                        text = { Text("No tienes conexión a Internet. La App funcionará en modo Offline hasta que haya señal.\n No te preocupes podras simpre guardar tus datos e informacionn localmente. \n La App se encargará de Actualizarlos con el servidor en cuanto tengas conexión a Internet Nuevamente .") }
                    )
                }

                Log.d("MainActivity", "Cambiando a pantalla: $currentScreen")
                when (currentScreen) {
                    "login" -> {
                        LoginScreen(
                            context = this@MainActivity,
                            onLoginSuccess = {
                                Log.d(
                                    "MainActivity",
                                    "Inicio de sesión exitoso. Navegando a eventList"
                                )
                                currentScreen = "eventList"
                            },
                            onNavigateToSignUp = {
                                Log.d("MainActivity", "Navegando a SignUpScreen")
                                currentScreen = "signup"
                            }
                        )
                    }

                    "signup" -> {
                        SignUpScreen(
                            onSignUpSuccess = {
                                Log.d("MainActivity", "Registro exitoso. Regresando a login")
                                currentScreen = "login"
                            }
                        )
                    }

                    "eventList" -> {
                        Log.d("MainActivity", "Cargando EventListScreen...")
                        EventListScreen(
                            context = this@MainActivity,
                            onNavigateToEventRegistration = {
                                Log.d("MainActivity", "Navegando a eventRegistration")
                                currentScreen = "eventRegistration"
                            },
                            onNavigateToReports = {
                                Log.d("MainActivity", "Navegando a reportScreen")
                                currentScreen = "reportScreen"
                            },
                            onBack = {
                                Log.d("MainActivity", "Volviendo a inicio desde eventList")
                                currentScreen = "login"
                            },
                            onEditEvent = { event ->
                                selectedEvent = event
                                Log.d("MainActivity", "Editando evento: $event")
                                currentScreen = "eventEdit"
                            },
                            onDeleteEvent = { eventId, onSuccess, onFailure ->
                                deleteEvent(
                                    eventId = eventId,
                                    context = this@MainActivity, // Pasar el contexto aquí
                                    onSuccess = {
                                        onSuccess() // Llamar a onSuccess proporcionado por EventListScreen
                                    },
                                    onFailure = { errorMessage ->
                                        onFailure() // Llamar a onFailure proporcionado por EventListScreen
                                        Log.e("MainActivity", "Error al eliminar evento: $errorMessage")
                                        // Mostrar un mensaje de error al usuario si es necesario
                                    }
                                )
                            }
                        )
                    }

                    "eventRegistration" -> {
                        EventRegistrationScreen(
                            context = this@MainActivity, // Pasar el contexto aquí
                            onEventRegistered = {
                                Log.d("MainActivity", "Evento registrado exitosamente")
                                currentScreen = "eventList"
                            },
                            onBack = {
                                Log.d("MainActivity", "Volviendo a eventList desde eventRegistration")
                                currentScreen = "eventList"
                            }
                        )
                    }

                    "eventEdit" -> {
                        if (selectedEvent != null) {
                            Log.d(
                                "MainActivity",
                                "Cargando pantalla de edición para evento: $selectedEvent"
                            )
                            EventEditScreen(
                                event = selectedEvent!!,
                                onEventUpdated = {
                                    Log.d("MainActivity", "Evento actualizado: $selectedEvent")
                                    selectedEvent = null
                                    currentScreen = "eventList"
                                },
                                onBack = {
                                    Log.d("MainActivity", "Volviendo a eventList desde eventEdit")
                                    selectedEvent = null
                                    currentScreen = "eventList"
                                }
                            )
                        } else {
                            Log.e("MainActivity", "Error: selectedEvent es null")
                            currentScreen = "eventList" // Volver a la lista en caso de error
                        }
                    }

                    "reportScreen" -> {
                        ReportScreen(
                            phoneNumberUser = phoneNumberUser, // 📌 Ahora se pasa el número de usuario real
                            onBack = {
                                Log.d("MainActivity", "Volviendo a eventList desde reportScreen")
                                currentScreen = "eventList"
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityObserver.unregister()
        Log.d("MainActivity", "MainActivity destruida")
    }
}