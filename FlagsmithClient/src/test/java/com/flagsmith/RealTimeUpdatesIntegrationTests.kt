package com.flagsmith

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import com.flagsmith.entities.FeatureStatePutBody
import com.flagsmith.entities.Flag
import com.flagsmith.internal.FlagsmithEventTimeTracker
import com.flagsmith.internal.FlagsmithRetrofitService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

class RealTimeUpdatesIntegrationTests : FlagsmithEventTimeTracker {

    private lateinit var flagsmith: Flagsmith

    private lateinit var retrofitService: FlagsmithRetrofitService

    // You'll need a valid account to test this
    private val environmentKey = System.getenv("ENV_KEY")
    private val authToken = "Token " + System.getenv("API_TOKEN")
    private val featureId = System.getenv("FEATURE_ID") ?: "integration-test-feature"
    private val featureStateId = System.getenv("FEATURE_STATE_ID") ?: "313512"

    @Mock
    private lateinit var mockApplicationContext: Context

    @Mock
    private lateinit var mockContextResources: Resources

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    // FlagsmithEventTimeTracker
    override var lastSeenAt: Double = 0.0

    @Before
    fun setup() {
        setupMocks()

        // We need the cache otherwise we'd be getting the new values from the server all the time
        // Rather than seeing the realtime updates
        flagsmith = Flagsmith(
            environmentKey = environmentKey!!,
            enableAnalytics = false,
            cacheConfig = FlagsmithCacheConfig(enableCache = true),
            enableRealtimeUpdates = true,
            context = mockApplicationContext,
        )

        val requestTimeoutSeconds: Long = 4L
        val readTimeoutSeconds: Long = 6L
        val writeTimeoutSeconds: Long = 6L

        retrofitService = FlagsmithRetrofitService.create(
            baseUrl = "https://api.flagsmith.com/api/v1/", environmentKey = environmentKey, context = mockApplicationContext, cacheConfig = FlagsmithCacheConfig(enableCache = false),
            timeTracker = this, requestTimeoutSeconds = requestTimeoutSeconds, readTimeoutSeconds = readTimeoutSeconds, writeTimeoutSeconds = writeTimeoutSeconds).first
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

    @Test(timeout = 10000) // Update after 5 secs, should be done in 10 seconds or fail
    fun testGettingFlagsWithRealtimeUpdatesAfterPuttingNewValue() = runBlocking {
        // Get the current value
        val currentFlagValueDouble =
            flagsmith.getValueForFeatureSync(featureId).getOrThrow() as Double?
        Assert.assertNotNull(currentFlagValueDouble)
        val currentFlagValue: Int = currentFlagValueDouble!!.toInt()

        // After 5 seconds try to update the value using the retrofit service
        CoroutineScope(Dispatchers.IO).launch {
            // Wait 5 seconds before updating the value
            delay(5000)

            val response = retrofitService
                .setFeatureStates(authToken, featureStateId, environmentKey!!, FeatureStatePutBody(true, currentFlagValue.inc()))
                .execute()
            Assert.assertTrue(response.isSuccessful)
        }

        var newUpdatedFeatureValue: Double?
        do {
            newUpdatedFeatureValue =
                flagsmith.getValueForFeatureSync(featureId).getOrThrow() as Double?
        } while (newUpdatedFeatureValue!! == currentFlagValueDouble)
    }
}