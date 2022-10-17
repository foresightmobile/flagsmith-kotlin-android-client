package com.flagsmith.interfaces

import com.flagsmith.response.ResponseTraitUpdate

interface ITraitUpdateResult {
    fun success( response: ResponseTraitUpdate);
    fun failed(str : String );

}