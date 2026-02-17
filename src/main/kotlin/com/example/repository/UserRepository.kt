package com.example.repository

import com.example.db.UsersTable
import com.example.domain.User
import com.example.domain.enums.UserRole
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

interface UserRepository {
    fun create(email: String, passwordHash: String, fullName: String, role: UserRole = UserRole.USER): User
    fun findByEmail(email: String): User?
    fun findById(id: Long): User?
}

class UserRepositoryImpl : UserRepository {
    override fun create(email: String, passwordHash: String, fullName: String, role: UserRole): User = transaction {
        val now = Instant.now()
        val id = UsersTable.insert {
            it[UsersTable.email] = email
            it[UsersTable.passwordHash] = passwordHash
            it[UsersTable.fullName] = fullName
            it[UsersTable.role] = role.name
            it[UsersTable.createdAt] = now
        }[UsersTable.id].value

        User(id, email, passwordHash, fullName, role, now)
    }

    override fun findByEmail(email: String): User? = transaction {
        UsersTable.selectAll().where { UsersTable.email eq email }.singleOrNull()?.toUser()
    }

    override fun findById(id: Long): User? = transaction {
        UsersTable.selectAll().where { UsersTable.id eq id }.singleOrNull()?.toUser()
    }

    private fun ResultRow.toUser(): User = User(
        id = this[UsersTable.id].value,
        email = this[UsersTable.email],
        passwordHash = this[UsersTable.passwordHash],
        fullName = this[UsersTable.fullName],
        role = UserRole.valueOf(this[UsersTable.role]),
        createdAt = this[UsersTable.createdAt]
    )
}
