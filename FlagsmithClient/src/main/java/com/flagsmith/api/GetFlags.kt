package com.flagsmith.api


import android.net.Uri
import com.flagsmith.builder.Flagsmith
import com.flagsmith.response.Flag
import com.flagsmith.interfaces.INetworkListener
import com.flagsmith.android.network.NetworkFlag
import com.flagsmith.android.network.ApiManager
import com.flagsmith.interfaces.IFlagArrayResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.URLEncoder

class GetFlags(builder: Flagsmith, identity: String?, finish: IFlagArrayResult) {
    var finish: IFlagArrayResult
    var identity: String?
    var builder: Flagsmith

    init {
        this.identity = identity
        this.finish = finish
        this.builder = builder

        startAPI()
    }

    private fun startAPI() {
        var url = ApiManager.BaseUrl.Url + "flags/"

        //TODO: Not sure this does anything
        if (identity != null) {
           url += "?identifier=" + URLEncoder.encode(identity, "utf-8")
        }

        ApiManager(url, NetworkFlag.getNetworkHeader(builder), object : INetworkListener {
            override fun success(response: String?) {
                _parse(response!!, finish)
            }

            override fun failed(exception: Exception) {
                finish.failed(exception.localizedMessage ?: "Error getting flags")
            }

        })
    }

    fun _parse(json: String, finish: IFlagArrayResult) {
        try {
            val gson = Gson()
            val type = object : TypeToken<ArrayList<Flag>>() {}.type
            val responseFromJson: ArrayList<Flag> = gson.fromJson(json, type)
            println("parse() - responseFromJson: $responseFromJson")

            //finish
            finish.success(responseFromJson)
        } catch (e: Exception) {
            finish.failed("exception: $e")
        }
    }
}