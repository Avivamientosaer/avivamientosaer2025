package com.saeo.fhn_Avivamiento.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.saeo.fhn_Avivamiento.eventos.Event

@Database(
    entities = [
        UserEntity::class,
        Event::class,
        CountryEntity::class,
        PendingDeletion::class
    ],
    version = 10, // Incrementar el número cuando se realicen cambios en la versión de la base de datos
    exportSchema = false
)
@TypeConverters(Converters::class) // Asegúrate de que esta línea esté presente
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun eventDao(): EventDao
    abstract fun countryDao(): CountryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "user_database"
                )
                    .fallbackToDestructiveMigration() // Agregar para manejar cambios en la base de datos
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}