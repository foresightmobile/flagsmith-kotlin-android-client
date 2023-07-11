package com.flagsmith.entities

import com.google.gson.annotations.SerializedName

data class FlagEvent (
    //TODO: Make it a DateTime when we've seen the format
    @SerializedName(value = "updated_at") val updatedAt: String
)
