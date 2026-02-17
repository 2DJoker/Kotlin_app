package com.example.unit

import com.example.security.PasswordHasher
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PasswordHasherTest {
    @Test
    fun `hash should be verifiable`() {
        val raw = "secret123"
        val hash = PasswordHasher.hash(raw)

        assertTrue(PasswordHasher.verify(raw, hash))
    }

    @Test
    fun `wrong password should not verify`() {
        val hash = PasswordHasher.hash("secret123")

        assertFalse(PasswordHasher.verify("wrong", hash))
    }
}
