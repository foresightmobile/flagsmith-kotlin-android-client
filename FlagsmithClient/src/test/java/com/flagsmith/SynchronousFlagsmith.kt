package com.flagsmith

import com.flagsmith.builder.Flagsmith
import com.flagsmith.response.Flag
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun Flagsmith.hasFeatureFlagSync(searchFeatureId: String): Result<Boolean>
    = suspendCoroutine { cont -> this.hasFeatureFlag(searchFeatureId) { cont.resume(it) } }

suspend fun Flagsmith.getFeatureFlagsSync() : Result<List<Flag>>
    = suspendCoroutine { cont -> this.getFeatureFlags { cont.resume(it) } }

suspend fun Flagsmith.getValueForFeatureSync(searchFeatureId: String, identity: String? = null): Result<Any?>
    = suspendCoroutine { cont -> this.getValueForFeature(searchFeatureId, identity) { cont.resume(it) } }
