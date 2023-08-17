package com.gf.common.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.cotesa.common.extensions.notEmpty
import com.gf.common.R
import com.google.android.material.button.MaterialButton


class CustomImageButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var _imageResource: Int = 0
    private var _buttonText: String? = "PRUEBA"
    private var _buttonColor: Int = 0

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_image_button, this, true)

        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.CustomButton, defStyleAttr, 0)

        // Obtener la imagen del atributo definido en el layout
        _imageResource = typedArray.getResourceId(R.styleable.CustomButton_imageSrc, R.drawable.icon_done)

        // Obtener el texto del atributo definido en el layout
        typedArray.getString(R.styleable.CustomButton_text).notEmpty {
            _buttonText = it
        }
        // Obtener el color del botón del atributo definido en el layout
        _buttonColor = typedArray.getColor(R.styleable.CustomButton_buttonColor, resources.getColor(R.color.orange_quaternary))

        updateButton()

        typedArray.recycle()
    }

    // Getter y Setter para la propiedad buttonText
    var buttonText: String?
        get() = _buttonText
        set(value) {
            _buttonText = value
            updateButton()
        }

    // Getter y Setter para la propiedad buttonColor
    var buttonColor: Int
        get() = _buttonColor
        set(value) {
            _buttonColor = value
            updateButton()
        }

    override fun setOnClickListener(l: OnClickListener?) {
        findViewById<MaterialButton>(R.id.bt_custom_button).setOnClickListener(l)
    }

    private fun updateButton() {
        findViewById<MaterialButton>(R.id.bt_custom_button).setBackgroundColor(_buttonColor)
        findViewById<TextView>(R.id.tv_custom_button_text).text= _buttonText?.uppercase()
        findViewById<ImageView>(R.id.iv_custom_button_image).setImageResource(_imageResource)
    }
}

