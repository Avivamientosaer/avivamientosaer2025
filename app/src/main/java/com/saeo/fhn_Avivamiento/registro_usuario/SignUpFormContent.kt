package com.saeo.fhn_Avivamiento.registro_usuario

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saeo.fhn_Avivamiento.utils.*
import kotlinx.coroutines.launch
import com.saeo.fhn_Avivamiento.utils.UserRepository
import androidx.compose.ui.platform.LocalContext

import com.saeo.fhn_Avivamiento.utils.ConnectivityObserver
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpFormContent(
    onSignUpSuccess: () -> Unit, modifier: Modifier = Modifier
) {
    // 1) Variables de estado
    var countries by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var selectedCountry by remember { mutableStateOf<Pair<String, String>?>(null) }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var names by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") } // <-- nueva variable
    var city by remember { mutableStateOf("") }
    var cityList by remember { mutableStateOf<List<CityItem>>(emptyList()) }
    var filteredCityList by remember { mutableStateOf<List<CityItem>>(emptyList()) }
    var chapter by remember { mutableStateOf("") }
    var chaptersList by remember { mutableStateOf<List<ChapterItem>>(emptyList()) }
    var filteredChapters by remember { mutableStateOf<List<ChapterItem>>(emptyList()) }

    var expanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessages by remember { mutableStateOf<List<String>>(emptyList()) }
    // Controla si se muestra la contraseña en texto plano o enmascarada
    var passwordVisible by remember { mutableStateOf(true) }

    // Mensaje local de error cuando las contraseñas no coinciden al pasar
    var passwordError by remember { mutableStateOf("") }

    // 2) Focus Manager
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val cityFocusRequester = remember { FocusRequester() }
    var resaltarSexo by remember { mutableStateOf(false) }

    // 3) CoroutineScope para cargar países
    val coroutineScope = rememberCoroutineScope()
    val localContext = LocalContext.current
    val connectivityObserver = remember { ConnectivityObserver(localContext) }
    val isConnected by connectivityObserver.isConnected.collectAsState(initial = true)


    LaunchedEffect(Unit) {
        coroutineScope.launch {
            // Cargar países:
            countries = CountryUtils.fetchCountries()
            // Cargar ciudades:
            cityList = CityUtils.fetchCities()
            // 🔹 Cargar capítulos
            chaptersList = ChapterUtils.fetchChapters()

            isLoading = false
            if (countries.isEmpty()) {
                errorMessages = listOf("No se pudieron cargar los países. Intenta más tarde.")
            }
        }
    }


    var showNoConnectionDialog by remember { mutableStateOf(!isConnected) }

    if (showNoConnectionDialog) {
        AlertDialog(
            onDismissRequest = { showNoConnectionDialog = false },
            confirmButton = {
                TextButton(onClick = { showNoConnectionDialog = false }) {
                    Text("Cerrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoConnectionDialog = !isConnected }) {
                    Text("Reintentar")
                }
            },
            title = { Text("Sin Conexión") },
            text = { Text("No tienes conexión a Internet. Algunas funciones pueden no estar disponibles.") }
        )
    }


    // 4) Contenedor principal
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 🔹 Mostrar errores en la parte superior 🔹
        AnimatedVisibility(
            visible = errorMessages.isNotEmpty(),
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp)
                    )
                    Column {
                        errorMessages.forEach { error ->
                            Text(
                                text = "• $error",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // 🔹 Muestra un loading mientras traes la lista de países 🔹
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp), color = MaterialTheme.colorScheme.primary
            )
        } else {
            // Sección de país y teléfono en la misma fila
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Campo de código de país
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedCountry?.first ?: "",
                        onValueChange = {},
                        label = { Text("País") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Expandir")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // Nombre del país abajo del campo
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 10.dp, bottom = 1.dp)
                    ) {
                        AnimatedContent(targetState = selectedCountry, transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(
                                animationSpec = tween(300)
                            )
                        }) { country ->
                            if (country != null) {
                                Text(
                                    text = country.second,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = MaterialTheme.shapes.small
                                        )
                                        .padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }

                    // Menú desplegable de países
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        countries.forEach { (code, name) ->
                            DropdownMenuItem(text = { Text("$code - $name") }, onClick = {
                                selectedCountry = code to name
                                expanded = false
                                focusRequester.requestFocus()
                            })
                        }
                    }
                }

                // Campo: Número de WhatsApp / Celular
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { newValue ->
                        val digitsOnly = newValue.replace("[^0-9]".toRegex(), "")
                        if (digitsOnly.length <= 15) {
                            phoneNumber = digitsOnly
                        }
                    },
                    label = { Text("Número de WhatsApp ó Celular") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(FocusDirection.Next)
                    }),
                    leadingIcon = {
                        Icon(Icons.Filled.Phone, contentDescription = "Teléfono")
                    },
                    visualTransformation = PhoneVisualTransformation(),
                    modifier = Modifier
                        .weight(2f)
                        .focusRequester(focusRequester),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

// Campo: Contraseña y Confirmación (misma línea)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Campo Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val iconImage = if (passwordVisible) Icons.Filled.Lock else Icons.Filled.Lock
                    val description = if (passwordVisible) "Ocultar" else "Mostrar"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(iconImage, contentDescription = description)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Next) }
                ),
                modifier = Modifier.weight(1f)
            )

            // Campo Confirmar Contraseña
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Repetir Contraseña") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val iconImage = if (passwordVisible) Icons.Filled.Lock else Icons.Filled.Lock
                    val description = if (passwordVisible) "Ocultar" else "Mostrar"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(iconImage, contentDescription = description)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        // 🔹 Al presionar Next en la confirmación, validamos localmente
                        passwordError = if (password != confirmPassword) {
                            "Las contraseñas no coinciden."
                        } else if (password.length < 6) {
                            "La contraseña debe tener al menos 6 caracteres."

                        } else {
                            ""
                        }
                        focusManager.moveFocus(FocusDirection.Next)
                    }
                ),
                modifier = Modifier.weight(1f)
            )
        }

// 🔹 Mostrar el error local debajo del Row (si existe)
        if (passwordError.isNotEmpty()) {
            Text(
                text = passwordError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 25.sp)

            )
        }

        // Campo: Nombres
        OutlinedTextField(
            value = names,
            onValueChange = { newVal ->
                // Forzar primera letra en mayúscula
                val forcedUpper = if (newVal.isNotEmpty()) {
                    newVal[0].uppercaseChar() + newVal.substring(1)
                } else {
                    newVal
                }
                names = forcedUpper
            },
            label = { Text("Nombres") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Next)
            })
        )


        // Campo: Apellido
        OutlinedTextField(
            value = lastName,
            onValueChange = { newVal ->
                // Forzar primera letra en mayúscula
                val forcedUpper = if (newVal.isNotEmpty()) {
                    newVal[0].uppercaseChar() + newVal.substring(1)
                } else {
                    newVal
                }
                lastName = forcedUpper
            },
            label = { Text("Apellidos") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = {
                    // 1) Ocultar el teclado
                    focusManager.clearFocus(force = true)
                    // 2) Activar el resalte de "Sexo"
                    resaltarSexo = true
                }
            )
        )


        // Campo: Sexo (Masculino / Femenino)
        AnimatedVisibility(visible = true) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Sexo",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                // 🔹 Determinar colores según si está resaltado o no
                val backgroundColor = if (resaltarSexo) Color(0xFF8575BB)
                else MaterialTheme.colorScheme.surface
                val textColor = if (resaltarSexo) Color.White
                else MaterialTheme.colorScheme.onSurface
                val radioColor = if (resaltarSexo) Color.White
                else MaterialTheme.colorScheme.primary

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(backgroundColor)
                        .padding(8.dp)
                ) {
                    // Radio masculino
                    RadioButton(
                        selected = (gender == "Masculino"),
                        onClick = {
                            gender = "Masculino"
                            resaltarSexo = false  // Quita el resalte
                            cityFocusRequester.requestFocus()
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = radioColor,
                            unselectedColor = radioColor
                        )
                    )
                    Text(
                        text = "Masculino",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(end = 16.dp)
                    )

                    // Radio femenino
                    RadioButton(
                        selected = (gender == "Femenino"),
                        onClick = {
                            gender = "Femenino"
                            resaltarSexo = false
                            cityFocusRequester.requestFocus()
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = radioColor,
                            unselectedColor = radioColor
                        )
                    )
                    Text(
                        text = "Femenino",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

// Campo: Ciudad con ExposedDropdownMenuBox
        var isMenuExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = isMenuExpanded,
            onExpandedChange = { isMenuExpanded = it }
        ) {
            OutlinedTextField(
                value = city,
                onValueChange = { newValue ->
                    // 1) Forzar primera letra mayúscula
                    val forcedUpper = if (newValue.isNotEmpty()) {
                        newValue[0].uppercaseChar() + newValue.substring(1)
                    } else {
                        newValue
                    }
                    city = forcedUpper

                    // 2) Filtrar SOLO las ciudades del país seleccionado
                    val currentCountry = selectedCountry?.second ?: ""
                    val matches = cityList.filter {
                        // Mismo país Y empieza con forcedUpper
                        it.country.equals(currentCountry, ignoreCase = true) &&
                                it.name.startsWith(forcedUpper, ignoreCase = true)
                    }

                    // Actualizar la lista filtrada
                    filteredCityList = matches

                    // 3) Mostrar menú si hay coincidencias
                    isMenuExpanded = matches.isNotEmpty()
                },
                label = { Text("Ciudad donde Vives") },
                singleLine = true,
                // FocusRequester para “Sexo”
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(cityFocusRequester)
                    .menuAnchor(), // Para anclar el menú

                // Muestra la tecla Next
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Next)
                    }
                ),

                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = isMenuExpanded
                    )
                }
            )

            ExposedDropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false }
            ) {
                filteredCityList.forEach { cityItem ->
                    DropdownMenuItem(
                        text = { Text(cityItem.name) },
                        onClick = {
                            // Asignar el city.name
                            city = cityItem.name
                            // Cerrar menú
                            isMenuExpanded = false
                            // Mover foco si deseas
                            focusManager.moveFocus(FocusDirection.Next)
                        }
                    )
                }
            }
        }


        // Campo: Capítulo con ExposedDropdownMenuBox
        var chapterMenuExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = chapterMenuExpanded,
            onExpandedChange = { chapterMenuExpanded = it }
        ) {
            OutlinedTextField(
                value = chapter,
                onValueChange = { newValue ->
                    // 1) Forzar primera letra mayúscula
                    val forcedUpper = if (newValue.isNotEmpty()) {
                        newValue[0].uppercaseChar() + newValue.substring(1)
                    } else {
                        newValue
                    }
                    chapter = forcedUpper

                    // 2) Filtrar SOLO por la ciudad + país
                    val countryName = selectedCountry?.second ?: ""
                    val matches = chaptersList.filter {
                        it.city.equals(city, ignoreCase = true) &&
                                it.country.equals(countryName, ignoreCase = true) &&
                                it.name.startsWith(forcedUpper, ignoreCase = true)
                    }

                    filteredChapters = matches
                    // 3) Mostrar menú si hay coincidencias
                    chapterMenuExpanded = matches.isNotEmpty()
                },
                label = { Text("Capítulo al que Pertenece") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Next)
                    }
                ),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = chapterMenuExpanded
                    )
                }
            )

            ExposedDropdownMenu(
                expanded = chapterMenuExpanded,
                onDismissRequest = { chapterMenuExpanded = false }
            ) {
                filteredChapters.forEach { chapterItem ->
                    DropdownMenuItem(
                        text = { Text(chapterItem.name) },
                        onClick = {
                            chapter = chapterItem.name
                            chapterMenuExpanded = false
                            focusManager.moveFocus(FocusDirection.Next)
                        }
                    )
                }
            }
        }


        // Botón de Registro
        val context = LocalContext.current // ✅ Obtener el contexto ANTES de la corrutina
        Button(
            onClick = {
                val countryCode = selectedCountry?.first ?: ""
                val fullPhoneNumber = "$countryCode$phoneNumber"
                val phoneRegex = "^[0-9]{8,15}$".toRegex()
                val country = selectedCountry?.second ?: ""


                val errors = mutableListOf<String>()

                // 🔹 Verificar conexión antes de continuar con el registro
                if (!isConnected) {
                    errorMessages = listOf("No hay conexión a Internet. Verifica tu conexión e intenta nuevamente.")
                    return@Button
                }


                if (!phoneRegex.matches(phoneNumber)) {
                    errors.add("Número de WhatsApp inválido. Debe tener entre 8 y 15 dígitos.")
                }

                val missingFields = mutableListOf<String>()
                if (phoneNumber.isBlank()) missingFields.add("Número de WhatsApp")
                if (password.isBlank()) missingFields.add("Contraseña")
                if (names.isBlank()) missingFields.add("Nombres")
                if (lastName.isBlank()) missingFields.add("Apellido")
                if (gender.isBlank()) missingFields.add("Sexo")
                if (city.isBlank()) missingFields.add("Ciudad")
                if (chapter.isBlank()) missingFields.add("Capítulo")

                if (missingFields.isNotEmpty()) {
                    errors.add("Faltan los siguientes campos: ${missingFields.joinToString(", ")}")
                }

                // Contraseñas coinciden
                if (password != confirmPassword) {
                    errors.add("Las contraseñas no coinciden.")
                }

                if (errors.isNotEmpty()) {
                    errorMessages = errors
                    return@Button
                }

                // 🔹 Si no hay errores, registramos
                coroutineScope.launch {
                    try {
                        // 1) Si la ciudad no está en la lista, la agregamos
                        if (filteredCityList.none { it.name.equals(city, ignoreCase = true) }) {
                            CityUtils.addCityIfNotExistsWithCountry(
                                cityName = city,
                                countryName = country
                            )
                        }

                        // 2) Crear el capítulo si no existe
                        if (chaptersList.none {
                                it.name.equals(chapter, ignoreCase = true) &&
                                        it.city.equals(city, ignoreCase = true) &&
                                        it.country.equals(country, ignoreCase = true)
                            }) {
                            ChapterUtils.addChapterIfNotExists(
                                chapterName = chapter,
                                cityName = city,
                                countryName = country
                            )
                        }

                        // 3) Llamamos a signUpUser
                        UserRepository.signUpUser(
                            context = context, // ✅ Se pasa correctamente antes de la corrutina
                            phoneNumber = fullPhoneNumber,
                            password = password,
                            names = names,
                            lastName = lastName,
                            city = city,
                            country = country,
                            chapter = chapter,
                            gender = gender,
                            onSuccess = {
                                errorMessages = emptyList()
                                onSignUpSuccess()
                            },
                            onFailure = { errorMsg ->
                                errorMessages = listOf(errorMsg)
                            }
                        )

                    } catch (e: Exception) {
                        errorMessages =
                            listOf("Error al guardar la ciudad/capítulo: ${e.localizedMessage}")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .shadow(elevation = 4.dp, shape = MaterialTheme.shapes.medium),
            enabled = isConnected // 🔹 Deshabilita el botón si no hay conexión
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Registrar",
                    modifier = Modifier.size(20.dp)
                )
                Text("Registrar")
            }
        }
    }
}