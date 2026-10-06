package br.com.zamfir.verticalize.importacao.domain

import br.com.zamfir.verticalize.core.domain.formatDate

private const val CABECALHO_COLUNAS =
    "materia;descricao;eixo;bloco;aulas;min_por_aula;concluido;ultima_revisao;questoes;prioridade"
private const val CABECALHO_COLUNAS_ESTUDO = "data;hora_inicio;hora_fim;materia"

/**
 * Faz o caminho inverso de [TxtSectionsParser]: escreve concursos no formato TXT com seções, de modo
 * que o arquivo exportado possa ser importado de volta.
 *
 * O formato não tem aspas nem escape, então nos textos `;` vira `,` e quebras de linha viram espaço
 * (única perda possível). Um `#` no início da matéria também é removido, para a linha não virar comentário.
 */
class TxtSectionsFormatter {

    fun format(concursos: List<ImportedConcurso>): String {
        val blocos = concursos.map(::formatConcurso)
        return buildString {
            appendLine("# Exportado pelo Verticalize. Este arquivo pode ser importado de volta pelo app.")
            blocos.forEach { bloco ->
                appendLine()
                append(bloco)
            }
        }
    }

    private fun formatConcurso(concurso: ImportedConcurso): String = buildString {
        appendLine("[CONCURSO]")
        appendLine("nome: ${concurso.nome.paraLinhaUnica()}")
        appendLine("nivel: ${concurso.nivel.name.lowercase()}")
        if (concurso.dataProva != null) appendLine("data_prova: ${formatDate(concurso.dataProva)}")
        if (concurso.banca.isNotBlank()) appendLine("banca: ${concurso.banca.paraLinhaUnica()}")
        if (concurso.valorInscricaoCentavos > 0) {
            appendLine("valor_inscricao: ${formatarValor(concurso.valorInscricaoCentavos)}")
        }

        if (concurso.conteudos.isNotEmpty()) {
            appendLine()
            appendLine("[CONTEUDOS]")
            appendLine(CABECALHO_COLUNAS)
            concurso.conteudos.forEach { appendLine(formatConteudo(it)) }
        }

        if (concurso.estudos.isNotEmpty()) {
            appendLine()
            appendLine("[ESTUDOS]")
            appendLine(CABECALHO_COLUNAS_ESTUDO)
            concurso.estudos
                .sortedWith(compareBy({ it.data }, { it.horaInicioMinutos }))
                .forEach { appendLine(formatEstudo(it)) }
        }
    }

    private fun formatEstudo(estudo: ImportedEstudo): String {
        return listOf(
            formatDate(estudo.data),
            formatarHora(estudo.horaInicioMinutos),
            formatarHora(estudo.horaFimMinutos),
            estudo.materia.paraLinhaUnica()
        ).joinToString(";")
    }

    /** 510 -> "08:30". */
    private fun formatarHora(minutosDoDia: Int): String {
        return "${(minutosDoDia / 60).toString().padStart(2, '0')}:${(minutosDoDia % 60).toString().padStart(2, '0')}"
    }

    private fun formatConteudo(conteudo: ImportedConteudo): String {
        return listOf(
            conteudo.materia.paraLinhaUnica().trimStart('#').trim(),
            conteudo.descricao.paraLinhaUnica(),
            conteudo.eixo?.toString().orEmpty(),
            conteudo.bloco?.toString().orEmpty(),
            conteudo.quantidadeAulas.toString(),
            conteudo.tempoMedioAulaMinutos.toString(),
            if (conteudo.concluido) "sim" else "nao",
            conteudo.dataUltimaRevisao?.let(::formatDate).orEmpty(),
            conteudo.quantidadeQuestoesRealizadas.toString(),
            conteudo.prioridade.name.lowercase()
        ).joinToString(";")
    }

    /** 12050 -> "120,50" (sem separador de milhar, que a importação também entende). */
    private fun formatarValor(centavos: Long): String {
        return "${centavos / 100},${(centavos % 100).toString().padStart(2, '0')}"
    }

    private fun String.paraLinhaUnica(): String {
        return replace(';', ',').replace(Regex("\\s*[\\r\\n]+\\s*"), " ").trim()
    }
}
