package com.gf.common.extensions

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cotesa.common.extensions.notNull
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Recibe un [StateFlow], lo inicia en una corrutina con [lifecycleScope] la cual se
 * reinicia en el [lifecycleState] seleccionado.
 * Si devuelve un valor distinto a NULL llama la función [handleNotNull]
 *
 * @param stateFlow Es el [StateFlow] seleccionado para colectar
 * @param lifecycleState Es el [Lifecycle.State] en el cual se reasignará el Collector
 * @param handleNotNull Función llamada si consigue colectar algun elemento distinto de NULL
 */
fun <T : Any, SF : StateFlow<T?>> androidx.fragment.app.Fragment.collectFlow(stateFlow : SF, lifecycleState : Lifecycle.State = Lifecycle.State.STARTED, handleNotNull: (T) -> Unit) =
    lifecycleScope.launch {
        repeatOnLifecycle(lifecycleState) {
            stateFlow.collect {
                it.notNull {
                    handleNotNull(it)
                }
            }
        }
    }

fun <T : Any, SF : SharedFlow<T?>> androidx.fragment.app.Fragment.collectFlow(sharedFlow : SF, lifecycleState : Lifecycle.State = Lifecycle.State.STARTED, handleNotNull: (T) -> Unit) =
    lifecycleScope.launch {
        repeatOnLifecycle(lifecycleState) {
            sharedFlow.collect {
                it.notNull {
                    handleNotNull(it)
                }
            }
        }
    }

fun <T : Any, SF : StateFlow<T?>> androidx.fragment.app.Fragment.collectFlowOnce(stateFlow : SF, handleNotNull: (T) -> Unit) =
    lifecycleScope.launch {
            stateFlow.collect {
                it.notNull {
                    handleNotNull(it)
                }
            }
    }

fun <T : Any, SF : StateFlow<T?>> androidx.lifecycle.LifecycleService.collectFlow(stateFlow : SF, lifecycleState : Lifecycle.State = Lifecycle.State.CREATED, handleNotNull: (T) -> Unit) =
    lifecycleScope.launch {
        repeatOnLifecycle(lifecycleState) {
            stateFlow.collect {
                it.notNull {
                    handleNotNull(it)
                }
            }
        }
    }

/**
 * Recibe un [StateFlow], lo inicia en una corrutina con [lifecycleScope] la cual se
 * reinicia en el [lifecycleState] seleccionado.
 * Si devuelve un valor distinto a NULL llama la función [handleNotNull]
 * Si devuelve un valor igual a NULL llama a la función [handleNull]
 *
 * @param stateFlow Es el [StateFlow] seleccionado para colectar
 * @param lifecycleState Es el [Lifecycle.State] en el cual se reasignará el Collector
 * @param handleNotNull Función llamada si consigue colectar algun elemento distinto de NULL
 * @param handleNull Función llamada si al recolectar recibe valor NULL
 */
fun <T : Any, SF : StateFlow<T?>> androidx.fragment.app.Fragment.collectFlow(stateFlow : SF, lifecycleState : Lifecycle.State = Lifecycle.State.STARTED, handleNotNull: (T) -> Unit, handleNull : () -> Unit) =
    lifecycleScope.launch {
        repeatOnLifecycle(lifecycleState) {
            stateFlow.collect {
                if (it != null)
                    handleNotNull(it)
                else
                    handleNull()
            }
        }
    }

fun <T : Any, SF : StateFlow<T?>> androidx.fragment.app.Fragment.collectFlowOnce(stateFlow : SF, handleNotNull: (T) -> Unit, handleNull : () -> Unit) =
    lifecycleScope.launch {
            stateFlow.collect {
                if (it != null)
                    handleNotNull(it)
                else
                    handleNull()
            }
    }


