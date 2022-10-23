package com.flagsmith.api

import com.flagsmith.builder.Flagsmith
import com.flagsmith.interfaces.IFlagArrayResult
import com.flagsmith.interfaces.IFlagSingle
import com.flagsmith.response.Flag

class Feature(builder: Flagsmith, searchText: String, finish: IFlagSingle) {

    var builder: Flagsmith
    var searchText: String
    var finish: IFlagSingle
    var resultList: ArrayList<Flag> = ArrayList()

    init {
        this.searchText = searchText
        this.finish = finish
        this.builder = builder

        startAPI()
    }


    private fun startAPI() {
        GetFlags(builder, identity = null, object : IFlagArrayResult {
            override fun success(list: ArrayList<Flag>) {
                resultList = list
                searchInResult()
            }

            override fun failed(str: String) {
                finish.failed(str)
            }
        })
    }


    private fun searchInResult() {

        //check empty
        if (resultList.size == 0) {
            finish.failed("Not Found")
            return
        }
        for (m in resultList) {
            if (m.feature.name == searchText) {
                foundResult(m)
                return
            }
        }
        //case: not found result
        finish.failed("Not Found")
    }

    private fun foundResult(m: Flag) {
        finish.success(m)
    }
}