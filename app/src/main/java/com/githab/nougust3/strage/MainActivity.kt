package com.githab.nougust3.strage

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(
                this,
                androidx.recyclerview.widget.RecyclerView.VERTICAL,
                false
            )
        val adapter = Strage(this)
            .build()

        val item = adapter[1]
        val size = adapter.size()

        listView.adapter = adapter
        adapter(arrayListOf(Azaza(1), "Azaza", Azaza(2), "Ololo"))
        adapter.notifyDataSetChanged()
    }
}

data class Azaza(val i: Int)
