package com.gf.common.extensions

import android.webkit.WebView


fun WebView.loadHTML(url: String ,html: String){
    settings.javaScriptEnabled = true
    loadDataWithBaseURL(url, html, "text/html; charset=utf-8", "UTF-8", null)
}