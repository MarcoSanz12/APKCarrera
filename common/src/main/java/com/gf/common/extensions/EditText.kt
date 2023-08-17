package com.gf.common.extensions

import android.content.Context
import android.text.Editable
import android.widget.EditText

fun EditText.textToString() = text.toString()

fun EditText.setText(text:String){
    this.text = Editable.Factory.getInstance().newEditable(text)
}

fun EditText.showKeyboard(context:Context?){
    this.requestFocus()
    context?.showKeyboard(this)
}

fun EditText.isEmpty() : Boolean = this.textToString().isEmpty()