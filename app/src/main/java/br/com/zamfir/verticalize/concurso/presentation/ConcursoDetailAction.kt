package br.com.zamfir.verticalize.concurso.presentation

import br.com.zamfir.verticalize.conteudo.domain.Prioridade
import br.com.zamfir.verticalize.conteudo.presentation.ConteudoAgrupamento
import br.com.zamfir.verticalize.conteudo.presentation.ConteudoOrdenacao

sealed interface ConcursoDetailAction {
    data object OnHeaderExpandToggle : ConcursoDetailAction

    data object OnConteudoFiltroClick : ConcursoDetailAction
    data object OnConteudoFiltroDismiss : ConcursoDetailAction
    data class OnConteudoFiltroEixoToggle(val eixo: Int) : ConcursoDetailAction
    data class OnConteudoFiltroBlocoToggle(val bloco: Int) : ConcursoDetailAction
    data class OnConteudoFiltroPrioridadeToggle(val prioridade: Prioridade) : ConcursoDetailAction
    data object OnConteudoFiltroLimparClick : ConcursoDetailAction

    data object OnConteudoOrdenacaoClick : ConcursoDetailAction
    data object OnConteudoOrdenacaoDismiss : ConcursoDetailAction
    data class OnConteudoOrdenacaoSelected(val ordenacao: ConteudoOrdenacao) : ConcursoDetailAction

    data object OnConteudoAgrupamentoClick : ConcursoDetailAction
    data object OnConteudoAgrupamentoDismiss : ConcursoDetailAction
    data class OnConteudoAgrupamentoSelected(val agrupamento: ConteudoAgrupamento) : ConcursoDetailAction
    data class OnConteudoGrupoToggle(val chave: String) : ConcursoDetailAction

    data object OnConteudoFabClick : ConcursoDetailAction
    data class OnConteudoTapped(val id: Long) : ConcursoDetailAction
    data object OnConteudoDetalheDismiss : ConcursoDetailAction
    data object OnConteudoDetalheEditClick : ConcursoDetailAction
    data class OnConteudoConcluidoToggle(val id: Long, val concluido: Boolean) : ConcursoDetailAction
    data object OnConteudoDismissDialog : ConcursoDetailAction
    data class OnConteudoMateriaChanged(val value: String) : ConcursoDetailAction
    data class OnConteudoDescricaoChanged(val value: String) : ConcursoDetailAction
    data class OnConteudoEixoChanged(val rawInput: String) : ConcursoDetailAction
    data class OnConteudoBlocoChanged(val rawInput: String) : ConcursoDetailAction
    data class OnConteudoQuantidadeAulasChanged(val rawInput: String) : ConcursoDetailAction
    data class OnConteudoTempoMedioAulaChanged(val rawInput: String) : ConcursoDetailAction
    data object OnConteudoDataRevisaoFieldClick : ConcursoDetailAction
    data class OnConteudoDataRevisaoPickerConfirm(val millis: Long) : ConcursoDetailAction
    data object OnConteudoDataRevisaoPickerDismiss : ConcursoDetailAction
    data class OnConteudoQuantidadeQuestoesChanged(val rawInput: String) : ConcursoDetailAction
    data class OnConteudoPrioridadeChanged(val prioridade: Prioridade) : ConcursoDetailAction
    data object OnConteudoSaveClick : ConcursoDetailAction

    data class OnConteudoLongPress(val id: Long) : ConcursoDetailAction
    data object OnConteudoSelecaoCancelar : ConcursoDetailAction
    data object OnConteudoSelecionarTodosClick : ConcursoDetailAction
    data object OnConteudoExcluirSelecionadosClick : ConcursoDetailAction
    data object OnConteudoExcluirSelecionadosConfirm : ConcursoDetailAction
    data object OnConteudoExcluirSelecionadosDismiss : ConcursoDetailAction

    data class OnTabSelected(val tab: ConcursoDetailTab) : ConcursoDetailAction

    data object OnMoreMenuToggle : ConcursoDetailAction
    data object OnMoreMenuDismiss : ConcursoDetailAction
    data object OnExportClick : ConcursoDetailAction
    data class OnExportFileSelected(val uri: String) : ConcursoDetailAction
}
