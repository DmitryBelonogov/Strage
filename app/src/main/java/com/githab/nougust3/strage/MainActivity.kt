package com.githab.nougust3.strage

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val adapter = MainAdapter().get(this)
        listView.adapter = adapter
        adapter.setItems(arrayListOf<Any>(Azaza(1), "Azaza", Azaza(2), "Ololo"))
        adapter.notifyDataSetChanged()
    }
}

data class Azaza(val i: Int)


