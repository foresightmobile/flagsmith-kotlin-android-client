package com.flagsmith.interfaces

import com.flagsmith.response.ResponseTrait

interface ITraitArrayResult {
    fun success( list: ArrayList<ResponseTrait>)
    fun failed(str : String )
}