package com.flagsmith.interfaces

import com.flagsmith.response.ResponseIdentity

interface IIdentityResult {
    fun success(response: ResponseIdentity)
    fun failed(e: Exception)
}
