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
    //TODO: Get from local properties file not in source control
    private val environmentKey = "F5X4CN67ZmSB547j2k2nX4"
    private val authToken = "Token 8653bcb29a9d074fd403618c6d2b98f2ace74ea6"

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

//    @Test
//    fun testGettingFlagsWithRealtimeUpdates() {
//        var foundFromServer: List<Flag>? = null
//        var testComplete: AtomicBoolean = AtomicBoolean(false);
//
//        flagsmith.getFeatureFlags { result ->
//            Assert.assertTrue(result.isSuccess)
//            Assert.assertTrue(result.getOrThrow().isNotEmpty())
//            foundFromServer = result.getOrThrow()
//        }
//
//        await untilNotNull { foundFromServer }
//
//        // Get the current value
//        var currentFlagValue: Double? = null
//        flagsmith.getValueForFeature("with-value") { result ->
//            Assert.assertTrue(result.isSuccess)
//            currentFlagValue = result.getOrThrow() as Double
//        }
//        await untilNotNull { currentFlagValue }
//
//        var newUpdatedFeatureValue: Double? = null
//        do {
//            flagsmith.getValueForFeature("with-value") { result ->
//                Assert.assertTrue(result.isSuccess)
//                newUpdatedFeatureValue = result.getOrThrow() as Double
//            }
//            await untilNotNull { newUpdatedFeatureValue }
//        } while (newUpdatedFeatureValue != currentFlagValue)
//
//        await untilTrue (testComplete)
//    }

    @Test(timeout = 1000000)
    fun testGettingFlagsWithRealtimeUpdatesUsingSynchronous() = runBlocking {
        // Get the current value
        val currentFlagValueDouble =
            flagsmith.getValueForFeatureSync("with-value").getOrThrow() as Double?
        Assert.assertNotNull(currentFlagValueDouble)
        val currentFlagValue: Int = currentFlagValueDouble!!.toInt()

        // After 5 seconds try to update the value using the retrofit service
        CoroutineScope(Dispatchers.IO).launch {
//            val featureStatesResponse = retrofitService
//                .getFeatureStates(featureName = "with-value", environmentKey = environmentKey, featureStateId = "165500")
//                .execute()
//            val map: Map<*, *>? = Gson().fromJson(featureStatesResponse.body(), Map::class.java)

            delay(5000)

            // val id: String = map?.get("results")?.let { (it as List<*>)[0] as Map<*, *> }?.get("id") as String
            val id = "165500"

            val response = retrofitService
                .setFeatureStates(authToken, id, environmentKey, FeatureStatePutBody(true, currentFlagValue.inc()))// "{\"enabled\": true, \"feature_state_value\": " + (currentFlagValue!!+1.0) + "}")
                .execute()
            Assert.assertTrue(response.isSuccessful)
        }

        var newUpdatedFeatureValue: Double?
        do {
            newUpdatedFeatureValue =
                flagsmith.getValueForFeatureSync("with-value").getOrThrow() as Double?
        } while (newUpdatedFeatureValue!! == currentFlagValueDouble)
    }
}