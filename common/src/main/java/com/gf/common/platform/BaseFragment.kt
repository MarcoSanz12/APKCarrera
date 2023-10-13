package com.gf.common.platform

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.viewbinding.ViewBinding
import com.gf.common.R
import com.gf.common.exception.Failure
import com.gf.common.extensions.putAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.lang.reflect.ParameterizedType
import java.util.Timer
import kotlin.concurrent.timerTask

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _bi: VB? = null
    protected val binding: VB get() = _bi!!
    protected lateinit var MAIN : CoroutineScope
    protected lateinit var preferences : SharedPreferences


    private lateinit var activity: BaseActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("STATUS_LIFE","${this.javaClass.simpleName} - ${this.lifecycle.currentState.name}")
        activity = requireActivity() as BaseActivity
        MAIN = CoroutineScope(Dispatchers.Main)
        preferences = requireContext().getSharedPreferences(
            requireContext().packageName + "_preferences",
            Context.MODE_PRIVATE
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initObservers()
        Log.d("STATUS_LIFE","${this.javaClass.simpleName} - CREATE")
    }

    override fun onResume() {
        super.onResume()
        Log.d("STATUS_LIFE","${this.javaClass.simpleName} - RESUME")
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("STATUS_LIFE","${this.javaClass.simpleName} - CREATE VIEW")
        val view = createBindingInstance(inflater, container).also { _bi = it }.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        Log.d("STATUS_LIFE","${this.javaClass.simpleName} - VIEW CREATED")
    }

    open fun initObservers(){}
    open fun initializeView(){}


    override fun onDestroyView() {
        super.onDestroyView()
        _bi = null
        MAIN.cancel()
        Log.d("STATUS_LIFE","${this.javaClass.simpleName} - DESTROY VIEW")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("STATUS_LIFE","${this.javaClass.simpleName} - DESTROY")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("STATUS_LIFE","${this.javaClass.simpleName} - DETACH")
    }

    override fun onStop() {
        super.onStop()
        Log.d("STATUS_LIFE","${this.javaClass.simpleName} - STOP")
    }

    protected fun showLoadingDialog(msg:String) = (requireActivity() as BaseActivity).showLoadingDialog(msg)
    fun hideLoadingDialog() = (requireActivity() as BaseActivity).hideLoadingDialog()

    protected open fun createBindingInstance(inflater: LayoutInflater, container: ViewGroup?): VB {
        val vbType = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
        val vbClass = vbType as Class<VB>
        val method = vbClass.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)

        return method.invoke(null, inflater, container, false) as VB
    }

    protected fun putPreference(constant: String, value: Any){
        preferences.putAny(constant,value)
    }

    protected fun navigate(id: Int){
        Navigation.findNavController(binding.root).navigate(id)
    }

    protected fun onBackPressed(){
        Navigation.findNavController(binding.root).navigateUp()
    }

    protected fun startTimerOnMain(delay : Long = 0, period : Long = 1000,run: () -> Unit) : Timer {
        return Timer().apply {
            scheduleAtFixedRate(timerTask {
                MAIN.launch {
                    run()
                }
            },delay,period)
        }
    }

    open fun handleFailure(failure: Failure?) {
        (requireActivity() as BaseActivity).hideLoadingDialog()
        Toast.makeText(requireContext(), getString(R.string.generic_error), Toast.LENGTH_SHORT).show()
        Log.e("ERROR",failure?.message ?: "Error en FragmentRegister2")
    }

}