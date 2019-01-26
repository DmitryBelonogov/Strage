package com.githab.nougust3.strage

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.lang.Exception
import java.util.ArrayList

class StrageHolder(private val v: View): RecyclerView.ViewHolder(v) {

    fun <T : View> view(id: Int): T =
        v.findViewById(id)

}

class StrageAdapter(
    val builder: Strage,
    private val items: ArrayList<*>,
    private val binders: HashMap<String, Pair<Int, StrageHolder.(Any) -> Unit>>,
    private val context: Context
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(view: ViewGroup, type: Int) =
        StrageHolder(LayoutInflater.from(context).inflate(type, view, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        val binder = binders[item.javaClass.name]?.second

        if (binder != null)
            binder(holder as StrageHolder, item)
        else
            throw Exception("Not found binding to class: " + item::class.java.canonicalName)
    }

    override fun getItemViewType(position: Int) =
        binders[items[position].javaClass.name]!!.first

    override fun getItemCount() =
        items.size
}

class Strage(
    private val context: Context,
    val data: List<*>
) {

    val binders = HashMap<String, Pair<Int, StrageHolder.(Any) -> Unit >>()

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> bind(layout: Int, noinline binder: StrageHolder.(item: T) -> Unit): StrageAdapter {
        binders[T::class.java.name] =
                Pair(layout, binder as StrageHolder.(Any) -> Unit)

        return build()
    }

    fun build(): StrageAdapter =
        StrageAdapter(this, data as ArrayList<*>, binders, context)

}

inline fun <reified T> StrageAdapter.bind(layout: Int, noinline binder: StrageHolder.(item: T) -> Unit) =
    this.builder.bind(layout, binder)

interface ListItem { }