package com.flagsmith.builder

import com.flagsmith.api.*
import com.flagsmith.interfaces.*
import com.flagsmith.response.Flag
import com.flagsmith.response.ResponseIdentityFlagsAndTraits
import com.flagsmith.response.Trait
import com.flagsmith.response.TraitWithIdentity

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
            GetIdentityFlagsAndTraits(this, identity, object : IIdentityFlagsAndTraitsResult {
                override fun success(response: ResponseIdentityFlagsAndTraits) {
                    result(Result.success(response.flags))
                }

                override fun failed(e: Exception) {
                    result(Result.failure(e))
                }
            })
        } else {
            GetFlags(this, object : IFlagArrayResult {
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
        if (identity != null) {
            GetIdentityFlagsAndTraits(this, identity = identity, object: IIdentityFlagsAndTraitsResult {
                override fun success(response: ResponseIdentityFlagsAndTraits) {
                    val flag = response.flags.find { flag -> flag.feature.name == forFeatureId && flag.enabled }
                    result(Result.success(flag != null))
                }

                override fun failed(e: Exception) {
                    result(Result.failure(e))
                }
            })
        } else {
            GetFlags(this, object : IFlagArrayResult {
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
    }

    fun getValueForFeature(searchFeatureId: String, identity: String? = null, result: (Result<Any?>) -> Unit) {
        if (identity != null) {
            GetIdentityFlagsAndTraits(this, identity = identity, object: IIdentityFlagsAndTraitsResult {
                override fun success(response: ResponseIdentityFlagsAndTraits) {
                    val flag = response.flags.find { flag -> flag.feature.name == searchFeatureId && flag.enabled }
                    result(Result.success(flag?.featureStateValue))
                }

                override fun failed(e: Exception) {
                    result(Result.failure(e))
                }
            })
        } else {
            GetFlags(this, object : IFlagArrayResult {
                override fun success(list: ArrayList<Flag>) {
                    val found = list.find { flag -> flag.feature.name == searchFeatureId }
                    result(Result.success(found?.featureStateValue))
                }

                override fun failed(str: String) {
                    result(Result.failure(IllegalStateException(str)))
                }
            })
        }
    }

    fun getTrait(id: String, identity: String, result: (Result<Trait?>) -> Unit) {
        GetIdentityFlagsAndTraits(this, identity = identity, object: IIdentityFlagsAndTraitsResult {
            override fun success(response: ResponseIdentityFlagsAndTraits) {
                val trait = response.traits.find { it.key == id }
                result(Result.success(trait))
            }

            override fun failed(e: Exception) {
                result(Result.failure(e))
            }
        })
    }

    fun getTraits(identity: String, result: (Result<List<Trait>>) -> Unit) {
        GetIdentityFlagsAndTraits(this, identity = identity, object: IIdentityFlagsAndTraitsResult {
            override fun success(response: ResponseIdentityFlagsAndTraits) {
                result(Result.success(response.traits))
            }

            override fun failed(e: Exception) {
                result(Result.failure(e))
            }
        })
    }

    fun setTrait(trait: Trait, identity: String, result:(Result<TraitWithIdentity>) -> Unit) {
        SetTrait(this, trait, identity, object: ITraitUpdateResult {
            override fun success(response: TraitWithIdentity) {
                result(Result.success(response))
            }

            override fun failed(exception: Exception) {
                result(Result.failure(exception))
            }
        })
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