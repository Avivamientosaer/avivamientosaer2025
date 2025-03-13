package com.saeo.fhn_Avivamiento.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.saeo.fhn_Avivamiento.data.local.AppDatabase
import com.saeo.fhn_Avivamiento.data.local.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date

class UserSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val userDao = db.userDao()
                val firestore = FirebaseFirestore.getInstance()

                // Verificar si hay conexión antes de sincronizar
                if (!NetworkUtils.isInternetAvailable(applicationContext)) {
                    Log.d("UserSyncWorker", "No hay conexión a Internet. Reintentando más tarde.")
                    return@withContext Result.retry()
                }

                // Obtener usuarios locales con cambios pendientes
                val localUsers = userDao.getUnsyncedUsers()
                Log.d(
                    "UserSyncWorker",
                    "Usuarios locales con cambios pendientes: ${localUsers.size}"
                )

                for (user in localUsers) {
                    val userRef = firestore.collection("users").document(user.phoneNumber)
                    val snapshot = userRef.get().await()

                    if (snapshot.exists()) {
                        val remoteLastModified = snapshot.getDate("lastModified") ?: Date(0)
                        Log.d(
                            "UserSyncWorker",
                            "Usuario ${user.phoneNumber} encontrado en Firestore."
                        )

                        if (user.lastModified.after(remoteLastModified)) {
                            Log.d(
                                "UserSyncWorker",
                                "Subiendo datos actualizados para ${user.phoneNumber}."
                            )
                            userRef.set(user.toMap()).await()
                        } else {
                            Log.d(
                                "UserSyncWorker",
                                "Datos en Firestore son más recientes para ${user.phoneNumber}, no se actualiza."
                            )
                        }
                    } else {
                        Log.d(
                            "UserSyncWorker",
                            "Usuario ${user.phoneNumber} no existe en Firestore. Subiendo datos."
                        )
                        userRef.set(user.toMap()).await()
                    }

                    // Marcar usuario como sincronizado en Room
                    userDao.markUserAsSynced(user.phoneNumber)
                    Log.d(
                        "UserSyncWorker",
                        "Usuario ${user.phoneNumber} marcado como sincronizado en Room."
                    )
                }

                Log.d("UserSyncWorker", "Sincronización completada exitosamente.")
                Result.success()
            } catch (e: Exception) {
                Log.e("UserSyncWorker", "Error en la sincronización: ${e.localizedMessage}")
                Result.retry() // Reintentar si falla la sincronización
            }
        }
    }
}

    // Funciones de conversión
fun UserEntity.toMap(): Map<String, Any> {
    return mapOf(
        "phoneNumber" to phoneNumber,
        "countryCode" to countryCode,
        "password" to password,
        "names" to names,
        "lastName" to lastName,
        "city" to city,
        "country" to country,
        "chapter" to chapter,
        "gender" to gender,
        "lastModified" to Date()
    )
}
