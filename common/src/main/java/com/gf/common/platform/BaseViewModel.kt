package com.gf.common.platform

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gf.common.exception.Failure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel class with default Failure handling.
 * @see ViewModel
 * @see Failure
 */
abstract class BaseViewModel : ViewModel() {

    var failure: MutableLiveData<Failure> = MutableLiveData()
    var IO : CoroutineScope = CoroutineScope(Dispatchers.IO)

    val _failureState = MutableStateFlow<Failure?>(null)
    val failureState = _failureState.asStateFlow()

    fun launch(func : suspend () -> Unit){
        viewModelScope.launch {
            func()
        }
    }

    fun handleFailure(failure: Failure?) {
        this.failure.postValue(failure)
    }
}