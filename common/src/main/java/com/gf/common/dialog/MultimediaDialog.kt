package com.gf.common.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.cotesa.common.extensions.notNull
import com.gf.common.R
import com.gf.common.adapter.MultimediaGalleryDialogAdapter

class MultimediaDialog(context: Context, var images: List<Bitmap>? = null, var url : List<String>? = null, var position: Int) : Dialog(context) {

    private val viewpager : ViewPager2 by lazy { findViewById(R.id.viewpager) }
    private val tvCount : TextView by lazy { findViewById(R.id.tv_page_count) }

    companion object{
        private const val TAG = "MultimediaDialog"
    }
    init {
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        setOnCancelListener { dismiss() }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.gallery_layout)

        window.notNull {
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }


        val adapter = MultimediaGalleryDialogAdapter(
            images,
            url,
            ::onEndScrolling
        )

        with(viewpager){
            this.adapter = adapter
            registerOnPageChangeCallback(object : OnPageChangeCallback(){
                override fun onPageSelected(position: Int) {
                    onImageChanged(position)
                }
            })
            setCurrentItem(position,false)
            isUserInputEnabled = true
        }
        findViewById<ImageView>(R.id.iv_close).setOnClickListener { dismiss() }
    }

    private fun onImageChanged(p: Int) {
        tvCount.text = "${p + 1} / ${images?.size ?: url?.size ?: 0}"
    }

    private fun onEndScrolling(b: Boolean) {
        viewpager.isUserInputEnabled = b
    }



}
