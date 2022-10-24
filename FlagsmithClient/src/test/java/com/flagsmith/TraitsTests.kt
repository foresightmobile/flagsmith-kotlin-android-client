package com.flagsmith

import com.flagsmith.builder.Flagsmith
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
            assertEquals(result.getOrThrow().find { trait -> trait.trait_key == "favourite-colour" }?.trait_value, "electric pink")
        }
    }

    @Test
    fun testGetTraitsNotDefinedForPerson() {
        runBlocking {
            val result = flagsmith.getTraitsSync("person")
            assertTrue(result.isSuccess)
            assertTrue(result.getOrThrow().isNotEmpty())
            assertNull(result.getOrThrow().find { trait -> trait.trait_key == "fake-trait" }?.trait_value)
        }
    }

}