package com.gf.common.functional

import androidx.recyclerview.widget.DiffUtil

class DiffUtilsGeneric<T>(val oldList : List<T>, val newList  : List<T>) : DiffUtil.Callback() {
    companion object{
        private const val TAG = "DiffUtiltsGeneric"
    }
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean{
        return if (oldList[oldItemPosition] is FilterCategory){
           val isSame = (oldList[oldItemPosition] as FilterCategory).isChecked == (newList[newItemPosition] as FilterCategory).isChecked
            isSame
        }
        else
            oldList[oldItemPosition] == newList[newItemPosition]
    }


}