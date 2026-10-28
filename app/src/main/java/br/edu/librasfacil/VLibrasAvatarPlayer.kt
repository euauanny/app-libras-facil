package br.edu.librasfacil

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONObject

/** Uses only the official web widget; playback stays in its own controls. */
@SuppressLint("SetJavaScriptEnabled")
class VLibrasAvatarPlayer(
    private val activity: Activity,
    private val onStatus: (Boolean, Boolean, String) -> Unit
) {
    val view = WebView(activity)
    private val handler = Handler(Looper.getMainLooper())
    private var released = false
    private var settled = false
    private val timeout = Runnable { fail() }

    init {
        view.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
        }
        view.webChromeClient = WebChromeClient()
        view.addJavascriptInterface(object {
            @JavascriptInterface
            fun postMessage(message: String) {
                activity.runOnUiThread {
                    if (released || settled) return@runOnUiThread
                    if (message == "scriptLoaded") {
                        settled = true
                        handler.removeCallbacks(timeout)
                        onStatus(true, false, "VLibras carregado")
                    } else if (message == "error") fail()
                }
            }
        }, "AvatarStatus")
        view.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(v: WebView, request: WebResourceRequest): Boolean =
                request.isForMainFrame

            override fun onReceivedError(v: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) fail()
            }
        }
    }

    fun load() {
        if (released) return
        settled = false
        handler.removeCallbacks(timeout)
        onStatus(false, true, "Carregando ferramenta VLibras…")
        handler.postDelayed(timeout, 50000)
        try {
            val html = activity.assets.open("avatar.html").bufferedReader().use { it.readText() }
            view.loadDataWithBaseURL("https://app.librasfacil.invalid/", html, "text/html", "UTF-8", null)
        } catch (_: Exception) { fail() }
    }

    fun prepare(text: String) {
        view.evaluateJavascript("setPhrase(${JSONObject.quote(text)})", null)
    }

    private fun fail() {
        if (released || settled) return
        settled = true
        handler.removeCallbacks(timeout)
        onStatus(false, false, "Não foi possível carregar o VLibras. Confira a internet e toque em Recarregar.")
    }

    fun release() {
        released = true
        handler.removeCallbacksAndMessages(null)
        view.stopLoading()
        view.removeJavascriptInterface("AvatarStatus")
        view.destroy()
    }
}

