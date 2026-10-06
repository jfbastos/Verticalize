package br.com.zamfir.verticalize.concurso.presentation

import br.com.zamfir.verticalize.conteudo.domain.Prioridade
import br.com.zamfir.verticalize.conteudo.presentation.ConteudoAgrupamento
import br.com.zamfir.verticalize.conteudo.presentation.ConteudoOrdenacao
import br.com.zamfir.verticalize.conteudo.presentation.ConteudoUi

enum class ConcursoDetailTab {
    CONTEUDOS,
    HORAS_ESTUDO
}

data class ConcursoDetailState(
    val isLoading: Boolean = true,
    val concurso: ConcursoDetailUi? = null,
    val eixos: List<EixoUi> = emptyList(),
    val isHeaderExpanded: Boolean = false,
    val totalMinutosAula: Int = 0,
    val totalMinutosAulaAssistidos: Int = 0,
    val isMoreMenuExpanded: Boolean = false,
    val selectedTab: ConcursoDetailTab = ConcursoDetailTab.CONTEUDOS,

    val conteudos: List<ConteudoUi> = emptyList(),
    val conteudoDetalheId: Long? = null,
    val conteudoFiltroEixosSelecionados: Set<Int> = emptySet(),
    val conteudoFiltroBlocosSelecionados: Set<Int> = emptySet(),
    val conteudoFiltroPrioridadesSelecionados: Set<Prioridade> = emptySet(),
    val isConteudoFiltroSheetVisible: Boolean = false,
    val conteudoOrdenacao: ConteudoOrdenacao = ConteudoOrdenacao.PADRAO,
    val isConteudoOrdenacaoSheetVisible: Boolean = false,
    val conteudoAgrupamento: ConteudoAgrupamento = ConteudoAgrupamento.NENHUM,
    val isConteudoAgrupamentoSheetVisible: Boolean = false,
    /** Chaves (ver [ConteudoGrupo.chave]) dos grupos que o usuário abriu; ausente = retraído. */
    val conteudoGruposExpandidos: Set<String> = emptySet(),
    val isConteudoDialogVisible: Boolean = false,
    val editingConteudoId: Long? = null,
    val conteudoMateria: String = "",
    val conteudoDescricao: String = "",
    val conteudoEixoInput: String = "",
    val conteudoBlocoInput: String = "",
    val conteudoQuantidadeAulasInput: String = "",
    val conteudoTempoMedioAulaInput: String = "",
    val conteudoConcluido: Boolean = false,
    val conteudoDataUltimaRevisaoMillis: Long? = null,
    val conteudoDataUltimaRevisaoFormatted: String = "",
    val isConteudoDatePickerVisible: Boolean = false,
    val conteudoQuantidadeQuestoesInput: String = "",
    val conteudoPrioridade: Prioridade = Prioridade.MEDIA,

    val conteudosSelecionados: Set<Long> = emptySet(),
    val isConteudoExcluirSelecionadosVisible: Boolean = false
)

val ConcursoDetailState.isConteudoSelecaoAtiva: Boolean
    get() = conteudosSelecionados.isNotEmpty()

val ConcursoDetailState.isConteudoSaveEnabled: Boolean
    get() = conteudoDescricao.isNotBlank()

val ConcursoDetailState.conteudoDetalhe: ConteudoUi?
    get() = conteudoDetalheId?.let { id -> conteudos.find { it.id == id } }

val ConcursoDetailState.eixosDisponiveisParaFiltro: List<Int>
    get() = conteudos.mapNotNull { it.eixo }.distinct().sorted()

val ConcursoDetailState.blocosDisponiveisParaFiltro: List<Int>
    get() = conteudos.mapNotNull { it.bloco }.distinct().sorted()

val ConcursoDetailState.isConteudoFiltroAtivo: Boolean
    get() = conteudoFiltroEixosSelecionados.isNotEmpty() ||
        conteudoFiltroBlocosSelecionados.isNotEmpty() ||
        conteudoFiltroPrioridadesSelecionados.isNotEmpty()

val ConcursoDetailState.conteudosExibidos: List<ConteudoUi>
    get() = conteudos
        .filter { conteudo ->
            (conteudoFiltroEixosSelecionados.isEmpty() || conteudo.eixo in conteudoFiltroEixosSelecionados) &&
                (conteudoFiltroBlocosSelecionados.isEmpty() || conteudo.bloco in conteudoFiltroBlocosSelecionados) &&
                (
                    conteudoFiltroPrioridadesSelecionados.isEmpty() ||
                        conteudo.prioridade in conteudoFiltroPrioridadesSelecionados
                    )
        }
        .let { filtrados ->
            when (conteudoOrdenacao) {
                ConteudoOrdenacao.PADRAO -> filtrados
                ConteudoOrdenacao.PRIORIDADE_ASC -> filtrados.sortedBy { it.prioridade }
                ConteudoOrdenacao.PRIORIDADE_DESC -> filtrados.sortedByDescending { it.prioridade }
                ConteudoOrdenacao.EIXO_ASC -> filtrados.sortedBy { it.eixo }
                ConteudoOrdenacao.EIXO_DESC -> filtrados.sortedByDescending { it.eixo }
                ConteudoOrdenacao.BLOCO_ASC -> filtrados.sortedBy { it.bloco }
                ConteudoOrdenacao.BLOCO_DESC -> filtrados.sortedByDescending { it.bloco }
                ConteudoOrdenacao.REVISAO_ASC -> filtrados.sortedBy { it.dataUltimaRevisaoMillis }
                ConteudoOrdenacao.REVISAO_DESC -> filtrados.sortedByDescending { it.dataUltimaRevisaoMillis }
                ConteudoOrdenacao.TEMPO_AULA_ASC -> filtrados.sortedBy { it.quantidadeAulas * it.tempoMedioAulaMinutos }
                ConteudoOrdenacao.TEMPO_AULA_DESC -> filtrados.sortedByDescending { it.quantidadeAulas * it.tempoMedioAulaMinutos }
            }
        }

/**
 * Um grupo de conteúdos exibidos juntos. [numero] identifica o grupo quando o agrupamento é por
 * eixo/bloco, e [materia] quando é por matéria; os dois nulos significam sem agrupamento, ou o
 * grupo dos conteúdos sem eixo/bloco/matéria. A ordem interna já vem de [conteudosExibidos].
 */
data class ConteudoGrupo(val numero: Int? = null, val materia: String? = null, val itens: List<ConteudoUi>)

/**
 * Identifica o grupo de forma estável entre recomposições, para lembrar se o usuário o recolheu.
 * Prefixado pelo [agrupamento] porque "sem eixo", "sem bloco" e "sem matéria" têm [numero] e
 * [materia] igualmente nulos.
 */
fun ConteudoGrupo.chave(agrupamento: ConteudoAgrupamento): String = "$agrupamento:$numero:$materia"

val ConcursoDetailState.conteudosAgrupados: List<ConteudoGrupo>
    get() {
        val exibidos = conteudosExibidos
        return when (conteudoAgrupamento) {
            ConteudoAgrupamento.NENHUM -> listOf(ConteudoGrupo(itens = exibidos))
            ConteudoAgrupamento.EIXO -> exibidos.agrupadosPorNumero { it.eixo }
            ConteudoAgrupamento.BLOCO -> exibidos.agrupadosPorNumero { it.bloco }
            ConteudoAgrupamento.MATERIA -> exibidos.agrupadosPorMateria()
        }
    }

/** Grupos com número ordenados em ordem crescente; o grupo sem número (se houver) vai por último. */
private fun List<ConteudoUi>.agrupadosPorNumero(numero: (ConteudoUi) -> Int?): List<ConteudoGrupo> {
    val porNumero = groupBy(numero)
    val grupos = porNumero.keys.filterNotNull().sorted().map { ConteudoGrupo(numero = it, itens = porNumero.getValue(it)) }
    val semNumero = porNumero[null]
    return if (semNumero != null) grupos + ConteudoGrupo(itens = semNumero) else grupos
}

/** Grupos por matéria em ordem alfabética; o grupo sem matéria (se houver) vai por último. */
private fun List<ConteudoUi>.agrupadosPorMateria(): List<ConteudoGrupo> {
    val porMateria = groupBy { it.materia }
    val grupos = porMateria.keys.filter { it.isNotBlank() }.sorted()
        .map { ConteudoGrupo(materia = it, itens = porMateria.getValue(it)) }
    val semMateria = porMateria.filterKeys { it.isBlank() }.values.flatten()
    return if (semMateria.isNotEmpty()) grupos + ConteudoGrupo(itens = semMateria) else grupos
}
