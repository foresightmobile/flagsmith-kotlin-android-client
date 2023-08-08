package com.flagsmith

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import com.flagsmith.entities.Flag
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.awaitility.Awaitility
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilNotNull
import org.junit.Assert
import org.mockito.Mock

class RealTimeUpdatesTests {

    private lateinit var flagsmith: Flagsmith
    private lateinit var realTimeFlagsmith: Flagsmith

    // You'll need a valid account to test this
    private val environmentKey = "F5X4CN67ZmSB547j2k2nX4"

    @Before
    fun setup() {
        flagsmith = Flagsmith(
            environmentKey = environmentKey,
            enableAnalytics = false,
            cacheConfig = FlagsmithCacheConfig(enableCache = false),
            enableRealtimeUpdates = false
        )
        realTimeFlagsmith = Flagsmith(
            environmentKey = environmentKey,
            enableAnalytics = false,
            cacheConfig = FlagsmithCacheConfig(enableCache = false),
            enableRealtimeUpdates = true
        )
    }

    @After
    fun tearDown() {
    }

    @Test
    fun testGettingFlagsWithoutRealtimeUpdates() {
        var foundFromServer: List<Flag>? = null
        flagsmith.getFeatureFlags { result ->
            Assert.assertTrue(result.isSuccess)
            Assert.assertTrue(result.getOrThrow().isNotEmpty())
            foundFromServer = result.getOrThrow()
        }

        await untilNotNull { foundFromServer }
    }

}