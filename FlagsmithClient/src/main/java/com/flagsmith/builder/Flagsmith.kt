package com.flagsmith.builder

import com.flagsmith.api.*
import com.flagsmith.interfaces.*
import com.flagsmith.response.Flag
import com.flagsmith.response.ResponseIdentity

class Flagsmith private constructor(
    val tokenApiKey: String?,
    val environmentKey: String?,
//    val identity: String?
) {

    override fun toString(): String {
        return "tokenApi: $tokenApiKey /environmentId: $environmentKey"
    }

    fun getFeatureFlags(identity: String?, result: (Result<List<Flag>>) -> Unit) {
        if (identity != null) {
            GetIdentityFlagsAndTraits(this, identity, object : )
        } else {
            GetFlags(this, identity = identity, object : IFlagArrayResult {
                override fun success(list: ArrayList<Flag>) {
                    result(Result.success(list))
                }

                override fun failed(str: String) {
                    result(Result.failure(IllegalStateException(str)))
                }
            })
        }
    }

    fun hasFeatureFlag(forFeatureId: String, identity: String? = null, result:(Result<Boolean>) -> Unit) {
        GetFlags(this, identity, object : IFlagArrayResult {
            override fun success(list: ArrayList<Flag>) {
                val found = list.find { flag -> flag.feature.name == forFeatureId }
                val enabled = found?.enabled ?: false
                result(Result.success(enabled))
            }

            override fun failed(str: String) {
                result(Result.failure(IllegalStateException(str)))
            }
        })
    }

    fun getValueForFeature(searchFeatureId: String, identity: String? = null, result: (Result<Any?>) -> Unit) {
        GetFlags(this, identity, object : IFlagArrayResult {
            override fun success(list: ArrayList<Flag>) {
                val found = list.find { flag -> flag.feature.name == searchFeatureId }
                result(Result.success(found?.featureStateValue))
            }

            override fun failed(str: String) {
                result(Result.failure(IllegalStateException(str)))
            }
        })
    }

    fun getTrait(forIdentity: String, finish: ITraitArrayResult) {
        GetIdentityFlagsAndTraits(this, forIdentity, finish)
    }

    fun getTraits(forIdentity: String, finish: ITraitArrayResult){
        GetIdentityFlagsAndTraits(this, forIdentity, finish)
    }

    fun setTrait(key: String, value: String, identity: String, result:(Result<ResponseIdentity>) -> Unit) {
        SetTrait(this, key, value, identity, object : IIdentityFlagsAndTraitsResult {
                override fun success(response: ResponseIdentity) {
                    result(Result.success(response))
                }

                override fun failed(e: Exception) {
                    result(Result.failure(e))
                }
            }
        )
    }
//
//    fun getIdentity (finish: IIdentity){
//        Identity(this, finish)
//    }
//
//    fun enableAnalytics(analytics: FlagsmithAnalytics) {
//        analytics(this, )
//    }


    data class Builder(
        var tokenApi: String? = null,
        var environmentKey: String? = null,
        // var identity: String? = null
    ) {

        fun tokenApi(v: String) = apply { this.tokenApi = v }
        fun environmentId(v: String) = apply { this.environmentKey = v }
        // fun identity(v: String) = apply { this.identity = v }


        fun build(): Flagsmith {
            return Flagsmith(tokenApi, environmentKey)
        }

    }
}