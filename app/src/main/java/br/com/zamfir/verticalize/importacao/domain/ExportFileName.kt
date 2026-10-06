package br.com.zamfir.verticalize.importacao.domain

/** Nome sugerido ao exportar todos os concursos de uma vez. */
const val NOME_DO_ARQUIVO_DE_TODOS_OS_CONCURSOS = "verticalize-concursos.txt"

private val NAO_ALFANUMERICO = Regex("[^a-z0-9]+")
private const val TAMANHO_MAXIMO_DO_NOME = 40
private const val NOME_PADRAO = "concurso"

/**
 * Nome de arquivo sugerido para exportar um concurso: "TRT 2ª Região - Analista" vira
 * "verticalize-trt-2-regiao-analista.txt". Sem acentos ou símbolos, para funcionar em qualquer armazenamento.
 */
fun nomeDoArquivoDeExportacao(nomeDoConcurso: String): String {
    val slug = nomeDoConcurso.normalizado()
        .replace(NAO_ALFANUMERICO, "-")
        .trim('-')
        .take(TAMANHO_MAXIMO_DO_NOME)
        .trim('-')
        .ifEmpty { NOME_PADRAO }
    return "verticalize-$slug.txt"
}
