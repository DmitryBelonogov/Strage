package com.githab.nougust3.strage

import android.support.constraint.ConstraintLayout
import android.view.View
import android.widget.TextView
import com.github.nougust3.strage.annotation.Adapter
import com.github.nougust3.strage.annotation.BindName

@Adapter
class Main {

    @BindName
    fun itemRegular(data: String, titleView: TextView, backgroundView: ConstraintLayout) {
        titleView.text = data
        backgroundView.visibility = View.GONE
    }

    @BindName
    fun itemCustom(data: Azaza, titleView: TextView) {
        titleView.text = data.i.toString()
    }

}
