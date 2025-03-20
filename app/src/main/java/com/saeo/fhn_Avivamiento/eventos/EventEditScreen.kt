package com.saeo.fhn_Avivamiento.eventos

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditScreen(
    event: Event, // Recibimos el evento seleccionado
    onEventUpdated: () -> Unit, // Callback para actualizar la lista de eventos
    onBack: () -> Unit // Callback para regresar a la lista
) {
    var eventNumber by remember { mutableStateOf(event.eventNumber.toString()) }
    var menCount by remember { mutableStateOf(event.menCount.toString()) }
    var womenCount by remember { mutableStateOf(event.womenCount.toString()) }
    var youthCount by remember { mutableStateOf(event.youthCount.toString()) }
    var ministrationCount by remember { mutableStateOf(event.ministrationCount.toString()) }
    var place by remember { mutableStateOf(event.place) }
    var department by remember { mutableStateOf(event.department) }
    var message by remember { mutableStateOf("") }
    var showDateTimePicker by remember { mutableStateOf(false) } // Estado para mostrar el diálogo
    var selectedDateTime by remember { mutableLongStateOf(event.createdAt) } // Fecha y hora seleccionadas

    // Obtener el contexto actual
    val context = LocalContext.current

    // Formatear la fecha y hora para mostrarla en el campo de texto
    val formattedDateTime = remember(selectedDateTime) {
        SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date(selectedDateTime))
    }

    // Manejar el botón de retroceso
    BackHandler {
        onBack() // Navegar a la pantalla anterior dentro de la aplicación
    }


    // Mostrar el diálogo de fecha y hora si showDateTimePicker es true
    if (showDateTimePicker) {
        DateTimePickerDialog(
            initialDate = selectedDateTime,
            onDateTimeSelected = { newDateTime ->
                selectedDateTime = newDateTime
                showDateTimePicker = false
            },
            onDismiss = { showDateTimePicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Editar Evento") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Campo para el número de evento/visita
            OutlinedTextField(
                value = eventNumber,
                onValueChange = { eventNumber = it },
                label = { Text("N° de evento/visita") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            // Campo para la cantidad de hombres y mujeres
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Campo para la cantidad de hombres
                OutlinedTextField(
                    value = menCount,
                    onValueChange = { menCount = it },
                    label = { Text("Cantidad de hombres") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                // Campo para la cantidad de mujeres
                OutlinedTextField(
                    value = womenCount,
                    onValueChange = { womenCount = it },
                    label = { Text("Cantidad de mujeres") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            // Campo para la cantidad de jóvenes y Ministracion
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Campo Jóvenes (50% del ancho)
                OutlinedTextField(
                    value = youthCount,
                    onValueChange = { youthCount = it },
                    label = { Text("Cantidad de Jóvenes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                // Campo Ministración (50% del ancho) - NUEVO
                OutlinedTextField(
                    value = ministrationCount,
                    onValueChange = { ministrationCount = it },
                    label = { Text("Ministraciónes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            // Campo para el departamento o región
            OutlinedTextField(
                value = department,
                onValueChange = { department = it },
                label = { Text("Departamento o región") },
                modifier = Modifier.fillMaxWidth()
            )

            // Campo para la fecha y hora
            OutlinedTextField(
                value = formattedDateTime,
                onValueChange = {}, // Seguimos sin permitir la edición manual directa
                label = { Text("Fecha y Hora") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDateTimePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit, // Asegúrate de importar Icons.Default.Edit
                            contentDescription = "Editar fecha"
                        )
                    }
                }
            )


            // Botón para guardar cambios
            Button(
                onClick = {
                    updateEvent(
                        event.id,
                        eventNumber.toIntOrNull() ?: 0,
                        menCount.toIntOrNull() ?: 0,
                        womenCount.toIntOrNull() ?: 0,
                        youthCount.toIntOrNull() ?: 0,
                        ministrationCount.toIntOrNull() ?: 0,
                        place,
                        department,
                        selectedDateTime, // Pasar la nueva fecha y hora
                        context,
                        onSuccess = {
                            message = "Evento actualizado exitosamente."
                            onEventUpdated()
                        },
                        onFailure = { message = it }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Cambios")
            }

            // Botón para regresar
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Regresar")
            }

            // Mostrar mensaje de feedback
            if (message.isNotEmpty()) {
                Text(message, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun DateTimePickerDialog(
    initialDate: Long,
    onDateTimeSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance().apply { timeInMillis = initialDate } }

    // Diálogo de fecha
    LaunchedEffect(Unit) {
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.set(year, month, day)
                // Después de seleccionar la fecha, mostrar el diálogo de hora
                val timePickerDialog = TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        onDateTimeSelected(calendar.timeInMillis) // Notificar la fecha y hora seleccionadas
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false // <--- Esto cambia a formato de 12h
                )
                timePickerDialog.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
}