package com.gf.common.platform

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.updatePadding
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cotesa.appcore.functional.IFilterable
import com.cotesa.appcore.platform.BaseAdapter
import com.gf.common.R
import com.gf.common.entity.Resource
import com.gf.common.extensions.assignNonScrollableLinearLayoutManager
import com.gf.common.extensions.invisible
import com.gf.common.extensions.visible
import com.gf.common.functional.FilterCategory
import com.gf.common.functional.FilterGroup
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textview.MaterialTextView
import net.cachapa.expandablelayout.ExpandableLayout

class BaseFilter @JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
): ExpandableLayout(context,attrs) {

    var onCategoryChangedListener : OnCategoryChangedListener? = null

    companion object {
        private const val TAG = "BaseFilter"
        fun List<IFilterable>.getFilterGroups() : List<FilterGroup>{
            val filterGroupList = this.flatMap { it.filterGroups }.groupBy { it.getName() }.map {
                FilterGroup(
                    title =
                    it.key,
                    listCategory =
                    it.value.flatMap { it.listCategory.map { FilterCategory(it) }}.distinctBy { it.title },
                    todos =
                    it.value.first().todos)
            }

            // Todos check de base
            filterGroupList.forEach { it.checkAll() }

            return filterGroupList
        }
    }


    var recyclerView : RecyclerView? = null

    private var mListAdapters : MutableList<BaseAdapter<Resource>> = mutableListOf()

    fun addListAdapter(adapter : BaseAdapter<Resource>?){
        adapter ?: return

        // Sacamos la lista de Filtros para pintar BaseFilter
        if (filterableList == null)
            filterableList = adapter.resourceList as List<IFilterable>

        // Aplicamos esta lista al Adaptador para así realizar el filtrado inicial
        adapter.filterByFilters(filterableList!!.getFilterGroups())

        mListAdapters.add(adapter)
    }
    var filterableList : List<IFilterable>? = null
        set(value) {
            if (value == null)
                return

            if (filterableList == null){
                recyclerView = RecyclerView(context).apply {
                    val newList = value.getFilterGroups()
                    adapter = RecyclerFilterGroupAdapter(newList,::onCategoryChanged)
                    layoutManager = LinearLayoutManager(context)
                    background = ResourcesCompat.getDrawable(resources,
                        R.drawable.basefilter_background,null)
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    isVerticalFadingEdgeEnabled = true
                    overScrollMode = View.OVER_SCROLL_NEVER
                    updatePadding(bottom = resources.getDimension(R.dimen.dim_10dp).toInt())
                }
                addView(recyclerView)



            }else {
                (recyclerView?.adapter as? BaseAdapter<FilterGroup>)?.actualizarLista(value.getFilterGroups())
            }
            field = value
        }


    private fun onCategoryChanged(filterGroupList: List<FilterGroup>){
        mListAdapters.forEach{ it.filterByFilters(filterGroupList) }

        // Se llama al OnCategoryChangedListener
        if (filterableList != null)
            onCategoryChangedListener?.onCategoryChanged(filterableList!!.applyCategoryFilter(filterGroupList))
    }

    class RecyclerFilterGroupAdapter(listFilterGroup : List<FilterGroup>, val onCategoryChangedListener : (filterGroupList : List<FilterGroup>) -> Unit) : BaseAdapter<FilterGroup>(listFilterGroup,
        R.layout.filter_group,false
    ) {

        override fun renderOnViewHolder(resource: FilterGroup, view: View) {
            val todosCheckBox = view.findViewById<MaterialCheckBox>(R.id.cb_todos)
            val expandableLayout = view.findViewById<ExpandableLayout>(R.id.exp_categorias)
            val tvTitle = view.findViewById<MaterialTextView>(R.id.tv_title)
            val recyclerView = view.findViewById<RecyclerView>(R.id.rv_categorias)

            if (resource.todos){
                todosCheckBox.visible()
                todosCheckBox.setOnClickListener{ buttonView ->
                    val todosIsChecked = todosCheckBox.isChecked
                    val categoriesList = (recyclerView.adapter as RecyclerFilterCategoryAdapter).resourceListFiltered.map { FilterCategory(it) }
                    if (todosIsChecked)
                        categoriesList.forEach { it.isChecked = true }
                    else
                        categoriesList.forEach { it.isChecked = false }

                    (recyclerView.adapter as RecyclerFilterCategoryAdapter).actualizarLista(categoriesList)
                    resourceListFiltered.find { it == resource }?.listCategory = categoriesList
                    onCategoryChangedListener(resourceListFiltered)
                }
            }
            else{
                todosCheckBox.invisible()
            }

            // Titulo
            tvTitle.let { materialTextView->
                materialTextView.text = resource.title

                // Asignar flechita ver más / menos segun número de categorías
                assignDeployArrow(resource, materialTextView, expandableLayout)

            }

            // Actualiza el Checkbox Todos si todas sus categorías se activan
            fun onCategoryChanged(filterCategory: FilterCategory, isChecked: Boolean, isAllChecked : Boolean) {
                Log.d(TAG, "Checked [${resource.listCategory.count { it.isChecked }}/${resource.listCategory.size}]")
                resourceListFiltered.find { resource == it }?.listCategory?.find { it == filterCategory }?.isChecked = isChecked

                todosCheckBox.isChecked = isAllChecked
                onCategoryChangedListener(resourceListFiltered)
            }

            recyclerView.let {
                it.adapter = RecyclerFilterCategoryAdapter(resource.listCategory,::onCategoryChanged)
                it.itemAnimator = null
                it.assignNonScrollableLinearLayoutManager()
            }


        }

        private fun assignDeployArrow(
            resource: FilterGroup,
            materialTextView: MaterialTextView,
            expandableLayout: ExpandableLayout,
        ) {
            if (resource.listCategory.size > 10) {
                materialTextView.setOnClickListener {
                    expandableLayout.toggle()
                    val drawable =
                        if (expandableLayout.isExpanded)
                            ContextCompat.getDrawable(it.context, R.drawable.icon_expand_less)!!
                        else
                            ContextCompat.getDrawable(it.context, R.drawable.icon_expand_more)!!

                    materialTextView.setCompoundDrawablesWithIntrinsicBounds(
                        drawable,
                        null,
                        null,
                        null
                    )
                    TextViewCompat.setCompoundDrawableTintList(
                        materialTextView,
                        ColorStateList.valueOf(Color.WHITE)
                    )
                }
            } else
                TextViewCompat.setCompoundDrawableTintList(
                    materialTextView,
                    ColorStateList.valueOf(Color.TRANSPARENT)
                )
        }


    }

    class RecyclerFilterCategoryAdapter(listFilterCategory : List<FilterCategory>, val onCategoryChangedListener : (filterCategory: FilterCategory, isChecked : Boolean, isAllChecked : Boolean)->Unit) : BaseAdapter<FilterCategory>(listFilterCategory,
        R.layout.filter_category,false
    ) {

        override fun renderOnViewHolder(resource: FilterCategory, view: View) {

            val checkBox =  view.findViewById<MaterialCheckBox>(R.id.checkbox)
            // Ajustar tamaño segun sus lineas dinámicamente
            adjustLineHeight(view, resource)

            checkBox.text = resource.title
            checkBox.isChecked = resource.isChecked

            checkBox.setOnClickListener { buttonView ->
                val updatedList = resourceListFiltered.map { FilterCategory(it) }.apply {
                    find{it == resource}?.isChecked = checkBox.isChecked
                }
                val updatedCategory = updatedList.find { it == resource }

                actualizarLista(updatedList)
                val isAllChecked = resourceListFiltered.all { it.isChecked }
                onCategoryChangedListener(updatedCategory!!,updatedCategory.isChecked,isAllChecked)
            }
        }

        private fun adjustLineHeight(
            view: View,
            resource: FilterCategory,
        ) {
            view.layoutParams.height =
                if (resource.title.length < 28)
                    view.context.resources.getDimension(R.dimen.filter_line_size_x1).toInt()
                else if (resource.title.length < 56)
                    view.context.resources.getDimension(R.dimen.filter_line_size_x2).toInt()
                else
                    view.context.resources.getDimension(R.dimen.filter_line_size_x3).toInt()
        }

    }

    private fun List<IFilterable>.applyCategoryFilter(filterGroups : List<FilterGroup>) : List<IFilterable>{
        return if (isEmpty())
            this
        else{
            var filteredRes  =this.filter { it.filterGroups.any { it in filterGroups }  }
            filteredRes.filter  { it.compareWith(filterGroups) }
        }
    }

    interface OnCategoryChangedListener {
        /**
         * Se llama cuando una categoría ha cambiado
         *
         * @param filteredList Lista ya filtrada
         */
        fun onCategoryChanged(filteredList: List<IFilterable>)
    }
}