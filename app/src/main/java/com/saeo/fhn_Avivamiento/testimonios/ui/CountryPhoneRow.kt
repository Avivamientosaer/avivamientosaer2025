package com.saeo.fhn_Avivamiento.testimonios.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.saeo.fhn_Avivamiento.utils.PhoneVisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPhoneRow(
    countries: List<Pair<String, String>>,
    selectedCountry: Pair<String, String>?,
    onCountrySelected: (Pair<String, String>) -> Unit,
    phone: String,
    onPhoneChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    countryLabel: String = "País",
    phoneLabel: String = "Teléfono",
    errorState: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    val phoneFocusRequester = remember { FocusRequester() }
    val countryNameFontSize = MaterialTheme.typography.bodyMedium

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Selector de País
        Box(modifier = Modifier.weight(1.3f)) {
            OutlinedTextField(
                value = selectedCountry?.first ?: "",
                onValueChange = {},
                label = { Text(countryLabel) },
                readOnly = true,
                trailingIcon = {
                    IconButton(
                        onClick = { expanded = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Seleccionar país"
                        )
                    }
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = if (errorState) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (errorState) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = countryNameFontSize.fontSize,
                    fontWeight = MaterialTheme.typography.titleMedium.fontWeight
                )
            )

            // Nombre completo del país debajo del código
            selectedCountry?.let { (code, name) ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 4.dp)
                        .align(Alignment.BottomStart)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 200.dp)
            ) {
                countries.forEach { (code, name) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "$code - $name",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            onCountrySelected(code to name)
                            expanded = false
                            phoneFocusRequester.requestFocus()
                        }
                    )
                }
            }
        }

        // Campo de Teléfono
        OutlinedTextField(
            value = phone,
            onValueChange = { onPhoneChanged(it.filter { c -> c.isDigit() }) },
            label = { Text(phoneLabel) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            visualTransformation = PhoneVisualTransformation(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = if (errorState) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (errorState) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier
                .weight(2f)
                .focusRequester(phoneFocusRequester),
            textStyle = LocalTextStyle.current.copy(
                fontSize = MaterialTheme.typography.bodyMedium.fontSize
            )
        )
    }
}