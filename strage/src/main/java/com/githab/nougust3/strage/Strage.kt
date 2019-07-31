package com.githab.nougust3.strage

import android.content.Context
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

@Suppress("UNCHECKED_CAST")
class StrageAdapter(
    val builder: Strage,
    private val binders: HashMap<String, Pair<Int, StrageHolder.(Any) -> Unit>>,
    private val context: Context
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items: ArrayList<Any> = ArrayList()
    private var calculatedItems: ArrayList<Any> = ArrayList()

    fun setItems(items: ArrayList<*>) {
        this.items.clear()
        this.items.addAll(items)
        updateCalculatedItems()
    }

    fun getItems() = items

    fun getCalculatedItems() = calculatedItems

    fun getItem(position: Int) = items[position]

    fun getCalculatedItem(position: Int) = calculatedItems[position]


    private var filters = ArrayList<IStrageFilter<Any>>()

    fun getFilters() = filters

    fun addFilter(filter: IStrageFilter<Any>) {
        filters.add(filter)
        updateCalculatedItems()
    }

    fun removeFilter(filterName: String) {
        val position = filters.indexOfFirst { it.name == filterName }
        if (position > -1) filters.removeAt(position)
        updateCalculatedItems()
    }

    private var sorter: Comparator<*>? = null

    fun <T> setSorter(sorter: Comparator<T>) {
        this.sorter = sorter
        updateCalculatedItems()
    }

    fun removeSorter() {
        this.sorter = null
        updateCalculatedItems()
    }

    private fun updateCalculatedItems() {
        val result = this.items.filter {
            filters.all { filter -> filter.execute(it) }
        }
        this.calculatedItems.clear()
        this.calculatedItems.addAll(result)
        if (sorter != null) this.calculatedItems.sortWith(sorter as Comparator<in Any>)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(view: ViewGroup, type: Int) =
        StrageHolder(LayoutInflater.from(context).inflate(type, view, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getCalculatedItem(position)
        val binder = binders[item.javaClass.name]?.second

        if (binder != null)
            binder(holder as StrageHolder, item)
        else
            throw Exception("Not found binding to class: " + item::class.java.canonicalName)

        holder.itemView.setOnClickListener {
            builder.clickListener?.invoke(position, item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getCalculatedItem(position)
        return binders[item.javaClass.name]!!.first
    }

    override fun getItemCount() = calculatedItems.size
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

interface ListItem

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