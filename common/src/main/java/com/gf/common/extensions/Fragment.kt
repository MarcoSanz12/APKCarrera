package com.gf.common.extensions

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


