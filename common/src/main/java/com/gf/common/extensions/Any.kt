package com.cotesa.common.extensions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import java.io.ByteArrayOutputStream
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow


fun String.removeAccents(): String {
    val regex = "\\p{InCombiningDiacriticalMarks}+".toRegex()
    val temp = java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
    return regex.replace(temp, "")
}

fun String?.notEmpty(isSomething: (it:String) -> Unit){
    if (!this.isNullOrEmpty())
        isSomething(this)
}

fun String?.notEmpty(isSomething: (it:String) -> Unit, isNull: () -> Unit){
    if (!this.isNullOrEmpty())
        isSomething(this)
    else
        isNull()
}

fun <T : Any> T?.notNull(something: (it: T) -> Unit) {
    if (this != null) something(this)
}

fun <T : Any> T?.notNull(isSomething: (it: T) -> Unit, isNull:() -> Unit) {
    if (this != null)
        isSomething(this)
    else
        isNull()
}

fun LatLng.distanceTo(other: LatLng) : Double = SphericalUtil.computeDistanceBetween(this,other)



/**
Obtiene una representación formateada del tamaño del archivo en bytes.
@param bytes El tamaño del archivo en bytes.
@return Una cadena de texto formateada que representa el tamaño del archivo.
 */
fun obtenerTamano(bytes : Long) : String{
    val unidades = arrayOf("B","KB","MB","GB","TB")
    val tamano = floor(log10(bytes.toDouble()) / log10(1024.0)).toInt()
    val tamanoFormateado = bytes / 1024.0.pow(tamano.toDouble())

    return String.format("%.2f %s", tamanoFormateado, unidades[tamano])
}

fun String.getFormato() : String?{
    val puntoPos = this.lastIndexOf('.')
    return if(puntoPos >= 0){
        this.substring(puntoPos+1)
    }else{
        null
    }
}

fun Bitmap.toBase64(): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

fun String.toBitmap(): Bitmap? {
    return try {
        val decodedByteArray = Base64.decode(this, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)
    }catch (ex:Throwable){
        ex.printStackTrace()
        null
    }

}
