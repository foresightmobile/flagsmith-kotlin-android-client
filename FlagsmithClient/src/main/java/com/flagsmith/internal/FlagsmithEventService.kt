package com.flagsmith.internal

import android.util.Log
import com.flagsmith.entities.FlagEvent
import com.flagsmith.entities.IdentityFlagsAndTraits
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

class FlagsmithEventService constructor(
    private val eventSourceUrl: String?,
    private val environmentKey: String,
    private val updates: (Result<FlagEvent>) -> Unit
) {
    private val defaultEventSourceHost = "https://realtime.flagsmith.com/"

    private val sseClient = OkHttpClient.Builder()
        .addInterceptor(FlagsmithRetrofitService.envKeyInterceptor(environmentKey))
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.MINUTES)
        .writeTimeout(10, TimeUnit.MINUTES)
        .build()

    private val defaultEventSourceUrl: String = defaultEventSourceHost + "sse/environments/" +  environmentKey + "/stream"

    private val sseRequest = Request.Builder()
        .url(eventSourceUrl ?: defaultEventSourceUrl)
        .header("Accept", "application/json")
        .addHeader("Accept", "text/event-stream")
        .build()

    var sseEventsFlow = MutableStateFlow(FlagEvent(updatedAt = 0.0))
        private set

    private val sseEventSourceListener = object : EventSourceListener() {
        override fun onClosed(eventSource: EventSource) {
            super.onClosed(eventSource)
            Log.d(TAG, "onClosed: $eventSource")
            updates(Result.failure(IllegalStateException("Connection Closed")))
            // val event = FlagEvent(STATUS.CLOSED)
            // sseEventsFlow.tryEmit(event)
        }

        override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
            super.onEvent(eventSource, id, type, data)
            Log.d(TAG, "onEvent: $data")
            if (type != null && type == "environment_updated" && data.isNotEmpty()) {
                val flagEvent = Gson().fromJson(data, FlagEvent::class.java)
                sseEventsFlow.tryEmit(flagEvent)
                updates(Result.success(flagEvent))
            }
        }

        override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
            super.onFailure(eventSource, t, response)
            t?.printStackTrace()
            Log.d(TAG, "onFailure: ${t?.message}")
            if (t != null)
                updates(Result.failure(t))
            else
                updates(Result.failure(Throwable("Unknown error")))

//            val event = SSEEventData(STATUS.ERROR)
//            sseEventsFlow.tryEmit(event)
        }

        override fun onOpen(eventSource: EventSource, response: Response) {
            super.onOpen(eventSource, response)
            Log.d(TAG, "onOpen: $eventSource")
//            val event = SSEEventData(STATUS.OPEN)
//            sseEventsFlow.tryEmit(event)
        }
    }

    init {
        initEventSource()
    }

    private fun initEventSource() {
        EventSources.createFactory(sseClient)
            .newEventSource(request = sseRequest, listener = sseEventSourceListener)
    }

    companion object {
        private const val TAG = "FlagsmithEventService"
    }
}