package com.gf.common.extensions

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import androidx.annotation.LayoutRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.cotesa.common.extensions.notNull
import com.bumptech.glide.request.target.Target
import com.gf.common.R
import java.io.File
import kotlin.math.abs


fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.GONE
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
        .error(context.getDrawable(R.drawable.ic_launcher_foreground))
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                // Error al cargar la imagen
                this@loadFromUrlLocal.scaleType = ScaleType.FIT_CENTER
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                // Imagen cargada exitosamente
                this@loadFromUrlLocal.scaleType = detectar(resource)
                return false
            }
        })
        .into(this)
}

fun detectar(resource :Drawable?) : ScaleType{
    var scaleType = ScaleType.CENTER_CROP
    resource.notNull {
        val ancho = resource!!.intrinsicWidth
        val alto = resource.intrinsicHeight

        val diff = abs(ancho - alto)

        scaleType = if(diff>50){
            ScaleType.CENTER_CROP
        } else{
            ScaleType.FIT_CENTER
        }
    }
    return scaleType
}

fun ImageView.loadFromUrlOverrideParams(
    url: String,
    imageWidthPixels: Int,
    imageHeightPixels: Int
) =
    Glide.with(this.context.applicationContext)
        .load(url)
        .transition(DrawableTransitionOptions.withCrossFade())
        .override(imageWidthPixels, imageHeightPixels)
        .apply(RequestOptions.bitmapTransform(RoundedCorners(4)))
        .into(this)
fun ImageView.loadFromDrawable(
    drawable: Int
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


val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

