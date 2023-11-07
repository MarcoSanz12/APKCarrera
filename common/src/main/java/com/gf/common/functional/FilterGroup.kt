package com.gf.common.functional

import com.gf.common.entity.Resource

class FilterGroup (
    val title : String,
    var listCategory: List<FilterCategory>,
    val todos : Boolean = false
) : Resource() {

     constructor(filterGroup: FilterGroup) : this(
         filterGroup.title,
         filterGroup.listCategory.map { FilterCategory(it) },
         filterGroup.todos
     )

    val isChecked get() = listCategory.all { it.isChecked }

    fun checkAll(){
        listCategory.forEach { it.isChecked = true }
    }
     fun uncheckAll(){
         listCategory.forEach { it.isChecked = false }
     }
    fun toggleAll(){
        if (isChecked)
            listCategory.forEach { it.isChecked = false }
        else
            listCategory.forEach { it.isChecked = true }
    }

     fun compareWith(other : FilterGroup) : Boolean{
         return if (this != other)
             false
      /*   else if (!other.listCategory.any{it.isChecked} || !this.listCategory.any{it.isChecked})
             false*/
         else{
             val categoriesChecked = this.listCategory.filter { it.isChecked }
             val filterChecked = other.listCategory.filter {it.isChecked}
             categoriesChecked.any { it in filterChecked }
         }

     }

    override fun getResourceType(): ResourceType = ResourceType.FILTRO

    override fun getID(): Int = 0

    override fun getName(): String = title

     override fun equals(other: Any?): Boolean {
         return if (other !is FilterGroup)
              false
         else
             other.title == title

     }


}