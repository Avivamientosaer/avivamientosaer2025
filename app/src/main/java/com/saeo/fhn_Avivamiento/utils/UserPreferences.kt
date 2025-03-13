package com.saeo.fhn_Avivamiento.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión para acceder al DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

object UserPreferences {

    // Claves para almacenar y recuperar datos
    private val COUNTRY_CODE_KEY = stringPreferencesKey("country_code")
    private val COUNTRY_NAME_KEY = stringPreferencesKey("country_name")
    private val PHONE_NUMBER_KEY = stringPreferencesKey("phone_number")
    private val PASSWORD_KEY = stringPreferencesKey("user_password")
    private val USER_NAME_KEY = stringPreferencesKey("user_name") // Nueva clave para el nombre de usuario

    // Obtener el código de país
    fun getCountryCode(context: Context): Flow<String?> =
        context.dataStore.data.map { it[COUNTRY_CODE_KEY] }

    // Obtener el nombre del país
    fun getCountryName(context: Context): Flow<String?> =
        context.dataStore.data.map { it[COUNTRY_NAME_KEY] }

    // Obtener el número de teléfono
    fun getPhoneNumber(context: Context): Flow<String?> =
        context.dataStore.data.map { it[PHONE_NUMBER_KEY] }

    // Obtener contraseña guardada
    fun getPassword(context: Context): Flow<String?> =
        context.dataStore.data.map { it[PASSWORD_KEY] }

    // Obtener el número de teléfono con el código de país
    fun getPhoneNumberWithCountryCode(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            val countryCode = preferences[COUNTRY_CODE_KEY] ?: ""
            val phoneNumber = preferences[PHONE_NUMBER_KEY] ?: ""
            if (countryCode.isNotEmpty() && phoneNumber.isNotEmpty()) {
                "$countryCode$phoneNumber" // Devuelve el número con el código de país
            } else {
                phoneNumber // Si no hay código de país, devuelve solo el número
            }
        }
    }

    // Guardar datos del usuario (actualizado para incluir el nombre de usuario)
    suspend fun saveUserData(
        context: Context,
        countryCode: String,
        countryName: String,
        phoneNumber: String,
        password: String,
        userName: String? = null // Nuevo parámetro opcional para el nombre de usuario
    ) {
        context.dataStore.edit { preferences ->
            preferences[COUNTRY_CODE_KEY] = countryCode
            preferences[COUNTRY_NAME_KEY] = countryName
            preferences[PHONE_NUMBER_KEY] = phoneNumber
            preferences[PASSWORD_KEY] = password
            userName?.let {
                preferences[USER_NAME_KEY] = it // Guardar el nombre de usuario si se proporciona
            }
        }
    }
}