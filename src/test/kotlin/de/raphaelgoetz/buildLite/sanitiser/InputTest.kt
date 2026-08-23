package de.raphaelgoetz.buildLite.sanitiser

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InputTest {

    @Test
    fun `sanitiseNameInput strips whitespace and punctuation`() {
        assertEquals("helloworld", "  hello world  ".sanitiseNameInput())
        assertEquals("helloworld", "hello-world!".sanitiseNameInput())
    }

    @Test
    fun `sanitiseNameInput keeps underscores since they are word characters`() {
        assertEquals("my_world", "My_World".sanitiseNameInput())
    }

    @Test
    fun `sanitiseNameInput lowercases the result`() {
        assertEquals("myworld", "MyWorld".sanitiseNameInput())
    }

    @Test
    fun `sanitiseNameInput truncates input longer than 254 characters`() {
        val result = "a".repeat(500).sanitiseNameInput()

        assertEquals(254, result.length)
        assertTrue(result.all { it == 'a' })
    }

    @Test
    fun `sanitiseGroupInput strips punctuation and lowercases`() {
        assertEquals("mygroup", "My Group!!".sanitiseGroupInput())
    }
}
