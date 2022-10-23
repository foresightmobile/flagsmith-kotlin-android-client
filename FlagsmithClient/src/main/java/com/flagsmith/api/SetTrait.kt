package com.flagsmith.api

import com.flagsmith.builder.Flagsmith
import com.flagsmith.interfaces.IIdentityFlagsAndTraitsResult
import com.flagsmith.interfaces.INetworkListener
import com.flagsmith.android.network.NetworkFlag
import com.flagsmith.android.network.ApiManager
import com.flagsmith.response.ResponseIdentity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SetTrait(builder: Flagsmith, key: String, value: String, identity: String, finish: IIdentityFlagsAndTraitsResult) {
    var finish: IIdentityFlagsAndTraitsResult
    var key: String
    var value: String
    var identity: String
    var builder: Flagsmith

    init {
        this.finish = finish
        this.key = key
        this.value = value
        this.identity = identity
        this.builder = builder

        if (validateData()) {
            startAPI()
        }
    }

    private fun validateData(): Boolean {
        val result = true
        //check identifier null
        if (identity.isEmpty()) {
            finish.failed(kotlin.IllegalStateException("User Identifier must to set in class 'FlagsmithBuilder' first"))
            return false
        }

        return result
    }

    private fun startAPI() {
        val url = ApiManager.BaseUrl.Url + "traits/"

        val header = NetworkFlag.getNetworkHeader(builder)

        ApiManager(
            url,
            header,
            getJsonPostBody(),
            object : INetworkListener {
                override fun success(response: String?) {
                    _parse(response!!)
                }

                override fun failed(exception: Exception) {
                    finish.failed(exception)
                }

            })
    }

    private fun getJsonPostBody(): String {
        return "{\n" +
                "    \"identity\": {\n" +
                "        \"identifier\": \"" + identity + "\"\n" +
                "    },\n" +
                "    \"trait_key\": \"" + key + "\",\n" +
                "    \"trait_value\": \"" + value + "\"\n" +
                "}"
    }


    fun _parse(json: String) {
        try {
            val gson = Gson()
            val type = object : TypeToken<ResponseIdentity>() {}.type
            val responseFromJson: ResponseIdentity = gson.fromJson(json, type)
            println("parse() - responseFromJson: $responseFromJson")

            //finish
            finish.success(responseFromJson)
        } catch (e: Exception) {
            finish.failed(e)
        }
    }


}