package com.gf.common.custom

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.cotesa.common.extensions.notEmpty
import com.gf.common.R
import com.google.android.material.button.MaterialButton

class CustomRoundButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var _buttonText: String? = "PRUEBA"
    private var _buttonColor: Int = 0
    private var _buttonTextColor: Int = 0

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_round_button, this, true)

        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.CustomButton, defStyleAttr, 0)

        // Obtener el texto del atributo definido en el layout
        typedArray.getString(R.styleable.CustomButton_text).notEmpty {
            _buttonText = it
        }
        // Obtener el color del botón del atributo definido en el layout
        _buttonColor = typedArray.getColor(
            R.styleable.CustomButton_buttonColor, resources.getColor(
                R.color.white))

        //Obtener el color del texto
        _buttonTextColor = typedArray.getColor(
            R.styleable.CustomButton_textColor, resources.getColor(R.color.purple_secondary)
        )

        updateButton()

        typedArray.recycle()
    }

    override fun setOnClickListener(l: OnClickListener?) {
        findViewById<MaterialButton>(R.id.bt_round_button).setOnClickListener(l)
    }

    private fun updateButton() {
        findViewById<MaterialButton>(R.id.bt_round_button).apply {
            setBackgroundColor(_buttonColor)
            rippleColor = ColorStateList.valueOf(_buttonTextColor)
            strokeColor = ColorStateList.valueOf(_buttonTextColor)
        }
        findViewById<TextView>(R.id.tv_round_button).apply {
            text = _buttonText
            setTextColor(_buttonTextColor)
        }
    }
}