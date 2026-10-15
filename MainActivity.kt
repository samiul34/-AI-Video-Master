package com.aivideomaster.app

import android.app.Activity
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.util.LruCache
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class LogoView(c: Context) : View(c) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    init { setLayerType(LAYER_TYPE_SOFTWARE, null) }
    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val pad = w * 0.1f
        val r = RectF(pad, pad, w - pad, w - pad)
        p.style = Paint.Style.FILL
        p.shader = LinearGradient(r.left, r.top, r.right, r.bottom,
            intArrayOf(Color.rgb(139, 92, 246), Color.rgb(24, 160, 255)), null, Shader.TileMode.CLAMP)
        p.setShadowLayer(w * 0.09f, 0f, w * 0.03f, Color.argb(200, 24, 217, 255))
        canvas.drawRoundRect(r, w * 0.24f, w * 0.24f, p)
        p.clearShadowLayer()
        p.shader = null
        p.color = Color.rgb(9, 14, 44)
        canvas.drawRoundRect(RectF(w * 0.22f, w * 0.22f, w * 0.78f, w * 0.78f), w * 0.16f, w * 0.16f, p)
        val path = Path().apply {
            moveTo(w * 0.42f, w * 0.36f); lineTo(w * 0.66f, w * 0.5f); lineTo(w * 0.42f, w * 0.64f); close()
        }
        p.shader = LinearGradient(w * 0.4f, w * 0.36f, w * 0.66f, w * 0.64f, Color.WHITE, Color.rgb(24, 217, 255), Shader.TileMode.CLAMP)
        canvas.drawPath(path, p)
        p.shader = null
        val b = RectF(w * 0.66f, 0f, w * 0.98f, w * 0.26f)
        p.color = Color.rgb(139, 92, 246)
        canvas.drawRoundRect(b, w * 0.07f, w * 0.07f, p)
        p.color = Color.WHITE
        p.textSize = w * 0.16f
        p.textAlign = Paint.Align.CENTER
        p.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("AI", b.centerX(), b.centerY() + w * 0.055f, p)
    }
}

class MainActivity : Activity() {
    private val bg = Color.rgb(5, 8, 22)
    private val panel = Color.rgb(15, 23, 42)
    private val cyan = Color.rgb(24, 217, 255)
    private val purple = Color.rgb(139, 92, 246)
    private val white = Color.rgb(245, 248, 255)
    private val grey = Color.rgb(151, 169, 198)
    private lateinit var container: FrameLayout
    private lateinit var navItems: List<TextView>
    private var tab = 0
    private var inPlayer = false
    private var searchText = ""
    private var autoRun = false
    private var chip = 0
    private var video: VideoView? = null
    private val cache = LruCache<String, Bitmap>(40)
    private val ui = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = gradient(intArrayOf(bg, Color.rgb(13, 17, 44)), 0)
        }
        root.setOnApplyWindowInsetsListener { v, insets ->
            @Suppress("DEPRECATION")
            v.setPadding(0, insets.systemWindowInsetTop, 0, insets.systemWindowInsetBottom)
            insets
        }
        container = FrameLayout(this)
        root.addView(container, LinearLayout.LayoutParams(-1, 0, 1f))
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.rgb(8, 12, 30))
            setPadding(0, dp(8), 0, dp(8))
        }
        val names = listOf("🏠\nHome", "🔍\nSearch", "⬇\nDownloads", "⋯\nMore")
        navItems = names.mapIndexed { i, n ->
            val t = TextView(this)
            t.text = n
            t.textSize = 11f
            t.gravity = Gravity.CENTER
            t.setTextColor(grey)
            t.setOnClickListener { show(i) }
            nav.addView(t, LinearLayout.LayoutParams(0, -2, 1f))
            t
        }
        root.addView(nav, LinearLayout.LayoutParams(-1, -2))
        setContentView(root)
        show(0)
    }

    private fun show(i: Int) {
        stopVideo()
        inPlayer = false
        tab = i
        navItems.forEachIndexed { k, t -> t.setTextColor(if (k == i) cyan else grey) }
        container.removeAllViews()
        container.addView(when (i) {
            0 -> homeView()
            1 -> searchView()
            2 -> downloadsView()
            else -> moreView()
        })
    }

    private fun stopVideo() { video?.stopPlayback(); video = null }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        when {
            inPlayer -> show(tab)
            tab != 0 -> show(0)
            else -> super.onBackPressed()
        }
    }

    override fun onPause() { super.onPause(); video?.pause() }

    private fun homeView(): View {
        val p = page()
        p.gravity = Gravity.CENTER_HORIZONTAL
        p.addView(LogoView(this), LinearLayout.LayoutParams(dp(120), dp(120)))
        p.addView(tv("AI Video Master", 28f, white, true).apply { gravity = Gravity.CENTER }, lp(-2, -2, 8))
        p.addView(tv("Find  •  Download  •  Enjoy", 13f, cyan).apply { gravity = Gravity.CENTER }, lp(-2, -2, 4))

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(16), dp(16), dp(16), dp(20))
            background = rounded(Color.rgb(12, 24, 56), 24, Color.argb(130, 24, 217, 255))
            elevation = dp(10).toFloat()
        }
        card.addView(tv("🎙 AI Voice Search", 13f, cyan, true))
        card.addView(tv("“বাংলাদেশের সুন্দর জায়গার ভিডিও খুঁজে দাও”", 15f).apply { gravity = Gravity.CENTER }, lp(-1, -2, 10))
        val wave = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        wave.addView(bars())
        val mic = tv("🎙", 34f, Color.WHITE).apply {
            gravity = Gravity.CENTER
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(purple, cyan)).apply {
                shape = GradientDrawable.OVAL
                setStroke(dp(3), Color.argb(120, 255, 255, 255))
            }
            elevation = dp(10).toFloat()
            setOnClickListener { startVoice() }
        }
        wave.addView(mic, LinearLayout.LayoutParams(dp(84), dp(84)).apply { leftMargin = dp(10); rightMargin = dp(10) })
        wave.addView(bars())
        card.addView(wave, lp(-2, -2, 16))
        p.addView(card, lp(-1, -2, 18))

        val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        val et = field("লিখে খুঁজুন…")
        et.setSingleLine(true)
        row.addView(et, LinearLayout.LayoutParams(0, dp(48), 1f))
        val go = button("⌕", Color.rgb(0, 133, 175))
        go.setOnClickListener { goSearch(et.text.toString()) }
        row.addView(go, LinearLayout.LayoutParams(dp(56), dp(48)).apply { leftMargin = dp(8) })
        p.addView(row, lp(-1, -2, 16))

        listOf("🎙  Voice Search", "⌨  Text Search", "⬇  Direct Download", "🛡  Safe open sources", "🎬  Cinematic UI").forEach {
            p.addView(tv(it, 13f).apply {
                setPadding(dp(14), dp(11), dp(14), dp(11))
                background = rounded(panel, 14)
            }, lp(-1, -2, 8))
        }
        return scroll(p)
    }

    private fun bars(): LinearLayout = LinearLayout(this).apply {
        gravity = Gravity.CENTER_VERTICAL
        listOf(10, 22, 34, 18, 28, 12, 20).forEach { h ->
            val v = View(this@MainActivity)
            v.setBackgroundColor(cyan)
            addView(v, LinearLayout.LayoutParams(dp(3), dp(h)).apply { leftMargin = dp(2); rightMargin = dp(2) })
        }
    }

    private fun searchView(): View {
        val p = page()
        p.addView(tv("Search Results", 20f, white, true))
        val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        val et = field("ভিডিও খুঁজুন…")
        et.setText(searchText)
        et.setSingleLine(true)
        et.imeOptions = EditorInfo.IME_ACTION_SEARCH
        row.addView(et, LinearLayout.LayoutParams(0, dp(48), 1f))
        val mic = button("🎙", purple)
        mic.setOnClickListener { startVoice() }
        row.addView(mic, LinearLayout.LayoutParams(dp(50), dp(48)).apply { leftMargin = dp(8) })
        val go = button("⌕", Color.rgb(0, 133, 175))
        row.addView(go, LinearLayout.LayoutParams(dp(50), dp(48)).apply { leftMargin = dp(8) })
        p.addView(row, lp(-1, -2, 12))

        val chips = LinearLayout(this)
        val chipViews = ArrayList<TextView>()
        fun style() {
            chipViews.forEachIndexed { k, t ->
                t.setTextColor(if (k == chip) bg else white)
                t.background = rounded(if (k == chip) cyan else panel, 16)
            }
        }
        listOf("All", "YouTube", "Facebook", "Other").forEachIndexed { i, n ->
            val t = tv(n, 12f)
            t.setPadding(dp(14), dp(8), dp(14), dp(8))
            t.setOnClickListener { chip = i; style() }
            chipViews.add(t)
            chips.addView(t, lp(-2, -2).apply { rightMargin = dp(8) })
        }
        style()
        p.addView(chips, lp(-1, -2, 12))
        p.addView(tv("YouTube/Facebook বেছে খুঁজলে ফলাফল ব্রাউজারে খুলবে। অ্যাপের ভেতরের ফলাফল Internet Archive-এর ওপেন ভিডিও থেকে আসে (ইংরেজি কীওয়ার্ডে ভালো কাজ করে)।", 11f, grey), lp(-1, -2, 8))
        val status = tv("", 13f, cyan)
        p.addView(status, lp(-1, -2, 10))
        val results = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        p.addView(results, lp(-1, -2, 4))

        val run = { runSearch(et.text.toString().trim(), results, status) }
        go.setOnClickListener { run() }
        et.setOnEditorActionListener { _, _, _ -> run(); true }
        if (autoRun) { autoRun = false; run() }
        return scroll(p)
    }

    private fun runSearch(q: String, results: LinearLayout, status: TextView) {
        searchText = q
        hideKeyboard()
        if (q.isEmpty()) { status.text = "আগে ভিডিওর নাম লিখুন বা বলুন।"; return }
        if (chip == 1 || chip == 2) {
            val base = if (chip == 1) "https://www.youtube.com/results?search_query=" else "https://www.facebook.com/watch/search/?q="
            openUrl(base + Uri.encode(q))
            status.text = "ব্রাউজারে ফলাফল খোলা হয়েছে।"
            return
        }
        status.text = "খোঁজা হচ্ছে…"
        results.removeAllViews()
        Thread {
            try {
                val api = "https://archive.org/advancedsearch.php?q=" + Uri.encode("($q) AND mediatype:movies") +
                    "&fl%5B%5D=identifier&fl%5B%5D=title&fl%5B%5D=creator&sort%5B%5D=downloads+desc&rows=20&output=json"
                val docs = JSONObject(httpGet(api)).getJSONObject("response").getJSONArray("docs")
                ui.post {
                    if (docs.length() == 0) status.text = "কোনো ফলাফল পাওয়া যায়নি। অন্য শব্দে (ইংরেজিতে) চেষ্টা করুন।"
                    else {
                        status.text = "${docs.length()}টি ফলাফল"
                        for (i in 0 until docs.length()) {
                            val d = docs.getJSONObject(i)
                            results.addView(resultCard(d.optString("identifier"), firstText(d.opt("title")), firstText(d.opt("creator"))), lp(-1, -2, 12))
                        }
                    }
                }
            } catch (e: Exception) {
                ui.post { status.text = "নেটওয়ার্ক ত্রুটি। ইন্টারনেট পরীক্ষা করুন।" }
            }
        }.start()
    }

    private fun firstText(o: Any?): String = when (o) {
        null -> ""
        is JSONArray -> o.optString(0)
        else -> o.toString()
    }

    private fun resultCard(id: String, title: String, by: String): View {
        val c = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(panel, 18)
            setPadding(dp(8), dp(8), dp(8), dp(12))
            elevation = dp(4).toFloat()
            isClickable = true
        }
        val fl = FrameLayout(this)
        fl.background = rounded(Color.rgb(20, 30, 60), 14)
        fl.clipToOutline = true
        val iv = ImageView(this)
        iv.scaleType = ImageView.ScaleType.CENTER_CROP
        fl.addView(iv, FrameLayout.LayoutParams(-1, dp(180)))
        val play = tv("▶", 20f, Color.WHITE)
        play.gravity = Gravity.CENTER
        play.background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.argb(150, 10, 20, 50)); setStroke(dp(2), Color.WHITE) }
        fl.addView(play, FrameLayout.LayoutParams(dp(54), dp(54), Gravity.CENTER))
        loadImage("https://archive.org/services/img/$id", iv)
        c.addView(fl, LinearLayout.LayoutParams(-1, dp(180)))
        c.addView(tv(title.ifEmpty { id }, 14f, white, true).apply { maxLines = 2; ellipsize = TextUtils.TruncateAt.END }, lp(-1, -2, 8))
        if (by.isNotEmpty()) c.addView(tv(by, 12f, grey).apply { maxLines = 1; ellipsize = TextUtils.TruncateAt.END }, lp(-1, -2, 3))
        c.setOnClickListener { openPlayer(title.ifEmpty { id }, id, null) }
        return c
    }

    private fun openPlayer(title: String, id: String?, local: Uri?) {
        stopVideo()
        inPlayer = true
        container.removeAllViews()
        val p = page()
        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        top.addView(tv("‹", 30f, white).apply { setPadding(0, 0, dp(14), 0); setOnClickListener { show(tab) } })
        top.addView(tv(if (local != null) "Downloaded video" else "Preview", 17f, white, true))
        p.addView(top)
        val vv = VideoView(this)
        video = vv
        val box = FrameLayout(this)
        box.setBackgroundColor(Color.BLACK)
        box.addView(vv, FrameLayout.LayoutParams(-1, dp(220), Gravity.CENTER))
        val msg = tv("ভিডিও লোড হচ্ছে…", 13f, cyan)
        msg.gravity = Gravity.CENTER
        box.addView(msg, FrameLayout.LayoutParams(-1, -2, Gravity.CENTER))
        p.addView(box, lp(-1, dp(220), 12))
        p.addView(tv(title, 16f, white, true), lp(-1, -2, 12))
        if (id != null) p.addView(tv("Source: Internet Archive", 12f, grey), lp(-1, -2, 4))
        val dlBtn = button("⬇   Download", Color.rgb(0, 150, 120))
        p.addView(dlBtn, lp(-1, dp(52), 16))

        fun play(uri: Uri) {
            val mc = MediaController(this)
            mc.setAnchorView(vv)
            vv.setMediaController(mc)
            vv.setOnPreparedListener { msg.visibility = View.GONE; it.start() }
            vv.setOnErrorListener { _, _, _ -> msg.text = "প্লে করা যায়নি।"; msg.visibility = View.VISIBLE; true }
            vv.setVideoURI(uri)
        }

        if (local != null) {
            dlBtn.visibility = View.GONE
            play(local)
        } else if (id != null) {
            dlBtn.setOnClickListener { toast("ফাইল খোঁজা হচ্ছে, একটু অপেক্ষা করুন।") }
            Thread {
                try {
                    val files = JSONObject(httpGet("https://archive.org/metadata/$id")).getJSONArray("files")
                    val mp4s = (0 until files.length()).map { files.getJSONObject(it) }
                        .filter { it.optString("name").lowercase(Locale.ROOT).endsWith(".mp4") }
                    val pick = mp4s.firstOrNull { it.optString("format").contains("h.264", true) }
                        ?: mp4s.minByOrNull { it.optString("size", "0").toLongOrNull() ?: Long.MAX_VALUE }
                    if (pick == null) {
                        ui.post { msg.text = "এই আইটেমে MP4 ফাইল নেই।" }
                    } else {
                        val size = pick.optString("size", "0").toLongOrNull() ?: 0L
                        val url = "https://archive.org/download/$id/" + Uri.encode(pick.optString("name"), "/")
                        ui.post {
                            if (inPlayer && video === vv) {
                                play(Uri.parse(url))
                                dlBtn.text = "⬇   Download (MP4 • " + String.format(Locale.US, "%.1f MB", size / 1048576.0) + ")"
                                dlBtn.setOnClickListener { enqueue(url, title, id) }
                            }
                        }
                    }
                } catch (e: Exception) {
                    ui.post { msg.text = "লোড করা যায়নি। ইন্টারনেট পরীক্ষা করুন।" }
                }
            }.start()
        }
        container.addView(scroll(p))
    }

    private fun downloadsView(): View {
        val p = page()
        val head = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        head.addView(tv("Downloads", 20f, white, true), LinearLayout.LayoutParams(0, -2, 1f))
        head.addView(tv("↻ রিফ্রেশ", 13f, cyan).apply { setOnClickListener { show(2) } })
        p.addView(head)
        val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val list = entries()
        if (list.length() == 0) p.addView(tv("এখনো কোনো ডাউনলোড নেই।", 14f, grey), lp(-2, -2, 16))
        for (i in 0 until list.length()) {
            val e = list.getJSONObject(i)
            val id = e.getLong("id")
            var status = -1
            var total = 0L
            dm.query(DownloadManager.Query().setFilterById(id))?.use { c ->
                if (c.moveToFirst()) {
                    status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    total = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                }
            }
            p.addView(downloadRow(dm, e.optString("title"), e.optString("thumb"), id, status, total), lp(-1, -2, 10))
        }
        return scroll(p)
    }

    private fun downloadRow(dm: DownloadManager, title: String, thumb: String, id: Long, status: Int, total: Long): View {
        val r = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            background = rounded(panel, 16)
            setPadding(dp(10), dp(10), dp(12), dp(10))
        }
        val iv = ImageView(this)
        iv.scaleType = ImageView.ScaleType.CENTER_CROP
        iv.background = rounded(Color.rgb(20, 30, 60), 10)
        iv.clipToOutline = true
        if (thumb.isNotEmpty()) loadImage("https://archive.org/services/img/$thumb", iv)
        r.addView(iv, LinearLayout.LayoutParams(dp(68), dp(68)))
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), 0, dp(8), 0) }
        col.addView(tv(title, 13f, white, true).apply { maxLines = 2; ellipsize = TextUtils.TruncateAt.END })
        val size = if (total > 0) String.format(Locale.US, " • %.1f MB", total / 1048576.0) else ""
        val (label, color) = when (status) {
            DownloadManager.STATUS_SUCCESSFUL -> Pair("✔ Downloaded$size", Color.rgb(52, 211, 153))
            DownloadManager.STATUS_FAILED -> Pair("✖ ব্যর্থ", Color.rgb(248, 113, 113))
            -1 -> Pair("ফাইলটি আর নেই", grey)
            else -> Pair("⏳ ডাউনলোড হচ্ছে$size", cyan)
        }
        col.addView(tv(label, 12f, color), lp(-2, -2, 4))
        r.addView(col, LinearLayout.LayoutParams(0, -2, 1f))
        if (status == DownloadManager.STATUS_SUCCESSFUL) {
            val b = tv("▶", 18f, Color.WHITE)
            b.gravity = Gravity.CENTER
            b.background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.rgb(0, 133, 175)) }
            b.setOnClickListener {
                val uri = dm.getUriForDownloadedFile(id)
                if (uri != null) openPlayer(title, null, uri) else toast("ফাইল খোলা যায়নি।")
            }
            r.addView(b, LinearLayout.LayoutParams(dp(42), dp(42)))
        }
        return r
    }

    private fun moreView(): View {
        val p = page()
        p.addView(tv("More", 20f, white, true))
        p.addView(tv("সরাসরি ভিডিও ডাউনলোড", 14f, cyan, true), lp(-2, -2, 18))
        p.addView(tv("শুধু অনুমোদিত https MP4 / M4V / WEBM / MOV ফাইল URL দিন।", 12f, grey), lp(-1, -2, 4))
        val et = field("https://example.com/video.mp4")
        et.setSingleLine(true)
        p.addView(et, lp(-1, dp(48), 10))
        val b = button("⬇   Download video", Color.rgb(0, 128, 105))
        b.setOnClickListener {
            val u = et.text.toString().trim()
            if (!u.startsWith("https://") || videoExt(u) == null) toast("সরাসরি https ভিডিও ফাইল URL দিন (.mp4/.webm ইত্যাদি)।")
            else enqueue(u, "Direct video", null)
        }
        p.addView(b, lp(-1, dp(50), 9))
        p.addView(tv("সীমাবদ্ধতা", 14f, cyan, true), lp(-2, -2, 24))
        p.addView(tv("• YouTube ও Facebook থেকে ভিডিও ডাউনলোড এই অ্যাপে নেই; ওই প্ল্যাটফর্মের নিয়ম মানতে হয়। সার্চ ব্রাউজারে খোলে।\n• অ্যাপের ভেতরের ফলাফল Internet Archive থেকে আসে।\n• শুধু নিজের বা ডাউনলোডের অনুমতি আছে এমন ভিডিও নামান।", 12f, grey), lp(-1, -2, 6))
        return scroll(p)
    }

    private fun goSearch(q: String) {
        searchText = q.trim()
        autoRun = true
        show(1)
    }

    private fun startVoice() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "কোন ভিডিও খুঁজবেন বলুন")
            }
            @Suppress("DEPRECATION")
            startActivityForResult(intent, 101)
        } catch (e: ActivityNotFoundException) {
            toast("এই ফোনে ভয়েস রিকগনিশন সুবিধা পাওয়া যায়নি।")
        }
    }

    @Deprecated("Legacy activity result")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == RESULT_OK) {
            val m = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!m.isNullOrEmpty()) goSearch(m[0])
        }
    }

    private fun videoExt(url: String): String? {
        val path = url.substringBefore("?").lowercase(Locale.ROOT)
        return listOf(".mp4", ".m4v", ".webm", ".mov").firstOrNull { path.endsWith(it) }
    }

    private fun enqueue(url: String, title: String, thumbId: String?) {
        try {
            val safe = title.replace(Regex("[^A-Za-z0-9 _-]"), "").trim().take(40).ifEmpty { "video" }
            val req = DownloadManager.Request(Uri.parse(url))
                .setTitle(title)
                .setDescription("AI Video Master")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "AIVM_${safe}_${System.currentTimeMillis()}${videoExt(url) ?: ".mp4"}")
            val dlId = (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(req)
            saveEntry(dlId, title, thumbId)
            toast("ডাউনলোড শুরু হয়েছে। Downloads ট্যাবে দেখুন।")
        } catch (e: Exception) {
            toast("ডাউনলোড শুরু করা যায়নি। URL পরীক্ষা করুন।")
        }
    }

    private fun entries(): JSONArray =
        try { JSONArray(getSharedPreferences("aivm", Context.MODE_PRIVATE).getString("dl", "[]")) } catch (e: Exception) { JSONArray() }

    private fun saveEntry(id: Long, title: String, thumb: String?) {
        val old = entries()
        val n = JSONArray()
        n.put(JSONObject().put("id", id).put("title", title).put("thumb", thumb ?: ""))
        for (i in 0 until old.length()) n.put(old.get(i))
        getSharedPreferences("aivm", Context.MODE_PRIVATE).edit().putString("dl", n.toString()).apply()
    }

    private fun httpGet(u: String): String {
        val c = URL(u).openConnection() as HttpURLConnection
        c.connectTimeout = 15000
        c.readTimeout = 20000
        c.setRequestProperty("User-Agent", "AIVideoMaster/1.0")
        return try { c.inputStream.bufferedReader().use { it.readText() } } finally { c.disconnect() }
    }

    private fun loadImage(url: String, iv: ImageView) {
        val hit = cache.get(url)
        if (hit != null) { iv.setImageBitmap(hit); return }
        Thread {
            try {
                val c = URL(url).openConnection() as HttpURLConnection
                c.connectTimeout = 10000
                c.readTimeout = 15000
                val bmp = c.inputStream.use { BitmapFactory.decodeStream(it) }
                if (bmp != null) { cache.put(url, bmp); ui.post { iv.setImageBitmap(bmp) } }
            } catch (e: Exception) { }
        }.start()
    }

    private fun openUrl(u: String) {
        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(u))) } catch (e: ActivityNotFoundException) { toast("ব্রাউজার পাওয়া যায়নি।") }
    }

    private fun hideKeyboard() {
        currentFocus?.let { (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(it.windowToken, 0) }
    }

    private fun page(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(18), dp(16), dp(18), dp(16))
    }

    private fun scroll(v: View): ScrollView = ScrollView(this).apply { addView(v) }

    private fun tv(t: String, size: Float, color: Int = white, bold: Boolean = false): TextView = TextView(this).apply {
        text = t
        textSize = size
        setTextColor(color)
        if (bold) typeface = Typeface.DEFAULT_BOLD
    }

    private fun lp(w: Int, h: Int, top: Int = 0) = LinearLayout.LayoutParams(w, h).apply { topMargin = dp(top) }

    private fun field(hintText: String): EditText = EditText(this).apply {
        hint = hintText
        setHintTextColor(Color.rgb(137, 153, 180))
        setTextColor(white)
        textSize = 14f
        setPadding(dp(14), dp(10), dp(14), dp(10))
        background = rounded(panel, 14)
    }

    private fun button(label: String, color: Int): TextView = TextView(this).apply {
        text = label
        textSize = 15f
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        setTextColor(Color.WHITE)
        background = gradient(intArrayOf(color, shade(color)), 15)
        elevation = dp(6).toFloat()
        isClickable = true
        isFocusable = true
    }

    private fun shade(color: Int): Int =
        Color.rgb((Color.red(color) * 0.65).toInt(), (Color.green(color) * 0.65).toInt(), (Color.blue(color) * 0.75).toInt())

    private fun rounded(color: Int, radius: Int, stroke: Int = Color.argb(50, 110, 210, 255)) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
        setStroke(dp(1), stroke)
    }

    private fun gradient(colors: IntArray, radius: Int) = GradientDrawable(GradientDrawable.Orientation.TL_BR, colors).apply {
        cornerRadius = dp(radius).toFloat()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
