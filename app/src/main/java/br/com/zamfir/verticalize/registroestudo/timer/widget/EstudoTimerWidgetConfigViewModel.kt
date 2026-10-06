package br.com.zamfir.verticalize.registroestudo.timer.widget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.zamfir.verticalize.concurso.domain.ConcursoRepository
import br.com.zamfir.verticalize.registroestudo.timer.EstudoTimerController
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EstudoTimerWidgetConfigViewModel(
    private val concursoRepository: ConcursoRepository,
    private val timerController: EstudoTimerController
) : ViewModel() {

    private val _state = MutableStateFlow(EstudoTimerWidgetConfigState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<EstudoTimerWidgetConfigEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            concursoRepository.observeConcursos().collect { concursos ->
                _state.update { current ->
                    current.copy(
                        isLoading = false,
                        concursos = concursos.map { ConcursoOptionUi(it.id, it.nome) },
                        // Pré-seleciona o primeiro concurso só na primeira carga; não sobrescreve uma escolha já feita.
                        concursoIdSelecionado = current.concursoIdSelecionado ?: concursos.firstOrNull()?.id
                    )
                }
            }
        }
    }

    fun onAction(action: EstudoTimerWidgetConfigAction) {
        when (action) {
            is EstudoTimerWidgetConfigAction.OnConcursoSelected -> _state.update {
                it.copy(concursoIdSelecionado = action.concursoId)
            }
            is EstudoTimerWidgetConfigAction.OnMateriaChanged -> _state.update { it.copy(materia = action.value) }
            EstudoTimerWidgetConfigAction.OnStartClick -> onStartClick()
            EstudoTimerWidgetConfigAction.OnCancelClick -> finish()
        }
    }

    private fun onStartClick() {
        val current = _state.value
        val concursoId = current.concursoIdSelecionado
        if (concursoId == null || current.materia.isBlank()) return

        timerController.start(concursoId, current.materia.trim())
        finish()
    }

    private fun finish() {
        viewModelScope.launch { eventChannel.send(EstudoTimerWidgetConfigEvent.Finish) }
    }
}
