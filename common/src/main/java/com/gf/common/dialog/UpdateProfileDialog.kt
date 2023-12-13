package com.gf.common.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import com.cotesa.common.extensions.toBase64
import com.cotesa.common.extensions.toBitmap
import com.gf.common.R
import com.gf.common.extensions.setText
import com.gf.common.extensions.textToString
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import java.util.Timer
import kotlin.concurrent.timerTask


class UpdateProfileDialog(context:Context, private val picture : String, private val name : String,
                          val onUpdateProfile : (name:String,picture:String) -> Unit,
                          val onImageUpdateClick : () -> Unit
): Dialog(context) {

    private val tvTitle by lazy { findViewById<MaterialTextView>(R.id.tv_title) }
    private val ivImage by lazy { findViewById<ShapeableImageView>(R.id.iv_profile_pic) }
    private val ivAddImage by lazy { findViewById<ShapeableImageView>(R.id.iv_upload) }
    private val etName by lazy { findViewById<TextInputEditText>(R.id.et_name) }
    private val lyName by lazy { findViewById<TextInputLayout>(R.id.ly_name) }
    private val btOk by lazy { findViewById<MaterialButton>(R.id.bt_ok) }
    private val btCancel by lazy { findViewById<MaterialButton>(R.id.bt_close) }

    var newPicture : String = picture

    init {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dlg_update_profile)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Imagen
        ivImage.setImageDrawable(picture.toBitmap()?.toDrawable(context.resources))

        // Texto
        etName.setText(name)

        // Añadir imagen
        ivAddImage.setOnClickListener {
            onImageUpdateClick()
        }

        // Aceptar
        btOk.setOnClickListener {
            onUpdateProfile(etName.textToString(),newPicture)
        }

        // Cancelar
        btCancel.setOnClickListener { dismiss() }
    }

    fun changeImage(image : Bitmap) {
        ivImage.setImageBitmap(image)
        newPicture = image.toBase64()
    }

}