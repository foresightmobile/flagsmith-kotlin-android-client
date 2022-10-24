package com.flagsmith.api


import com.flagsmith.builder.Flagsmith
import com.flagsmith.interfaces.ITraitArrayResult
import com.flagsmith.response.ResponseTraits
import com.flagsmith.interfaces.INetworkListener
import com.flagsmith.android.network.NetworkFlag
import com.flagsmith.android.network.ApiManager
import com.flagsmith.interfaces.IIdentityFlagsAndTraitsResult
import com.flagsmith.response.ResponseIdentityFlagsAndTraits
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GetIdentityFlagsAndTraits(builder: Flagsmith, identity: String, finish: IIdentityFlagsAndTraitsResult) {

    var finish: IIdentityFlagsAndTraitsResult
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
                finish.failed(exception)
            }

        })
    }


    fun _parse(json: String) {
        try {
            val gson = Gson()
            val type = object : TypeToken<ResponseIdentityFlagsAndTraits>() {}.type
            val responseFromJson: ResponseIdentityFlagsAndTraits = gson.fromJson(json, type)
            println("parse() - responseFromJson: $responseFromJson")

            //finish
            finish.success(responseFromJson)
        } catch (e: Exception) {
            finish.failed(e)
        }
    }


}