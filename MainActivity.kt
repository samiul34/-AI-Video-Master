package com.aivideomaster.app

import android.app.Activity
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.speech.RecognizerIntent
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import java.util.Locale

class MainActivity : Activity() {
    private val bg = Color.rgb(5, 8, 22)
    private val panel = Color.rgb(15, 23, 42)
    private val cyan = Color.rgb(24, 217, 255)
    private val purple = Color.rgb(139, 92, 246)
    private val white = Color.rgb(245, 248, 255)
    private lateinit var query: EditText
    private lateinit var status: TextView
    private lateinit var directUrl: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(12))
            background = gradient(intArrayOf(bg, Color.rgb(13, 17, 44)), 0)
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val logo = TextView(this).apply {
            text = "▶"
            textSize = 27f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = gradient(intArrayOf(purple, Color.rgb(0, 160, 255)), 20)
            elevation = dp(8).toFloat()
        }
        top.addView(logo, LinearLayout.LayoutParams(dp(54), dp(54)))
        val brandBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }
        brandBox.addView(TextView(this).apply {
            text = "AI VIDEO MASTER"
            textSize = 16f
            letterSpacing = 0.08f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
        })
        brandBox.addView(TextView(this).apply {
            text = "FIND  •  PREVIEW  •  DOWNLOAD"
            textSize = 9f
            letterSpacing = 0.07f
            setTextColor(cyan)
        })
        top.addView(brandBox)
        val spacer = View(this)
        top.addView(spacer, LinearLayout.LayoutParams(0, 1, 1f))
        top.addView(TextView(this).apply {
            text = "3D ✦"
            textSize = 12f
            setTextColor(cyan)
            setPadding(dp(10), dp(8), dp(10), dp(8))
            background = rounded(Color.rgb(22, 40, 68), 18)
        })
        content.addView(top)

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(22), dp(18), dp(20))
            background = gradient(intArrayOf(Color.rgb(34, 35, 91), Color.rgb(9, 88, 125), Color.rgb(17, 22, 49)), 24)
            elevation = dp(9).toFloat()
        }
        content.addView(hero, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(22) })
        hero.addView(TextView(this).apply {
            text = "YOUR VIDEO,\\nYOUR COMMAND."
            textSize = 27f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })
        hero.addView(TextView(this).apply {
            text = "বলুন বা লিখুন—আপনার পছন্দের ভিডিও খুঁজুন"
            textSize = 14f
            setTextColor(Color.rgb(216, 238, 255))
            setPadding(0, dp(10), 0, dp(16))
        })
        val tags = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("✦ AI Search", "◉ Voice", "⬇ Save").forEach { label ->
            tags.addView(TextView(this).apply {
                text = label
                textSize = 11f
                setTextColor(white)
                setPadding(dp(10), dp(8), dp(10), dp(8))
                background = rounded(Color.argb(65, 255, 255, 255), 18)
            }, LinearLayout.LayoutParams(-2, -2).apply { rightMargin = dp(7) })
        }
        hero.addView(tags)

        section(content, "01", "AI VIDEO SEARCH", "আপনি কী দেখতে চান?")
        query = EditText(this).apply {
            hint = "যেমন: বাংলাদেশের সুন্দর জায়গার ভিডিও"
            setHintTextColor(Color.rgb(137, 153, 180))
            setTextColor(white)
            textSize = 15f
            minLines = 2
            maxLines = 4
            setPadding(dp(15), dp(13), dp(15), dp(13))
            background = rounded(panel, 16)
        }
        content.addView(query, LinearLayout.LayoutParams(-1, -2))
        val buttons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val voice = button("🎙 বলুন", purple)
        buttons.addView(voice, LinearLayout.LayoutParams(0, dp(50), 1f).apply { rightMargin = dp(8) })
        voice.setOnClickListener { startVoiceInput() }
        val search = button("⌕ খুঁজুন", Color.rgb(0, 133, 175))
        buttons.addView(search, LinearLayout.LayoutParams(0, dp(50), 1f))
        search.setOnClickListener { searchVideos() }
        content.addView(buttons, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(10) })

        section(content, "02", "DISCOVER", "জনপ্রিয় সার্চ")
        val chips = listOf("বাংলাদেশের প্রকৃতি", "রান্নার ভিডিও", "ভ্রমণ ভিডিও")
        chips.forEachIndexed { i, item ->
            val card = TextView(this).apply {
                text = listOf("🌄   ", "🍜   ", "✈   ")[i] + item + "                                      ↗"
                textSize = 14f
                setTextColor(white)
                setPadding(dp(15), dp(15), dp(12), dp(15))
                background = rounded(panel, 15)
                elevation = dp(3).toFloat()
            }
            content.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(9) })
            card.setOnClickListener { query.setText(item); searchVideos() }
        }

        section(content, "03", "DIRECT DOWNLOAD", "শুধু অনুমোদিত সরাসরি ভিডিও URL")
        content.addView(TextView(this).apply {
            text = "MP4 / M4V / WEBM / MOV ফাইল URL দিন। YouTube বা Facebook পেজ লিংক থেকে এই প্রোটোটাইপ সরাসরি ভিডিও ফাইল বের করে না।"
            textSize = 12f
            setTextColor(Color.rgb(161, 174, 199))
        })
        directUrl = EditText(this).apply {
            hint = "https://example.com/video.mp4"
            setHintTextColor(Color.rgb(137, 153, 180))
            setTextColor(white)
            textSize = 13f
            setSingleLine(true)
            setPadding(dp(13), dp(12), dp(13), dp(12))
            background = rounded(panel, 13)
        }
        content.addView(directUrl, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(10) })
        val download = button("⬇   Download video", Color.rgb(0, 128, 105))
        content.addView(download, LinearLayout.LayoutParams(-1, dp(50)).apply { topMargin = dp(9) })
        download.setOnClickListener { downloadDirectFile() }

        status = TextView(this).apply {
            text = "● READY  •  AI VIDEO MASTER"
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(cyan)
            setPadding(0, dp(12), 0, dp(5))
        }
        root.addView(status)
        setContentView(root)
    }

    private fun section(parent: LinearLayout, number: String, title: String, subtitle: String) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(24), 0, dp(11))
        }
        row.addView(TextView(this).apply {
            text = number
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(cyan)
            setPadding(dp(9), dp(7), dp(9), dp(7))
            background = rounded(Color.rgb(21, 45, 76), 10)
        })
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), 0, 0, 0)
        }
        col.addView(TextView(this).apply {
            text = title
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
            setTextColor(white)
        })
        col.addView(TextView(this).apply {
            text = subtitle
            textSize = 11f
            setTextColor(Color.rgb(151, 169, 198))
            setPadding(0, dp(3), 0, 0)
        })
        row.addView(col)
        parent.addView(row)
    }

    private fun searchVideos() {
        val q = query.text.toString().trim()
        if (q.isEmpty()) { toast("আগে ভিডিওর নাম লিখুন বা বলুন।"); return }
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(query.windowToken, 0)
        status.text = "SEARCHING • সার্চ ফলাফল খোলা হচ্ছে"
        val url = "https://www.google.com/search?tbm=vid&q=" + Uri.encode(q)
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun startVoiceInput() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "কোন ভিডিও খুঁজবেন বলুন")
            }
            @Suppress("DEPRECATION")
            startActivityForResult(intent, 101)
        } catch (e: ActivityNotFoundException) {
            toast("এই ফোনে ভয়েস রিকগনিশন সুবিধা পাওয়া যায়নি।")
        }
    }

    @Deprecated("Legacy activity result")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == RESULT_OK) {
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                query.setText(matches[0])
                searchVideos()
            }
        }
    }

    private fun downloadDirectFile() {
        val url = directUrl.text.toString().trim()
        if (!(url.startsWith("https://") || url.startsWith("http://"))) {
            toast("একটি বৈধ http/https ভিডিও URL দিন।"); return
        }
        val path = url.substringBefore("?").lowercase(Locale.ROOT)
        if (!listOf(".mp4", ".m4v", ".webm", ".mov").any { path.endsWith(it) }) {
            toast("সরাসরি ভিডিও ফাইল URL দিন, যেমন .mp4 বা .webm।"); return
        }
        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("AI Video Master")
                .setDescription("ভিডিও ডাউনলোড হচ্ছে")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "AIVideo_${System.currentTimeMillis()}.mp4")
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)
            (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
            toast("ডাউনলোড শুরু হয়েছে।")
        } catch (e: Exception) { toast("ডাউনলোড শুরু করা যায়নি। URL পরীক্ষা করুন।") }
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

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
        setStroke(dp(1), Color.argb(50, 110, 210, 255))
    }

    private fun gradient(colors: IntArray, radius: Int) = GradientDrawable(
        GradientDrawable.Orientation.TL_BR, colors
    ).apply { cornerRadius = dp(radius).toFloat() }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
