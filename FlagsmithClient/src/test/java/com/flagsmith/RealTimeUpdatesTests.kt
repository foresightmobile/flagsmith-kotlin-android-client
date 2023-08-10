package com.flagsmith

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import com.flagsmith.entities.Flag
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilNotNull
import org.awaitility.kotlin.untilTrue
import org.junit.Assert
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class RealTimeUpdatesTests {

    private lateinit var flagsmith: Flagsmith
    private lateinit var realTimeFlagsmith: Flagsmith

    // You'll need a valid account to test this
    private val environmentKey = "F5X4CN67ZmSB547j2k2nX4"

    @Mock
    private lateinit var mockApplicationContext: Context

    @Mock
    private lateinit var mockContextResources: Resources

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Before
    fun setup() {
        setupMocks()

//        flagsmith = Flagsmith(
//            environmentKey = environmentKey,
//            enableAnalytics = false,
//            cacheConfig = FlagsmithCacheConfig(enableCache = false),
//            enableRealtimeUpdates = false,
//            context = mockApplicationContext,
//        )

        // We need the cache otherwise we'd be getting the new values from the server all the time
        // Rather than seeing the realtime updates
        flagsmith = Flagsmith(
            environmentKey = environmentKey,
            enableAnalytics = false,
            cacheConfig = FlagsmithCacheConfig(enableCache = true),
            enableRealtimeUpdates = true,
            context = mockApplicationContext,
        )
    }

    @After
    fun tearDown() {
    }

    private fun setupMocks() {
        MockitoAnnotations.initMocks(this)

        Mockito.`when`(mockApplicationContext.getResources()).thenReturn(mockContextResources)
        Mockito.`when`(
            mockApplicationContext.getSharedPreferences(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyInt()
            )
        ).thenReturn(
            mockSharedPreferences
        )
        Mockito.`when`(mockApplicationContext.cacheDir).thenReturn(File("cache"))

        Mockito.`when`(mockContextResources.getString(ArgumentMatchers.anyInt())).thenReturn("mocked string")
        Mockito.`when`(mockContextResources.getStringArray(ArgumentMatchers.anyInt())).thenReturn(
            arrayOf(
                "mocked string 1",
                "mocked string 2"
            )
        )
        Mockito.`when`(mockContextResources.getColor(ArgumentMatchers.anyInt())).thenReturn(Color.BLACK)
        Mockito.`when`(mockContextResources.getBoolean(ArgumentMatchers.anyInt())).thenReturn(false)
        Mockito.`when`(mockContextResources.getDimension(ArgumentMatchers.anyInt())).thenReturn(100f)
        Mockito.`when`(mockContextResources.getIntArray(ArgumentMatchers.anyInt()))
            .thenReturn(intArrayOf(1, 2, 3))
    }

    @Test
    fun testGettingFlagsWithRealtimeUpdates() {
        var foundFromServer: List<Flag>? = null
        var testComplete: AtomicBoolean = AtomicBoolean(false);

        flagsmith.getFeatureFlags { result ->
            Assert.assertTrue(result.isSuccess)
            Assert.assertTrue(result.getOrThrow().isNotEmpty())
            foundFromServer = result.getOrThrow()
        }

        await untilNotNull { foundFromServer }

        // Get the current value
        var currentFlagValue: Double? = null
        flagsmith.getValueForFeature("with-value") { result ->
            Assert.assertTrue(result.isSuccess)
            currentFlagValue = result.getOrThrow() as Double
        }
        await untilNotNull { currentFlagValue }

        var newUpdatedFeatureValue: Double? = null
        do {
            flagsmith.getValueForFeature("with-value") { result ->
                Assert.assertTrue(result.isSuccess)
                newUpdatedFeatureValue = result.getOrThrow() as Double
            }
            await untilNotNull { newUpdatedFeatureValue }
        } while (newUpdatedFeatureValue != currentFlagValue)

        await untilTrue (testComplete)
    }

    @Test(timeout = 100000)
    fun testGettingFlagsWithRealtimeUpdatesUsingSynchronous() = runBlocking {
        // Get the current value
        val currentFlagValue =
            flagsmith.getValueForFeatureSync("with-value").getOrThrow() as Double?
        Assert.assertNotNull(currentFlagValue)

        var newUpdatedFeatureValue: Double? = null
        do {
            newUpdatedFeatureValue =
                flagsmith.getValueForFeatureSync("with-value").getOrThrow() as Double?
        } while (newUpdatedFeatureValue!!.equals(currentFlagValue!!))
    }
}