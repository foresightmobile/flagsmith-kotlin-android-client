package com.flagsmith.response

data class ResponseIdentity(
    val flags: ArrayList<ResponseFlag>,
    val responseTraits: ArrayList<ResponseTrait>
)