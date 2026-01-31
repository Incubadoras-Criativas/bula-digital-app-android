package com.example.app_buladigital

import android.text.Html

sealed class HtmlToText {
    data class Text(val html: String) : HtmlToText()
    data class Image(val url: String, val legenda: String?) : HtmlToText()
}

 fun fatiarHtml(htmlOriginal: String): List<HtmlToText> {
    val listaDePecas = mutableListOf<HtmlToText>()

    // Regex para capturar a tag completa do ActionText e extrair URL e CAPTION
    // Esta regex procura o campo url="..." e o campo caption="..." dentro da tag
    val regexAnexo = "<action-text-attachment[^>]*url=\"([^\"]+)\"[^>]*caption=\"([^\"]+)\"[^>]*>.*?</action-text-attachment>|<action-text-attachment[^>]*url=\"([^\"]+)\"[^>]*>.*?</action-text-attachment>".toRegex()

    var ultimoIndice = 0

    regexAnexo.findAll(htmlOriginal).forEach { match ->
        // 1. Pega o texto antes do anexo
        val textoAntes = htmlOriginal.substring(ultimoIndice, match.range.first)
        if (textoAntes.isNotBlank()) {
            listaDePecas.add(HtmlToText.Text(textoAntes))
        }

        // 2. Extrai URL e Legenda
        // Como a tag pode ter ou não legenda, verificamos qual grupo do regex capturou
        val url = match.groupValues[1].ifBlank { match.groupValues[3] }
        val legenda = if (match.groupValues[2].isNotBlank()) match.groupValues[2] else null

        // Ajuste da URL (BaseURL) se for relativa
        val urlFinal = if (url.startsWith("/")) "${BuildConfig.BASE_URL}$url" else url

        listaDePecas.add(HtmlToText.Image(urlFinal, legenda))

        ultimoIndice = match.range.last + 1
    }

    val restoTexto = htmlOriginal.substring(ultimoIndice)
    if (restoTexto.isNotBlank()) {
        listaDePecas.add(HtmlToText.Text(restoTexto))
    }

    return listaDePecas
}