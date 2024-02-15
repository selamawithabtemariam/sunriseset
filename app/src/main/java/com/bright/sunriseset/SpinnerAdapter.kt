package com.bright.sunriseset

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.view.LayoutInflaterFactory
import com.bright.sunriseset.databinding.ItemSpinnerBinding

class SpinnerAdapter(context: Context, languageList: ArrayList<Language>)
    : ArrayAdapter<Language>(context, 0, languageList) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val  language = getItem(position)

        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_spinner, parent, false)
        val languageName: TextView = view.findViewById(R.id.languageName)
        languageName.text = language?.name

        return view
    }
}