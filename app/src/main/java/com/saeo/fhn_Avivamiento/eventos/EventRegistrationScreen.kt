package com.saeo.fhn_Avivamiento.eventos

import android.content.Context
import android.icu.util.Calendar
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.saeo.fhn_Avivamiento.utils.UserPreferences
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.saeo.fhn_Avivamiento.data.local.AppDatabase
import com.saeo.fhn_Avivamiento.utils.GeocoderUtils
import com.saeo.fhn_Avivamiento.utils.LocationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventRegistrationScreen(
    context: Context,
    onEventRegistered: () -> Unit, // Callback para redirigir a la lista de eventos
    onBack: () -> Unit
) {
    var eventNumber by remember { mutableStateOf("1") } // Valor predeterminado: 1
    var menCount by remember { mutableStateOf("0") }    // Valor predeterminado: 0
    var womenCount by remember { mutableStateOf("0") }  // Valor predeterminado: 0
    var youthCount by remember { mutableStateOf("0") }   // Valor predeterminado: 0
    var place by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Obtener número de teléfono del usuario
    val phoneNumberUser by UserPreferences.getPhoneNumberWithCountryCode(context)
        .collectAsState(initial = "")

    // Arreglo de FocusRequester para manejar el foco entre los campos
    val focusRequesters = remember {
        Array(6) { FocusRequester() }
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Estado para la animación del color del título
    val infiniteTransition = rememberInfiniteTransition()
    val animatedColor by infiniteTransition.animateColor(
        initialValue = Color.Blue,
        targetValue = Color(0xFF6200EA), // Un tono de morado
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Obtener el inicio del día actual
    val startOfDay = remember {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        calendar.timeInMillis
    }

    // Listas de sugerencias
    var places by remember { mutableStateOf(emptyList<String>()) }
    var departments by remember { mutableStateOf(emptyList<String>()) }

    var filteredPlaces by remember { mutableStateOf(emptyList<String>()) }
    var filteredDepartments by remember { mutableStateOf(emptyList<String>()) }

    // Cargar datos al iniciar
    LaunchedEffect(Unit) {
        val db = AppDatabase.getDatabase(context)
        places = db.eventDao().getAllPlaces() // Cargar todos los lugares
        departments = db.eventDao().getAllDepartments() // Cargar todos los departamentos

        // Establecer valores predeterminados del último evento
        db.eventDao().getLastEventForToday(startOfDay)?.let { lastEvent ->
            place = lastEvent.place
            department = lastEvent.department
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Registro de Eventos",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Blue,
                                    Color(0xFF00BCD4)
                                ) // Gradiente azul a turquesa
                            ),
                            shadow = Shadow(
                                color = Color.Gray,
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        ),
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .clickable { /* Puedes agregar una acción al hacer clic */ }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Campo para eventos realizados con texto descriptivo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Eventos Realizados:",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize * 1.2f,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(0.3f)
                )
                OutlinedTextField(
                    value = eventNumber,
                    onValueChange = { newValue ->
                        // Solo permitir números y asegurarse de que el valor sea mayor a 0
                        if (newValue.isEmpty() || newValue.toIntOrNull()?.let { it > 0 } == true) {
                            eventNumber = newValue
                        }
                    },
                    label = { Text("Cantidad de Eventos Realizado") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusRequesters[1].requestFocus() }
                    ),
                    modifier = Modifier
                        .weight(0.7f)
                        .focusRequester(focusRequesters[0]),
                    textStyle = LocalTextStyle.current.copy(
                        color = Color.Blue,
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize
                    )
                )
            }

            // Campo para hombres con texto descriptivo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Hombres:",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize * 1.2f,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(0.3f)
                )
                OutlinedTextField(
                    value = menCount,
                    onValueChange = { menCount = it },
                    label = { Text("Cantidad de hombres") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusRequesters[2].requestFocus() }
                    ),
                    modifier = Modifier
                        .weight(0.7f)
                        .focusRequester(focusRequesters[1]),
                    textStyle = getTextStyle(menCount) // Aplicar estilo condicional
                )
            }

            // Campo para mujeres con texto descriptivo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Mujeres:",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize * 1.2f,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(0.3f)
                )
                OutlinedTextField(
                    value = womenCount,
                    onValueChange = { womenCount = it },
                    label = { Text("Cantidad de mujeres") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusRequesters[3].requestFocus() }
                    ),
                    modifier = Modifier
                        .weight(0.7f)
                        .focusRequester(focusRequesters[2]),
                    textStyle = getTextStyle(womenCount) // Aplicar estilo condicional
                )
            }

            // Campo para jóvenes con texto descriptivo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Jóvenes:",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize * 1.2f,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(0.3f)
                )
                OutlinedTextField(
                    value = youthCount,
                    onValueChange = { youthCount = it },
                    label = { Text("Cantidad de jóvenes") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusRequesters[4].requestFocus() }
                    ),
                    modifier = Modifier
                        .weight(0.7f)
                        .focusRequester(focusRequesters[3]),
                    textStyle = getTextStyle(youthCount)// Aplicar estilo condicional
                )
            }

            // Campo para Lugar con sugerencias
            Column {
                OutlinedTextField(
                    value = place,
                    onValueChange = { newValue ->
                        place = newValue.replaceFirstChar { it.uppercase() }
                        // Filtrar sugerencias basadas en lo que el usuario escribe
                        filteredPlaces =
                            places.filter { it.startsWith(newValue, ignoreCase = true) }
                    },
                    label = {
                        Text(
                            "Barrio, Municipio, Ciudad que se Realizó el Evento",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusRequesters[5].requestFocus() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequesters[4]),
                    textStyle = LocalTextStyle.current.copy(color = Color.Black)
                )

                // Mostrar sugerencias debajo del campo
                if (filteredPlaces.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp)
                            .padding(vertical = 4.dp)
                    ) {
                        items(filteredPlaces) { suggestion ->
                            Text(
                                text = suggestion,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        place = suggestion
                                        filteredPlaces = emptyList() // Ocultar sugerencias
                                        focusRequesters[5].requestFocus() // Mover foco al siguiente campo
                                    }
                                    .padding(8.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Blue)
                            )
                        }
                    }
                }
            }

            // Campo para Departamento con sugerencias
            Column {
                OutlinedTextField(
                    value = department,
                    onValueChange = { newValue ->
                        department = newValue.replaceFirstChar { it.uppercase() }
                        // Filtrar sugerencias basadas en lo que el usuario escribe
                        filteredDepartments =
                            departments.filter { it.startsWith(newValue, ignoreCase = true) }
                    },
                    label = { Text("Departamento o región") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequesters[5]),
                    textStyle = LocalTextStyle.current.copy(color = Color.Black)
                )

                // Mostrar sugerencias debajo del campo
                if (filteredDepartments.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp)
                            .padding(vertical = 4.dp)
                    ) {
                        items(filteredDepartments) { suggestion ->
                            Text(
                                text = suggestion,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        department = suggestion
                                        filteredDepartments = emptyList() // Ocultar sugerencias
                                        keyboardController?.hide() // Ocultar el teclado
                                    }
                                    .padding(8.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Blue)
                            )
                        }
                    }
                }
            }

            // Botón de registro con animación de carga
            Button(
                onClick = {
                    // Validar campos numéricos
                    if (eventNumber.toIntOrNull()?.let { it <= 0 } == true) {
                        message = "La cantidad de eventos debe ser mayor a 0."
                        keyboardController?.hide()
                        return@Button
                    }

                    // Validar al menos un participante
                    if ((menCount.toIntOrNull() ?: 0) <= 0 &&
                        (womenCount.toIntOrNull() ?: 0) <= 0 &&
                        (youthCount.toIntOrNull() ?: 0) <= 0
                    ) {
                        message = "Al menos un campo (hombres, mujeres, jóvenes) debe ser > 0."
                        keyboardController?.hide()
                        return@Button
                    }

                    // Validar campos obligatorios
                    if (eventNumber.isBlank() || menCount.isBlank() ||
                        womenCount.isBlank() || youthCount.isBlank() ||
                        place.isBlank() || department.isBlank()
                    ) {
                        message = "Todos los campos son obligatorios."
                        keyboardController?.hide()
                        return@Button
                    }

                    isLoading = true
                    keyboardController?.hide()

                    // Obtener ubicación y país
                    LocationUtils.getLastKnownLocation(context) { location ->
                        val country = if (location != null) {
                            GeocoderUtils.getCountryName(
                                context,
                                location.latitude,
                                location.longitude
                            ) ?: "Desconocido"
                        } else {
                            "Desconocido"
                        }

                        // Procesar textos con mayúsculas
                        val finalPlace = place.replaceFirstChar { it.uppercase() }
                        val finalDepartment = department.replaceFirstChar { it.uppercase() }

                        createEvent(
                            eventNumber = eventNumber.toIntOrNull() ?: 1,
                            menCount = menCount.toIntOrNull() ?: 0,
                            womenCount = womenCount.toIntOrNull() ?: 0,
                            youthCount = youthCount.toIntOrNull() ?: 0,
                            place = finalPlace,
                            department = finalDepartment,
                            country = country, // País obtenido del GPS
                            phoneNumberUser = phoneNumberUser,
                            context = context,
                            onSuccess = {
                                isLoading = false
                                message = "✅ Evento guardado localmente. Se sincronizará automáticamente."
                                onEventRegistered()
                            },
                            onFailure = { error ->
                                isLoading = false
                                message = if (error.contains("localmente")) {
                                    "✅ Evento guardado localmente. Se sincronizará automáticamente."
                                } else {
                                    "❌ Error: ${error.take(50)}..."
                                }
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Guardar Evento",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

// Mensaje de feedback mejorado
            if (message.isNotEmpty()) {
                val color = when {
                    message.startsWith("✅") -> Color(0xFF388E3C) // Verde
                    message.startsWith("⚠️") -> Color(0xFFFFA000) // Ámbar
                    else -> Color(0xFFD32F2F) // Rojo
                }

                Text(
                    text = message,
                    color = color,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth()
                        .background(
                            color = color.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(8.dp)
                )
            }


            // Botón para regresar
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("Regresar")
            }
        }
    }
}


@Composable
fun getTextStyle(value: String): TextStyle {
    return if ((value.toIntOrNull() ?: 0) > 0) {
        LocalTextStyle.current.copy(
            color = Color.Blue,
            fontWeight = FontWeight.Bold
        )
    } else {
        LocalTextStyle.current.copy(color = Color.Black)
    }
}