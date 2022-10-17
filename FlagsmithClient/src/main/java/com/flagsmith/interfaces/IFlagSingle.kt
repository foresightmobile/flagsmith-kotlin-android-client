package com.flagsmith.interfaces

import com.flagsmith.response.ResponseFlag

interface IFlagSingle {
    fun success(flag: ResponseFlag)
    fun failed(str: String)
}