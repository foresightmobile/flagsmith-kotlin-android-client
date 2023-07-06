package com.flagsmith.entities

import com.flagsmith.internal.Deserializer
import com.flagsmith.internal.fromJson
import com.github.kittinunf.fuse.core.Fuse
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

class FlagListDeserializer: Deserializer<List<Flag>> {
    override fun deserialize(reader: Reader): List<Flag>? {
        val type = object : TypeToken<ArrayList<Flag>>() {}.type
        return reader.fromJson<ArrayList<Flag>?>(type = type)
    }
}

data class Flag(
    val feature: Feature,
    @SerializedName(value = "feature_state_value") val featureStateValue: Any?,
    val enabled: Boolean
)

data class Feature(
    val id: Long,
    val name: String,
    @SerializedName(value = "created_date") val createdDate: String,
    val description: String,
    @SerializedName(value = "initial_value") val initialValue: String,
    @SerializedName(value = "default_enabled") val defaultEnabled: Boolean,
    val type: String
)

class FlagsConvertible: Fuse.DataConvertible<List<Flag>> {
    override fun convertFromData(bytes: ByteArray): List<Flag> {
        val collectionType: TypeToken<List<Flag>> = object : TypeToken<List<Flag>>() {}
        return Gson().fromJson(String(bytes), collectionType)
    }

    override fun convertToData(value: List<Flag>): ByteArray =
        Gson().toJson(value).toByteArray()
}