package com.flagsmith

import com.flagsmith.builder.Flagsmith
import com.flagsmith.response.Trait
import junit.framework.Assert.*
import kotlinx.coroutines.runBlocking
import org.junit.Test

class TraitsTests {

    private val flagsmith = Flagsmith.Builder()
        // .tokenApi("tokenApiKey")
        .environmentId("F5X4CN67ZmSB547j2k2nX4")
        .build();

    @Test
    fun testGetTraitsDefinedForPerson() {
        runBlocking {
            val result = flagsmith.getTraitsSync("person")
            assertTrue(result.isSuccess)
            assertTrue(result.getOrThrow().isNotEmpty())
            assertEquals(result.getOrThrow().find { trait -> trait.key == "favourite-colour" }?.value, "electric pink")
        }
    }

    @Test
    fun testGetTraitsNotDefinedForPerson() {
        runBlocking {
            val result = flagsmith.getTraitsSync("person")
            assertTrue(result.isSuccess)
            assertTrue(result.getOrThrow().isNotEmpty())
            assertNull(result.getOrThrow().find { trait -> trait.key == "fake-trait" }?.value)
        }
    }

    @Test
    fun testGetTraitById() {
        runBlocking {
            val result = flagsmith.getTraitSync("favourite-colour", "person")
            assertTrue(result.isSuccess)
            assertEquals(result.getOrThrow()?.value, "electric pink")
        }
    }

    @Test
    fun testGetUndefinedTraitById() {
        runBlocking {
            val result = flagsmith.getTraitSync("favourite-cricketer", "person")
            assertTrue(result.isSuccess)
            assertNull(result.getOrThrow())
        }
    }

    @Test
    fun testSetTrait() {
        runBlocking {
            val result = flagsmith.setTraitSync(Trait(key = "set-from-client", value = "12345"), "person")
            assertTrue(result.isSuccess)
            assertEquals(result.getOrThrow().key, "set-from-client")
            assertEquals(result.getOrThrow().value, "12345")
            assertEquals(result.getOrThrow().identity.identifier, "person")
        }
    }
}