package com.githab.nougust3.strage

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.lang.Exception
import kotlin.collections.HashMap
import java.lang.reflect.ParameterizedType
import java.util.*

class StrageHolder(view: View): RecyclerView.ViewHolder(view)

class StrageAdapter(
    val builder: Strage,
    private val items : ArrayList<*>,
    private val binders: HashMap<Class<*>, Pair<Int, StrageHolder.(Any) -> Unit>>,
    private val context: Context
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(view: ViewGroup, type: Int) =
        StrageHolder(LayoutInflater.from(context).inflate(type, view, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        if (binders.containsKey(item::class.java)) {
            val binder = binders[item::class.java]!!.second
            binder(holder as StrageHolder, item)
            return
        }

        throw Exception("Not found binding to class: " + item::class.java.canonicalName)
    }

    override fun getItemViewType(position: Int)
            = binders[items[position]::class.java]!!.first

    override fun getItemCount()
            = items.size
}

class Strage(
    private val context: Context,
    val data: List<*>
) {

    private val binders = HashMap<Class<*>, Pair<Int, StrageHolder.(Any) -> Unit >>()

    @Suppress("UNCHECKED_CAST")
    fun <T> bind(layout: Int, binder: StrageHolder.(item: T) -> Unit): StrageAdapter {
        binders[getTargetClass()] = Pair(layout, binder as StrageHolder.(Any) -> Unit)
        return StrageAdapter(this, data as ArrayList<*>, binders, context)
    }

    private fun getTargetClass() =
        (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>
}

fun <T> StrageAdapter.bind(layout: Int, binder: StrageHolder.(item: T) -> Unit) =
    this.builder.bind(layout, binder)