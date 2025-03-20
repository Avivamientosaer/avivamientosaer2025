package com.saeo.fhn_Avivamiento.testimonios.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saeo.fhn_Avivamiento.di.testimonyViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestimonyListScreen(
    onNavigateToAddTestimony: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel: TestimonyViewModel = viewModel(
        factory = testimonyViewModelFactory(LocalContext.current)
    )
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadTestimonies()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("👨‍💼 Lista de Testimonios 👩‍💼") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text(
                            "⬅️",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAddTestimony) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Testimonio",
                            tint = Color.Green,
                            modifier = Modifier.size(50.dp)

                        )
                        Text(
                            "➕",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Green,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(uiState.testimonies) { testimony ->
                        TestimonyCard(
                            testimony = testimony,
                            onEdit = { /* Implementar edición */ },
                            onDelete = { viewModel.deleteTestimony(testimony.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            uiState.errorMessage?.let { message ->
                ErrorMessageDialog(
                    message = message,
                    onDismiss = { viewModel.clearError() }
                )
            }
        }
    }
}

@Composable
private fun ErrorMessageDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}