package com.flagsmith.builder

import com.flagsmith.api.*
import com.flagsmith.interfaces.*
import com.flagsmith.response.*

class Flagsmith private constructor(
    val tokenApiKey: String?,
    val environmentKey: String?,
    val baseUrl: String = DEFAULT_BASE_URL
) {
    companion object {
        const val DEFAULT_BASE_URL = "https://edge.api.flagsmith.com/api/v1/"
    }

    override fun toString(): String {
        return "Flagsmith(tokenApiKey=$tokenApiKey, environmentKey=$environmentKey, baseUrl='$baseUrl')"
    }

    fun getFeatureFlags(identity: String?, result: (Result<List<Flag>>) -> Unit) {
        if (identity != null) {
            GetIdentityFlagsAndTraits(this, identity, object : IIdentityFlagsAndTraitsResult {
                override fun success(response: IdentityFlagsAndTraits) {
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
                override fun success(response: IdentityFlagsAndTraits) {
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
                override fun success(response: IdentityFlagsAndTraits) {
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
            override fun success(response: IdentityFlagsAndTraits) {
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
            override fun success(response: IdentityFlagsAndTraits) {
                result(Result.success(response.traits))
            }

            override fun failed(e: Exception) {
                result(Result.failure(e))
            }
        })
    }

    fun setTrait(trait: Trait, identity: String, result: (Result<TraitWithIdentity>) -> Unit) {
        SetTrait(this, trait, identity, object: ITraitUpdateResult {
            override fun success(response: TraitWithIdentity) {
                result(Result.success(response))
            }

            override fun failed(exception: Exception) {
                result(Result.failure(exception))
            }
        })
    }

    fun getIdentity(identity: String, result: (Result<IdentityFlagsAndTraits>) -> Unit){
        GetIdentityFlagsAndTraits(this, identity = identity, object: IIdentityFlagsAndTraitsResult {
            override fun success(response: IdentityFlagsAndTraits) {
                result(Result.success(response))
            }

            override fun failed(e: Exception) {
                result(Result.failure(e))
            }
        })
    }

//    fun enableAnalytics(analytics: FlagsmithAnalytics) {
//        analytics(this, )
//    }


    data class Builder(
        var tokenApi: String? = null,
        var environmentKey: String? = null,
        var baseUrl: String? = null
    ) {

        fun tokenApi(v: String) = apply { this.tokenApi = v }
        fun environmentId(v: String) = apply { this.environmentKey = v }
        fun baseUrl(v: String) = apply { this.baseUrl = v }

        fun build(): Flagsmith {
            return Flagsmith(tokenApi, environmentKey, baseUrl ?: DEFAULT_BASE_URL)
        }

    }
}