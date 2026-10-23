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
    private val service: TranslationService = DemoTranslationService()
    private lateinit var avatar: AvatarPlayer
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var input: EditText
    private lateinit var status: TextView
    private lateinit var translate: Button
    private lateinit var replay: Button
    private var lastTranslation: Translation? = null
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
        root.addView(text("Uma ponte para\na comunicação", 30f).apply { setTypeface(null, Typeface.BOLD) })
        root.addView(text("Escreva uma frase em português para iniciar.", 16f))
        root.addView(text("Seu texto", 18f).apply { labelFor = R.id.source_text })
        input = EditText(this).apply {
            id = R.id.source_text; hint = "Ex.: Olá, como você está?"; minLines = 3; gravity = android.view.Gravity.TOP
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE or android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            filters = arrayOf(InputFilter.LengthFilter(500)); textSize = 18f
            setText(savedInstanceState?.getString("input") ?: "")
        }
        root.addView(input, LinearLayout.LayoutParams(-1, -2))
        root.addView(text("Até 500 caracteres • comece com uma frase curta", 13f))
        translate = Button(this).apply {
            text = "Traduzir / Reproduzir"; isAllCaps = false; minHeight = dp(52)
            setTextColor(Color.WHITE); backgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(16, 105, 94))
            setOnClickListener { submit() }
        }
        root.addView(translate, LinearLayout.LayoutParams(-1, -2))
        status = text(service.status, 14f).apply { accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE }
        root.addView(status)
        avatar = PlaceholderAvatarPlayer(this)
        avatar.view.background = GradientDrawable().apply { setColor(Color.rgb(222, 238, 233)); cornerRadius = dp(20).toFloat() }
        root.addView(avatar.view, LinearLayout.LayoutParams(-1, dp(240)).apply { topMargin = dp(12); bottomMargin = dp(12) })
        replay = Button(this).apply {
            text = "Repetir demonstração"; isAllCaps = false; isEnabled = false
            setOnClickListener { lastTranslation?.let { avatar.play(it) } }
        }
        root.addView(replay, LinearLayout.LayoutParams(-1, -2))
        root.addView(text("Protótipo acadêmico • modo demonstração", 14f).apply { setTypeface(null, Typeface.BOLD) })
        root.addView(text("Esta versão testa a interface. Ainda não traduz para Libras nem reproduz sinais. Na versão integrada, a tradução automática poderá apresentar falhas.", 14f))
    }
    private fun submit() {
        if (busy) return
        val phrase = input.text.toString().trim()
        if (phrase.isEmpty()) { input.error = "Digite uma frase em português"; input.requestFocus(); return }
        busy = true; translate.isEnabled = false; replay.isEnabled = false
        status.text = "Preparando demonstração local…"
        // Apenas uma espera visual. O adaptador de rede deve executar fora da thread principal.
        handler.postDelayed({
            try {
                val result = service.translate(phrase)
                avatar.play(result); lastTranslation = result; replay.isEnabled = true
                status.text = "Demonstração concluída • API e avatar ainda não integrados"
            } catch (error: Exception) {
                status.text = error.message ?: "Não foi possível concluir. Tente novamente."
            } finally { busy = false; translate.isEnabled = true }
        }, 700)
    }
    override fun onSaveInstanceState(outState: Bundle) { outState.putString("input", input.text.toString()); super.onSaveInstanceState(outState) }
    override fun onDestroy() { handler.removeCallbacksAndMessages(null); avatar.release(); super.onDestroy() }
}
