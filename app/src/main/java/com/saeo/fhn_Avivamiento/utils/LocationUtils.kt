package com.saeo.fhn_Avivamiento.utils

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task

object LocationUtils {
    private var fusedLocationClient: FusedLocationProviderClient? = null

    // Obtener última ubicación conocida (incluso sin conexión)
    fun getLastKnownLocation(context: Context, callback: (Location?) -> Unit) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        try {
            fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
                callback(location)
            }?.addOnFailureListener { e ->
                Log.e("LocationUtils", "Error al obtener ubicación: ${e.message}")
                callback(null)
            }
        } catch (e: SecurityException) {
            Log.e("LocationUtils", "Permisos de ubicación no concedidos")
            callback(null)
        }
    }
}