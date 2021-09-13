package com.example.axxtransdemo.data.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.axxtransdemo.R
import com.example.axxtransdemo.data.model.AccessPoint


class CustomArrayAdapter : ArrayAdapter<AccessPoint> {

    private var dataList: List<AccessPoint>? = null
    private var mContext: Context? = null
    private var itemLayout = 0

     constructor(context: Context, resource: Int, storeDataLst: List<AccessPoint>): super(context, resource, storeDataLst) {
        dataList = storeDataLst
        mContext = context
        itemLayout = resource
    }

    override fun getCount(): Int {
        return dataList!!.size
    }

    override fun getItem(position: Int): AccessPoint {
        return dataList!![position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//         super.getView(position, convertView, parent)

        var convertView: View? = convertView
        try {

            if (convertView == null) {
                convertView = LayoutInflater.from(parent.context)
                    .inflate(itemLayout, parent, false);
//                val inflater = (mContext as FirstFragment).layoutInflater
//                convertView = inflater.inflate(itemLayout, parent, false)
            }

            // object item based on the position
            val objectItem: AccessPoint = dataList!![position]

            // get the TextView and then set the text (item name) and tag (item ID) values
            val titleTxt = convertView?.findViewById(R.id.title_txt) as TextView

            titleTxt.text = objectItem.fields.Name

        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return convertView!!
    }

}