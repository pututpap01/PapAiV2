package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.DownloadListener
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.utils.DownloadUtils
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GeneratorWebView(
    url: String,
    modifier: Modifier = Modifier,
    webViewRef: (WebView?) -> Unit = {},
    onProgressChanged: (Int) -> Unit = {},
    onPageStarted: () -> Unit = {},
    onPageFinished: () -> Unit = {},
    onError: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // File chooser callback holder
    var filePathCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uriResult = if (result.resultCode == android.app.Activity.RESULT_OK) {
            val intent = result.data
            if (intent?.data != null) {
                arrayOf(intent.data!!)
            } else if (intent?.clipData != null) {
                val count = intent.clipData!!.itemCount
                Array(count) { i -> intent.clipData!!.getItemAt(i).uri }
            } else {
                null
            }
        } else {
            null
        }
        filePathCallback?.onReceiveValue(uriResult)
        filePathCallback = null
    }

    // JavaScript code to strip header and lock down navigation controls
    val hideHeaderJs = """
        (function() {
            function hidePerchanceHeader() {
                var selectors = [
                    '#top-bar', '#header', '#perchance-top-bar', '.top-bar', '#topbar',
                    '#perchance-header', '.perchance-header', '#edit-button', '#top-nav',
                    'iframe[src*="topbar"]', '#top-menu', '.top-menu', '#nav-bar', '.navbar',
                    'header', 'nav', 'div[id*="topbar"]', 'div[class*="topbar"]', 'div[id*="header"]'
                ];
                
                selectors.forEach(function(sel) {
                    document.querySelectorAll(sel).forEach(function(el) {
                        el.style.setProperty('display', 'none', 'important');
                        el.style.setProperty('visibility', 'hidden', 'important');
                        el.style.setProperty('height', '0px', 'important');
                        el.style.setProperty('max-height', '0px', 'important');
                        el.style.setProperty('opacity', '0', 'important');
                        el.style.setProperty('pointer-events', 'none', 'important');
                        el.style.setProperty('overflow', 'hidden', 'important');
                    });
                });

                // Scan top elements containing "edit", "account", "generator", "new"
                var topDivs = document.querySelectorAll('div, header, nav, bar');
                topDivs.forEach(function(el) {
                    var rect = el.getBoundingClientRect();
                    if (rect.top <= 15 && rect.height > 0 && rect.height < 120) {
                        var text = (el.innerText || '').toLowerCase();
                        if ((text.includes('edit') && text.includes('account')) || text.includes('generator') || text.includes('new')) {
                            el.style.setProperty('display', 'none', 'important');
                            el.style.setProperty('visibility', 'hidden', 'important');
                            el.style.setProperty('height', '0px', 'important');
                        }
                    }
                });

                // Disable links that attempt to open perchance editor or user profiles
                document.querySelectorAll('a').forEach(function(a) {
                    var href = (a.getAttribute('href') || '').toLowerCase();
                    var text = (a.innerText || '').trim().toLowerCase();
                    if (href.indexOf('/edit') !== -1 || href.indexOf('/account') !== -1 || text === 'edit' || text === 'account' || text === 'new') {
                        a.style.setProperty('display', 'none', 'important');
                        a.onclick = function(e) {
                            e.preventDefault();
                            e.stopPropagation();
                            return false;
                        };
                    }
                });
            }

            // Inject CSS style rule immediately
            if (!document.getElementById('pap-ai-custom-style')) {
                var style = document.createElement('style');
                style.id = 'pap-ai-custom-style';
                style.innerHTML = `
                    #top-bar, #header, #perchance-top-bar, .top-bar, #topbar, #perchance-header,
                    .perchance-header, #edit-button, #top-nav, iframe[src*="topbar"],
                    header, nav, a[href*="/edit"], a[href*="/account"] {
                        display: none !important;
                        visibility: hidden !important;
                        height: 0px !important;
                        max-height: 0px !important;
                        opacity: 0 !important;
                        pointer-events: none !important;
                        overflow: hidden !important;
                    }
                    body {
                        margin-top: 0px !important;
                        padding-top: 0px !important;
                    }
                `;
                (document.head || document.documentElement).appendChild(style);
            }

            hidePerchanceHeader();

            // Observe dynamic DOM changes to keep header hidden
            try {
                if (!window.papAiObserver) {
                    window.papAiObserver = new MutationObserver(function() {
                        hidePerchanceHeader();
                    });
                    window.papAiObserver.observe(document.body || document.documentElement, { childList: true, subtree: true });
                }
            } catch(e) {}
        })();
    """.trimIndent()

    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                allowFileAccess = true
                allowContentAccess = true
                mediaPlaybackRequiresUserGesture = false
                setSupportZoom(true)
                builtInZoomControls = false
                displayZoomControls = false
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                userAgentString = userAgentString.replace("wv", "").replace("Android", "Android Pixel")
            }

            // Long click listener on images to allow saving
            setOnLongClickListener { v ->
                val hitTestResult = (v as WebView).hitTestResult
                if (hitTestResult.type == WebView.HitTestResult.IMAGE_TYPE ||
                    hitTestResult.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE
                ) {
                    val imageUrl = hitTestResult.extra
                    if (!imageUrl.isNullOrEmpty()) {
                        coroutineScope.launch {
                            DownloadUtils.saveImageFromDataUrlOrHttp(context, imageUrl)
                        }
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            }

            // Download listener for generated image downloads
            setDownloadListener(DownloadListener { downloadUrl, _, _, _, _ ->
                if (!downloadUrl.isNullOrEmpty()) {
                    coroutineScope.launch {
                        DownloadUtils.saveImageFromDataUrlOrHttp(context, downloadUrl)
                    }
                }
            })

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    onPageStarted()
                    onError(false)
                    view?.evaluateJavascript(hideHeaderJs, null)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    onPageFinished()
                    view?.evaluateJavascript(hideHeaderJs, null)
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val reqUrl = request?.url?.toString() ?: return false
                    
                    // Allow perchance generator page and user asset frames
                    if (reqUrl.contains("perchance.org/papaigeneratorv2") ||
                        reqUrl.contains("user-uploads.perchance.org") ||
                        reqUrl.contains("perchance.org/api") ||
                        reqUrl.startsWith("data:") ||
                        reqUrl.startsWith("blob:")
                    ) {
                        return false
                    }

                    // Lock down edit and account pages
                    if (reqUrl.contains("/edit") || reqUrl.contains("/account") || reqUrl.contains("perchance.org/welcome")) {
                        Toast.makeText(context, "Menu edit & akun dikunci.", Toast.LENGTH_SHORT).show()
                        return true
                    }

                    return false
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                        onError(true)
                    }
                }

                @SuppressLint("WebViewClientOnReceivedSslError")
                override fun onReceivedSslError(
                    view: WebView?,
                    handler: SslErrorHandler?,
                    error: SslError?
                ) {
                    handler?.proceed()
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    onProgressChanged(newProgress)
                    if (newProgress > 25) {
                        view?.evaluateJavascript(hideHeaderJs, null)
                    }
                }

                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallbackParam: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    filePathCallback?.onReceiveValue(null)
                    filePathCallback = filePathCallbackParam

                    val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "image/*"
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }

                    try {
                        filePickerLauncher.launch(intent)
                    } catch (e: Exception) {
                        filePathCallback = null
                        Toast.makeText(context, "Gagal membuka pemilih file.", Toast.LENGTH_SHORT).show()
                        return false
                    }
                    return true
                }

                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    return true
                }
            }
        }
    }

    DisposableEffect(webView) {
        webViewRef(webView)
        onDispose {
            webViewRef(null)
            webView.stopLoading()
            webView.destroy()
        }
    }

    LaunchedEffect(url) {
        webView.loadUrl(url)
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize()
    )
}
