package com.flagsmith.builder

import android.content.Context
import com.flagsmith.android.network.FlagsmithApi
import com.flagsmith.api.*
import com.flagsmith.interfaces.*
import com.flagsmith.response.*
import com.github.kittinunf.fuel.Fuel

class Flagsmith private constructor(
    val apiAuthToken: String?,
    val environmentKey: String,
    val baseUrl: String = DEFAULT_BASE_URL,
    val context: Context?,
    private val enableAnalytics: Boolean = DEFAULT_ENABLE_ANALYTICS,
    private val analyticsFlushPeriod: Int = DEFAULT_ANALYTICS_FLUSH_PERIOD_SECONDS
) {
    private var analytics: FlagsmithAnalytics? = null

    init {
        if (enableAnalytics && context != null) {
            this.analytics = FlagsmithAnalytics(this, context, analyticsFlushPeriod)
        }
        if (enableAnalytics && context == null) {
            throw IllegalArgumentException("Flagsmith requires a context to use the analytics feature")
        }
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://edge.api.flagsmith.com/api/v1/"
        const val DEFAULT_ENABLE_ANALYTICS = true
        const val DEFAULT_ANALYTICS_FLUSH_PERIOD_SECONDS = 10
    }

    fun getFeatureFlags(identity: String?, result: (Result<List<Flag>>) -> Unit) {
        if (identity != null) {
            Fuel.request(
                FlagsmithApi.getIdentityFlagsAndTraits(identity = identity, environmentKey = environmentKey))
                .responseObject(IdentityFlagsAndTraitsDeserializer()) { _, _, res ->
                    res.fold(
                        success = { value -> result(Result.success(value.flags)) },
                        failure = { err -> result(Result.failure(err)) }
                    )
                }
        } else {
            Fuel.request(FlagsmithApi.getFlags(environmentKey = environmentKey))
                .responseObject(FlagListDeserializer()) { _, _, res ->
                    res.fold(
                        success = { value -> result(Result.success(value)) },
                        failure = { err -> result(Result.failure(err)) }
                    )
                }
        }
    }

    fun hasFeatureFlag(forFeatureId: String, identity: String? = null, result:(Result<Boolean>) -> Unit) {
        getFeatureFlags(identity) { result ->
            result.fold(
                onSuccess = { flags ->
                    val foundFlag = flags.find { flag -> flag.feature.name == forFeatureId && flag.enabled }
                    analytics?.trackEvent(forFeatureId)
                    result(Result.success(foundFlag != null))
                },
                onFailure = { err -> result(Result.failure(err))}
            )
        }
    }

    fun getValueForFeature(searchFeatureId: String, identity: String? = null, result: (Result<Any?>) -> Unit) {
        getFeatureFlags(identity) { result ->
            result.fold(
                onSuccess = { flags ->
                    val foundFlag = flags.find { flag -> flag.feature.name == searchFeatureId && flag.enabled }
                    analytics?.trackEvent(searchFeatureId)
                    result(Result.success(foundFlag?.featureStateValue))
                },
                onFailure = { err -> result(Result.failure(err))}
            )
        }
    }

    fun getTrait(id: String, identity: String, result: (Result<Trait?>) -> Unit) {
        Fuel.request(
            FlagsmithApi.getIdentityFlagsAndTraits(identity = identity, environmentKey = environmentKey))
            .responseObject(IdentityFlagsAndTraitsDeserializer()) { _, _, res ->
                res.fold(
                    success = { value ->
                                val trait = value.traits.find { it.key == id }
                                result(Result.success(trait))
                              },
                    failure = { err -> result(Result.failure(err)) }
                )
            }
    }

    fun getTraits(identity: String, result: (Result<List<Trait>>) -> Unit) {
        Fuel.request(
            FlagsmithApi.getIdentityFlagsAndTraits(identity = identity, environmentKey = environmentKey))
            .responseObject(IdentityFlagsAndTraitsDeserializer()) { _, _, res ->
                res.fold(
                    success = { value -> result(Result.success(value.traits)) },
                    failure = { err -> result(Result.failure(err)) }
                )
            }
    }

    fun setTrait(trait: Trait, identity: String, result: (Result<TraitWithIdentity>) -> Unit) {
        Fuel.request(
            FlagsmithApi.setTrait(trait = trait, identity = identity, environmentKey = environmentKey))
            .responseObject(TraitWithIdentityDeserializer()) { _, _, res ->
                res.fold(
                    success = { value -> result(Result.success(value)) },
                    failure = { err -> result(Result.failure(err)) }
                )
            }
    }

    fun getIdentity(identity: String, result: (Result<IdentityFlagsAndTraits>) -> Unit){
        Fuel.request(
            FlagsmithApi.getIdentityFlagsAndTraits(identity = identity, environmentKey = environmentKey))
            .responseObject(IdentityFlagsAndTraitsDeserializer()) { _, _, res ->
                res.fold(
                    success = { value ->
                        result(Result.success(value))
                    },
                    failure = { err -> result(Result.failure(err)) }
                )
            }
    }

    override fun toString(): String {
        return "Flagsmith(apiAuthToken=$apiAuthToken, environmentKey=$environmentKey, baseUrl='$baseUrl', context=$context, enableAnalytics=$enableAnalytics, analyticsFlushPeriod=$analyticsFlushPeriod, analytics=$analytics)"
    }

    data class Builder(
        var apiAuthToken: String? = null,
        var environmentKey: String,
        var baseUrl: String? = null,
        var enableAnalytics: Boolean? = null,
        var analyticsFlushPeriod: Int? = null,
        var context: Context? = null
    ) {

        fun apiAuthToken(v: String) = apply { this.apiAuthToken = v }
        fun environmentKey(v: String) = apply { this.environmentKey = v }
        fun baseUrl(v: String) = apply { this.baseUrl = v }
        fun enableAnalytics(v: Boolean) = apply { this.enableAnalytics = v }
        fun analyticsFlushPeriod(v: Int) = apply { this.analyticsFlushPeriod = v }
        fun context(v: Context) = apply { this.context = v }

        fun build(): Flagsmith {
            return Flagsmith(apiAuthToken = apiAuthToken, environmentKey = environmentKey,
                baseUrl = baseUrl ?: DEFAULT_BASE_URL, enableAnalytics = enableAnalytics ?: DEFAULT_ENABLE_ANALYTICS,
                analyticsFlushPeriod = analyticsFlushPeriod ?: DEFAULT_ANALYTICS_FLUSH_PERIOD_SECONDS,
                context = context
            )
        }
    }



}