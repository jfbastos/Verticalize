package br.com.zamfir.verticalize.concurso.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.concurso.domain.ConcursoRepository
import br.com.zamfir.verticalize.concurso.domain.calcularHorasEstudadas
import br.com.zamfir.verticalize.conteudo.domain.Conteudo
import br.com.zamfir.verticalize.conteudo.domain.ConteudoRepository
import br.com.zamfir.verticalize.conteudo.domain.Prioridade
import br.com.zamfir.verticalize.conteudo.presentation.ConteudoUi
import br.com.zamfir.verticalize.conteudo.presentation.toConteudoUi
import br.com.zamfir.verticalize.core.domain.Result
import br.com.zamfir.verticalize.core.domain.onError
import br.com.zamfir.verticalize.core.domain.onSuccess
import br.com.zamfir.verticalize.core.presentation.UiText
import br.com.zamfir.verticalize.core.presentation.filterDigits
import br.com.zamfir.verticalize.core.domain.formatDate
import br.com.zamfir.verticalize.importacao.domain.ConcursoExporter
import br.com.zamfir.verticalize.importacao.domain.nomeDoArquivoDeExportacao
import br.com.zamfir.verticalize.importacao.presentation.ExportFeedback
import br.com.zamfir.verticalize.importacao.presentation.toFeedback
import br.com.zamfir.verticalize.registroestudo.domain.RegistroEstudoRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConcursoDetailViewModel(
    private val concursoId: Long,
    private val concursoRepository: ConcursoRepository,
    private val conteudoRepository: ConteudoRepository,
    private val registroEstudoRepository: RegistroEstudoRepository,
    private val exporter: ConcursoExporter
) : ViewModel() {

    private val _state = MutableStateFlow(ConcursoDetailState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<ConcursoDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            val result = concursoRepository.getConcursoById(concursoId)
            result
                .onSuccess { concurso ->
                    _state.update { it.copy(isLoading = false, concurso = concurso.toConcursoDetailUi()) }
                }
                .onError {
                    _state.update { it.copy(isLoading = false) }
                    eventChannel.send(ConcursoDetailEvent.ShowError(UiText.StringResource(R.string.error_not_found)))
                }

            if (result is Result.Success) {
                combine(
                    conteudoRepository.observeConteudosByConcursoId(concursoId),
                    registroEstudoRepository.observeRegistrosByConcursoId(concursoId)
                ) { conteudos, registros -> conteudos to registros }
                    .collect { (conteudos, registros) ->
                        val eixos = conteudos.toEixoUiList()
                        val novoPercentual = eixos.averagePercentual()
                        val percentualAnterior = _state.value.concurso?.percentualCompletude
                        val totalMinutosAula = conteudos.sumOf { it.quantidadeAulas * it.tempoMedioAulaMinutos }
                        val totalMinutosAssistidos = conteudos
                            .filter { it.concluido }
                            .sumOf { it.quantidadeAulas * it.tempoMedioAulaMinutos }

                        val totalMinutosEstudados = registros.sumOf { it.horaFimMinutos - it.horaInicioMinutos }
                        val novasHorasEstudadas = calcularHorasEstudadas(totalMinutosEstudados)
                        val horasAnteriores = _state.value.concurso?.horasEstudadas

                        val idsExistentes = conteudos.mapTo(HashSet()) { it.id }
                        _state.update {
                            it.copy(
                                conteudos = conteudos.map(Conteudo::toConteudoUi),
                                // Itens removidos por outra via (ex.: sync) não podem continuar selecionados.
                                conteudosSelecionados = it.conteudosSelecionados.intersect(idsExistentes),
                                eixos = eixos,
                                concurso = it.concurso?.copy(
                                    percentualCompletude = novoPercentual,
                                    horasEstudadas = novasHorasEstudadas
                                ),
                                totalMinutosAula = totalMinutosAula,
                                totalMinutosAulaAssistidos = totalMinutosAssistidos
                            )
                        }

                        if (percentualAnterior != null && percentualAnterior != novoPercentual) {
                            concursoRepository.updatePercentualCompletude(concursoId, novoPercentual)
                        }
                        if (horasAnteriores != null && horasAnteriores != novasHorasEstudadas) {
                            concursoRepository.updateHorasEstudadas(concursoId, novasHorasEstudadas)
                        }
                    }
            }
        }
    }

    fun onAction(action: ConcursoDetailAction) {
        when (action) {
            ConcursoDetailAction.OnHeaderExpandToggle -> _state.update {
                it.copy(isHeaderExpanded = !it.isHeaderExpanded)
            }

            ConcursoDetailAction.OnConteudoFiltroClick -> _state.update {
                it.copy(isConteudoFiltroSheetVisible = true)
            }
            ConcursoDetailAction.OnConteudoFiltroDismiss -> _state.update {
                it.copy(isConteudoFiltroSheetVisible = false)
            }
            is ConcursoDetailAction.OnConteudoFiltroEixoToggle -> _state.update {
                it.copy(conteudoFiltroEixosSelecionados = it.conteudoFiltroEixosSelecionados.toggled(action.eixo))
            }
            is ConcursoDetailAction.OnConteudoFiltroBlocoToggle -> _state.update {
                it.copy(conteudoFiltroBlocosSelecionados = it.conteudoFiltroBlocosSelecionados.toggled(action.bloco))
            }
            is ConcursoDetailAction.OnConteudoFiltroPrioridadeToggle -> _state.update {
                it.copy(
                    conteudoFiltroPrioridadesSelecionados =
                        it.conteudoFiltroPrioridadesSelecionados.toggled(action.prioridade)
                )
            }
            ConcursoDetailAction.OnConteudoFiltroLimparClick -> _state.update {
                it.copy(
                    conteudoFiltroEixosSelecionados = emptySet(),
                    conteudoFiltroBlocosSelecionados = emptySet(),
                    conteudoFiltroPrioridadesSelecionados = emptySet()
                )
            }

            ConcursoDetailAction.OnConteudoOrdenacaoClick -> _state.update {
                it.copy(isConteudoOrdenacaoSheetVisible = true)
            }
            ConcursoDetailAction.OnConteudoOrdenacaoDismiss -> _state.update {
                it.copy(isConteudoOrdenacaoSheetVisible = false)
            }
            is ConcursoDetailAction.OnConteudoOrdenacaoSelected -> _state.update {
                it.copy(conteudoOrdenacao = action.ordenacao, isConteudoOrdenacaoSheetVisible = false)
            }

            ConcursoDetailAction.OnConteudoAgrupamentoClick -> _state.update {
                it.copy(isConteudoAgrupamentoSheetVisible = true, isMoreMenuExpanded = false)
            }
            ConcursoDetailAction.OnConteudoAgrupamentoDismiss -> _state.update {
                it.copy(isConteudoAgrupamentoSheetVisible = false)
            }
            is ConcursoDetailAction.OnConteudoAgrupamentoSelected -> _state.update {
                // Todo agrupamento escolhido começa com os grupos retraídos.
                it.copy(
                    conteudoAgrupamento = action.agrupamento,
                    isConteudoAgrupamentoSheetVisible = false,
                    conteudoGruposExpandidos = emptySet()
                )
            }
            is ConcursoDetailAction.OnConteudoGrupoToggle -> _state.update {
                it.copy(conteudoGruposExpandidos = it.conteudoGruposExpandidos.toggled(action.chave))
            }

            ConcursoDetailAction.OnConteudoFabClick -> openConteudoCreateDialog()
            // No modo de seleção, o toque marca/desmarca em vez de abrir os detalhes.
            is ConcursoDetailAction.OnConteudoTapped -> _state.update {
                if (it.isConteudoSelecaoAtiva) {
                    it.copy(conteudosSelecionados = it.conteudosSelecionados.toggled(action.id))
                } else {
                    it.copy(conteudoDetalheId = action.id)
                }
            }
            ConcursoDetailAction.OnConteudoDetalheDismiss -> _state.update { it.copy(conteudoDetalheId = null) }
            // O id dos detalhes é mantido: ao fechar a edição (salvar ou cancelar) a dialog de detalhes volta.
            ConcursoDetailAction.OnConteudoDetalheEditClick -> _state.value.conteudoDetalheId?.let(::openConteudoEditDialog)
            is ConcursoDetailAction.OnConteudoConcluidoToggle -> toggleConteudoConcluido(action.id, action.concluido)
            ConcursoDetailAction.OnConteudoDismissDialog -> _state.update { it.copy(isConteudoDialogVisible = false) }
            is ConcursoDetailAction.OnConteudoMateriaChanged -> _state.update { it.copy(conteudoMateria = action.value) }
            is ConcursoDetailAction.OnConteudoDescricaoChanged -> _state.update { it.copy(conteudoDescricao = action.value) }
            is ConcursoDetailAction.OnConteudoEixoChanged -> _state.update {
                it.copy(conteudoEixoInput = action.rawInput.filterDigits(4))
            }
            is ConcursoDetailAction.OnConteudoBlocoChanged -> _state.update {
                it.copy(conteudoBlocoInput = action.rawInput.filterDigits(4))
            }
            is ConcursoDetailAction.OnConteudoQuantidadeAulasChanged -> _state.update {
                it.copy(conteudoQuantidadeAulasInput = action.rawInput.filterDigits(4))
            }
            is ConcursoDetailAction.OnConteudoTempoMedioAulaChanged -> _state.update {
                it.copy(conteudoTempoMedioAulaInput = action.rawInput.filterDigits(4))
            }
            ConcursoDetailAction.OnConteudoDataRevisaoFieldClick -> _state.update {
                it.copy(isConteudoDatePickerVisible = true)
            }
            is ConcursoDetailAction.OnConteudoDataRevisaoPickerConfirm -> _state.update {
                it.copy(
                    conteudoDataUltimaRevisaoMillis = action.millis,
                    conteudoDataUltimaRevisaoFormatted = formatDate(action.millis),
                    isConteudoDatePickerVisible = false
                )
            }
            ConcursoDetailAction.OnConteudoDataRevisaoPickerDismiss -> _state.update {
                it.copy(isConteudoDatePickerVisible = false)
            }
            is ConcursoDetailAction.OnConteudoQuantidadeQuestoesChanged -> _state.update {
                it.copy(conteudoQuantidadeQuestoesInput = action.rawInput.filterDigits(4))
            }
            is ConcursoDetailAction.OnConteudoPrioridadeChanged -> _state.update {
                it.copy(conteudoPrioridade = action.prioridade)
            }
            ConcursoDetailAction.OnConteudoSaveClick -> saveConteudo()

            is ConcursoDetailAction.OnConteudoLongPress -> _state.update {
                it.copy(conteudosSelecionados = it.conteudosSelecionados.toggled(action.id))
            }
            ConcursoDetailAction.OnConteudoSelecaoCancelar -> _state.update { it.copy(conteudosSelecionados = emptySet()) }
            // "Todos" = os exibidos (respeita os filtros ativos), sem perder os já marcados fora deles.
            ConcursoDetailAction.OnConteudoSelecionarTodosClick -> _state.update {
                it.copy(conteudosSelecionados = it.conteudosSelecionados + it.conteudosExibidos.map(ConteudoUi::id))
            }
            ConcursoDetailAction.OnConteudoExcluirSelecionadosClick -> _state.update {
                it.copy(isConteudoExcluirSelecionadosVisible = it.isConteudoSelecaoAtiva)
            }
            ConcursoDetailAction.OnConteudoExcluirSelecionadosDismiss -> _state.update {
                it.copy(isConteudoExcluirSelecionadosVisible = false)
            }
            ConcursoDetailAction.OnConteudoExcluirSelecionadosConfirm -> deleteConteudosSelecionados()

            is ConcursoDetailAction.OnTabSelected -> _state.update {
                it.copy(selectedTab = action.tab, conteudosSelecionados = emptySet())
            }

            ConcursoDetailAction.OnMoreMenuToggle -> _state.update { it.copy(isMoreMenuExpanded = !it.isMoreMenuExpanded) }
            ConcursoDetailAction.OnMoreMenuDismiss -> _state.update { it.copy(isMoreMenuExpanded = false) }
            ConcursoDetailAction.OnExportClick -> requestExport()
            is ConcursoDetailAction.OnExportFileSelected -> export(action.uri)
        }
    }

    private fun requestExport() {
        _state.update { it.copy(isMoreMenuExpanded = false) }
        val nome = _state.value.concurso?.nome ?: return

        viewModelScope.launch {
            eventChannel.send(ConcursoDetailEvent.OpenExportPicker(nomeDoArquivoDeExportacao(nome)))
        }
    }

    private fun export(uri: String) {
        viewModelScope.launch {
            val feedback = exporter.exportarConcurso(concursoId, uri).toFeedback { resumo ->
                UiText.StringResource(R.string.export_concurso_success_format, listOf(resumo.conteudos, resumo.estudos))
            }
            eventChannel.send(
                when (feedback) {
                    is ExportFeedback.Success -> ConcursoDetailEvent.ShowMessage(feedback.message)
                    is ExportFeedback.Failure -> ConcursoDetailEvent.ShowError(feedback.message)
                }
            )
        }
    }

    private fun openConteudoCreateDialog() {
        _state.update {
            it.copy(
                isConteudoDialogVisible = true,
                editingConteudoId = null,
                conteudoMateria = "",
                conteudoDescricao = "",
                conteudoEixoInput = "",
                conteudoBlocoInput = "",
                conteudoQuantidadeAulasInput = "",
                conteudoTempoMedioAulaInput = "",
                conteudoConcluido = false,
                conteudoDataUltimaRevisaoMillis = null,
                conteudoDataUltimaRevisaoFormatted = "",
                conteudoQuantidadeQuestoesInput = "",
                conteudoPrioridade = Prioridade.MEDIA
            )
        }
    }

    private fun openConteudoEditDialog(id: Long) {
        viewModelScope.launch {
            conteudoRepository.getConteudoById(id)
                .onSuccess { conteudo ->
                    _state.update {
                        it.copy(
                            isConteudoDialogVisible = true,
                            editingConteudoId = conteudo.id,
                            conteudoMateria = conteudo.materia,
                            conteudoDescricao = conteudo.descricao,
                            conteudoEixoInput = conteudo.eixo?.toString() ?: "",
                            conteudoBlocoInput = conteudo.bloco?.toString() ?: "",
                            conteudoQuantidadeAulasInput = conteudo.quantidadeAulas.toString(),
                            conteudoTempoMedioAulaInput = conteudo.tempoMedioAulaMinutos.toString(),
                            conteudoConcluido = conteudo.concluido,
                            conteudoDataUltimaRevisaoMillis = conteudo.dataUltimaRevisao,
                            conteudoDataUltimaRevisaoFormatted = conteudo.dataUltimaRevisao?.let(::formatDate) ?: "",
                            conteudoQuantidadeQuestoesInput = conteudo.quantidadeQuestoesRealizadas.toString(),
                            conteudoPrioridade = conteudo.prioridade
                        )
                    }
                }
                .onError {
                    eventChannel.send(ConcursoDetailEvent.ShowError(UiText.StringResource(R.string.error_not_found)))
                }
        }
    }

    private fun toggleConteudoConcluido(id: Long, concluido: Boolean) {
        viewModelScope.launch {
            conteudoRepository.setConcluido(id, concluido)
                .onError {
                    eventChannel.send(ConcursoDetailEvent.ShowError(UiText.StringResource(R.string.error_unknown)))
                }
        }
    }

    private fun deleteConteudosSelecionados() {
        val ids = _state.value.conteudosSelecionados
        if (ids.isEmpty()) return

        viewModelScope.launch {
            conteudoRepository.deleteConteudos(ids)
                .onSuccess {
                    _state.update { it.copy(conteudosSelecionados = emptySet(), isConteudoExcluirSelecionadosVisible = false) }
                    eventChannel.send(
                        ConcursoDetailEvent.ShowMessage(UiText.PluralResource(R.plurals.conteudo_excluidos_format, ids.size))
                    )
                }
                .onError {
                    // A seleção é mantida para o usuário poder tentar de novo.
                    _state.update { it.copy(isConteudoExcluirSelecionadosVisible = false) }
                    eventChannel.send(ConcursoDetailEvent.ShowError(UiText.StringResource(R.string.error_unknown)))
                }
        }
    }

    private fun saveConteudo() {
        val current = _state.value
        if (!current.isConteudoSaveEnabled) return

        val conteudo = Conteudo(
            id = current.editingConteudoId ?: 0L,
            concursoId = concursoId,
            materia = current.conteudoMateria.trim(),
            descricao = current.conteudoDescricao.trim(),
            eixo = current.conteudoEixoInput.toIntOrNull()?.takeIf { it >= 1 },
            bloco = current.conteudoBlocoInput.toIntOrNull()?.takeIf { it >= 1 },
            quantidadeAulas = current.conteudoQuantidadeAulasInput.toIntOrNull() ?: 0,
            tempoMedioAulaMinutos = current.conteudoTempoMedioAulaInput.toIntOrNull() ?: 0,
            concluido = current.conteudoConcluido,
            dataUltimaRevisao = current.conteudoDataUltimaRevisaoMillis,
            quantidadeQuestoesRealizadas = current.conteudoQuantidadeQuestoesInput.toIntOrNull() ?: 0,
            prioridade = current.conteudoPrioridade
        )

        viewModelScope.launch {
            conteudoRepository.upsertConteudo(conteudo)
                .onSuccess { _state.update { it.copy(isConteudoDialogVisible = false) } }
                .onError {
                    eventChannel.send(ConcursoDetailEvent.ShowError(UiText.StringResource(R.string.error_unknown)))
                }
        }
    }
}

private fun <T> Set<T>.toggled(value: T): Set<T> = if (value in this) this - value else this + value
