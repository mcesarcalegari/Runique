package com.plcoding.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.bson.types.ObjectId

@Entity
data class GoalEntity(
    val type: String,
    val period: String,
    val target: Long,
    val periodStartDate: String,
    val periodEndDate: String,
    val createdAtUtc: String,
    val completedAtUtc: String?,
    @PrimaryKey(autoGenerate = false)
    val id: String = ObjectId().toHexString()
)
