package com.example.db

import com.example.config.AppSettings
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun init(settings: AppSettings) {
        val hikari = HikariConfig().apply {
            jdbcUrl = settings.dbUrl
            username = settings.dbUser
            password = settings.dbPassword
            driverClassName = if (settings.dbUrl.startsWith("jdbc:h2:")) "org.h2.Driver" else "org.postgresql.Driver"
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        val dataSource = HikariDataSource(hikari)
        Database.connect(dataSource)

        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load()
            .migrate()
    }
}
