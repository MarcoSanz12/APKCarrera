package com.gf.common.extensions

import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUiSaveStateControl

/**
 * Devuelve la ID a la que va a llevar el NavDestination cuando se navegue a el.
 * @return
 * Si es un [NavGraph] devolverá su [NavGraph.startDestinationId].
 * Si es un [Fragment] devolverá su [NavDestination.id]
 */
fun NavDestination.getDestinationId(): Int =
    if (this is NavGraph)
        this.startDestinationId
    else
        this.id

/**
 * Navega al [MenuItem] que se haya seleccionado en el BottomNavigationView]
 * @param this [NavController] que se encarga de la navegación
 * @param menuItem [MenuItem] seleccionado
 *
 * @return [true]
 */
@OptIn(NavigationUiSaveStateControl::class)
fun NavController.navigateToMenuItem(menuItem: MenuItem) : Boolean{
    if (this.findDestination(menuItem.itemId)?.getDestinationId() != this.currentDestination?.id){
       NavigationUI.onNavDestinationSelected(menuItem,this,false)
    }

    return true
}

