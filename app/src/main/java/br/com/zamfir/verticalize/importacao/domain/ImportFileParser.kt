package br.com.zamfir.verticalize.importacao.domain

/** Transforma o texto de um arquivo em concursos/conteúdos. Novos formatos (ex.: CSV) implementam esta interface. */
interface ImportFileParser {
    fun parse(text: String): ParsedImport
}
