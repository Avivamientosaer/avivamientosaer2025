package com.saeo.fhn_Avivamiento.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.saeo.fhn_Avivamiento.data.local.AppDatabase
import com.saeo.fhn_Avivamiento.testimonios.ui.TestimonyViewModel

fun testimonyViewModelFactory(context: Context): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TestimonyViewModel(
                database = AppDatabase.getDatabase(context)
            ) as T
        }
    }