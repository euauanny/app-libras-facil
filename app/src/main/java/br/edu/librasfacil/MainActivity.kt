package br.edu.librasfacil

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.WindowInsets
import android.widget.*
import android.text.InputFilter

class MainActivity : Activity() {
    private lateinit var avatar: VLibrasAvatarPlayer
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var input: EditText
    private lateinit var status: TextView
    private lateinit var translate: Button
    private lateinit var replay: Button
    private lateinit var progress: ProgressBar
    private var busy = false
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun text(value: String, size: Float = 16f) = TextView(this).apply {
        text = value; textSize = size; setTextColor(Color.rgb(23, 49, 54)); setPadding(0, dp(8), 0, dp(8))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(242, 247, 246)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(24), dp(20), dp(24), dp(24))
            setBackgroundColor(Color.rgb(242, 247, 246))
        }
        val scroll = ScrollView(this).apply { isFillViewport = true; addView(root) }
        setContentView(scroll)
        scroll.setOnApplyWindowInsetsListener { view, insets ->
            val top = if (android.os.Build.VERSION.SDK_INT >= 30) insets.getInsets(WindowInsets.Type.systemBars()).top else insets.systemWindowInsetTop
            val bottom = if (android.os.Build.VERSION.SDK_INT >= 30) insets.getInsets(WindowInsets.Type.systemBars()).bottom else insets.systemWindowInsetBottom
            view.setPadding(0, top, 0, bottom); insets
        }
        root.addView(text("LIBRAS FÁCIL", 14f).apply { setTypeface(null, Typeface.BOLD) })
        root.addView(text("Escreva uma frase em português para iniciar.", 16f))
        root.addView(text("Seu texto", 18f).apply { labelFor = R.id.source_text })
        input = EditText(this).apply {
            id = R.id.source_text; hint = "Ex.: Olá, como você está?"; minLines = 3; gravity = android.view.Gravity.TOP
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE or android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            filters = arrayOf(InputFilter.LengthFilter(500)); textSize = 18f
            setText(savedInstanceState?.getString("input") ?: "")
        }
        root.addView(input, LinearLayout.LayoutParams(-1, -2))
        root.addView(text("Até 500 caracteres", 13f))
        translate = Button(this).apply {
            text = "Testar API"; isAllCaps = false; minHeight = dp(52)
            setTextColor(Color.WHITE); backgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(16, 105, 94))
            setOnClickListener { submit() }
        }
        root.addView(translate, LinearLayout.LayoutParams(-1, -2))
        status = text("Carregando ferramenta VLibras…", 14f).apply { accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE }
        root.addView(status)
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { isIndeterminate = true }
        root.addView(progress, LinearLayout.LayoutParams(-1, dp(4)))
        avatar = VLibrasAvatarPlayer(this) { ready, loading, message ->
            busy = loading
            translate.isEnabled = ready
            progress.visibility = if (loading) View.VISIBLE else View.GONE
            status.text = message
            if (::replay.isInitialized) replay.isEnabled = !loading
        }
        avatar.view.background = GradientDrawable().apply { setColor(Color.rgb(222, 238, 233)); cornerRadius = dp(20).toFloat() }

        replay = Button(this).apply {
            text = "Recarregar VLibras"; isAllCaps = false; isEnabled = false
            setOnClickListener { avatar.load() }
        }
        root.addView(replay, LinearLayout.LayoutParams(-1, -2))
        root.addView(avatar.view, LinearLayout.LayoutParams(-1, dp(480)).apply { topMargin = dp(12); bottomMargin = dp(12) })
        translate.isEnabled = false
        avatar.load()
    }
    private fun submit() {
        if (busy) return
        val phrase = input.text.toString().trim()
        if (phrase.isEmpty()) { input.error = "Digite uma frase em português"; input.requestFocus(); return }
        avatar.prepare(phrase)
        status.text = "VLibras carregado • texto enviado ao widget"
    }
    override fun onSaveInstanceState(outState: Bundle) { outState.putString("input", input.text.toString()); super.onSaveInstanceState(outState) }
    override fun onDestroy() { handler.removeCallbacksAndMessages(null); avatar.release(); super.onDestroy() }
}




