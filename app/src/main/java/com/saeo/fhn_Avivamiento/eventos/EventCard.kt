package com.saeo.fhn_Avivamiento.eventos

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.outlined.Delete


@Composable
fun EventCard(
    event: Event,
    onEditEvent: () -> Unit,
    onDeleteEvent: (String) -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) } // Estado para mostrar el diálogo

    // Diálogo de confirmación
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Eliminar Evento") },
            text = { Text("¿Estás seguro de que deseas eliminar este evento?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeleteEvent(event.id) // Llamar a la función de eliminación
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Línea de cantidad de eventos y fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cantidad de eventos
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2),
                                fontSize = 20.sp
                            )
                        ) {
                            append("${event.eventNumber}")
                        }
                        append(" Eventos Realizados")
                    },
                    style = MaterialTheme.typography.titleMedium
                )

                // Fecha (sin el título "Fecha")
                Text(
                    text = event.createdAt.toFormattedDate(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Línea de cantidades (hombres, mujeres, jóvenes)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2),
                                fontSize = 20.sp
                            )
                        ) {
                            append("${event.menCount}")
                        }
                        append(" hombres")
                    }
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2),
                                fontSize = 20.sp
                            )
                        ) {
                            append("${event.womenCount}")
                        }
                        append(" mujeres")
                    }
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2),
                                fontSize = 20.sp
                            )
                        ) {
                            append("${event.youthCount}")
                        }
                        append(" jóvenes")
                    }
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Línea de lugar
            Text("Lugar: ${event.place}")

            // Botones de editar y eliminar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onEditEvent) {
                    Text("Editar")
                }
                Button(
                    onClick = { showDeleteConfirmation = true }, // Mostrar el diálogo de confirmación
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Eliminar")
                }
            }
            // Icono de sincronización para eliminaciones pendientes (NUEVO)
            if (event.isDeleted) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Eliminación pendiente",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 4.dp)
                    )
                }
            }
        }
    }
}



// Función de extensión para formatear la fecha
fun Long.toFormattedDate(): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
    return dateFormat.format(Date(this))
}