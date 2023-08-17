package com.gf.common.platform

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gf.common.exception.Failure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Base ViewModel class with default Failure handling.
 * @see ViewModel
 * @see Failure
 */
abstract class BaseViewModel : ViewModel() {

    var failure: MutableLiveData<Failure> = MutableLiveData()
    var IO : CoroutineScope = CoroutineScope(Dispatchers.IO)

    fun handleFailure(failure: Failure?) {
        this.failure.postValue(failure)
    }
}