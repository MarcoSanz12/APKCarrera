package com.gf.common.extensions

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.cotesa.common.extensions.notNull
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import java.io.File
import kotlin.math.abs


/**
 * Función que intenta cambiar el texto de un TextView, si no hay texto (es decir valor nulo) hará la vista [View.GONE]
 * @param text El texto a cargar
 */
fun TextView.cargarText(pText : String?) : Boolean{
    text = pText?.fromHTML()
    visible(pText != null)
    return pText != null
}

fun TextView.cargarText(pText : String?,vistasSecundarias : List<View>) : Boolean{
    text = pText
    (pText != null).let {vis->
        visible(vis)
        vistasSecundarias.forEach { visible(vis) }
    }
    return pText != null
}
fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.GONE
}

fun View.visible(visible:Boolean?){
    if (visible == true)
        this.visible()
    else
        this.invisible()
}

fun ViewGroup.inflate(@LayoutRes layoutRes: Int): View =
    LayoutInflater.from(context).inflate(layoutRes, this, false)

fun ImageView.loadFromUrl(url: String) = loadFromUrlLocal(
    this.context.applicationContext,
    if (url.contains("http://")) url.replace("http://", "https://") else url
)
//    Glide.with(this.context.applicationContext)
//        .load(if(url.contains("http://")) url.replace("http://", "https://") else url)
//        .transition(DrawableTransitionOptions.withCrossFade())
//        .into(this)!!

fun ImageView.loadFromUrlLocal(context: Context, url: String) {
    if (url.contains("http://")) url.replace("http://", "https://")
    val file: File =
        File(
            context.getExternalFilesDir(null)
                .toString() + File.separator + (if (url.contains("http://")) url.replace(
                "http://",
                "https://"
            ) else url).substring(
                url.lastIndexOf("/") + 1
            )
        )
    Glide.with(this.context.applicationContext)
        .load(if (file.exists()) file.absolutePath else url)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun detectar(resource :Drawable?) : ImageView.ScaleType{
    var scaleType = ImageView.ScaleType.CENTER_CROP
    resource.notNull {
        val ancho = resource!!.intrinsicWidth
        val alto = resource.intrinsicHeight

        val diff = abs(ancho - alto)

        scaleType = if(diff>50){
            ImageView.ScaleType.CENTER_CROP
        } else{
            ImageView.ScaleType.FIT_CENTER
        }
    }
    return scaleType
}

fun View.hideKeyboard(){
    this.clearFocus()
    val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}

fun ImageView.loadFromUrlOverrideParams(
    url: String,
    imageWidthPixels: Int,
    imageHeightPixels: Int,
) =
    Glide.with(this.context.applicationContext)
        .load(url)
        .transition(DrawableTransitionOptions.withCrossFade())
        .override(imageWidthPixels, imageHeightPixels)
        .apply(RequestOptions.bitmapTransform(RoundedCorners(4)))
        .into(this)
fun ImageView.loadFromDrawable(
    drawable: Int,
) =
    Glide.with(this.context.applicationContext)
        .load(drawable)
        .into(this)


fun ImageView.loadFromUrlWithError(url: String, idDrawable: Int) {
    val file: File =
        File(
            context.getExternalFilesDir(null)
                .toString() + File.separator + (if (url.contains("http://")) url.replace(
                "http://",
                "https://"
            ) else url).substring(
                url.lastIndexOf("/") + 1
            )
        )
    try {
        val options: RequestOptions = RequestOptions()
            .error(idDrawable)
        Glide.with(this.context.applicationContext)
            .load(
                if (file.exists()) file.absolutePath else if (url.contains("http://")) url.replace(
                    "http://",
                    "https://"
                ) else url
            ).apply(options)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(this)
    } catch (ex: Exception) {
        Glide.with(this.context.applicationContext)
            .load(idDrawable)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(this)
    }
}

fun TabLayout.addOnTabSelectedListener(onTabSelected : (tab: TabLayout.Tab?) -> Unit ){
    addOnTabSelectedListener(object : OnTabSelectedListener{
        override fun onTabSelected(tab: TabLayout.Tab?) {
            onTabSelected(tab)
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
        }

    })
}


val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()


