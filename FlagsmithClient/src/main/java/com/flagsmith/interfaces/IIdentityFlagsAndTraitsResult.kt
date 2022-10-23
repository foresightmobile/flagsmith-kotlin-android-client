package com.flagsmith.interfaces

import com.flagsmith.response.ResponseIdentity

interface IIdentityFlagsAndTraitsResult {
    fun success(response: ResponseIdentity)
    fun failed(e: Exception)
}
