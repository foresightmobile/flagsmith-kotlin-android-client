package com.flagsmith.api


import com.flagsmith.builder.Flagsmith
import com.flagsmith.interfaces.ITraitArrayResult
import com.flagsmith.response.ResponseTraits
import com.flagsmith.interfaces.INetworkListener
import com.flagsmith.android.network.NetworkFlag
import com.flagsmith.android.network.ApiManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GetTrait(builder: Flagsmith, identity: String, finish: ITraitArrayResult) {

    var finish: ITraitArrayResult
    var builder: Flagsmith
    var identity: String

    init {
        this.finish = finish
        this.builder = builder
        this.identity = identity

        if (validateData()) {
            startAPI()
        }
    }

    private fun validateData(): Boolean {
        val result = true

        //check identifier null
//        if (builder.identity.isNullOrEmpty()) {
//            finish.failed("User Identifier must to set in class 'FlagsmithBuilder' first")
//            return false
//        }

        return result
    }

    private fun startAPI() {
        val url = ApiManager.BaseUrl.Url + "identities/?identifier=" + identity
        ApiManager(url, NetworkFlag.getNetworkHeader(builder), object :
            INetworkListener {
            override fun success(response: String?) {
                _parse(response!!)
            }

            override fun failed(exception: Exception) {
                finish.failed(exception.localizedMessage ?: "Error getting trait")
            }

        })
    }


    fun _parse(json: String) {
        try {
            val gson = Gson()
            val type = object : TypeToken<ResponseTraits>() {}.type
            val responseFromJson: ResponseTraits = gson.fromJson(json, type)
            println("parse() - responseFromJson: $responseFromJson")

            //finish
            finish.success(responseFromJson.responseTraits)
        } catch (e: Exception) {
            finish.failed("exception: $e")
        }
    }


}