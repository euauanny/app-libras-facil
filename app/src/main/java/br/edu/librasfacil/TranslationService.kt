package br.edu.librasfacil

/** Contrato interno do app. Adaptar ao contrato real do serviço escolhido. */
data class Translation(val source: String, val glosses: List<String>)
interface TranslationService {
    val status: String
    fun translate(text: String): Translation
}
/** Testa somente o fluxo da interface; não produz glosas nem tradução linguística. */
class DemoTranslationService : TranslationService {
    override val status = "Demonstração local • API não conectada"
    override fun translate(text: String): Translation {
        require(text.isNotBlank()) { "Digite uma frase antes de continuar." }
        return Translation(text.trim(), emptyList())
    }
}
/** Ponto de substituição pelo adaptador REST/VLibras. Nunca colocar chaves de LLM no app. */
class PendingTranslationService : TranslationService {
    override val status = "Serviço de tradução não configurado"
    override fun translate(text: String): Translation = error(status)
}
