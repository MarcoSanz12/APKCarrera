package com.gf.common.extensions

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.StyleSpan
import android.util.Patterns

fun String.Companion.empty() = ""
fun String.fromHTML(): Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
} else {
    Html.fromHtml(this)
}

fun String.textStyle(): SpannableStringBuilder {
    val bss = StyleSpan(Typeface.BOLD) // Span to make text bold
    val iss = StyleSpan(Typeface.ITALIC) //Span to make text italic
    var without = replace("*", "")
    without = without.replace("_", "")
    val sb = SpannableStringBuilder(without)
    val bolds = split("*")
    val itallics = split("_")
    for (bold in bolds) {
        if (bolds.indexOf(bold) % 2 != 0) {
            sb.setSpan(
                bss,
                without.indexOf(bold),
                without.indexOf(bold) + bold.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
    }
    for (itallic in itallics) {
        if (itallics.indexOf(itallic) % 2 != 0) {
            sb.setSpan(
                iss,
                without.indexOf(itallic),
                without.indexOf(itallic) + itallic.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
    }
    return sb
}

fun String.capitalizeFirstChar() : String{
    return if (length > 1)
        this.trim().lowercase().replaceFirstChar { first().uppercaseChar() }
    else
        this
}

fun String.gotoUrl(context: Context) {
    val uriUrl = Uri.parse(this)
    val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
    context.startActivity(launchBrowser)
}

fun String.isValidEmail() =
    !TextUtils.isEmpty(this) && Patterns.EMAIL_ADDRESS.matcher(this).matches()
