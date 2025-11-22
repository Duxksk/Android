package com.ohare.browser

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.GeckoRuntime

class MainActivity : AppCompatActivity() {

    private lateinit var geckoView: GeckoView
    private lateinit var session: GeckoSession
    private lateinit var runtime: GeckoRuntime

    private lateinit var addressBar: EditText
    private lateinit var backBtn: ImageButton
    private lateinit var forwardBtn: ImageButton
    private lateinit var refreshBtn: ImageButton
    private lateinit var newTabBtn: ImageButton

    private val tabs = mutableListOf<GeckoSession>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        geckoView = findViewById(R.id.geckoView)
        addressBar = findViewById(R.id.addressBar)
        backBtn = findViewById(R.id.backBtn)
        forwardBtn = findViewById(R.id.forwardBtn)
        refreshBtn = findViewById(R.id.refreshBtn)
        newTabBtn = findViewById(R.id.newTabBtn)

        runtime = GeckoRuntime.create(this)
        createNewTab()

        backBtn.setOnClickListener { session.goBack() }
        forwardBtn.setOnClickListener { session.goForward() }
        refreshBtn.setOnClickListener { session.reload() }

        newTabBtn.setOnClickListener {
            createNewTab()
        }

        addressBar.setOnEditorActionListener { _, _, _ ->
            var url = addressBar.text.toString()
            if (!url.startsWith("http")) {
                url = "https://www.google.com/search?q=${url}"
            }
            session.loadUri(url)
            true
        }
    }

    private fun createNewTab() {
        session = GeckoSession()
        session.open(GeckoSession.Settings())
        tabs.add(session)

        geckoView.setSession(session)
        session.loadUri("https://google.com")

        enableAdBlock(session)
        enableDownloads(session)
    }

    /** 광고 차단 */
    private fun enableAdBlock(session: GeckoSession) {
        val adPatterns = listOf(
            "doubleclick.net",
            "googlesyndication.com",
            "taboola.com",
            "outbrain.com",
            "/ads?",
            "adservice",
            "/banner"
        )

        session.webExtensionController.setMessageDelegate(
            "blocker", { _, message, _ ->
                val url = message.toString()
                if (adPatterns.any { url.contains(it) }) null else message
            },
            "filter"
        )
    }

    /** 다운로드 매니저 */
    private fun enableDownloads(session: GeckoSession) {
        session.downloadDelegate = object : GeckoSession.DownloadDelegate {
            override fun onDownload(session: GeckoSession, download: GeckoSession.Download) {
                download.start() // 자동 저장
            }
        }
    }
}
