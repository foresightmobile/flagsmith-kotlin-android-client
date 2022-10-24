package com.flagsmith.response

data class ResponseIdentityFlagsAndTraits(
    val flags: ArrayList<Flag>,
    val responseTraits: ArrayList<ResponseTrait>
)