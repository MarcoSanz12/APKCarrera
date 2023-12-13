package com.gf.common.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import com.gf.common.R
import java.util.Timer
import kotlin.concurrent.timerTask


class LoadingDialog(context:Context, private var message : String?,private val animated : Boolean = true): Dialog(context) {

    private val tvLoadingMessage by lazy { findViewById<TextView>(R.id.tv_loadingMessage) }

    init {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.loading_dialog)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        if (animated){
            var points = 0
            tvLoadingMessage.text = message
            Timer().scheduleAtFixedRate(timerTask {
                if (points == 3){
                    tvLoadingMessage.text = message
                    points = 0
                }else{
                    tvLoadingMessage.text = "${tvLoadingMessage.text}."
                    points++
                }
            },0L,777L)

        }else{
            tvLoadingMessage.text = message
        }

    }

    override fun onStop() {
        super.onStop()
    }
    fun setText(msg:String) {
        message = msg
        tvLoadingMessage.text = msg
    }
}