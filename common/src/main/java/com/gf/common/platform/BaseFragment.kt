package com.gf.common.platform

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.viewbinding.ViewBinding
import com.gf.common.R
import com.gf.common.dialog.LoadingDialog
import com.gf.common.exception.Failure
import com.gf.common.extensions.putAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.lang.reflect.ParameterizedType

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _bi: VB? = null
    protected val binding: VB get() = _bi!!
    protected val MAIN = CoroutineScope(Dispatchers.Main)
    private var loadingDialog : LoadingDialog? = null
    protected lateinit var preferences : SharedPreferences

    private lateinit var activity: BaseActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = requireActivity() as BaseActivity
        preferences = requireContext().getSharedPreferences(
            requireContext().packageName + "_preferences",
            Context.MODE_PRIVATE
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return createBindingInstance(inflater, container).also { _bi = it }.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bi = null
    }

    protected fun showLoadingDialog(msg:String){
        if (loadingDialog == null)
            loadingDialog = LoadingDialog(requireContext(),msg)
        else
            loadingDialog!!.setText(msg)

        loadingDialog!!.show()
    }

    protected fun hideLoadingDialog(){
        loadingDialog?.hide()
    }


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

    protected fun setOnBackPressed(idDestiny : Int){
        activity.onBackPressedDispatcher.addCallback(this) {
            activity.navController.popBackStack(idDestiny,false)
            this.isEnabled = false
        }
    }

    protected fun onBackPressed(){
        activity.onBackPressedDispatcher.onBackPressed()
    }

    open fun handleFailure(failure: Failure?) {
        hideLoadingDialog()
        Toast.makeText(requireContext(), getString(R.string.generic_error), Toast.LENGTH_SHORT).show()
        Log.e("ERROR",failure?.message ?: "Error en FragmentRegister2")
    }

}