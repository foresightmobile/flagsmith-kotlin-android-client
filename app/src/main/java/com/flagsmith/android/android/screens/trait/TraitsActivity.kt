package com.flagsmith.android.android.screens.trait

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flagsmith.android.adapter.TraitAdapter
import com.flagsmith.android.adapter.TraitPickerSelect
import com.flagsmith.builder.Flagsmith
import com.flagsmith.interfaces.ITraitArrayResult
import com.flagsmith.response.ResponseTrait
import com.flagsmith.android.android.toolbar.ToolbarSimple
import com.flagmsith.R
import com.flagsmith.android.helper.Helper

class TraitsActivity : AppCompatActivity() {

    lateinit var flagBuilder: Flagsmith

    lateinit var activity: Activity
    lateinit var context: Context

    lateinit var rv_traits: RecyclerView

    lateinit var prg_traits: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_traits)

        context = this
        activity = this

        //find by ids
        rv_traits = findViewById(R.id.rv_traits)
        prg_traits = findViewById(R.id.prg_traits)

        initBuilder()

        setupToolbar()

        setupButtonCreate()
    }

    private fun setupButtonCreate() {
        val bt_create_trait: Button = findViewById(R.id.bt_create_trait)
        bt_create_trait.setOnClickListener {
            val i = Intent(this, TraitCreateActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
        }
    }


    private fun setupToolbar() {
        ToolbarSimple(this, R.id.toolbarTraitsList, "Traits")
    }


    private fun initBuilder() {
        flagBuilder = Flagsmith.Builder()
            .tokenApi(Helper.tokenApiKey)
            .environmentId(Helper.environmentDevelopmentKey)
            .identity(Helper.identifierUserKey)
            .build()
    }


    private fun getAllData() {
        prg_traits.visibility = View.VISIBLE

        flagBuilder.getTrait(object : ITraitArrayResult {
            override fun success(list: ArrayList<ResponseTrait>) {
                Helper.callViewInsideThread(activity) {

                    prg_traits.visibility = View.GONE
                    if (list.size == 0) {
                        Toast.makeText(this@TraitsActivity, "No Data Found", Toast.LENGTH_SHORT)
                            .show()
                        return@callViewInsideThread
                    }
                    createAdapterFlag(list)
                }

            }

            override fun failed(str: String) {
                Helper.callViewInsideThread(activity) {
                    //progress
                    prg_traits.visibility = View.GONE
                    //toast
                    Toast.makeText(this@TraitsActivity, str, Toast.LENGTH_SHORT).show()
                }

            }
        })
    }


    private fun createAdapterFlag(list: ArrayList<ResponseTrait>) {
        val manager = LinearLayoutManager(context)
        manager.orientation = LinearLayoutManager.VERTICAL
        rv_traits.layoutManager = manager
        val customAdapter = TraitAdapter(context, list, object : TraitPickerSelect {
            override fun click(mSelect: ResponseTrait?) {
            }
        })
        rv_traits.adapter = customAdapter
    }


    override fun onResume() {
        super.onResume()
        getAllData()
    }

}