package com.gf.common.extensions

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.Factory

inline fun androidx.fragment.app.FragmentManager.inTransaction(func: androidx.fragment.app.FragmentTransaction.() -> androidx.fragment.app.FragmentTransaction) =
    beginTransaction().func().commit()

inline fun <reified T : ViewModel> androidx.fragment.app.Fragment.viewModel(
    factory: Factory,
    body: T.() -> Unit
): T {
    val vm = ViewModelProvider(activity!!, factory)[T::class.java]
    vm.body()
    return vm
}

inline fun <reified T : ViewModel> androidx.fragment.app.Fragment.viewModel(factory: Factory): T {
    return ViewModelProvider(activity!!, factory)[T::class.java]
}

inline fun <reified T : ViewModel> androidx.fragment.app.FragmentActivity.viewModel(
    factory: Factory,
    body: T.() -> Unit
): T {
    val vm = ViewModelProvider(this, factory)[T::class.java]
    vm.body()
    return vm
}

/**
 * Muestra un toast
 * @param string a mostrar en el toast
 * @param toastLength *opcional* Duración del toast, por defecto LENGTH_SHORT
 */
fun Fragment.toast(string:String,toastLength: Int = Toast.LENGTH_SHORT) = Toast.makeText(this.requireContext(),string,toastLength).show()

/**
 * Muestra un toast
 * @param stringId a mostrar en el toast
 * @param toastLength *opcional* Duración del toast, por defecto LENGTH_SHORT
 */
fun Fragment.toast(stringId:Int,toastLength: Int = Toast.LENGTH_SHORT) = Toast.makeText(this.requireContext(),getString(stringId),toastLength).show()

