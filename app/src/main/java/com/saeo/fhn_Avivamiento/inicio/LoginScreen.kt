package com.saeo.fhn_Avivamiento.inicio

import android.content.Context
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.saeo.fhn_Avivamiento.R
import kotlinx.coroutines.launch
import com.saeo.fhn_Avivamiento.utils.PhoneVisualTransformation
import com.saeo.fhn_Avivamiento.utils.CountryUtils
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.VisualTransformation
import com.saeo.fhn_Avivamiento.utils.UserPreferences

import com.saeo.fhn_Avivamiento.utils.ConnectivityObserver
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import com.saeo.fhn_Avivamiento.data.local.UserEntity
import com.saeo.fhn_Avivamiento.utils.*

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await

import com.saeo.fhn_Avivamiento.data.local.AppDatabase
import com.saeo.fhn_Avivamiento.data.local.CountryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class LargeAsteriskTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val transformed = AnnotatedString("✸".repeat(text.text.length)) // Asterisco más grande
        return TransformedText(transformed, OffsetMapping.Identity)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    context: Context,
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    var countries by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var selectedCountry by remember { mutableStateOf<Pair<String, String>?>(null) }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingCountries by remember { mutableStateOf(true) } // Estado para controlar la carga de países
    val coroutineScope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    val phoneFocusRequester = remember { FocusRequester() }
    val countryNameFontSize = MaterialTheme.typography.bodyMedium.fontSize * 2

    // 🔹 Cargar valores guardados en DataStore
    val countryCodeFlow = UserPreferences.getCountryCode(context).collectAsState(initial = null)
    val phoneNumberFlow = UserPreferences.getPhoneNumber(context).collectAsState(initial = null)
    val savedPasswordFlow = UserPreferences.getPassword(context).collectAsState(initial = null)

    // 🟢  Obtenemos el CountryCode y phone number aquí mismo al inicio de la función.
    val savedCountryCode by countryCodeFlow
    val savedPhoneNumber by phoneNumberFlow
    val savedPassword by savedPasswordFlow

    // 🔹 Detectar conexión a Internet
    val localContext = LocalContext.current
    val connectivityObserver = remember { ConnectivityObserver(localContext) }
    val isConnected by connectivityObserver.isConnected.collectAsState(initial = true)

    LaunchedEffect(countryCodeFlow.value, phoneNumberFlow.value) {
        countryCodeFlow.value?.let { code ->
            selectedCountry = countries.find { it.first == code }
        }
        phoneNumberFlow.value?.let { phone -> phoneNumber = phone }
    }

    // ➕ Nuevo LaunchedEffect para la contraseña
    LaunchedEffect(savedPasswordFlow.value) {
        savedPasswordFlow.value?.let { pass -> password = pass }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isLoadingCountries = true
            val db = AppDatabase.getDatabase(context)


            // 1. Intentar cargar desde Room
            val localCountries = db.countryDao().getAllCountries()
            if (localCountries.isNotEmpty()) {
                countries = localCountries.map { it.code to it.name }

                val localCountryCode = withContext(Dispatchers.IO) {
                    UserPreferences.getCountryCode(context).firstOrNull()
                }
                val localCountryName = withContext(Dispatchers.IO) {
                    UserPreferences.getCountryName(context).firstOrNull()
                }

                // Buscar país usando código Y nombre para mayor precisión
                selectedCountry = localCountries.firstOrNull {
                    it.code == localCountryCode && it.name == localCountryName
                }?.let { it.code to it.name }

            } else {
                // 2. Cargar desde Firestore solo si hay conexión
                if (NetworkUtils.isInternetAvailable(context)) {
                    try {
                        countries = CountryUtils.fetchCountries()
                        // Guardar en Room
                        db.countryDao().insertCountries(
                            countries.map { CountryEntity(it.first, it.second) }
                        )
                    } catch (_: Exception) {
                        errorMessage = "Error cargando países. Reintenta más tarde."
                    }
                } else {
                    errorMessage = "Conectate a Internet para cargar los países."
                }
            }

            // Cargar teléfono guardado
            phoneNumber = savedPhoneNumber ?: ""
            password = savedPassword ?: ""
            if (countries.isEmpty() && !NetworkUtils.isInternetAvailable(context)) {
                errorMessage = "Países no disponibles en modo offline. Conéctate a Internet."
            }
            isLoadingCountries = false
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.logo), // Reemplaza con tu recurso de logotipo
                        contentDescription = "Logotipo",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        "Inicio de Sesión",
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = MaterialTheme.typography.headlineMedium.fontSize * 1.3f,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF39317),
                        textDecoration = TextDecoration.Underline
                    )
                }
            })
        }
    ) { innerPadding ->

        // Estado para mostrar la alerta de conexión
        var showNoConnectionDialog by remember { mutableStateOf(!isConnected) }

        // Detectar conexión solo una vez y permitir cerrar manualmente
        LaunchedEffect(Unit) {
            if (!isConnected) {
                showNoConnectionDialog = true
            }
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = errorMessage.isNotEmpty(),
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    OutlinedTextField(
                        value = if (isLoadingCountries) "Cargando..." else selectedCountry?.first ?: savedCountryCode ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = {
                            Text(
                                "Código País",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.25f
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isLoadingCountries) MaterialTheme.typography.bodySmall.fontSize else countryNameFontSize * 0.75f // Ajustar el tamaño de la fuente dinámicamente
                        ),
                        minLines = 1,
                        maxLines = 1,
                        trailingIcon = {
                            if (isLoadingCountries) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                IconButton(
                                    onClick = { expanded = true },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.ArrowDropDown,
                                        contentDescription = "Expandir",
                                        modifier = Modifier.size(40.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        enabled = !isLoadingCountries // Deshabilitar el campo mientras se carga
                    )

                    // Menú desplegable dentro del Box
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        countries.forEach { (code, name) ->
                            DropdownMenuItem(
                                text = { Text("$code - $name") },
                                onClick = {
                                    selectedCountry = code to name
                                    expanded = false
                                    phoneFocusRequester.requestFocus()

                                    coroutineScope.launch {
                                        UserPreferences.saveUserData(
                                            context = context,
                                            countryCode = code,
                                            countryName = name,
                                            phoneNumber = phoneNumber,
                                            password = password
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                // Mostrar el nombre del país subrayado
                val savedCountryName by UserPreferences.getCountryName(context).collectAsState(initial = null)

                AnimatedContent(
                    targetState = savedCountryName,
                    modifier = Modifier.padding(start = 8.dp)
                ) { countryName ->
                    Text(
                        text = countryName ?: selectedCountry?.second ?: "Selecciona país",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = countryNameFontSize * 1.25f,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        ),
                        maxLines = 1
                    )
                }
            }


            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it.filter { char -> char.isDigit() } },
                label = {
                    Text(
                        text = "Número de WhatsApp",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.25f // Aumentar 25%
                        )
                    )
                },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = "Teléfono") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                visualTransformation = PhoneVisualTransformation(),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.25f, // Aumentar 25%
                    fontWeight = FontWeight.Bold // Negrita
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(phoneFocusRequester)
            )

            // Estado para controlar la visibilidad de la contraseña
            var passwordVisible by remember { mutableStateOf(false) }

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = {
                    Text(
                        text = "Contraseña",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.25f // Aumentar 25%
                        )
                    )
                },
                leadingIcon = { Icon(Icons.Filled.Build, contentDescription = "Contraseña") },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Lock else Icons.Filled.Lock,
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else LargeAsteriskTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.25f // Aumentar 25%
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Botón de inicio de sesión
            Button(
                onClick = {
                    val fullPhoneNumber = "${selectedCountry?.first.orEmpty()}$phoneNumber"
                    isLoading = true
                    errorMessage = ""
                    coroutineScope.launch {
                        // Guardar valores en DataStore después del inicio de sesión
                        UserPreferences.saveUserData(
                            context = context,
                            countryCode = selectedCountry?.first.orEmpty(),
                            countryName = selectedCountry?.second.orEmpty(), // Parámetro faltante
                            phoneNumber = phoneNumber,
                            password = password
                        )

                        signInUser(
                            context = context, // Pasar el contexto
                            phoneNumber = fullPhoneNumber,
                            password = password,
                            onSuccess = {
                                isLoading = false
                                onLoginSuccess()
                            },
                            onFailure = { error ->
                                isLoading = false
                                errorMessage = error
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedCountry != null && phoneNumber.isNotBlank() && password.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Iniciar Sesión")
                }
            }


            TextButton(onClick = onNavigateToSignUp) {
                Text(
                    "Si es tu primera vez en la App y No tienes cuenta Regístrate Aquí",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.7f,
                        color = Color.Red
                    )
                )
            }
        }
    }
}


suspend fun signInUser(
    context: Context,
    phoneNumber: String,
    password: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    try {
        // 🔹 Intentar autenticar localmente primero
        val localUser = UserRepository.getUserFromLocal(context, phoneNumber)
        if (localUser != null && localUser.password == password) {
            Log.d("Login", "Inicio de sesión exitoso en modo offline")
            onSuccess()
            return
        }

        // 🔹 Si no hay Internet, permitir acceso solo si el usuario está en Room
        if (!NetworkUtils.isInternetAvailable(context)) {
            Log.w("Login", "No hay conexión, pero el usuario no se encontró en Room.")
            onFailure("No hay conexión a internet y no se encontraron datos locales. \n La primera vez que accedes a la App debes de tener conexión a Internet. ")
            return
        }

        // 🔹 Autenticación con Firestore si hay conexión
        val db = FirebaseFirestore.getInstance()
        val querySnapshot = db.collection("users")
            .whereEqualTo("phoneNumber", phoneNumber)
            .get()
            .await()

        if (querySnapshot.isEmpty) {
            Log.e("Login", "Usuario no encontrado en Firestore")
            onFailure("Usuario no encontrado")
            return
        }

        val document = querySnapshot.documents[0]
        val storedPassword = document.getString("password") ?: ""

        if (storedPassword == password) {
            Log.d("Login", "Inicio de sesión exitoso en Firestore")

            // 🔹 Guardar usuario en Room para futuros inicios sin conexión
            val newUser = UserEntity(
                phoneNumber = phoneNumber,
                countryCode = document.getString("country") ?: "",
                password = password,
                names = document.getString("names") ?: "",
                lastName = document.getString("lastName") ?: "",
                city = document.getString("city") ?: "",
                country = document.getString("country") ?: "",
                chapter = document.getString("chapter") ?: "",
                gender = document.getString("gender") ?: ""
            )
            UserRepository.saveUserLocally(context, newUser)

            onSuccess()
        } else {
            Log.e("Login", "Contraseña incorrecta")
            onFailure("Contraseña incorrecta")
        }

    } catch (e: Exception) {
        Log.e("Login", "Error en el inicio de sesión: ${e.localizedMessage}")
        onFailure("Error al iniciar sesión: ${e.localizedMessage}")
    }
}