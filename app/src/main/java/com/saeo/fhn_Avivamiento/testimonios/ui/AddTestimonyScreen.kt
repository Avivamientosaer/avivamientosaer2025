package com.saeo.fhn_Avivamiento.testimonios.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saeo.fhn_Avivamiento.data.local.AppDatabase
import com.saeo.fhn_Avivamiento.di.testimonyViewModelFactory
import com.saeo.fhn_Avivamiento.testimonios.data.Testimony
import com.saeo.fhn_Avivamiento.testimonios.data.TestimonyType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTestimonyScreen(
    onTestimonySaved: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel: TestimonyViewModel =
        viewModel(factory = testimonyViewModelFactory(LocalContext.current))
    val uiState by viewModel.uiState.collectAsState()
    var showTypeMenu by remember { mutableStateOf(false) }

    var countries by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var selectedCountry by remember { mutableStateOf<Pair<String, String>?>(null) }
    val phoneFocusRequester = remember { FocusRequester() }

    val db = AppDatabase.getDatabase(LocalContext.current)

    LaunchedEffect(Unit) {
        viewModel.setCurrentTestimony(null)
        countries = db.countryDao().getAllCountries().map { it.code to it.name }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("  Agregar Nuevo Testimonio 📝",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = Bold,
                    color = Color(0xFF398E3C)
                ) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver",
                            modifier = Modifier.size(40.dp),
                            tint = Color.Red,

                            )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // Campo Teléfono
            CountryPhoneRow(
                countries = countries,
                selectedCountry = selectedCountry,
                onCountrySelected = { country ->
                    selectedCountry = country
                    viewModel.updateTestimonyField(
                        countryCode = country.first,
                        phone = "${country.first}${viewModel.uiState.value.currentTestimony?.phone ?: ""}"
                    )
                },
                phone = viewModel.uiState.value.currentTestimony?.phone?.replace("${selectedCountry?.first}", "") ?: "",
                onPhoneChanged = { newPhone ->
                    viewModel.updateTestimonyField(
                        phone = "${selectedCountry?.first.orEmpty()}$newPhone"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )


            Spacer(modifier = Modifier.height(16.dp))

            // Campo Nombre
            OutlinedTextField(
                value = uiState.currentTestimony?.name ?: "",
                onValueChange = { viewModel.updateTestimonyField(name = it) },
                label = { Text("Nombre Completo") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Tipo de Testimonio
            ExposedDropdownMenuBox(
                expanded = showTypeMenu,
                onExpandedChange = { showTypeMenu = !showTypeMenu }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = uiState.currentTestimony?.testimonyType?.toString()?.replace("_", " ")
                        ?: "",
                    onValueChange = {},
                    label = { Text("Tipo de Testimonio") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = showTypeMenu,
                    onDismissRequest = { showTypeMenu = false }
                ) {
                    TestimonyType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.toString().replace("_", " ")) },
                            onClick = {
                                viewModel.updateTestimonyField(testimonyType = type)
                                showTypeMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resto de campos (Capítulo, Ciudad, Años)
            OutlinedTextField(
                value = uiState.currentTestimony?.chapter ?: "",
                onValueChange = { viewModel.updateTestimonyField(chapter = it) },
                label = { Text("Capítulo") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.currentTestimony?.city ?: "",
                onValueChange = { viewModel.updateTestimonyField(city = it) },
                label = { Text("Ciudad") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.currentTestimony?.joinedYear?.takeIf { it > 0 }?.toString() ?: "",
                onValueChange = {
                    viewModel.updateTestimonyField(
                        joinedYear = it.toIntOrNull() ?: 0
                    )
                },
                label = { Text("Año de ingreso") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.currentTestimony?.birthYear?.takeIf { it > 0 }?.toString() ?: "",
                onValueChange = {
                    viewModel.updateTestimonyField(
                        birthYear = it.toIntOrNull() ?: 0
                    )
                },
                label = { Text("Año de nacimiento") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Notas
            OutlinedTextField(
                value = uiState.currentTestimony?.notes ?: "",
                onValueChange = { viewModel.updateTestimonyField(notes = it) },
                label = { Text("Notas importantes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Guardar
            Button(
                onClick = {
                    uiState.currentTestimony?.let {
                        viewModel.saveTestimony(it)
                        onTestimonySaved()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = validateTestimony(uiState.currentTestimony)
            ) {
                Text("Guardar Testimonio")
            }
        }
    }
}

private fun validateTestimony(testimony: Testimony?): Boolean {
    return !testimony?.name.isNullOrEmpty() &&
            !testimony?.phone.isNullOrEmpty() &&
            testimony?.joinedYear ?: 0 > 1900 &&
            testimony?.birthYear ?: 0 > 1900
}