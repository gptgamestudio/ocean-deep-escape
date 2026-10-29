package com.example

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color as AndroidColor
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.game.ads.AdMobManager
import com.example.ui.theme.OceanDeepEscapeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize Google Mobile Ads SDK
        AdMobManager.initialize(this)

        setContent {
            OceanDeepEscapeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF010814)
                ) {
                    OceanWebGameApp()
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OceanWebGameApp() {
    val context = LocalContext.current
    val activity = context as? Activity
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // Hardware Back Navigation
    BackHandler {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            activity?.finish()
        }
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF010814)),
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(AndroidColor.parseColor("#010814"))
                setLayerType(View.LAYER_TYPE_HARDWARE, null)

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                    mediaPlaybackRequiresUserGesture = false
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    cacheMode = if (isNetworkAvailable(ctx)) {
                        WebSettings.LOAD_DEFAULT
                    } else {
                        WebSettings.LOAD_CACHE_ELSE_NETWORK
                    }
                }

                // Native Android Bridge for AdMob Interstitials
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun showNativeInterstitial() {
                        activity?.runOnUiThread {
                            AdMobManager.showInterstitial(activity) {}
                        }
                    }
                }, "AndroidBridge")

                webViewClient = object : WebViewClient() {
                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        if (request?.isForMainFrame == true) {
                            // Seamless offline fallback to bundled updated asset
                            view?.loadUrl("file:///android_asset/index.html")
                        }
                    }
                }

                webChromeClient = WebChromeClient()

                val primaryUrl = if (isNetworkAvailable(ctx)) {
                    "https://gptgamestudio.github.io/ocean-deep-escape/"
                } else {
                    "file:///android_asset/index.html"
                }

                loadUrl(primaryUrl)
                webViewInstance = this
            }
        },
        update = { webView ->
            webViewInstance = webView
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.destroy()
            webViewInstance = null
        }
    }
}

private fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
