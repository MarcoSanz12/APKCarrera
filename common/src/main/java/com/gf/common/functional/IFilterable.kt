package com.cotesa.appcore.functional

import com.gf.common.functional.FilterGroup

interface IFilterable {

        companion object{
                private const val TAG = "IFilterable"
        }

        val filterGroups : List<FilterGroup>

        fun compareWith(other:List<FilterGroup>) : Boolean{

                // 1. Recogemos los FilterGroup que tenga nuestro Objeto en comun con los Filtros
                val commonGroups = filterGroups.filter { it in other }

                // 1. Si no tienen grupos en común retornar FALSE
                if (commonGroups.isEmpty())
                        return false
                else {
                        var isIN = false
                        isIN = commonGroups.all { commonGroup ->
                                // 2. Vamos comprobando por cada FilterGroup
                                val otherGroupListCategories =
                                        other.find { it == commonGroup }!!.listCategory.filter { it.isChecked }

                                val checkedGroups = commonGroup.listCategory.filter { it.isChecked }
                                checkedGroups.any { resourceCategory ->
                                        resourceCategory in otherGroupListCategories
                                }
                        }
                        return isIN
                }

        }



}