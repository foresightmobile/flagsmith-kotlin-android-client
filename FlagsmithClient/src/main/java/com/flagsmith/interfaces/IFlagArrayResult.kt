package com.flagsmith.interfaces

import com.flagsmith.response.ResponseFlag

interface IFlagArrayResult {
    fun success(list: ArrayList<ResponseFlag>)
    fun failed(str: String)
}