package com.flagsmith.response

data class ResponseIdentity(
    val flags: ArrayList<Flag>,
    val responseTraits: ArrayList<ResponseTrait>
)