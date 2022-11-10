package com.flagsmith.internal

import com.flagsmith.entities.Identity
import com.flagsmith.entities.Trait
import com.flagsmith.entities.TraitWithIdentity
import com.github.kittinunf.fuel.core.HeaderValues
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.util.FuelRouting
import com.google.gson.Gson
import okhttp3.internal.toImmutableMap

sealed class FlagsmithApi(
    private val environmentKey: String,
    private val baseUrl: String
    ): FuelRouting {
    class getIdentityFlagsAndTraits(val identity: String, environmentKey: String, baseUrl: String): FlagsmithApi(environmentKey, baseUrl)
    class getFlags(environmentKey: String, baseUrl: String): FlagsmithApi(environmentKey, baseUrl)
    class setTrait(val trait: Trait, val identity: String, environmentKey: String, baseUrl: String): FlagsmithApi(environmentKey, baseUrl)
    class postAnalytics(val eventMap: Map<String, Int?>, environmentKey: String, baseUrl: String): FlagsmithApi(environmentKey, baseUrl)

    override val basePath: String
        get() = baseUrl

    override val body: String?
        get() {
            return when(this) {
                is setTrait -> Gson().toJson(TraitWithIdentity(key = trait.key, value = trait.value, identity = Identity(identity)))
                is postAnalytics -> Gson().toJson(eventMap)
                else -> null
            }
        }

    override val bytes: ByteArray?
        get() = null

    override val headers: Map<String, HeaderValues>?
        get() {
            val headers = mutableMapOf<String, HeaderValues>("X-Environment-Key" to listOf(environmentKey))
            if (method == Method.POST) {
                headers["Content-Type"] = listOf("application/json")
            }
            return headers.toImmutableMap()
        }

    override val method: Method
        get() {
            return when(this) {
                is getIdentityFlagsAndTraits -> Method.GET
                is getFlags -> Method.GET
                is setTrait -> Method.POST
                is postAnalytics -> Method.POST
            }
        }

    override val params: Parameters?
        get() {
            return when(this) {
                is getIdentityFlagsAndTraits -> listOf("identifier" to this.identity)
                is setTrait -> listOf("identifier" to this.identity)
                else -> null
            }
        }

    override val path: String
        get() {
            return when(this) {
                is getIdentityFlagsAndTraits -> "/identities"
                is getFlags -> "/flags"
                is setTrait -> "/traits"
                is postAnalytics -> "/analytics/flags"
            }
        }
}