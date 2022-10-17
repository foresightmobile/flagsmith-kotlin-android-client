package com.flagsmith.android.screens.flag

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flagsmith.builder.Flagsmith
import com.flagsmith.interfaces.IFlagArrayResult
import com.flagsmith.response.ResponseFlag
import com.flagmsith.R
import com.flagsmith.android.adapter.FlagAdapter
import com.flagsmith.android.adapter.FlagPickerSelect

import com.flagsmith.android.helper.Helper
import com.flagsmith.android.toolbar.ToolbarSimple


class FlagListActivity : AppCompatActivity() {

    lateinit var flagBuilder : Flagsmith

    lateinit var activity: Activity
    lateinit var context : Context;

    lateinit var rv_flags : RecyclerView;

    lateinit var prg_flags : ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flag_list)

        activity = this;
        context = this;

        //find by ids
        rv_flags = findViewById(R.id.rv_flags);
        prg_flags = findViewById(R.id.prg_flags)

        initBuilder();

        setupToolbar();

    }

    private fun setupToolbar() {
        ToolbarSimple( this, R.id.toolbarFlagList, "Flags");
    }


    private fun initBuilder() {
        flagBuilder = Flagsmith.Builder()
            .tokenApi( Helper.tokenApiKey)
            .environmentId(Helper.environmentDevelopmentKey)
            .identity( Helper.identifierUserKey)
            .build();
    }


    private fun getAllData() {

        //progress
        prg_flags.visibility = View.VISIBLE

        //listener
        flagBuilder.getFeatureFlags(   object : IFlagArrayResult{
            override fun success(list: ArrayList<ResponseFlag>) {

                Helper.callViewInsideThread( activity) {
                    //progress
                    prg_flags.visibility = View.GONE

                    //check size
                    if (list.size == 0) {
                        Toast.makeText(this@FlagListActivity, "No Data Found", Toast.LENGTH_SHORT)
                            .show()
                        return@callViewInsideThread;
                    }

                    //list
                    createAdapterFlag(list);
                }

            }
            override fun failed(str: String) {

                Helper.callViewInsideThread( activity) {
                    //progress
                    prg_flags.visibility = View.GONE

                    //toast
                    Toast.makeText(activity, str, Toast.LENGTH_SHORT).show()
                }


            }
        })
    }


    private fun createAdapterFlag(list: ArrayList<ResponseFlag>) {
        val manager = LinearLayoutManager(context )
        manager.orientation = LinearLayoutManager.VERTICAL
        rv_flags.layoutManager = manager
        val customAdapter = FlagAdapter(  context , list, object : FlagPickerSelect{
            override fun click(favContact: ResponseFlag?) {

            }
        })
        rv_flags.adapter = customAdapter
    }


    override fun onResume() {
        super.onResume()

        getAllData();
    }

}