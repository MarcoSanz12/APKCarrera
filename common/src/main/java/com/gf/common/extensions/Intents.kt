package com.cotesa.common.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri

fun Intent.sendEmail(activity: Activity, correos: Array<String>, cabecera:String?, texto:String?){
    action = Intent.ACTION_SENDTO

    data= Uri.parse("mailto:")
    putExtra(Intent.EXTRA_EMAIL, correos)
    putExtra(Intent.EXTRA_SUBJECT,cabecera ?: "")
    putExtra(Intent.EXTRA_TEXT,texto ?: "")

    activity.startActivity(this)
}

fun Intent.phoneCall(activity: Activity, numero: String){
    action = Intent.ACTION_DIAL
    data= Uri.parse("tel:$numero")

    activity.startActivity(this)
}

fun Intent.openUrl(context: Context, url: String){
    action = Intent.ACTION_VIEW
    data= Uri.parse(url)

    context.startActivity(this)
}

fun Intent.openGoogleMaps(context: Context, lat: Double, lon: Double){
    action = Intent.ACTION_VIEW
    data = Uri.parse("geo:0,0?q=" + lat +"," + lon)
    setPackage("com.google.android.apps.maps")
    context.startActivity(this)
}

fun Intent.shareMessage(context: Context, message : String){

    action = Intent.ACTION_SEND
    putExtra(Intent.EXTRA_TEXT, message)
    type = "text/plain"

    context.startActivity(Intent.createChooser(this,null))
}