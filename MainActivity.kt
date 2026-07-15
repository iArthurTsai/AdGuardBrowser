package com.example.adguardbrowser

import android.os.Bundle
/*import androidx.activity.enableEdgeToEdge*/
import androidx.appcompat.app.AppCompatActivity
/*import androidx.core.view.ViewCompat*/
/*import androidx.core.view.WindowInsetsCompat*/

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

/*import android.view.inputmethod.EditorInfo*/
/*import android.widget.Button*/
/*import android.widget.EditText*/
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
/*import android.view.View*/

class MainActivity : AppCompatActivity() {

    // 🔴 請將這裡改成你家 MBP 2015 的區域網路 IP
    private val mbpIp = "192.168.50.173"

    // 偵測 AdGuard Home 的 DNS 埠口 (預設是 53)
    private val mbpPort = 53
    private val timeoutMs = 2000 // 連線逾時設定為 2 秒
    private lateinit var webView: WebView
    /*private lateinit var urlInput: EditText*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/

        /*hideSystemUI()*/

        // 1. 建立轉圈圈載入畫面
        val progressBar = ProgressBar(this).apply { isIndeterminate = true }
        val loadingLayout = FrameLayout(this).apply {
            addView(progressBar, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                android.view.Gravity.CENTER
            ))
        }
        setContentView(loadingLayout)

        // 2. 返回鍵監聽
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (::webView.isInitialized && webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        // 3. 背景檢查 MBP
        // 啟動時先在背景檢查 MBP 是否在線，避免卡住畫面
        checkMbpConnection()
    }

    /*// 💡 當 App 重新獲得焦點（例如使用者滑出導航列又縮回去）時，確保維持全螢幕
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }*/

    /*// 💡 沉浸式全螢幕演算法 (完美支援 Android 8.0 Oreo)
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }*/

    // 💡 如果 App 已經在背景開啟，此時使用者又點了別的連結，這裡會被觸發更新網址
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val incomingUrl = intent.dataString
        if (!incomingUrl.isNullOrEmpty() && ::webView.isInitialized) {
            webView.loadUrl(incomingUrl)
        }
    }

    private fun checkMbpConnection() {
        CoroutineScope(Dispatchers.IO).launch {
            val isConnected = try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(mbpIp, mbpPort), timeoutMs)
                    true
                }
            } catch (e: Exception) {
                false
            }

            withContext(Dispatchers.Main) {
                if (isConnected) {
                    // 在家：初始化網頁視窗
                    setupWebViewWithProxy()
                } else {
                    // 不在家：提示、開 Chrome、關閉自己
                    Toast.makeText(this@MainActivity, "偵測到不在家，自動切換至 Chrome", Toast.LENGTH_SHORT).show()
                    redirectToChrome()
                }
            }
        }
    }

    /*private fun setupWebView() {
        webView = WebView(this)
        setContentView(webView)

        webView.settings.apply {
            javaScriptEnabled = true      // 啟用 JS，網頁排版才不會壞
            domStorageEnabled = true      // 啟用本地儲存，許多現代網頁需要
            useWideViewPort = true
            loadWithOverviewMode = true
            // 這裡不修改 User-Agent，它會自動使用你手機系統預設的 User-Agent
        }

        webView.webViewClient = WebViewClient()

        // 🔴 這裡設定你希望一打開預設載入的網頁（例如 Google）
        webView.loadUrl("https://www.google.com")
    }*/

    /*private fun setupWebViewWithProxy() {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
        }

        urlInput = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
            hint = "請輸入網址 (例如 yahoo.com)"
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_GO
        }

        val goButton = Button(this).apply {
            text = "前往"
        }

        toolBar.addView(urlInput)
        toolBar.addView(goButton)

        webView = WebView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
            }
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    url?.let { urlInput.setText(it) }
                }
            }
        }

        // 🔴【核心邏輯】強行將 WebView 流量綁定至 MBP 的 Tinyproxy 埠口
        if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            val proxyConfig = ProxyConfig.Builder()
                .addProxyRule("$mbpIp:8888") // 指向你的 MBP 代理服務
                .build()

            ProxyController.getInstance().setProxyOverride(
                proxyConfig,
                { executor -> executor.run() },
                { /* 綁定成功 */ }
            )
        } else {
            // 如果平板的 WebView 版本太舊，不支援此功能，跳出更新提示
            Toast.makeText(this, "請至 Play 商店更新 Android System WebView 以啟用去廣告功能！", Toast.LENGTH_LONG).show()
        }

        goButton.setOnClickListener { loadUserUrl() }

        urlInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                loadUserUrl()
                true
            } else {
                false
            }
        }

        mainLayout.addView(toolBar)
        mainLayout.addView(webView)
        setContentView(mainLayout)

        // 🔴【核心優化】：自動讀取外部傳進來的網址，如果沒有，才預設開啟 Google
        val incomingUrl = intent?.dataString
        if (!incomingUrl.isNullOrEmpty()) {
            webView.loadUrl(incomingUrl)
        } else {
            webView.loadUrl("https://www.google.com")
        }
    }*/

    private fun setupWebViewWithProxy() {
        webView = WebView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
            }
            webViewClient = WebViewClient()
        }

        // 綁定 Proxy
        if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            val proxyConfig = ProxyConfig.Builder().addProxyRule("$mbpIp:8888").build()
            ProxyController.getInstance().setProxyOverride(proxyConfig, { it.run() }, {})
        }

        setContentView(webView) // 🔴 直接讓 WebView 成為唯一的主畫面！

        val incomingUrl = intent?.dataString
        webView.loadUrl(incomingUrl ?: "https://www.google.com")
    }

    /*private fun loadUserUrl() {
        var input = urlInput.text.toString().trim()
        if (input.isEmpty()) return

        if (!input.startsWith("http://") && !input.startsWith("https://")) {
            input = "https://$input"
        }
        webView.loadUrl(input)
    }*/

    private fun redirectToChrome() {
        // 🔴【核心優化】：如果在外面，就將被點擊的網址無縫轉交給 Chrome 開啟；如果沒有網址，預設開啟 Google 首頁
        val targetUrl = intent?.dataString ?: "https://www.google.com"
        try {
            // 強制呼叫 Chrome 開啟
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                setPackage("com.android.chrome")
            }
            startActivity(intent)
        } catch (e: Exception) {
            // 如果手機剛好沒裝 Chrome，就使用系統預設瀏覽器開啟
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
            startActivity(intent)
        }
        finish() // 關閉這個 App，不佔用背景記憶體
    }

    // 讓手機的「返回鍵」能用來回上一頁，而不是直接退出 App
    /*override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }*/
}