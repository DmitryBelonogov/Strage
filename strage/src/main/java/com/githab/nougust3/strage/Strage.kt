@file:Suppress("UNCHECKED_CAST")

package com.githab.nougust3.strage

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception
import java.util.ArrayList

interface IStrageFilter <T> {
    var name: String
    fun execute(data: T): Boolean
}

class StrageHolder(private val v: View): RecyclerView.ViewHolder(v) {

    fun <T : View> view(id: Int): T =
        v.findViewById(id)

}

typealias RecyclerViewAdapter = RecyclerView.Adapter<RecyclerView.ViewHolder>
typealias Binders = HashMap<String, Pair<Int, StrageHolder.(Any) -> Unit>>

class StrageAdapter(val builder: Strage, private val binders: Binders, private val context: Context):
    RecyclerViewAdapter() {

    private var sorter: Comparator<*>? = null
    private var items: List<Any> = ArrayList()
    private var calculatedItems: List<Any> = ArrayList()

    var filters: List<IStrageFilter<Any>> = ArrayList()

    operator fun get(index: Int) =
        calculatedItems[index]

    operator fun invoke(newItems: List<Any>) {
        items = newItems
        updateCalculatedItems()
    }

    fun all() =
        calculatedItems

    fun size() =
        calculatedItems.size

    fun addFilter(filter: IStrageFilter<Any>) {
        filters = filters.plus(filter)
        updateCalculatedItems()
    }

    fun removeFilter(name: String) =
        filters.firstOrNull { it.name == name }?.let {
            filters = filters.filter { it.name != name }
            updateCalculatedItems()
        }

    fun hasFilter(name: String) =
        filters.any {
            it.name == name
        }

    fun <T: Any> setSorter(sorter: Comparator<in T>) {
        this.sorter = sorter
        updateCalculatedItems()
    }

    fun removeSorter() =
        sorter?.let {
            sorter = null
            updateCalculatedItems()
        }

    private fun updateCalculatedItems() {
        calculatedItems = getFilteredItems()
        sortCalculatedItems()
        notifyDataSetChanged()
    }

    private fun sortCalculatedItems() =
        sorter?.let {
            calculatedItems.sortedWith(it as Comparator<in Any>)
        }

    private fun getFilteredItems() =
        items.filter {
            filters.all { filter ->
                filter.execute(it)
            }
        }

    override fun onCreateViewHolder(view: ViewGroup, type: Int) =
        StrageHolder(LayoutInflater.from(context).inflate(type, view, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = get(position)
        val binder = binders[item.javaClass.name]?.second

        holder.itemView.setOnClickListener {
            builder.clickListener?.invoke(position, item)
        }

        if (binder != null)
            binder(holder as StrageHolder, item)
        else
            throw Exception("Not found binding to class: " + item::class.java.canonicalName)
    }

    override fun getItemViewType(position: Int) =
        binders[get(position).javaClass.name]!!.first

    override fun getItemCount() =
        calculatedItems.size
}

class Strage(private val context: Context) {

    private var diffUtillCallback: DiffUtillCallback? = null

    var clickListener: ((Int, Any) -> Unit)? = null
    val binders = HashMap<String, Pair<Int, StrageHolder.(Any) -> Unit >>()

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> bind(layout: Int, noinline binder: StrageHolder.(item: T) -> Unit): StrageAdapter {
        binders[T::class.java.name] =
            Pair(layout, binder as StrageHolder.(Any) -> Unit)

        return build()
    }

    fun setComparators(itemsComparator: (Any, Any) -> Boolean, contentComparator: ((Any, Any) -> Boolean)?): StrageAdapter {
        diffUtillCallback = DiffUtillCallback()
        diffUtillCallback!!.itemsComparator = itemsComparator

        if (contentComparator != null)
            diffUtillCallback!!.contentComparator = contentComparator

        return build()
    }

    fun setOnClickListener(listener: (Int, Any) -> Unit): StrageAdapter {
        clickListener = listener
        return build()
    }

    fun build(): StrageAdapter =
        StrageAdapter(this, binders, context)

}

inline fun <reified T> StrageAdapter.bind(layout: Int, noinline binder: StrageHolder.(item: T) -> Unit) =
    this.builder.bind(layout, binder)

fun StrageAdapter.setOnClickListener(listener: (Int, Any) -> Unit) =
    this.builder.setOnClickListener(listener)

fun StrageAdapter.setComparators(itemsComparator: (Any, Any) -> Boolean, contentComparator: ((Any, Any) -> Boolean)?) =
    this.builder.setComparators(itemsComparator, contentComparator)

class DiffUtillCallback: DiffUtil.Callback() {

    private val oldList: List<*>? = null
    private val newList: List<*>? = null

    var itemsComparator: ((Any, Any) -> Boolean)? = null
    var contentComparator: ((Any, Any) -> Boolean)? = null

    override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
        return if (itemsComparator == null) false
        else itemsComparator?.invoke(oldList?.get(oldPos)!!, newList?.get(newPos)!!)!!
    }

    override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
        return if (contentComparator == null) false
        else contentComparator?.invoke(oldList?.get(oldPos)!!, newList?.get(newPos)!!)!!
    }

    override fun getOldListSize() =
        oldList!!.size

    override fun getNewListSize() =
        newList!!.size
}