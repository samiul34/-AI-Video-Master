package com.aivideomaster.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.ClipboardManager
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
import android.text.InputType
import android.text.TextUtils
import android.util.LruCache
import android.view.Gravity
import android.view.MotionEvent
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
import android.widget.ProgressBar
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class LogoView(c: Context) : View(c) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val rad = w * 0.2f
        // 3D নিচের কিনারা
        p.style = Paint.Style.FILL
        p.shader = null
        p.color = Color.rgb(72, 42, 150)
        canvas.drawRoundRect(RectF(w * 0.06f, w * 0.11f, w * 0.94f, w * 0.99f), rad, rad, p)
        // মূল বক্স
        val face = RectF(w * 0.06f, w * 0.06f, w * 0.94f, w * 0.94f)
        p.shader = LinearGradient(0f, face.top, 0f, face.bottom,
            Color.rgb(150, 118, 245), Color.rgb(104, 66, 214), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(face, rad, rad, p)
        p.shader = null
        // সাদা আউটলাইন প্লে আইকন
        p.style = Paint.Style.STROKE
        p.color = Color.WHITE
        p.strokeWidth = w * 0.05f
        p.strokeJoin = Paint.Join.ROUND
        p.strokeCap = Paint.Cap.ROUND
        val path = Path().apply {
            moveTo(w * 0.41f, w * 0.31f); lineTo(w * 0.69f, w * 0.5f); lineTo(w * 0.41f, w * 0.69f); close()
        }
        canvas.drawPath(path, p)
        // AI ব্যাজ
        p.style = Paint.Style.FILL
        val b = RectF(w * 0.67f, 0f, w * 0.99f, w * 0.27f)
        p.color = Color.rgb(160, 126, 250)
        canvas.drawRoundRect(b, w * 0.08f, w * 0.08f, p)
        p.color = Color.WHITE
        p.textSize = w * 0.15f
        p.textAlign = Paint.Align.CENTER
        p.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("AI", b.centerX(), b.centerY() + w * 0.052f, p)
    }
}

/** লাইন আইকন: 0=ডাউনলোড 1=সার্চ 2=প্লে 3=সেটিংস 4=মাইক */
class IconView(c: Context, private val kind: Int) : View(c) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    var tint: Int = Color.WHITE
        set(v) { field = v; invalidate() }
    override fun onDraw(canvas: Canvas) {
        val s = minOf(width, height).toFloat()
        canvas.translate((width - s) / 2f, (height - s) / 2f)
        p.style = Paint.Style.STROKE
        p.color = tint
        p.strokeWidth = s * 0.085f
        p.strokeCap = Paint.Cap.ROUND
        p.strokeJoin = Paint.Join.ROUND
        when (kind) {
            0 -> {
                canvas.drawLine(s * 0.5f, s * 0.12f, s * 0.5f, s * 0.62f, p)
                val a = Path()
                a.moveTo(s * 0.3f, s * 0.44f); a.lineTo(s * 0.5f, s * 0.64f); a.lineTo(s * 0.7f, s * 0.44f)
                canvas.drawPath(a, p)
                val t = Path()
                t.moveTo(s * 0.14f, s * 0.66f); t.lineTo(s * 0.14f, s * 0.86f); t.lineTo(s * 0.86f, s * 0.86f); t.lineTo(s * 0.86f, s * 0.66f)
                canvas.drawPath(t, p)
            }
            1 -> {
                canvas.drawCircle(s * 0.44f, s * 0.44f, s * 0.28f, p)
                canvas.drawLine(s * 0.65f, s * 0.65f, s * 0.88f, s * 0.88f, p)
            }
            2 -> {
                val a = Path()
                a.moveTo(s * 0.28f, s * 0.16f); a.lineTo(s * 0.82f, s * 0.5f); a.lineTo(s * 0.28f, s * 0.84f); a.close()
                canvas.drawPath(a, p)
            }
            3 -> {
                canvas.drawCircle(s / 2f, s / 2f, s * 0.15f, p)
                canvas.drawCircle(s / 2f, s / 2f, s * 0.31f, p)
                for (i in 0 until 8) {
                    canvas.save()
                    canvas.rotate(i * 45f, s / 2f, s / 2f)
                    canvas.drawLine(s / 2f, s * 0.1f, s / 2f, s * 0.19f, p)
                    canvas.restore()
                }
            }
            else -> {
                canvas.drawRoundRect(RectF(s * 0.36f, s * 0.08f, s * 0.64f, s * 0.58f), s * 0.14f, s * 0.14f, p)
                canvas.drawArc(RectF(s * 0.22f, s * 0.3f, s * 0.78f, s * 0.78f), 0f, 180f, false, p)
                canvas.drawLine(s * 0.5f, s * 0.78f, s * 0.5f, s * 0.92f, p)
            }
        }
    }
}

class BackdropView(c: Context) : View(c) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cols = intArrayOf(Color.rgb(59, 130, 246), Color.rgb(239, 68, 68), Color.rgb(34, 197, 94),
        Color.rgb(236, 72, 153), Color.rgb(139, 92, 246), Color.rgb(245, 158, 11))
    private fun mul(c: Int, f: Float) = Color.rgb((Color.red(c) * f).toInt(), (Color.green(c) * f).toInt(), (Color.blue(c) * f).toInt())
    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val s = w * 0.30f
        val gap = s * 0.22f
        val limit = h * 0.62f
        canvas.save()
        canvas.rotate(-22f, w / 2, h * 0.2f)
        var i = 0
        var y = -s
        while (y < limit) {
            var x = -s
            while (x < w + s) {
                val fade = 1f - (y / limit).coerceIn(0f, 1f)
                val base = cols[i++ % cols.size]
                val off = s * 0.08f
                // নিচের গাঢ় কিনারা
                p.shader = null
                p.color = mul(base, 0.28f)
                p.alpha = (150 * fade).toInt()
                canvas.drawRoundRect(RectF(x, y + off, x + s, y + s + off), s * 0.22f, s * 0.22f, p)
                // মূল টাইল
                p.shader = LinearGradient(x, y, x, y + s, mul(base, 0.75f), mul(base, 0.5f), Shader.TileMode.CLAMP)
                p.alpha = (150 * fade).toInt()
                canvas.drawRoundRect(RectF(x, y, x + s, y + s), s * 0.22f, s * 0.22f, p)
                p.shader = null
                x += s + gap
            }
            y += s + gap
        }
        canvas.restore()
    }
}

/** একটি সার্চ ফলাফল। src = archive | commons | nasa | pexels */
data class Opt(val label: String, val url: String, val size: Long, val ext: String, val pv: Boolean = false)

data class Item(
    val title: String,
    val by: String,
    val thumb: String,
    val src: String,
    val key: String = "",
    val direct: String = "",
    val opts: List<Opt> = emptyList()
)

class MainActivity : Activity() {
    private val bg = Color.rgb(5, 8, 22)
    private val panel = Color.rgb(15, 23, 42)
    private val cyan = Color.rgb(24, 217, 255)
    private val purple = Color.rgb(139, 92, 246)
    private val white = Color.rgb(245, 248, 255)
    private val grey = Color.rgb(151, 169, 198)
    private lateinit var container: FrameLayout
    private lateinit var navItems: List<TextView>
    private lateinit var navIcons: List<IconView>
    private var tab = 0
    private var inPlayer = false
    private var searchText = ""
    private var autoRun = false
    private var chip = 0
    private var video: VideoView? = null
    private var browser: WebView? = null
    private var anyRunning = false
    private val cache = LruCache<String, Bitmap>(40)
    private val ui = Handler(Looper.getMainLooper())

    // Claude API কীওয়ার্ড অনুবাদে ব্যবহৃত মডেল (প্রয়োজনে বদলান)
    private val claudeModel = "claude-haiku-5-5"

    private val videoExts = listOf(".mp4", ".m4v", ".webm", ".mov", ".mkv")
    private val audioExts = listOf(".mp3", ".m4a", ".ogg", ".wav", ".aac", ".flac")
    private val imageExts = listOf(".jpg", ".jpeg", ".png", ".webp", ".gif")
    private val docExts = listOf(".pdf")

    private val ticker = Runnable { if (tab == 2 && !inPlayer) refreshDownloads() }

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
            setBackgroundColor(Color.rgb(12, 12, 12))
            setPadding(0, dp(10), 0, dp(8))
        }
        val names = listOf("Download", "Play", "Settings")
        val kinds = listOf(1, 2, 3)
        val tabMap = listOf(0, 2, 3)
        val icons = ArrayList<IconView>()
        navItems = names.mapIndexed { i, n ->
            val col = LinearLayout(this)
            col.orientation = LinearLayout.VERTICAL
            col.gravity = Gravity.CENTER_HORIZONTAL
            col.setOnClickListener { show(tabMap[i]) }
            val ic = IconView(this, kinds[i])
            ic.tint = grey
            icons.add(ic)
            col.addView(ic, LinearLayout.LayoutParams(dp(26), dp(26)))
            val t = TextView(this)
            t.text = n
            t.textSize = 12f
            t.gravity = Gravity.CENTER
            t.setTextColor(grey)
            col.addView(t, lp(-2, -2, 3))
            nav.addView(col, LinearLayout.LayoutParams(0, -2, 1f))
            t
        }
        navIcons = icons
        val divider = View(this)
        divider.setBackgroundColor(Color.argb(70, 151, 169, 198))
        root.addView(divider, LinearLayout.LayoutParams(-1, dp(1)))
        root.addView(nav, LinearLayout.LayoutParams(-1, -2))
        setContentView(root)
        show(0)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onDestroy() {
        ui.removeCallbacks(ticker)
        super.onDestroy()
    }

    private fun handleIntent(i: Intent?): Boolean {
        if (i == null || i.action != Intent.ACTION_SEND) return false
        val text = i.getStringExtra(Intent.EXTRA_TEXT) ?: return false
        val m = Regex("https?://\\S+").find(text)
        if (m == null) { toast("শেয়ার করা লেখায় কোনো লিংক পাওয়া যায়নি।"); return false }
        openLink(m.value)
        return true
    }

    private fun openLink(url: String) {
        stopVideo()
        ui.removeCallbacks(ticker)
        inPlayer = true
        tab = 0
        container.removeAllViews()
        container.addView(sharedView(url))
    }

    /** টেক্সটে লিংক থাকলে লিংক খোলে, না থাকলে সার্চ করে */
    private fun smartGo(t: String) {
        val s = t.trim()
        if (s.isEmpty()) { toast("লিংক পেস্ট করুন বা নাম লিখুন।"); return }
        val m = Regex("https?://\\S+").find(s)
        if (m != null) openLink(m.value) else goSearch(s)
    }

    private fun clipboardText(): String {
        return try {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(this)?.toString()?.trim() ?: ""
        } catch (e: Exception) { "" }
    }

    // ---------- Shared link ----------

    private fun sharedView(url: String): View {
        val p = page()
        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        top.addView(tv("‹", 30f, white).apply { setPadding(0, 0, dp(14), 0); setOnClickListener { show(0) } })
        top.addView(tv("Shared link", 17f, white, true))
        p.addView(top)
        val host = try { Uri.parse(url).host ?: "" } catch (e: Exception) { "" }
        val yt = host.contains("youtube.com") || host.contains("youtu.be")
        val ext = mediaExt(url)

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(10), dp(10), dp(16))
            background = gradient(intArrayOf(Color.rgb(34, 35, 91), Color.rgb(9, 88, 125), Color.rgb(17, 22, 49)), 24)
            elevation = dp(14).toFloat()
        }
        val fl = FrameLayout(this)
        fl.background = rounded(Color.rgb(20, 30, 60), 16)
        fl.clipToOutline = true
        val iv = ImageView(this)
        iv.scaleType = ImageView.ScaleType.CENTER_CROP
        if (ext != null && imageExts.contains(ext)) loadImage(url, iv)
        fl.addView(iv, FrameLayout.LayoutParams(-1, dp(190)))
        val play = tv("▶", 22f, Color.WHITE)
        play.gravity = Gravity.CENTER
        play.background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.argb(150, 10, 20, 50)); setStroke(dp(2), Color.WHITE) }
        if (ext == null || videoExts.contains(ext) || audioExts.contains(ext)) fl.addView(play, FrameLayout.LayoutParams(dp(58), dp(58), Gravity.CENTER))
        card.addView(fl, LinearLayout.LayoutParams(-1, dp(190)))
        val fileName = Uri.decode(url.substringBefore('?').substringAfterLast('/')).ifEmpty { host }
        val title = tv(if (yt) "লোড হচ্ছে…" else fileName, 16f, white, true)
        title.maxLines = 3
        title.ellipsize = TextUtils.TruncateAt.END
        card.addView(title, lp(-1, -2, 12))
        val sub = tv(host, 12f, cyan)
        card.addView(sub, lp(-1, -2, 4))
        val info = tv("", 12f, Color.rgb(216, 238, 255))
        card.addView(info, lp(-1, -2, 10))
        p.addView(card, lp(-1, -2, 16))

        if (yt) {
            info.text = "এই অ্যাপ YouTube থেকে ডাউনলোড করে না। ভিডিওটি YouTube-এ দেখুন।"
            Thread {
                try {
                    val j = JSONObject(httpGet("https://www.youtube.com/oembed?format=json&url=" + Uri.encode(url)))
                    ui.post {
                        title.text = j.optString("title")
                        sub.text = "YouTube  •  " + j.optString("author_name")
                        val th = j.optString("thumbnail_url")
                        if (th.isNotEmpty()) loadImage(th, iv)
                    }
                } catch (e: Exception) {
                    ui.post { title.text = "YouTube ভিডিও" }
                }
            }.start()
            val b1 = button("▶   YouTube-এ খুলুন", Color.rgb(200, 30, 50))
            b1.setOnClickListener { openUrl(url) }
            p.addView(b1, lp(-1, dp(52), 16))
            val b2 = button("⌕   অ্যাপে অনুরূপ ভিডিও খুঁজুন", Color.rgb(0, 133, 175))
            b2.setOnClickListener { goSearch(title.text.toString()) }
            p.addView(b2, lp(-1, dp(52), 10))
        } else if (ext != null) {
            info.text = "ফাইলের তথ্য আনা হচ্ছে…"
            Thread {
                var len = -1L
                try {
                    val c = URL(url).openConnection() as HttpURLConnection
                    c.requestMethod = "HEAD"
                    c.connectTimeout = 10000
                    c.readTimeout = 10000
                    len = c.contentLengthLong
                    c.disconnect()
                } catch (e: Exception) { }
                ui.post {
                    info.text = kindIcon(ext) + " " + ext.removePrefix(".").uppercase() +
                        (if (len > 0) "   •   " + String.format(Locale.US, "%.1f MB", len / 1048576.0) else "")
                }
            }.start()
            if (videoExts.contains(ext) || audioExts.contains(ext)) {
                val b1 = button("▶   Preview", Color.rgb(0, 133, 175))
                b1.setOnClickListener { openPlayer(title.text.toString(), null, Uri.parse(url)) }
                p.addView(b1, lp(-1, dp(52), 16))
            }
            val b2 = button("⬇   Download", Color.rgb(0, 150, 120))
            b2.setOnClickListener { enqueue(url, title.text.toString(), if (imageExts.contains(ext)) url else "") }
            p.addView(b2, lp(-1, dp(52), 10))
        } else {
            info.text = "এটি সরাসরি ফাইলের লিংক নয় (ভিডিও/অডিও/ছবি/PDF)। YouTube, Facebook, Instagram, TikTok-এর ভিডিও এই অ্যাপে ডাউনলোড হয় না।"
            val b1 = button("ব্রাউজারে খুলুন", Color.rgb(0, 133, 175))
            b1.setOnClickListener { openUrl(url) }
            p.addView(b1, lp(-1, dp(52), 16))
        }
        return scroll(p)
    }

    private fun kindIcon(ext: String): String = when {
        videoExts.contains(ext) -> "🎞"
        audioExts.contains(ext) -> "🎵"
        imageExts.contains(ext) -> "🖼"
        else -> "📄"
    }

    // ---------- Navigation ----------

    private fun show(i: Int) {
        stopVideo()
        ui.removeCallbacks(ticker)
        inPlayer = false
        tab = i
        val ni = when (i) { 0, 1 -> 0; 2 -> 1; else -> 2 }
        navItems.forEachIndexed { k, t ->
            t.setTextColor(if (k == ni) white else grey)
            navIcons[k].tint = if (k == ni) white else grey
        }
        container.removeAllViews()
        container.addView(when (i) {
            0 -> homeView()
            1 -> searchView()
            2 -> downloadsView()
            else -> moreView()
        })
    }

    private fun stopVideo() { video?.stopPlayback(); video = null; closeBrowser() }

    private fun closeBrowser() {
        browser?.let { try { it.stopLoading(); it.destroy() } catch (e: Exception) { } }
        browser = null
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        when {
            browser?.canGoBack() == true -> browser?.goBack()
            inPlayer -> show(tab)
            tab != 0 -> show(0)
            else -> super.onBackPressed()
        }
    }

    override fun onPause() { super.onPause(); video?.pause(); browser?.onPause() }

    override fun onResume() { super.onResume(); browser?.onResume() }

    // ---------- Home ----------

    private fun homeView(): View {
        val amber = Color.rgb(255, 196, 30)
        val frame = FrameLayout(this)
        frame.setBackgroundColor(Color.rgb(12, 12, 12))
        frame.addView(BackdropView(this), FrameLayout.LayoutParams(-1, -1))
        val p = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(18), dp(16), dp(18), dp(16))
        }
        fun curTab(): Int = when (chip) { 0 -> 0; 4 -> 1; 6 -> 2; else -> 3 }
        fun srcLabel(): String = when (chip) {
            0 -> "Internet Archive (ভিডিও)"
            1 -> "Wikimedia Commons"
            2 -> "NASA"
            3 -> "Pexels"
            4 -> "YouTube: ব্রাউজারে খুলবে, এই অ্যাপে ডাউনলোড নেই"
            6 -> "Internet Archive (গান ও অডিও)"
            else -> "Facebook: ব্রাউজারে খুলবে"
        }
        val srcHint = tv("", 12f, grey)
        val labels = listOf("Search", "YouTube", "Music", "More")
        val texts = ArrayList<TextView>()
        val lines = ArrayList<View>()
        fun styleTabs() {
            texts.forEachIndexed { k, t -> t.setTextColor(if (k == curTab()) white else grey) }
            lines.forEachIndexed { k, v -> v.setBackgroundColor(if (k == curTab()) amber else Color.TRANSPARENT) }
            srcHint.text = "খোঁজার সোর্স: " + srcLabel()
        }
        val tabsRow = LinearLayout(this).apply { gravity = Gravity.CENTER_HORIZONTAL }
        labels.forEachIndexed { i, n ->
            val col = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                setPadding(dp(10), dp(6), dp(10), dp(6))
            }
            val t = tv(n, 15f, grey, true)
            val ln = View(this)
            col.addView(t)
            col.addView(ln, LinearLayout.LayoutParams(dp(30), dp(3)).apply { topMargin = dp(6) })
            col.setOnClickListener {
                when (i) {
                    0 -> chip = 0
                    1 -> chip = 4
                    2 -> chip = 6
                    else -> {
                        val opts = arrayOf("Wikimedia Commons", "NASA", "Pexels", "বিল্ট-ইন ব্রাউজার")
                        AlertDialog.Builder(this@MainActivity)
                            .setItems(opts) { _, w -> if (w == 3) openBrowser("") else { chip = 1 + w; styleTabs() } }
                            .show()
                    }
                }
                styleTabs()
            }
            texts.add(t)
            lines.add(ln)
            tabsRow.addView(col)
        }
        p.addView(tabsRow, LinearLayout.LayoutParams(-2, -2))

        p.addView(View(this), LinearLayout.LayoutParams(-1, 0, 1.3f))
        p.addView(LogoView(this), LinearLayout.LayoutParams(dp(84), dp(84)))
        p.addView(tv("AI Video Master", 34f, amber, true).apply {
            gravity = Gravity.CENTER
            setShadowLayer(1f, dp(3).toFloat(), dp(4).toFloat(), Color.rgb(150, 100, 0))
        }, lp(-2, -2, 8))
        p.addView(View(this), LinearLayout.LayoutParams(-1, 0, 0.7f))

        val pill = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(5), dp(5), dp(5))
            background = rounded(Color.rgb(24, 24, 24), 30, Color.argb(140, 151, 169, 198))
            elevation = dp(10).toFloat()
        }
        val dlIcon = IconView(this, 0)
        dlIcon.tint = grey
        pill.addView(dlIcon, LinearLayout.LayoutParams(dp(26), dp(26)))
        val et = EditText(this).apply {
            hint = "নাম লিখুন বা লিংক পেস্ট করুন…"
            setHintTextColor(Color.rgb(137, 153, 180))
            setTextColor(white)
            textSize = 14f
            background = rounded(Color.rgb(32, 32, 32), 8, Color.argb(70, 151, 169, 198))
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            setPadding(dp(10), dp(10), dp(6), dp(10))
        }
        pill.addView(et, LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = dp(10); rightMargin = dp(6) })
        val mic = IconView(this, 4)
        mic.tint = Color.WHITE
        pill.addView(mic, LinearLayout.LayoutParams(dp(24), dp(24)).apply { leftMargin = dp(4); rightMargin = dp(12) })
        val go = FrameLayout(this)
        val goEdge = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.rgb(165, 112, 0)) }
        val goFace = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.rgb(255, 224, 110), amber)).apply {
            shape = GradientDrawable.OVAL
            setStroke(dp(1), Color.argb(120, 255, 255, 255))
        }
        val goLayers = android.graphics.drawable.LayerDrawable(arrayOf<android.graphics.drawable.Drawable>(goEdge, goFace))
        goLayers.setLayerInset(1, 0, 0, 0, dp(3))
        go.background = goLayers
        go.elevation = dp(8).toFloat()
        val goIcon = IconView(this, 1)
        goIcon.tint = Color.rgb(20, 20, 20)
        go.addView(goIcon, FrameLayout.LayoutParams(dp(24), dp(24), Gravity.CENTER).apply { bottomMargin = dp(2) })
        pill.addView(go, LinearLayout.LayoutParams(dp(52), dp(52)))
        p.addView(pill, lp(-1, -2, 0))

        dlIcon.setOnClickListener {
            val c = clipboardText()
            if (c.isEmpty()) toast("ক্লিপবোর্ড খালি।") else { et.setText(c); smartGo(c) }
        }
        mic.setOnClickListener { startVoice() }
        et.setOnEditorActionListener { _, _, _ -> smartGo(et.text.toString()); true }
        go.setOnClickListener { smartGo(et.text.toString()) }

        // সবুজ 3D Download বাটন
        val dlBtn = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            background = buttonBg(Color.rgb(22, 170, 140))
            setPadding(0, 0, 0, dp(4))
            elevation = dp(8).toFloat()
            isClickable = true
            isFocusable = true
            setOnTouchListener { v, e ->
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> v.animate().translationY(dp(3).toFloat()).setDuration(60).start()
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().translationY(0f).setDuration(90).start()
                }
                false
            }
        }
        val dlBtnIcon = IconView(this, 0)
        dlBtnIcon.tint = Color.WHITE
        dlBtn.addView(dlBtnIcon, LinearLayout.LayoutParams(dp(20), dp(20)).apply { rightMargin = dp(10) })
        dlBtn.addView(tv("Download", 17f, Color.WHITE, true))
        dlBtn.setOnClickListener {
            val t = et.text.toString().trim()
            if (t.isNotEmpty()) smartGo(t) else {
                val c = clipboardText()
                if (c.isEmpty()) toast("লিংক পেস্ট করুন বা নাম লিখুন।") else { et.setText(c); smartGo(c) }
            }
        }
        p.addView(dlBtn, lp(dp(222), dp(48), 22))
        p.addView(tv("লিংক পেস্ট করে Download চাপুন", 13f, amber).apply { gravity = Gravity.CENTER }, lp(-2, -2, 10))
        srcHint.gravity = Gravity.CENTER
        p.addView(srcHint, lp(-1, -2, 8))
        p.addView(View(this), LinearLayout.LayoutParams(-1, 0, 2f))
        styleTabs()
        frame.addView(ScrollView(this).apply { isFillViewport = true; addView(p) }, FrameLayout.LayoutParams(-1, -1))
        return frame
    }

    private fun bars(): LinearLayout = LinearLayout(this).apply {
        gravity = Gravity.CENTER_VERTICAL
        listOf(10, 22, 34, 18, 28, 12, 20).forEach { h ->
            val v = View(this@MainActivity)
            v.setBackgroundColor(cyan)
            addView(v, LinearLayout.LayoutParams(dp(3), dp(h)).apply { leftMargin = dp(2); rightMargin = dp(2) })
        }
    }

    // ---------- Search ----------

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

        val chipScroll = android.widget.HorizontalScrollView(this)
        chipScroll.isHorizontalScrollBarEnabled = false
        val chips = LinearLayout(this)
        val chipViews = ArrayList<TextView>()
        fun style() {
            chipViews.forEachIndexed { k, t ->
                t.setTextColor(if (k == chip) bg else white)
                t.background = rounded(if (k == chip) cyan else panel, 16)
            }
        }
        listOf("Archive", "Commons", "NASA", "Pexels", "YouTube", "Facebook", "Music").forEachIndexed { i, n ->
            val t = tv(n, 12f)
            t.setPadding(dp(14), dp(8), dp(14), dp(8))
            t.setOnClickListener { chip = i; style() }
            chipViews.add(t)
            chips.addView(t, lp(-2, -2).apply { rightMargin = dp(8) })
        }
        style()
        chipScroll.addView(chips)
        p.addView(chipScroll, lp(-1, -2, 12))
        p.addView(tv("Archive, Commons, NASA ও Pexels-এর ফলাফল অ্যাপের ভেতরেই আসে (Pexels-এর জন্য More ট্যাবে ফ্রি API key লাগে)। YouTube/Facebook বেছে খুঁজলে ফলাফল ব্রাউজারে খুলবে।", 11f, grey), lp(-1, -2, 8))
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
        if (chip == 4 || chip == 5) {
            val base = if (chip == 4) "https://www.youtube.com/results?search_query=" else "https://www.facebook.com/watch/search/?q="
            openUrl(base + Uri.encode(q))
            status.text = "ব্রাউজারে ফলাফল খোলা হয়েছে।"
            return
        }
        val pexelsKey = pref("pexels_key")
        if (chip == 3 && pexelsKey.isEmpty()) {
            status.text = "More ট্যাবে Pexels API key দিন (pexels.com/api থেকে ফ্রিতে পাওয়া যায়)।"
            return
        }
        status.text = "খোঁজা হচ্ছে…"
        results.removeAllViews()
        val which = chip
        Thread {
            try {
                val kw = aiKeywords(q)
                if (kw != q) ui.post { status.text = "খোঁজা হচ্ছে…  (AI: $kw)" }
                val list = when (which) {
                    0 -> searchArchive(kw)
                    1 -> searchCommons(kw)
                    2 -> searchNasa(kw)
                    3 -> searchPexels(kw, pexelsKey)
                    6 -> searchArchiveAudio(kw)
                    else -> searchArchive(kw)
                }
                ui.post {
                    if (list.isEmpty()) status.text = "কোনো ফলাফল পাওয়া যায়নি। অন্য শব্দে (ইংরেজিতে) চেষ্টা করুন।"
                    else {
                        status.text = "${list.size}টি ফলাফল" + (if (kw != q) "  (AI: $kw)" else "")
                        list.forEach { results.addView(resultCard(it), lp(-1, -2, 12)) }
                    }
                }
            } catch (e: Exception) {
                ui.post { status.text = "নেটওয়ার্ক ত্রুটি। ইন্টারনেট বা API key পরীক্ষা করুন।" }
            }
        }.start()
    }

    /** বাংলা/অন্য ভাষার প্রশ্নকে Claude দিয়ে ইংরেজি কীওয়ার্ডে রূপান্তর (ব্যাকগ্রাউন্ড থ্রেডে চালাতে হবে) */
    private fun aiKeywords(q: String): String {
        val key = pref("claude_key")
        if (key.isEmpty() || pref("ai_on") != "1") return q
        if (q.all { it.code < 128 }) return q
        return try {
            val msg = JSONObject().put("role", "user").put("content",
                "Convert this video search request into 2-5 concise English search keywords. Reply with only the keywords, nothing else.\n\n$q")
            val body = JSONObject().put("model", claudeModel).put("max_tokens", 60).put("messages", JSONArray().put(msg))
            val r = JSONObject(httpPost("https://api.anthropic.com/v1/messages", body.toString(),
                mapOf("x-api-key" to key, "anthropic-version" to "2023-06-01")))
            val t = r.getJSONArray("content").getJSONObject(0).optString("text").trim().trim('"')
            if (t.isNotEmpty()) t else q
        } catch (e: Exception) { q }
    }

    private fun searchArchive(q: String): List<Item> {
        val api = "https://archive.org/advancedsearch.php?q=" + Uri.encode("($q) AND mediatype:movies") +
            "&fl%5B%5D=identifier&fl%5B%5D=title&fl%5B%5D=creator&sort%5B%5D=downloads+desc&rows=20&output=json"
        val docs = JSONObject(httpGet(api)).getJSONObject("response").getJSONArray("docs")
        val out = ArrayList<Item>()
        for (i in 0 until docs.length()) {
            val d = docs.getJSONObject(i)
            val id = d.optString("identifier")
            out.add(Item(firstText(d.opt("title")).ifEmpty { id }, firstText(d.opt("creator")),
                "https://archive.org/services/img/$id", "archive", id))
        }
        return out
    }

    private fun searchArchiveAudio(q: String): List<Item> {
        val api = "https://archive.org/advancedsearch.php?q=" + Uri.encode("($q) AND mediatype:audio") +
            "&fl%5B%5D=identifier&fl%5B%5D=title&fl%5B%5D=creator&sort%5B%5D=downloads+desc&rows=20&output=json"
        val docs = JSONObject(httpGet(api)).getJSONObject("response").getJSONArray("docs")
        val out = ArrayList<Item>()
        for (i in 0 until docs.length()) {
            val d = docs.getJSONObject(i)
            val id = d.optString("identifier")
            out.add(Item(firstText(d.opt("title")).ifEmpty { id }, firstText(d.opt("creator")),
                "https://archive.org/services/img/$id", "archiveaudio", id))
        }
        return out
    }

    private fun searchCommons(q: String): List<Item> {
        val api = "https://commons.wikimedia.org/w/api.php?action=query&format=json&generator=search&gsrnamespace=6&gsrlimit=20" +
            "&gsrsearch=" + Uri.encode("$q filetype:video") + "&prop=imageinfo&iiprop=url&iiurlwidth=400"
        val pages = JSONObject(httpGet(api)).optJSONObject("query")?.optJSONObject("pages") ?: return emptyList()
        val tmp = ArrayList<Pair<Int, Item>>()
        val keys = pages.keys()
        while (keys.hasNext()) {
            val pg = pages.getJSONObject(keys.next())
            val ii = pg.optJSONArray("imageinfo")?.optJSONObject(0) ?: continue
            val u = ii.optString("url")
            val low = u.lowercase(Locale.ROOT)
            if (!(low.endsWith(".webm") || low.endsWith(".mp4"))) continue
            val title = pg.optString("title").removePrefix("File:").substringBeforeLast('.').replace('_', ' ')
            tmp.add(Pair(pg.optInt("index"), Item(title, "Wikimedia Commons", ii.optString("thumburl"), "commons", "", u)))
        }
        return tmp.sortedBy { it.first }.map { it.second }
    }

    private fun searchNasa(q: String): List<Item> {
        val items = JSONObject(httpGet("https://images-api.nasa.gov/search?media_type=video&q=" + Uri.encode(q)))
            .getJSONObject("collection").getJSONArray("items")
        val out = ArrayList<Item>()
        for (i in 0 until minOf(items.length(), 20)) {
            val o = items.getJSONObject(i)
            val d = o.getJSONArray("data").getJSONObject(0)
            val id = d.optString("nasa_id")
            if (id.isEmpty()) continue
            val thumb = o.optJSONArray("links")?.optJSONObject(0)?.optString("href")?.replace("http://", "https://") ?: ""
            out.add(Item(d.optString("title").ifEmpty { id }, "NASA", thumb, "nasa", id))
        }
        return out
    }

    private fun searchPexels(q: String, key: String): List<Item> {
        val arr = JSONObject(httpGet("https://api.pexels.com/videos/search?per_page=20&query=" + Uri.encode(q),
            mapOf("Authorization" to key))).optJSONArray("videos") ?: return emptyList()
        val out = ArrayList<Item>()
        for (i in 0 until arr.length()) {
            val v = arr.getJSONObject(i)
            val files = v.optJSONArray("video_files") ?: continue
            val mp4 = (0 until files.length()).map { files.getJSONObject(it) }.filter { it.optString("file_type") == "video/mp4" }
            val pick = mp4.filter { it.optInt("width") >= 640 }.minByOrNull { it.optInt("width") }
                ?: mp4.maxByOrNull { it.optInt("width") } ?: continue
            val popts = mp4.sortedByDescending { it.optInt("height") }.map { f ->
                val h = f.optInt("height")
                val tag = if (h >= 1080) " (Full HD)" else if (h >= 720) " (HD)" else " (SD)"
                Opt("MP4 • " + f.optInt("width") + "×" + h + tag, f.optString("link"), 0L, ".mp4", f.optString("link") == pick.optString("link"))
            }
            val slug = v.optString("url").trimEnd('/').substringAfterLast('/').replace('-', ' ')
            out.add(Item(slug.ifEmpty { "Pexels video" }, v.optJSONObject("user")?.optString("name") ?: "Pexels",
                v.optString("image"), "pexels", "", pick.optString("link"), popts))
        }
        return out
    }

    /** আইটেমের আসল ফাইল URL ও সাইজ বের করে (ব্যাকগ্রাউন্ড থ্রেডে চালাতে হবে) */
    private fun resolve(item: Item): Pair<String, Long>? = when (item.src) {
        "archive" -> resolveArchive(item.key)
        "archiveaudio" -> resolveArchiveAudio(item.key)
        "nasa" -> resolveNasa(item.key)
        else -> if (item.direct.isEmpty()) null else Pair(item.direct, 0L)
    }

    private fun resolveArchive(id: String): Pair<String, Long>? {
        val files = JSONObject(httpGet("https://archive.org/metadata/$id")).getJSONArray("files")
        val mp4s = (0 until files.length()).map { files.getJSONObject(it) }
            .filter { it.optString("name").lowercase(Locale.ROOT).endsWith(".mp4") }
        val pick = mp4s.firstOrNull { it.optString("format").contains("h.264", true) }
            ?: mp4s.minByOrNull { it.optString("size", "0").toLongOrNull() ?: Long.MAX_VALUE } ?: return null
        val size = pick.optString("size", "0").toLongOrNull() ?: 0L
        return Pair("https://archive.org/download/$id/" + Uri.encode(pick.optString("name"), "/"), size)
    }

    private fun archiveOpts(id: String, exts: List<String>): List<Opt> {
        val files = JSONObject(httpGet("https://archive.org/metadata/$id")).getJSONArray("files")
        val list = ArrayList<Opt>()
        for (i in 0 until files.length()) {
            val f = files.getJSONObject(i)
            val name = f.optString("name")
            val low = name.lowercase(Locale.ROOT)
            val ext = exts.firstOrNull { low.endsWith(it) } ?: continue
            val size = f.optString("size", "0").toLongOrNull() ?: 0L
            val fmt = f.optString("format")
            val label = ext.removePrefix(".").uppercase() + (if (fmt.isNotEmpty()) " • $fmt" else "") +
                (if (size > 0) String.format(Locale.US, " • %.1f MB", size / 1048576.0) else "")
            list.add(Opt(label, "https://archive.org/download/$id/" + Uri.encode(name, "/"), size, ext))
        }
        val sorted = list.sortedByDescending { it.size }.take(8)
        val pv = sorted.filter { it.size > 0 }.minByOrNull { it.size } ?: sorted.firstOrNull()
        return sorted.map { if (it === pv) it.copy(pv = true) else it }
    }

    private fun nasaOpts(id: String): List<Opt> {
        val items = JSONObject(httpGet("https://images-api.nasa.gov/asset/" + Uri.encode(id)))
            .getJSONObject("collection").getJSONArray("items")
        val urls = (0 until items.length()).map {
            items.getJSONObject(it).optString("href").replace("http://", "https://").replace(" ", "%20")
        }.filter { it.lowercase(Locale.ROOT).endsWith(".mp4") }
        val order = listOf("orig", "large", "medium", "small", "mobile")
        val names = mapOf("orig" to "Original (সবচেয়ে বড়)", "large" to "Large", "medium" to "Medium", "small" to "Small", "mobile" to "Mobile (ছোট)")
        val tagged = urls.map { u ->
            val tag = Regex("~([a-z]+)\\.mp4", RegexOption.IGNORE_CASE).find(u)?.groupValues?.get(1)?.lowercase(Locale.ROOT) ?: "video"
            Pair(tag, u)
        }.sortedBy { val k = order.indexOf(it.first); if (k < 0) 99 else k }
        val pvTag = listOf("medium", "small", "mobile").firstOrNull { t -> tagged.any { it.first == t } } ?: tagged.firstOrNull()?.first
        return tagged.map { Opt("MP4 • " + (names[it.first] ?: it.first), it.second, 0L, ".mp4", it.first == pvTag) }
    }

    private fun resolveAll(item: Item): List<Opt> = when (item.src) {
        "archive" -> archiveOpts(item.key, listOf(".mp4", ".webm", ".mkv", ".ogv"))
        "archiveaudio" -> archiveOpts(item.key, listOf(".mp3", ".ogg", ".m4a", ".flac"))
        "nasa" -> nasaOpts(item.key)
        else -> if (item.opts.isNotEmpty()) item.opts
            else if (item.direct.isEmpty()) emptyList()
            else listOf(Opt("ভিডিও", item.direct, 0L, mediaExt(item.direct) ?: ".mp4", true))
    }

    private fun pickQuality(title: String, thumb: String, opts: List<Opt>) {
        if (opts.size == 1) { enqueue(opts[0].url, title, thumb, opts[0].ext); return }
        val dialog = AlertDialog.Builder(this).create()
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(18), dp(16), dp(14))
            background = rounded(Color.rgb(18, 18, 18), 22, Color.argb(90, 151, 169, 198))
        }
        box.addView(tv("ডাউনলোড কোয়ালিটি বেছে নিন", 18f, white, true))
        box.addView(tv(title, 12f, grey).apply { maxLines = 1; ellipsize = TextUtils.TruncateAt.END }, lp(-1, -2, 3))
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val amber = Color.rgb(255, 196, 30)
        opts.forEachIndexed { i, o ->
            val isAudio = audioExts.contains(o.ext)
            val row = LinearLayout(this).apply {
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(11), dp(12), dp(11))
                background = rounded(if (i == 0) Color.rgb(24, 44, 40) else Color.rgb(28, 28, 28), 14,
                    if (i == 0) Color.argb(160, 22, 170, 140) else Color.argb(60, 151, 169, 198))
                isClickable = true
                isFocusable = true
            }
            row.addView(IconView(this, if (isAudio) 4 else 2).apply { tint = if (i == 0) Color.rgb(22, 200, 165) else grey },
                LinearLayout.LayoutParams(dp(24), dp(24)).apply { rightMargin = dp(12) })
            val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            val hasMb = o.label.contains("MB")
            col.addView(tv(o.label, 14f, white, true).apply { maxLines = 2 })
            val sub = tv(o.ext.removePrefix(".").uppercase() + (if (isAudio) "  •  অডিও" else "  •  ভিডিও") +
                (if (o.size > 0 && !hasMb) String.format(Locale.US, "  •  %.1f MB", o.size / 1048576.0) else ""), 11f, grey)
            col.addView(sub, lp(-2, -2, 2))
            row.addView(col, LinearLayout.LayoutParams(0, -2, 1f))
            val tag = when {
                i == 0 -> "সেরা"
                o.pv -> "হালকা"
                else -> ""
            }
            if (tag.isNotEmpty()) {
                row.addView(tv(tag, 11f, Color.rgb(20, 20, 20), true).apply {
                    setPadding(dp(8), dp(3), dp(8), dp(3))
                    background = rounded(if (i == 0) Color.rgb(22, 200, 165) else amber, 10, Color.TRANSPARENT)
                })
            }
            row.setOnClickListener { dialog.dismiss(); enqueue(o.url, title, thumb, o.ext) }
            list.addView(row, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
            // সাইজ জানা না থাকলে ব্যাকগ্রাউন্ডে বের করা
            if (o.size <= 0 && !hasMb) Thread {
                var len = -1L
                try {
                    val c = URL(o.url).openConnection() as HttpURLConnection
                    c.requestMethod = "HEAD"
                    c.connectTimeout = 8000
                    c.readTimeout = 8000
                    len = c.contentLengthLong
                    c.disconnect()
                } catch (e: Exception) { }
                if (len > 0) ui.post { sub.text = sub.text.toString() + String.format(Locale.US, "  •  %.1f MB", len / 1048576.0) }
            }.start()
        }
        box.addView(ScrollView(this).apply { addView(list) }, LinearLayout.LayoutParams(-1, -2))
        val cancel = tv("বাতিল", 14f, grey, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(12), 0, dp(4)) }
        cancel.setOnClickListener { dialog.dismiss() }
        box.addView(cancel, lp(-1, -2, 6))
        dialog.setView(box)
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun resolveArchiveAudio(id: String): Pair<String, Long>? {
        val files = JSONObject(httpGet("https://archive.org/metadata/$id")).getJSONArray("files")
        val mp3s = (0 until files.length()).map { files.getJSONObject(it) }
            .filter { it.optString("name").lowercase(Locale.ROOT).endsWith(".mp3") }
        val pick = mp3s.firstOrNull { it.optString("format").contains("VBR", true) }
            ?: mp3s.minByOrNull { it.optString("size", "0").toLongOrNull() ?: Long.MAX_VALUE } ?: return null
        val size = pick.optString("size", "0").toLongOrNull() ?: 0L
        return Pair("https://archive.org/download/$id/" + Uri.encode(pick.optString("name"), "/"), size)
    }

    private fun resolveNasa(id: String): Pair<String, Long>? {
        val items = JSONObject(httpGet("https://images-api.nasa.gov/asset/" + Uri.encode(id)))
            .getJSONObject("collection").getJSONArray("items")
        val urls = (0 until items.length()).map {
            items.getJSONObject(it).optString("href").replace("http://", "https://").replace(" ", "%20")
        }.filter { it.lowercase(Locale.ROOT).endsWith(".mp4") }
        val pick = urls.firstOrNull { it.contains("~medium") }
            ?: urls.firstOrNull { it.contains("~mobile") }
            ?: urls.firstOrNull { !it.contains("~orig") }
            ?: urls.firstOrNull() ?: return null
        return Pair(pick, 0L)
    }

    private fun firstText(o: Any?): String = when (o) {
        null -> ""
        is JSONArray -> o.optString(0)
        else -> o.toString()
    }

    private fun sourceName(src: String) = when (src) {
        "archive" -> "Internet Archive"
        "archiveaudio" -> "Internet Archive (Music)"
        "commons" -> "Wikimedia Commons"
        "nasa" -> "NASA Image and Video Library"
        "pexels" -> "Pexels"
        else -> src
    }

    private fun resultCard(item: Item): View {
        val c = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(panel, 18)
            setPadding(dp(8), dp(8), dp(8), dp(12))
            elevation = dp(8).toFloat()
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
        if (item.thumb.isNotEmpty()) loadImage(item.thumb, iv)
        c.addView(fl, LinearLayout.LayoutParams(-1, dp(180)))
        c.addView(tv(item.title, 14f, white, true).apply { maxLines = 2; ellipsize = TextUtils.TruncateAt.END }, lp(-1, -2, 8))
        val sub = if (item.by.isNotEmpty() && item.by != sourceName(item.src)) sourceName(item.src) + "  •  " + item.by else sourceName(item.src)
        c.addView(tv(sub, 12f, grey).apply { maxLines = 1; ellipsize = TextUtils.TruncateAt.END }, lp(-1, -2, 3))
        c.setOnClickListener { openPlayer(item.title, item, null) }
        return c
    }

    // ---------- Player ----------

    private fun openPlayer(title: String, item: Item?, local: Uri?) {
        stopVideo()
        ui.removeCallbacks(ticker)
        inPlayer = true
        container.removeAllViews()
        val p = page()
        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        top.addView(tv("‹", 30f, white).apply { setPadding(0, 0, dp(14), 0); setOnClickListener { show(tab) } })
        val remote = local != null && local.scheme == "https"
        top.addView(tv(if (local != null && !remote) "Downloaded" else "Preview", 17f, white, true))
        p.addView(top)
        val vv = VideoView(this)
        video = vv
        val box = FrameLayout(this)
        box.setBackgroundColor(Color.BLACK)
        box.addView(vv, FrameLayout.LayoutParams(-1, dp(220), Gravity.CENTER))
        val msg = tv("লোড হচ্ছে…", 13f, cyan)
        msg.gravity = Gravity.CENTER
        box.addView(msg, FrameLayout.LayoutParams(-1, -2, Gravity.CENTER))
        p.addView(box, lp(-1, dp(220), 12))
        p.addView(tv(title, 16f, white, true), lp(-1, -2, 12))
        if (item != null) p.addView(tv("Source: " + sourceName(item.src), 12f, grey), lp(-1, -2, 4))
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
            if (remote) dlBtn.setOnClickListener { enqueue(local.toString(), title, "") } else dlBtn.visibility = View.GONE
            play(local)
        } else if (item != null) {
            dlBtn.setOnClickListener { toast("ফাইল খোঁজা হচ্ছে, একটু অপেক্ষা করুন।") }
            Thread {
                try {
                    val opts = resolveAll(item)
                    if (opts.isEmpty()) {
                        ui.post { msg.text = "এই আইটেমে প্লে করার মতো MP4/WEBM ফাইল নেই।" }
                    } else {
                        ui.post {
                            if (inPlayer && video === vv) {
                                play(Uri.parse((opts.firstOrNull { it.pv } ?: opts[0]).url))
                                dlBtn.text = "⬇   Download" + (if (opts.size > 1) "  (" + opts.size + "টি কোয়ালিটি/ফরম্যাট)" else "")
                                dlBtn.setOnClickListener { pickQuality(title, item.thumb, opts) }
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

    // ---------- Built-in browser ----------

    private fun blockedHost(h: String): Boolean {
        val l = h.lowercase(Locale.ROOT)
        return listOf("youtube.com", "youtu.be", "googlevideo.com", "ytimg.com", "facebook.com", "fbcdn.net",
            "instagram.com", "cdninstagram.com", "tiktok.com", "tiktokcdn.com").any { l == it || l.endsWith(".$it") }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun openBrowser(start: String) {
        stopVideo()
        ui.removeCallbacks(ticker)
        inPlayer = true
        container.removeAllViews()
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(8), dp(12), dp(8)) }
        top.addView(tv("‹", 30f, white).apply { setPadding(0, 0, dp(12), 0); setOnClickListener { show(tab) } })
        val addr = field("সাইটের লিংক বা সার্চ…")
        addr.setSingleLine(true)
        addr.imeOptions = EditorInfo.IME_ACTION_GO
        addr.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
        top.addView(addr, LinearLayout.LayoutParams(0, dp(44), 1f))
        val reload = tv("↻", 22f, cyan).apply { setPadding(dp(12), 0, 0, 0); gravity = Gravity.CENTER }
        top.addView(reload)
        col.addView(top, LinearLayout.LayoutParams(-1, -2))

        val pb = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        pb.max = 100
        pb.visibility = View.GONE
        col.addView(pb, LinearLayout.LayoutParams(-1, dp(3)))

        // নিচের Download পপ-আপ বার
        val offerBar = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(10), dp(10), dp(10))
            background = rounded(Color.rgb(15, 23, 42), 0, Color.argb(120, 24, 217, 255))
            visibility = View.GONE
        }
        val oName = tv("", 13f, white, true).apply { maxLines = 1; ellipsize = TextUtils.TruncateAt.END }
        val oInfo = tv("", 11f, cyan)
        val oCol = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(oName); addView(oInfo) }
        offerBar.addView(oCol, LinearLayout.LayoutParams(0, -2, 1f))
        val oBtn = button("⬇ Download", Color.rgb(0, 150, 120))
        oBtn.setPadding(dp(14), dp(10), dp(14), dp(10))
        offerBar.addView(oBtn, LinearLayout.LayoutParams(-2, -2))
        val oClose = tv("✕", 16f, grey).apply { setPadding(dp(12), dp(6), dp(6), dp(6)) }
        offerBar.addView(oClose)
        oClose.setOnClickListener { offerBar.visibility = View.GONE }

        var offered = ""
        fun offer(u: String, name: String, ext: String, len0: Long) {
            if (u == offered) return
            offered = u
            oName.text = name
            oInfo.text = kindIcon(ext) + " " + ext.removePrefix(".").uppercase() +
                (if (len0 > 0) "   •   " + String.format(Locale.US, "%.1f MB", len0 / 1048576.0) else "")
            offerBar.visibility = View.VISIBLE
            oBtn.setOnClickListener {
                enqueue(u, name.substringBeforeLast('.', name), if (imageExts.contains(ext)) u else "", ext, true)
                offerBar.visibility = View.GONE
            }
            if (len0 <= 0) Thread {
                var len = -1L
                try {
                    val c = URL(u).openConnection() as HttpURLConnection
                    c.requestMethod = "HEAD"
                    c.connectTimeout = 10000
                    c.readTimeout = 10000
                    len = c.contentLengthLong
                    c.disconnect()
                } catch (e: Exception) { }
                if (len > 0) ui.post {
                    if (offered == u) oInfo.text = kindIcon(ext) + " " + ext.removePrefix(".").uppercase() +
                        "   •   " + String.format(Locale.US, "%.1f MB", len / 1048576.0)
                }
            }.start()
        }

        val wv = WebView(this)
        browser = wv
        wv.settings.javaScriptEnabled = true
        wv.settings.domStorageEnabled = true
        wv.settings.mediaPlaybackRequiresUserGesture = true
        wv.setBackgroundColor(Color.BLACK)
        var pageHost = ""
        wv.webViewClient = object : WebViewClient() {
            @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
            override fun shouldOverrideUrlLoading(v: WebView?, url: String?): Boolean {
                val u = url ?: return true
                return !(u.startsWith("http://") || u.startsWith("https://"))
            }

            override fun onPageStarted(v: WebView?, url: String?, favicon: Bitmap?) {
                pageHost = Uri.parse(url ?: "").host ?: ""
                if (url != null) addr.setText(url)
                offerBar.visibility = View.GONE
                offered = ""
            }

            @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
            override fun shouldInterceptRequest(v: WebView?, url: String?): WebResourceResponse? {
                val u = url ?: return null
                val e = mediaExt(u)
                val host = Uri.parse(u).host ?: ""
                if (e != null && !imageExts.contains(e) && !blockedHost(host) && !blockedHost(pageHost)) {
                    val name = Uri.decode(u.substringBefore('?').substringAfterLast('/')).ifEmpty { "file$e" }
                    ui.post { offer(u, name, e, -1L) }
                }
                return null
            }
        }
        wv.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(v: WebView?, newProgress: Int) {
                pb.progress = newProgress
                pb.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
            }
        }
        wv.setDownloadListener { u, _, cd, mime, len ->
            val host = Uri.parse(u).host ?: ""
            if (blockedHost(host) || blockedHost(pageHost)) {
                toast("এই সাইট থেকে ডাউনলোড এই অ্যাপে সাপোর্ট করে না।")
            } else {
                val name = URLUtil.guessFileName(u, cd, mime)
                val ext = "." + name.substringAfterLast('.', "").lowercase(Locale.ROOT)
                if (!(videoExts + audioExts + imageExts + docExts).contains(ext)) {
                    toast("শুধু ভিডিও, অডিও, ছবি ও PDF ডাউনলোড সাপোর্ট করে।")
                } else {
                    offered = ""
                    offer(u, name, ext, len)
                }
            }
        }

        fun go(textIn: String) {
            val t = textIn.trim()
            if (t.isEmpty()) return
            val u = when {
                t.startsWith("http://") -> "https://" + t.removePrefix("http://")
                t.startsWith("https://") -> t
                t.contains(" ") || !t.contains(".") -> "https://duckduckgo.com/?q=" + Uri.encode(t)
                else -> "https://$t"
            }
            hideKeyboard()
            wv.loadUrl(u)
        }
        addr.setOnEditorActionListener { _, _, _ -> go(addr.text.toString()); true }
        reload.setOnClickListener { wv.reload() }

        col.addView(wv, LinearLayout.LayoutParams(-1, 0, 1f))
        col.addView(offerBar, LinearLayout.LayoutParams(-1, -2))
        container.addView(col)
        wv.loadUrl(if (start.isBlank()) "https://archive.org/details/movies" else start)
    }

    // ---------- Downloads ----------

    private fun refreshDownloads() {
        val old = container.getChildAt(0) as? ScrollView
        val y = old?.scrollY ?: 0
        val v = downloadsView()
        container.removeAllViews()
        container.addView(v)
        v.post { v.scrollY = y }
    }

    private fun downloadsView(): ScrollView {
        val p = page()
        val head = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        head.addView(tv("Downloads", 20f, white, true), LinearLayout.LayoutParams(0, -2, 1f))
        head.addView(tv("↻ রিফ্রেশ", 13f, cyan).apply { setOnClickListener { refreshDownloads() } })
        p.addView(head)
        val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val list = entries()
        anyRunning = false
        if (list.length() == 0) p.addView(tv("এখনো কোনো ডাউনলোড নেই।", 14f, grey), lp(-2, -2, 16))
        for (i in 0 until list.length()) {
            val e = list.getJSONObject(i)
            val id = e.getLong("id")
            var status = -1
            var total = 0L
            var done = 0L
            dm.query(DownloadManager.Query().setFilterById(id))?.use { c ->
                if (c.moveToFirst()) {
                    status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    total = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    done = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                }
            }
            if (status == DownloadManager.STATUS_PENDING || status == DownloadManager.STATUS_RUNNING || status == DownloadManager.STATUS_PAUSED) anyRunning = true
            p.addView(downloadRow(dm, e.optString("title"), e.optString("thumb"), e.optString("url"), id, status, total, done), lp(-1, -2, 10))
        }
        if (anyRunning) ui.postDelayed(ticker, 2000)
        return scroll(p)
    }

    private fun downloadRow(dm: DownloadManager, title: String, thumb: String, url: String, id: Long, status: Int, total: Long, done: Long): View {
        val r = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            background = rounded(panel, 16)
            setPadding(dp(10), dp(10), dp(12), dp(10))
            elevation = dp(6).toFloat()
        }
        val iv = ImageView(this)
        iv.scaleType = ImageView.ScaleType.CENTER_CROP
        iv.background = rounded(Color.rgb(20, 30, 60), 10)
        iv.clipToOutline = true
        if (thumb.isNotEmpty()) loadImage(if (thumb.startsWith("http")) thumb else "https://archive.org/services/img/$thumb", iv)
        r.addView(iv, LinearLayout.LayoutParams(dp(68), dp(68)))
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), 0, dp(8), 0) }
        col.addView(tv(title, 13f, white, true).apply { maxLines = 2; ellipsize = TextUtils.TruncateAt.END })
        val size = if (total > 0) String.format(Locale.US, " • %.1f MB", total / 1048576.0) else ""
        val running = status == DownloadManager.STATUS_PENDING || status == DownloadManager.STATUS_RUNNING || status == DownloadManager.STATUS_PAUSED
        val pct = if (total > 0) ((done * 100) / total).toInt().coerceIn(0, 100) else 0
        val (label, color) = when {
            status == DownloadManager.STATUS_SUCCESSFUL -> Pair("✔ Downloaded$size", Color.rgb(52, 211, 153))
            status == DownloadManager.STATUS_FAILED -> Pair("✖ ব্যর্থ", Color.rgb(248, 113, 113))
            status == -1 -> Pair("ফাইলটি আর নেই", grey)
            else -> Pair("⏳ ডাউনলোড হচ্ছে" + (if (total > 0) " • $pct%$size" else ""), cyan)
        }
        col.addView(tv(label, 12f, color), lp(-2, -2, 4))
        if (running && total > 0) {
            val bar = LinearLayout(this).apply { weightSum = 100f; background = rounded(Color.rgb(30, 41, 70), 3, Color.TRANSPARENT) }
            val fill = View(this)
            fill.setBackgroundColor(cyan)
            bar.addView(fill, LinearLayout.LayoutParams(0, dp(5), pct.toFloat()))
            bar.addView(View(this), LinearLayout.LayoutParams(0, dp(5), (100 - pct).toFloat()))
            col.addView(bar, lp(-1, dp(5), 6))
        }
        r.addView(col, LinearLayout.LayoutParams(0, -2, 1f))

        if (status == DownloadManager.STATUS_SUCCESSFUL) {
            val b = tv("▶", 18f, Color.WHITE)
            b.gravity = Gravity.CENTER
            b.background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.rgb(0, 133, 175)) }
            b.setOnClickListener { openDownloaded(dm, id, title) }
            r.addView(b, LinearLayout.LayoutParams(dp(40), dp(40)))
        } else if ((status == DownloadManager.STATUS_FAILED || status == -1) && url.isNotEmpty()) {
            val b = tv("↻", 20f, Color.WHITE)
            b.gravity = Gravity.CENTER
            b.background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.rgb(160, 110, 20)) }
            b.setOnClickListener {
                dm.remove(id)
                removeEntry(id)
                enqueue(url, title, thumb)
                refreshDownloads()
            }
            r.addView(b, LinearLayout.LayoutParams(dp(40), dp(40)))
        }
        val del = tv("🗑", 16f, Color.WHITE)
        del.gravity = Gravity.CENTER
        del.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage("এই ডাউনলোড মুছে ফেলবেন? ফাইলও মুছে যাবে।")
                .setPositiveButton("মুছুন") { _, _ -> dm.remove(id); removeEntry(id); refreshDownloads() }
                .setNegativeButton("না", null)
                .show()
        }
        r.addView(del, LinearLayout.LayoutParams(dp(40), dp(40)).apply { leftMargin = dp(4) })
        return r
    }

    private fun openDownloaded(dm: DownloadManager, id: Long, title: String) {
        var uri: Uri? = dm.getUriForDownloadedFile(id)
        if (uri == null) {
            // কিছু ডিভাইসে null আসে, তাই LOCAL_URI দিয়ে fallback
            dm.query(DownloadManager.Query().setFilterById(id))?.use { c ->
                if (c.moveToFirst()) {
                    val s = c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                    if (!s.isNullOrEmpty()) uri = Uri.parse(s)
                }
            }
        }
        val u = uri
        if (u == null) { toast("ফাইল খোলা যায়নি।"); return }
        val mime = dm.getMimeTypeForDownloadedFile(id) ?: ""
        if (mime.startsWith("video/") || mime.startsWith("audio/")) {
            openPlayer(title, null, u)
        } else {
            if (u.scheme == "file") { toast("ফাইলটি Downloads ফোল্ডারে আছে, ফাইল ম্যানেজার থেকে খুলুন।"); return }
            try {
                startActivity(Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(u, if (mime.isEmpty()) "*/*" else mime)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                })
            } catch (e: ActivityNotFoundException) {
                toast("এই ফাইল খোলার মতো অ্যাপ পাওয়া যায়নি।")
            } catch (e: Exception) {
                toast("ফাইল খোলা যায়নি।")
            }
        }
    }

    // ---------- More ----------

    private fun moreView(): View {
        val p = page()
        p.addView(tv("More", 20f, white, true))

        p.addView(tv("সরাসরি ফাইল ডাউনলোড", 14f, cyan, true), lp(-2, -2, 18))
        p.addView(tv("অনুমোদিত https লিংক দিন: ভিডিও (MP4/WEBM/MOV/MKV), অডিও (MP3/M4A/OGG/WAV), ছবি (JPG/PNG/WEBP/GIF), PDF।", 12f, grey), lp(-1, -2, 4))
        val urlRow = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        val et = field("https://example.com/file.mp4")
        et.setSingleLine(true)
        urlRow.addView(et, LinearLayout.LayoutParams(0, dp(48), 1f))
        val paste = button("📋", purple)
        paste.setOnClickListener {
            val c = clipboardText()
            if (c.isEmpty()) toast("ক্লিপবোর্ড খালি।") else et.setText(Regex("https?://\\S+").find(c)?.value ?: c)
        }
        urlRow.addView(paste, LinearLayout.LayoutParams(dp(50), dp(48)).apply { leftMargin = dp(8) })
        p.addView(urlRow, lp(-1, -2, 10))
        val b = button("⬇   Download", Color.rgb(0, 128, 105))
        b.setOnClickListener {
            val u = et.text.toString().trim()
            if (!u.startsWith("https://") || mediaExt(u) == null) toast("সরাসরি https ফাইল URL দিন (.mp4/.mp3/.jpg/.pdf ইত্যাদি)।")
            else enqueue(u, Uri.decode(u.substringBefore('?').substringAfterLast('/')).substringBeforeLast('.').ifEmpty { "file" }, "")
        }
        p.addView(b, lp(-1, dp(50), 9))

        p.addView(tv("বিল্ট-ইন ব্রাউজার", 14f, cyan, true), lp(-2, -2, 26))
        p.addView(tv("যেকোনো সাইট খুলুন। সরাসরি ভিডিও, অডিও বা PDF ফাইল পেলে নিচে Download পপ-আপ আসবে। YouTube, Facebook, Instagram, TikTok-এ এটা কাজ করে না।", 12f, grey), lp(-1, -2, 4))
        val bb = button("🌐   ব্রাউজার খুলুন", Color.rgb(0, 133, 175))
        bb.setOnClickListener { openBrowser("") }
        p.addView(bb, lp(-1, dp(50), 10))

        p.addView(tv("API সেটিংস", 14f, cyan, true), lp(-2, -2, 26))
        p.addView(tv("Pexels API key (ফ্রি: pexels.com/api)", 12f, grey), lp(-1, -2, 6))
        val pk = field("Pexels API key")
        pk.setSingleLine(true)
        pk.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        pk.setText(pref("pexels_key"))
        p.addView(pk, lp(-1, dp(48), 6))
        p.addView(tv("Claude API key (বাংলা সার্চকে ইংরেজি কীওয়ার্ডে অনুবাদের জন্য, ঐচ্ছিক)", 12f, grey), lp(-1, -2, 12))
        val ck = field("Claude API key")
        ck.setSingleLine(true)
        ck.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        ck.setText(pref("claude_key"))
        p.addView(ck, lp(-1, dp(48), 6))
        val aiToggle = tv("", 13f, white, true)
        fun aiLabel() { aiToggle.text = if (pref("ai_on") == "1") "🤖 AI কীওয়ার্ড অনুবাদ: চালু" else "🤖 AI কীওয়ার্ড অনুবাদ: বন্ধ" }
        aiLabel()
        aiToggle.setPadding(dp(14), dp(11), dp(14), dp(11))
        aiToggle.background = rounded(panel, 14)
        aiToggle.setOnClickListener { setPref("ai_on", if (pref("ai_on") == "1") "0" else "1"); aiLabel() }
        p.addView(aiToggle, lp(-1, -2, 10))
        val save = button("সংরক্ষণ করুন", Color.rgb(100, 70, 200))
        save.setOnClickListener {
            setPref("pexels_key", pk.text.toString().trim())
            setPref("claude_key", ck.text.toString().trim())
            hideKeyboard()
            toast("সংরক্ষিত হয়েছে।")
        }
        p.addView(save, lp(-1, dp(50), 10))
        p.addView(tv("কীগুলো শুধু এই ফোনেই থাকে। অন্য কারো সাথে অ্যাপ শেয়ার করলে নিজের কী দেবেন না।", 11f, grey), lp(-1, -2, 6))

        p.addView(tv("সীমাবদ্ধতা", 14f, cyan, true), lp(-2, -2, 24))
        p.addView(tv("• YouTube, Facebook, Instagram, TikTok থেকে ডাউনলোড এই অ্যাপে নেই; ওই প্ল্যাটফর্মের নিয়ম মানতে হয়। সার্চ ব্রাউজারে খোলে।\n• অ্যাপের ভেতরের ফলাফল Internet Archive, Wikimedia Commons, NASA ও Pexels থেকে আসে। প্রতিটি ভিডিওর লাইসেন্স আলাদা, ব্যবহারের আগে দেখে নিন।\n• শুধু নিজের বা ডাউনলোডের অনুমতি আছে এমন ফাইল নামান।", 12f, grey), lp(-1, -2, 6))
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

    // ---------- Helpers ----------

    private fun mediaExt(url: String): String? {
        val path = url.substringBefore("?").substringBefore("#").lowercase(Locale.ROOT)
        return (videoExts + audioExts + imageExts + docExts).firstOrNull { path.endsWith(it) }
    }

    private fun enqueue(url: String, title: String, thumb: String, forceExt: String? = null, web: Boolean = false) {
        try {
            val u = if (url.startsWith("http://")) "https://" + url.removePrefix("http://") else url
            // বাংলাসহ ইউনিকোড অক্ষর থাকবে, শুধু ফাইল নামে নিষিদ্ধ চিহ্ন বাদ যাবে
            val safe = title.replace(Regex("[\\\\/:*?\"<>|\\p{Cntrl}]"), "").trim().take(40).ifEmpty { "file" }
            val ext = forceExt ?: mediaExt(u) ?: ".mp4"
            val req = DownloadManager.Request(Uri.parse(u))
                .setTitle(title)
                .setDescription("AI Video Master")
                .addRequestHeader("User-Agent", "AIVideoMaster/1.0")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "AIVM_${safe}_${System.currentTimeMillis()}$ext")
            if (web) CookieManager.getInstance().getCookie(u)?.let { req.addRequestHeader("Cookie", it) }
            val dlId = (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(req)
            saveEntry(dlId, title, thumb, u)
            toast("ডাউনলোড শুরু হয়েছে। Downloads ট্যাবে দেখুন।")
        } catch (e: Exception) {
            toast("ডাউনলোড শুরু করা যায়নি। URL পরীক্ষা করুন।")
        }
    }

    private fun entries(): JSONArray =
        try { JSONArray(getSharedPreferences("aivm", Context.MODE_PRIVATE).getString("dl", "[]")) } catch (e: Exception) { JSONArray() }

    private fun saveEntry(id: Long, title: String, thumb: String, url: String) {
        val old = entries()
        val n = JSONArray()
        n.put(JSONObject().put("id", id).put("title", title).put("thumb", thumb).put("url", url))
        for (i in 0 until old.length()) n.put(old.get(i))
        getSharedPreferences("aivm", Context.MODE_PRIVATE).edit().putString("dl", n.toString()).apply()
    }

    private fun removeEntry(id: Long) {
        val old = entries()
        val n = JSONArray()
        for (i in 0 until old.length()) {
            val o = old.getJSONObject(i)
            if (o.getLong("id") != id) n.put(o)
        }
        getSharedPreferences("aivm", Context.MODE_PRIVATE).edit().putString("dl", n.toString()).apply()
    }

    private fun pref(k: String): String = getSharedPreferences("aivm", Context.MODE_PRIVATE).getString(k, "") ?: ""
    private fun setPref(k: String, v: String) { getSharedPreferences("aivm", Context.MODE_PRIVATE).edit().putString(k, v).apply() }

    private fun httpGet(u: String, headers: Map<String, String> = emptyMap()): String {
        val c = URL(u).openConnection() as HttpURLConnection
        c.connectTimeout = 15000
        c.readTimeout = 20000
        c.setRequestProperty("User-Agent", "AIVideoMaster/1.0 (Android app)")
        headers.forEach { (k, v) -> c.setRequestProperty(k, v) }
        return try { c.inputStream.bufferedReader().use { it.readText() } } finally { c.disconnect() }
    }

    private fun httpPost(u: String, body: String, headers: Map<String, String>): String {
        val c = URL(u).openConnection() as HttpURLConnection
        c.requestMethod = "POST"
        c.doOutput = true
        c.connectTimeout = 15000
        c.readTimeout = 30000
        c.setRequestProperty("Content-Type", "application/json")
        headers.forEach { (k, v) -> c.setRequestProperty(k, v) }
        return try {
            OutputStreamWriter(c.outputStream, Charsets.UTF_8).use { it.write(body) }
            c.inputStream.bufferedReader().use { it.readText() }
        } finally { c.disconnect() }
    }

    private fun loadImage(url: String, iv: ImageView) {
        val hit = cache.get(url)
        if (hit != null) { iv.setImageBitmap(hit); return }
        Thread {
            try {
                val c = URL(url).openConnection() as HttpURLConnection
                c.connectTimeout = 10000
                c.readTimeout = 15000
                c.setRequestProperty("User-Agent", "AIVideoMaster/1.0 (Android app)")
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
        background = buttonBg(color)
        setPadding(0, 0, 0, dp(4))
        elevation = dp(8).toFloat()
        setOnTouchListener { v, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> v.animate().translationY(dp(3).toFloat()).setDuration(60).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().translationY(0f).setDuration(90).start()
            }
            false
        }
        isClickable = true
        isFocusable = true
    }

    private fun buttonBg(color: Int): android.graphics.drawable.Drawable {
        val edge = GradientDrawable().apply { setColor(shade(shade(color))); cornerRadius = dp(15).toFloat() }
        val face = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(lighten(color), color)).apply {
            cornerRadius = dp(15).toFloat()
            setStroke(dp(1), Color.argb(70, 255, 255, 255))
        }
        val layers = android.graphics.drawable.LayerDrawable(arrayOf<android.graphics.drawable.Drawable>(edge, face))
        layers.setLayerInset(1, 0, 0, 0, dp(4))
        return layers
    }

    private fun lighten(color: Int): Int = Color.rgb(
        minOf(255, (Color.red(color) * 1.3).toInt() + 20),
        minOf(255, (Color.green(color) * 1.3).toInt() + 20),
        minOf(255, (Color.blue(color) * 1.3).toInt() + 20))

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
