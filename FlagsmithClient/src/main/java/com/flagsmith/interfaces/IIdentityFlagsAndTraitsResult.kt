package com.flagsmith.interfaces

import com.flagsmith.response.ResponseIdentityFlagsAndTraits

interface IIdentityFlagsAndTraitsResult {
    fun success(response: ResponseIdentityFlagsAndTraits)
    fun failed(e: Exception)
}
