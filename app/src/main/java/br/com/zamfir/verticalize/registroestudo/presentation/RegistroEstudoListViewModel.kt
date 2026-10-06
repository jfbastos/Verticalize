package br.com.zamfir.verticalize.registroestudo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.core.domain.onError
import br.com.zamfir.verticalize.core.domain.onSuccess
import br.com.zamfir.verticalize.core.presentation.UiText
import br.com.zamfir.verticalize.core.domain.formatDate
import br.com.zamfir.verticalize.registroestudo.domain.RegistroEstudo
import br.com.zamfir.verticalize.registroestudo.domain.RegistroEstudoRepository
import br.com.zamfir.verticalize.registroestudo.timer.EstudoTimerController
import br.com.zamfir.verticalize.registroestudo.timer.EstudoTimerState
import br.com.zamfir.verticalize.registroestudo.timer.EstudoTimerStatus
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegistroEstudoListViewModel(
    private val concursoId: Long,
    private val repository: RegistroEstudoRepository,
    private val timerController: EstudoTimerController
) : ViewModel() {

    private val _state = MutableStateFlow(RegistroEstudoListState(concursoId = concursoId))
    val state = _state.asStateFlow()

    private val eventChannel = Channel<RegistroEstudoListEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.observeRegistrosByConcursoId(concursoId).collect { registros ->
                val idsExistentes = registros.mapTo(HashSet()) { it.id }
                _state.update {
                    it.copy(
                        registros = registros.map(RegistroEstudo::toRegistroEstudoUi),
                        // Itens removidos por outra via (ex.: sync) não podem continuar selecionados.
                        registrosSelecionados = it.registrosSelecionados.intersect(idsExistentes)
                    )
                }
            }
        }
        // O cronômetro é global (só existe um por vez); cada tela só reflete o mesmo estado compartilhado.
        viewModelScope.launch {
            EstudoTimerState.snapshot.collect { snapshot ->
                _state.update { it.copy(timerSnapshot = snapshot) }
            }
        }
    }

    fun onAction(action: RegistroEstudoListAction) {
        when (action) {
            RegistroEstudoListAction.OnFabClick -> _state.update { it.copy(isRegistroOpcaoSheetVisible = true) }
            RegistroEstudoListAction.OnDismissDialog -> _state.update { it.copy(isDialogVisible = false) }
            is RegistroEstudoListAction.OnMateriaChanged -> _state.update { it.copy(materia = action.value) }
            RegistroEstudoListAction.OnDataFieldClick -> _state.update { it.copy(isDatePickerVisible = true) }
            is RegistroEstudoListAction.OnDataPickerConfirm -> _state.update {
                it.copy(
                    dataMillis = action.millis,
                    dataFormatted = formatDate(action.millis),
                    isDatePickerVisible = false
                )
            }
            RegistroEstudoListAction.OnDataPickerDismiss -> _state.update { it.copy(isDatePickerVisible = false) }
            is RegistroEstudoListAction.OnHoraInicioChanged -> _state.update { it.copy(horaInicioMinutos = action.minutos) }
            is RegistroEstudoListAction.OnHoraFimChanged -> _state.update { it.copy(horaFimMinutos = action.minutos) }
            RegistroEstudoListAction.OnSaveClick -> save()

            is RegistroEstudoListAction.OnEditSwipe -> openEditDialog(action.id)
            is RegistroEstudoListAction.OnDeleteSwipe -> _state.update {
                it.copy(isDeleteConfirmationVisible = true, registroPendingDeleteId = action.id)
            }
            RegistroEstudoListAction.OnDeleteConfirm -> deletePendingRegistro()
            RegistroEstudoListAction.OnDeleteDismiss -> _state.update {
                it.copy(isDeleteConfirmationVisible = false, registroPendingDeleteId = null)
            }

            // Fora do modo de seleção o toque não faz nada (a edição é pelo swipe).
            is RegistroEstudoListAction.OnRegistroTapped -> _state.update {
                if (it.isSelecaoAtiva) it.copy(registrosSelecionados = it.registrosSelecionados.toggled(action.id)) else it
            }
            is RegistroEstudoListAction.OnRegistroLongPress -> _state.update {
                it.copy(registrosSelecionados = it.registrosSelecionados.toggled(action.id))
            }
            RegistroEstudoListAction.OnSelecaoCancelar -> _state.update { it.copy(registrosSelecionados = emptySet()) }
            RegistroEstudoListAction.OnSelecionarTodosClick -> _state.update {
                it.copy(registrosSelecionados = it.registros.mapTo(HashSet()) { registro -> registro.id })
            }
            RegistroEstudoListAction.OnExcluirSelecionadosClick -> _state.update {
                it.copy(isExcluirSelecionadosVisible = it.isSelecaoAtiva)
            }
            RegistroEstudoListAction.OnExcluirSelecionadosDismiss -> _state.update {
                it.copy(isExcluirSelecionadosVisible = false)
            }
            RegistroEstudoListAction.OnExcluirSelecionadosConfirm -> deleteSelecionados()

            // A seleção só existe na lista; trocar para o calendário a descarta.
            is RegistroEstudoListAction.OnViewModeChanged -> _state.update {
                it.copy(viewMode = action.viewMode, registrosSelecionados = emptySet())
            }
            RegistroEstudoListAction.OnCalendarPreviousMonthClick -> _state.update {
                it.copy(calendarMonth = it.calendarMonth.minusMonths(1))
            }
            RegistroEstudoListAction.OnCalendarNextMonthClick -> _state.update {
                it.copy(calendarMonth = it.calendarMonth.plusMonths(1))
            }

            RegistroEstudoListAction.OnRegistroOpcaoDismiss -> _state.update { it.copy(isRegistroOpcaoSheetVisible = false) }
            RegistroEstudoListAction.OnRegistroManualOptionClick -> {
                _state.update { it.copy(isRegistroOpcaoSheetVisible = false) }
                openCreateDialog()
            }
            RegistroEstudoListAction.OnRegistroCronometroOptionClick -> onRegistroCronometroOptionClick()

            is RegistroEstudoListAction.OnTimerMateriaChanged -> _state.update { it.copy(timerMateriaInput = action.value) }
            RegistroEstudoListAction.OnTimerStartDismiss -> _state.update { it.copy(isTimerStartDialogVisible = false) }
            RegistroEstudoListAction.OnTimerStartConfirm -> onTimerStartConfirm()
            RegistroEstudoListAction.OnTimerPauseClick -> timerController.pause()
            RegistroEstudoListAction.OnTimerResumeClick -> timerController.resume()
            RegistroEstudoListAction.OnTimerRegistrarClick -> timerController.registrar()
            RegistroEstudoListAction.OnTimerDescartarClick -> timerController.descartar()
        }
    }

    private fun onRegistroCronometroOptionClick() {
        _state.update { it.copy(isRegistroOpcaoSheetVisible = false) }

        if (_state.value.timerSnapshot.status != EstudoTimerStatus.IDLE) {
            viewModelScope.launch {
                eventChannel.send(
                    RegistroEstudoListEvent.ShowError(UiText.StringResource(R.string.estudo_timer_erro_ja_em_andamento))
                )
            }
            return
        }

        _state.update { it.copy(isTimerStartDialogVisible = true, timerMateriaInput = "") }
    }

    private fun onTimerStartConfirm() {
        val current = _state.value
        if (!current.isTimerStartSaveEnabled) return

        timerController.start(concursoId, current.timerMateriaInput.trim())
        _state.update { it.copy(isTimerStartDialogVisible = false) }
    }

    private fun openCreateDialog() {
        _state.update {
            it.copy(
                isDialogVisible = true,
                editingId = null,
                materia = "",
                dataMillis = null,
                dataFormatted = "",
                horaInicioMinutos = 480,
                horaFimMinutos = 1380
            )
        }
    }

    private fun openEditDialog(id: Long) {
        viewModelScope.launch {
            repository.getRegistroEstudoById(id)
                .onSuccess { registro ->
                    _state.update {
                        it.copy(
                            isDialogVisible = true,
                            editingId = registro.id,
                            materia = registro.materia,
                            dataMillis = registro.data,
                            dataFormatted = formatDate(registro.data),
                            horaInicioMinutos = registro.horaInicioMinutos,
                            horaFimMinutos = registro.horaFimMinutos
                        )
                    }
                }
                .onError {
                    eventChannel.send(RegistroEstudoListEvent.ShowError(UiText.StringResource(R.string.error_not_found)))
                }
        }
    }

    private fun save() {
        val current = _state.value
        if (!current.isSaveEnabled) return

        val registro = RegistroEstudo(
            id = current.editingId ?: 0L,
            concursoId = concursoId,
            materia = current.materia.trim(),
            data = requireNotNull(current.dataMillis),
            horaInicioMinutos = current.horaInicioMinutos,
            horaFimMinutos = current.horaFimMinutos
        )

        viewModelScope.launch {
            repository.upsertRegistroEstudo(registro)
                .onSuccess { _state.update { it.copy(isDialogVisible = false) } }
                .onError {
                    eventChannel.send(RegistroEstudoListEvent.ShowError(UiText.StringResource(R.string.error_unknown)))
                }
        }
    }

    private fun deleteSelecionados() {
        val ids = _state.value.registrosSelecionados
        if (ids.isEmpty()) return

        viewModelScope.launch {
            repository.deleteRegistrosEstudo(ids)
                .onSuccess {
                    _state.update { it.copy(registrosSelecionados = emptySet(), isExcluirSelecionadosVisible = false) }
                    eventChannel.send(
                        RegistroEstudoListEvent.ShowMessage(
                            UiText.PluralResource(R.plurals.registro_estudo_excluidos_format, ids.size)
                        )
                    )
                }
                .onError {
                    // A seleção é mantida para o usuário poder tentar de novo.
                    _state.update { it.copy(isExcluirSelecionadosVisible = false) }
                    eventChannel.send(RegistroEstudoListEvent.ShowError(UiText.StringResource(R.string.error_unknown)))
                }
        }
    }

    private fun deletePendingRegistro() {
        val id = _state.value.registroPendingDeleteId ?: return

        viewModelScope.launch {
            repository.deleteRegistroEstudo(id)
                .onSuccess {
                    _state.update { it.copy(isDeleteConfirmationVisible = false, registroPendingDeleteId = null) }
                }
                .onError {
                    _state.update { it.copy(isDeleteConfirmationVisible = false, registroPendingDeleteId = null) }
                    eventChannel.send(RegistroEstudoListEvent.ShowError(UiText.StringResource(R.string.error_unknown)))
                }
        }
    }
}

private fun <T> Set<T>.toggled(value: T): Set<T> = if (value in this) this - value else this + value
