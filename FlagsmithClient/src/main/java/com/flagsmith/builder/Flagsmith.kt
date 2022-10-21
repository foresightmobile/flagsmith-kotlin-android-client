package com.flagsmith.builder

import com.flagsmith.api.*
import com.flagsmith.interfaces.*
import com.flagsmith.response.ResponseIdentity

class Flagsmith private constructor(
    val tokenApiKey: String?,
    val environmentKey: String?,
    val identity: String?
) {

    override fun toString(): String {
        return "tokenApi: $tokenApiKey /environmentId: $environmentKey /identifierUser: $identity"
    }


    fun getFeatureFlags(finish: IFlagArrayResult) {
        GetFlags(this, finish)
    }

    fun hasFeatureFlag(searchFeatureId: String, finish: IFlagSingle) {
        Feature(this, searchFeatureId, finish)
    }


    fun getTrait(finish: ITraitArrayResult) {
        Trait(this, finish)
    }

    fun getTraits(finish: ITraitArrayResult){
        Trait(this, finish)
    }

    fun setTrait(key: String, value: String, result:(Result<ResponseIdentity>) -> Unit) {
        SetTrait(this, key, value, object : IIdentityResult {
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
        var identity: String? = null
    ) {

        fun tokenApi(v: String) = apply { this.tokenApi = v }
        fun environmentId(v: String) = apply { this.environmentKey = v }
        fun identity(v: String) = apply { this.identity = v }


        fun build(): Flagsmith {
            return Flagsmith(tokenApi, environmentKey, identity)
        }

    }
}