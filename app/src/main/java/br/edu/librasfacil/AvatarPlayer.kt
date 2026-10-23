package br.edu.librasfacil

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.TextView

/** O módulo do avatar pode expor um player de clipes 2D ou uma superfície 3D. */
interface AvatarPlayer {
    val view: View
    fun play(translation: Translation)
    fun release()
}
class PlaceholderAvatarPlayer(context: Context) : AvatarPlayer {
    private val label = TextView(context).apply {
        gravity = Gravity.CENTER
        textSize = 19f
        setTextColor(0xFF185E58.toInt())
        text = "Espaço do avatar\n\nO avatar da equipe aparecerá aqui."
        contentDescription = "Área reservada para o avatar de Libras, ainda não integrado"
    }
    override val view: View = label
    override fun play(translation: Translation) {
        label.text = "Demonstração concluída\n\nTexto recebido.\nA reprodução de sinais depende da integração do avatar."
    }
    override fun release() = Unit
}
