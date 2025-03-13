package com.saeo.fhn_Avivamiento.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_deletions")
data class PendingDeletion(
    @PrimaryKey val eventId: String
)