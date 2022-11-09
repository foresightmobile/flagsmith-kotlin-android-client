package com.flagsmith.android.network

import com.github.kittinunf.fuel.core.HeaderValues
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.util.FuelRouting
import okhttp3.internal.toImmutableMap

sealed class FlagsmithApi(
    private val environmentKey: String
    ): FuelRouting {
    class getIdentityFlagsAndTraits(val identity: String, environmentKey: String): FlagsmithApi(environmentKey)
    class getFlags(environmentKey: String): FlagsmithApi(environmentKey)

    override val basePath = "https://edge.api.flagsmith.com/api/v1"

    override val body: String?
        get() = null

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
            }
        }

    override val params: Parameters?
        get() {
            return when(this) {
                is getIdentityFlagsAndTraits -> listOf("identifier" to this.identity)
                else -> null
            }
        }

    override val path: String
        get() {
            return when(this) {
                is getIdentityFlagsAndTraits -> "/identities"
                is getFlags -> "/flags"
            }
        }
}