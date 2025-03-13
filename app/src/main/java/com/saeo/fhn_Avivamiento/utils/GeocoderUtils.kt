package com.saeo.fhn_Avivamiento.utils

import android.content.Context
import android.location.Geocoder
import android.util.Log
import java.util.Locale

object GeocoderUtils {
    fun getCountryName(context: Context, latitude: Double, longitude: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            addresses?.get(0)?.countryName ?: "Desconocido"
        } catch (e: Exception) {
            Log.e("GeocoderUtils", "Error al obtener país: ${e.message}")
            null
        }
    }
}