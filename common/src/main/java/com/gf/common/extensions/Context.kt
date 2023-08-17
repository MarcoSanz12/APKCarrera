package com.gf.common.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import com.cotesa.common.extensions.notNull
import java.util.Locale

val Context.networkInfo: NetworkInfo?
    get() =
        (this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo

fun Context.start(intent: Intent) {
    this.startActivity(intent)
}

fun Context?.hideKeyboard(view: View) {
    this.notNull {
        val inputMethodManager = it.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}

fun Context?.showKeyboard(view: View) {
    this.notNull {
        val inputMethodManager = it.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun Context.getStringByLocale(@StringRes stringRes: Int, vararg formatArgs: String): String {
    val configuration = Configuration(resources.configuration)
    val preferences = getSharedPreferences(
        packageName + "_preferences",
        Context.MODE_PRIVATE
    )
    var locale: Locale? = null
    if (preferences.contains("LANGUAGE")) {

        when (preferences.getString("LANGUAGE", "es")) {
            "es" -> locale = Locale("es", "ES")
            "en" -> locale = Locale("en", "GB")
            "fr" -> locale = Locale("fr", "FR")
            "it" -> locale = Locale("it", "IT")
            "de" -> locale = Locale("de", "DE")
            "nl" -> locale = Locale("nl", "NL")
        }
//            val res = resources
//            val dm = res.displayMetrics
//            val conf = res.configuration
//            if (conf.locale != locale) {
//                conf.locale = locale
//                res.updateConfiguration(conf, dm)
//            }
    } else
        locale = Locale.getDefault()
    configuration.setLocale(locale)
    return createConfigurationContext(configuration).resources.getString(stringRes, *formatArgs)
}