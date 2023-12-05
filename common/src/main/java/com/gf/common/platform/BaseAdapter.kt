package com.cotesa.appcore.platform

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cotesa.appcore.functional.IFilterable
import com.gf.common.R
import com.gf.common.entity.Resource
import com.gf.common.extensions.animateItem
import com.gf.common.extensions.removeWhiteSpaces
import com.gf.common.extensions.stripAccents
import com.gf.common.functional.DiffUtilsGeneric
import com.gf.common.functional.FilterGroup

/**
 * Adaptador genérico para ser usado por listas de [Resource]
 *
 *  - [RES] es el tipo de [Resource] a usar como objeto principal
 *
 *  @param resourceList Lista de recursos a mostrar
 *  @param itemLayoutId Id del layout a usar como item
 */


abstract class BaseAdapter<RES : Resource>(
    var resourceList : List<RES>,
    val itemLayoutId : Int,
    orderInit : Boolean = true
) : RecyclerView.Adapter<BaseAdapter<RES>.BaseViewHolder>() {


    open var isAnimationEnabled = true

    var resourceListFiltered : List<RES>
    private var idsToAnimate : MutableList<Int>

    private var mName : String = ""
    private var SORT_TYPE = SORT_ALPHABETICAL_ASC
    private var mFilterGroupList : List<FilterGroup> = listOf()

    private var mRecyclerView : RecyclerView? = null
    companion object {
        private const val TAG = "BaseAdapter"
        const val SORT_ALPHABETICAL_ASC = 0
        const val SORT_ALPHABETICAL_DESC = 1
    }
    init {
        if (orderInit)
            resourceList = resourceList.sortedBy { it.getName().stripAccents() }

        resourceListFiltered = resourceList
        idsToAnimate = resourceListFiltered.map { it.getID() }.toMutableList()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    abstract fun renderOnViewHolder (resource : RES, view: View)
    override fun getItemCount(): Int  = resourceListFiltered.size

    override fun onBindViewHolder(holder: BaseAdapter<RES>.BaseViewHolder, position: Int) {
        with (holder){
            render(resourceListFiltered[position])

            if (isAnimationEnabled)
                animateItem(idsToAnimate,resourceListFiltered[position], R.anim.animation_item_fade_in)

        }
    }

    // 1. Filtrar por String
    /**
     * Filtra el adaptador por [Resource] cuyo [Resource.getName] contenga [text]
     *
     * @param text String a filtrar por
     */
    fun filterByString(text: String){
        mName = text
        applyAllFilters()
    }

    fun toggleOrder() : Int {
        return if (SORT_TYPE == SORT_ALPHABETICAL_ASC)
            filterByOrder(SORT_ALPHABETICAL_DESC)
        else
            filterByOrder()
    }

    // 1. Filtrar por orden
    /**
     * Filtra el adaptador por [Resource] cuyo [Resource.getName] este ordenado con el siguiente [order]
     *
     * @param order Orden a ordenar por, por defecto será [SORT_ALPHABETICAL_ASC] -> Ascendente
     *
     * @see [SORT_ALPHABETICAL_ASC]
     * @see [SORT_ALPHABETICAL_DESC]
     */
    fun filterByOrder(order : Int = SORT_ALPHABETICAL_ASC) : Int {
        SORT_TYPE = order
        applyAllFilters()
        return SORT_TYPE
    }

    fun filterByFilters(filterGroupList : List<FilterGroup>) {
        mFilterGroupList = filterGroupList
        applyAllFilters()
    }

    // 2. Aplica los filtros
    fun applyAllFilters(){
        val listToFilter =
            resourceList
                .applyStringFilter()
                .applyCategoryFilter()
                .applyOrderFilter()

        actualizarLista(listToFilter)
    }

    // 2. Aplica filtro por nombre
    private fun List<RES>.applyStringFilter() : List<RES>{
        return if (mName.isNotEmpty()){
            this.filter {
                it.getName().stripAccents().removeWhiteSpaces().contains(mName.stripAccents().removeWhiteSpaces(),true)}.sortedBy { it.getName() }
        }else
            this

    }

    // 2. Aplica filtros por FilterCategories
    private fun List<RES>.applyCategoryFilter() : List<RES>{
        return if (mFilterGroupList.isEmpty() || resourceList.firstOrNull() !is IFilterable)
            this
        else{
            var filteredRes  =this.filter { (it as IFilterable).filterGroups.any { it in mFilterGroupList }  }

            filteredRes.filter  {
                (it as IFilterable).compareWith(mFilterGroupList)
            }
        }
    }

    // 2. Aplica filtro por orden
    private fun List<RES>.applyOrderFilter() : List<RES> {
        return when (SORT_TYPE){
            SORT_ALPHABETICAL_ASC ->
                this.sortedBy { it.getName() }

            SORT_ALPHABETICAL_DESC ->
                this.sortedByDescending { it.getName() }

            else -> {
                this
            }
        }
    }

    // 3. Actualiza la lista utilizando DiffUtils
    fun actualizarLista(newList : List<RES>){

        val diffUtils = DiffUtilsGeneric(resourceListFiltered,newList)
        val result = DiffUtil.calculateDiff(diffUtils)

        resourceListFiltered = newList
        result.dispatchUpdatesTo(this) //notifyAll() pero optimizado
    }
    override fun onViewDetachedFromWindow(holder: BaseAdapter<RES>.BaseViewHolder) {
        holder.itemView.animation?.cancel()
    }
    /**
     * Inicializa un [ViewHolder], el cual llamara al método [renderOnViewHolder]
     * cuando se cree este
     *
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseAdapter<RES>.BaseViewHolder = object : BaseAdapter<RES>.BaseViewHolder(LayoutInflater.from(parent.context).inflate(itemLayoutId,parent,false)){
        override fun render(resource: RES) {
            renderOnViewHolder(resource,view)
        }
    }

    abstract inner class BaseViewHolder(val view : View) : RecyclerView.ViewHolder(view){
        abstract fun render(resource: RES)
    }
}



