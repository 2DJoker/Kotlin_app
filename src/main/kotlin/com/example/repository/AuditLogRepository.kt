package com.example.repository

import com.example.db.AuditLogsTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

interface AuditLogRepository {
    fun log(userId: Long?, action: String, payload: String)
}

class AuditLogRepositoryImpl : AuditLogRepository {
    override fun log(userId: Long?, action: String, payload: String) {
        transaction {
            AuditLogsTable.insert {
                it[AuditLogsTable.userId] = userId
                it[AuditLogsTable.action] = action
                it[AuditLogsTable.payload] = payload
                it[createdAt] = Instant.now()
            }
        }
    }
}
