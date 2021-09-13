package com.example.axxtransdemo.data.model

object Utility {

    fun isAnyElementCommon(aList: List<String>, bList: List<String>):Boolean {
        val aSet = aList.toSet()
        return bList.any { it in aSet }
    }
}