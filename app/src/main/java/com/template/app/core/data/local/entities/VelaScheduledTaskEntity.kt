package com.template.app.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.template.app.domain.model.VelaScheduledTask

@Entity(tableName = "vela_scheduled_tasks")
data class VelaScheduledTaskEntity(
    @PrimaryKey val id: String,
    val command: String,
    val nextRun: String,
    val recurring: String?
) {
    fun toDomain() = VelaScheduledTask(
        id = id,
        command = command,
        nextRun = nextRun,
        recurring = recurring
    )

    companion object {
        fun fromDomain(domain: VelaScheduledTask) = VelaScheduledTaskEntity(
            id = domain.id,
            command = domain.command,
            nextRun = domain.nextRun,
            recurring = domain.recurring
        )
    }
}
