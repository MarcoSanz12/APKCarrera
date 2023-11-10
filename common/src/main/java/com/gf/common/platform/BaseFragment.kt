package com.gf.common.platform

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.gf.common.R
import com.gf.common.exception.Failure
import com.gf.common.extensions.hideKeyboard
import com.gf.common.extensions.putAny
import com.gf.common.extensions.showKeyboard
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.cachapa.expandablelayout.ExpandableLayout
import java.lang.reflect.ParameterizedType
import java.util.Timer
import kotlin.concurrent.timerTask

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _bi: VB? = null
    protected val binding: VB get() = _bi!!
    protected lateinit var MAIN : CoroutineScope
    protected lateinit var preferences : SharedPreferences

    protected val baseActivity : BaseActivity by lazy { requireActivity() as BaseActivity }

    private val mExpandables : Set<ExpandableLayout>
        get() = setOfNotNull(mLySearch,mLyFilter)

    // VAR Search
    private var mLySearch : ExpandableLayout? = null
    private var mEtSearch : TextInputEditText? = null

    private lateinit var mSearchTextWatcher : TextWatcher

    // VAR Filter
    private var mLyFilter : BaseFilter? = null



    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("STATUS_LIFE","${this.javaClass.simpleName} - ${this.lifecycle.currentState.name}")
        MAIN = CoroutineScope(Dispatchers.Main)

        preferences = requireContext().getSharedPreferences(
            requireContext().packageName + "_preferences",
            Context.MODE_PRIVATE
        )

        requireActivity().onBackPressedDispatcher.addCallback {
            onBackPressed()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initObservers()
        Log.d("STATUS_LIFE","${this.javaClass.simpleName} - CREATE")
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
        baseActivity.navController.navigate(id)
    }

    protected fun onBackPressed(){
        baseActivity.navController.navigateUp()
    }

    protected fun startScheduleTimerOnMain(delay : Long = 0, period : Long = 1000, run: () -> Unit) : Timer {
        return Timer().apply {
            scheduleAtFixedRate(timerTask {
                MAIN.launch {
                    run()
                }
            },delay,period)
        }
    }
    protected fun startTimerOnMain(delay : Long = 0, run: () -> Unit) : Timer {
        return Timer().apply {
            schedule(timerTask {
                MAIN.launch {
                    run()
                }
            },delay)
        }
    }

    open fun handleFailure(failure: Failure?) {
        (requireActivity() as BaseActivity).hideLoadingDialog()
        Toast.makeText(requireContext(), getString(R.string.generic_error), Toast.LENGTH_SHORT).show()
        Log.e("ERROR",failure?.message ?: "Error en FragmentRegister2")
    }

    private fun assignActionBarButtons(){
        // Search
        mLySearch = binding.root.findViewWithTag(getString(R.string.expandable_tag_search))
        mEtSearch = mLySearch?.getChildAt(0)?.findViewWithTag(getString(R.string.expandable_tag_search))

        //Filter
        mLyFilter = binding.root.findViewWithTag(getString(R.string.expandable_tag_filter))
    }

    fun setOnAddFriendClickListener(listener: View.OnClickListener) {
        baseActivity.setOnAddFriendByClickListener(listener)
    }
    fun setOnSearchTextWatcher(onTextChanged :(text : String) -> Unit){
        setOnSearchTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                onTextChanged(s.toString())
            }
        })
    }

    fun setOnSearchTextWatcher(textWatcher: TextWatcher) {
        mLySearch?.let { expandable ->
            baseActivity.setOnSearchByClickListener {
                mLyFilter?.collapse(true)
                expandable.toggle()
                if (expandable.isExpanded)
                    mEtSearch!!.showKeyboard()
                else
                    mEtSearch!!.hideKeyboard()
            }

            if (this::mSearchTextWatcher.isInitialized)
                mEtSearch!!.removeTextChangedListener(mSearchTextWatcher)

            mSearchTextWatcher = textWatcher
            mEtSearch!!.addTextChangedListener(mSearchTextWatcher)

        }
    }

}