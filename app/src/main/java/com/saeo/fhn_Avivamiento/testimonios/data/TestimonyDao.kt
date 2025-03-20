package com.saeo.fhn_Avivamiento.testimonios.data

import androidx.room.*

@Dao
interface TestimonyDao {
    // OPERACIONES CRUD BÁSICAS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestimony(testimony: Testimony)

    @Update
    suspend fun updateTestimony(testimony: Testimony)

    // ELIMINACIÓN LÓGICA
    @Query("UPDATE testimonies SET isDeleted = 1 WHERE id = :testimonyId")
    suspend fun markAsDeleted(testimonyId: String)

    // OBTENER DATOS
    @Query("SELECT * FROM testimonies WHERE isDeleted = 0 ORDER BY createdAt DESC")
    suspend fun getAllActiveTestimonies(): List<Testimony>

    @Query("SELECT * FROM testimonies WHERE id = :testimonyId LIMIT 1")
    suspend fun getTestimonyById(testimonyId: String): Testimony?

    // SINCRONIZACIÓN
    @Query("SELECT * FROM testimonies WHERE isSynced = 0 AND isDeleted = 0")
    suspend fun getUnsyncedTestimonies(): List<Testimony>

    @Query("UPDATE testimonies SET isSynced = 1 WHERE id = :testimonyId")
    suspend fun markAsSynced(testimonyId: String)

    @Query("UPDATE testimonies SET isSynced = 1 WHERE id IN (:testimonyIds)")
    suspend fun markMultipleAsSynced(testimonyIds: List<String>)

    // LIMPIEZA DE DATOS
    @Query("DELETE FROM testimonies WHERE isDeleted = 1")
    suspend fun deletePermanently()

    // BÚSQUEDAS OPTIMIZADAS
    @Query("""
    SELECT * FROM testimonies 
    WHERE 
        (name LIKE '%' || :query || '%') OR
        (chapter LIKE '%' || :query || '%') OR
        (city LIKE '%' || :query || '%')
    ORDER BY 
        CASE WHEN name LIKE :query || '%' THEN 0 ELSE 1 END,
        createdAt DESC
""")
    suspend fun searchTestimonies(query: String): List<Testimony>

    // ESTADÍSTICAS
    @Query("SELECT COUNT(*) FROM testimonies WHERE testimonyType = :type AND isDeleted = 0")
    suspend fun countByType(type: TestimonyType): Int

    @Query("""
        SELECT 
            testimonyType as type, 
            COUNT(*) as count 
        FROM testimonies 
        WHERE isDeleted = 0 
        GROUP BY testimonyType
    """)
    suspend fun getTypeDistribution(): List<TestimonyTypeCount>
}

data class TestimonyTypeCount(
    @ColumnInfo(name = "type") val type: TestimonyType,
    @ColumnInfo(name = "count") val count: Int
)