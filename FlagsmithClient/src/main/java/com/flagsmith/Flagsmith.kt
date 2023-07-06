package com.flagsmith

import android.content.Context
import android.util.Log
import com.flagsmith.entities.*
import com.flagsmith.internal.FlagsmithAnalytics
import com.flagsmith.internal.FlagsmithClient
import com.flagsmith.internal.FlagsmithRetrofitService
import com.github.kittinunf.fuse.android.config
import com.github.kittinunf.fuse.android.defaultAndroidMemoryCache
import com.github.kittinunf.fuse.core.*
import com.github.kittinunf.result.isSuccess
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

/**
 * Flagsmith
 *
 * The main interface to all of the Flagsmith functionality
 *
 * @property environmentKey Take this API key from the Flagsmith dashboard and pass here
 * @property baseUrl By default we'll connect to the Flagsmith backend, but if you self-host you can configure here
 * @property context The current context is required to use the Flagsmith Analytics functionality
 * @property enableAnalytics Enable analytics - default true
 * @property analyticsFlushPeriod The period in seconds between attempts by the Flagsmith SDK to push analytic events to the server
 * @constructor Create empty Flagsmith
 */
class Flagsmith constructor(
    private val environmentKey: String,
    private val baseUrl: String = "https://edge.api.flagsmith.com/api/v1",
    private val context: Context? = null,
    private val enableAnalytics: Boolean = DEFAULT_ENABLE_ANALYTICS,
    private val enableCache: Boolean = DEFAULT_ENABLE_CACHE,
    private val analyticsFlushPeriod: Int = DEFAULT_ANALYTICS_FLUSH_PERIOD_SECONDS,
    private val defaultFlags: List<Flag> = emptyList()
) {
    private val client: FlagsmithClient = FlagsmithClient(baseUrl, environmentKey)
    private val retrofit: FlagsmithRetrofitService = FlagsmithRetrofitService.create(baseUrl, environmentKey)
    private val analytics: FlagsmithAnalytics? =
        if (!enableAnalytics) null
        else if (context != null) FlagsmithAnalytics(context, client, analyticsFlushPeriod)
        else throw IllegalArgumentException("Flagsmith requires a context to use the analytics feature")

    // The cache can be overridden if necessary for e.g. a file-based cache
    var identityFlagsAndTraitsCache: Cache<IdentityFlagsAndTraits>? =
        if (!enableCache) null
        else if (context == null) throw IllegalArgumentException("Flagsmith requires a context to use the cache feature")
        else getDefaultCache(IdentityFlagsAndTraitsDataConvertible())

    var flagsCache: Cache<List<Flag>>? =
        if (!enableCache) null
        else if (context == null) throw IllegalArgumentException("Flagsmith requires a context to use the cache feature")
        else getDefaultCache(FlagsConvertible())

    companion object {
        const val DEFAULT_ENABLE_ANALYTICS = true
        const val DEFAULT_ENABLE_CACHE = true
        const val DEFAULT_ANALYTICS_FLUSH_PERIOD_SECONDS = 10
        const val DEFAULT_CACHE_KEY = "flagsmith"
    }

    // Default in-memory cache to be used when API requests fail
    // Pass to the cache parameter of the constructor to override
    private fun <T : Any> getDefaultCache(convertible: Fuse.DataConvertible<T>): Cache<T> {
        return CacheBuilder.config(context!!, convertible = convertible) {
            memCache = defaultAndroidMemoryCache()
        }.build()
    }

    fun getFeatureFlags(identity: String? = null, result: (Result<List<Flag>>) -> Unit) {
        if (identity != null) {
            retrofit.getIdentityFlagsAndTraits(identity).cachedEnqueueWithResult(identityFlagsAndTraitsCache) { res ->
                result(res.map { it.flags })
            }
        } else {
            retrofit.getFlags().cachedEnqueueWithResult(flagsCache, result)
        }
    }

    fun hasFeatureFlag(
        featureId: String,
        identity: String? = null,
        result: (Result<Boolean>) -> Unit
    ) = getFeatureFlag(featureId, identity) { res ->
        result(res.map { flag -> flag != null })
    }

    fun getValueForFeature(
        featureId: String,
        identity: String? = null,
        result: (Result<Any?>) -> Unit
    ) = getFeatureFlag(featureId, identity) { res ->
        result(res.map { flag -> flag?.featureStateValue })
    }

    fun getTrait(id: String, identity: String, result: (Result<Trait?>) -> Unit) =
        retrofit.getIdentityFlagsAndTraits(identity).cachedEnqueueWithResult(identityFlagsAndTraitsCache) { res ->
            result(res.map { value -> value.traits.find { it.key == id } })
        }

    fun getTraits(identity: String, result: (Result<List<Trait>>) -> Unit) =
        retrofit.getIdentityFlagsAndTraits(identity).cachedEnqueueWithResult(identityFlagsAndTraitsCache) { res ->
            result(res.map { it.traits })
        }

    fun setTrait(trait: Trait, identity: String, result: (Result<TraitWithIdentity>) -> Unit) {
        val call = retrofit.postTraits(TraitWithIdentity(trait.key, trait.value, Identity(identity)))
        call.enqueue(object : Callback<TraitWithIdentity> {
            override fun onResponse(
                call: Call<TraitWithIdentity>,
                response: Response<TraitWithIdentity>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    result(Result.success(response.body()!!))
                } else {
                    result(Result.failure(HttpException(response)))
                }
            }

            override fun onFailure(call: Call<TraitWithIdentity>, t: Throwable) {
                result(Result.failure(t))
            }
        })
    }

    fun getIdentity(identity: String, result: (Result<IdentityFlagsAndTraits>) -> Unit) =
        retrofit.getIdentityFlagsAndTraits(identity).cachedEnqueueWithResult(identityFlagsAndTraitsCache, result)

    private fun getFeatureFlag(
        featureId: String,
        identity: String?,
        result: (Result<Flag?>) -> Unit
    ) = getFeatureFlags(identity) { res ->
        result(res.map { flags ->
            val foundFlag = flags.find { flag -> flag.feature.name == featureId && flag.enabled }
            analytics?.trackEvent(featureId)
            foundFlag
        })
    }

    // Convert a Retrofit Call to a Result by extending the Call class
    private fun <T> Call<T>.enqueueWithResult(result: (Result<T>) -> Unit) {
        this.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful && response.body() != null) {
                    result(Result.success(response.body()!!))
                } else {
                    result(Result.failure(HttpException(response)))
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                result(Result.failure(t))
            }
        })
    }

    private fun <T : Any> Call<T>.cachedEnqueueWithResult(cache: Cache<T>?, result: (Result<T>) -> Unit) {
        val cacheKey = this.request().url().toString()
        this.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    cache?.put(key = cacheKey, putValue = body).also { cacheResult ->
                        if (cacheResult != null) {
                            if (!cacheResult.isSuccess()) {
                                Log.e("Flagsmith", "Failed to cache flags and traits")
                            }
                        }
                    }
                    result(Result.success(response.body()!!))
                } else {
                    // Reuse the onFailure callback to handle non-200 responses and avoid code duplication
                    onFailure(call, HttpException(response))
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                if (cache != null) {
                    cache?.get(key = DEFAULT_CACHE_KEY)?.fold(
                        success = { value ->
                            Log.i("Flagsmith", "Using cached result")
                            result(Result.success(value))
                        },
                        failure = { err ->
                            Log.e("Flagsmith", "Failed to get cached result")
                            result(Result.failure(err))
                        }
                    )
                } else {
                    result(Result.failure(t))
                }
            }
        })
    }
}