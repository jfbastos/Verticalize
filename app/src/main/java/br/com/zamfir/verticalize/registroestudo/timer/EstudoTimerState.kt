package br.com.zamfir.verticalize.registroestudo.timer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Fonte única do estado do cronômetro, compartilhada entre o [EstudoTimerService] (quem escreve) e
 * qualquer tela do app (que só observa). Vive fora do ViewModel porque uma única instância de
 * cronômetro é global — não por concurso — e precisa sobreviver à troca/recriação de telas.
 *
 * Isso só sobrevive enquanto o processo do app estiver vivo; se o sistema matar o processo por
 * completo (não apenas remover a tela), o cronômetro é perdido junto com o Foreground Service.
 */
object EstudoTimerState {
    private val _snapshot = MutableStateFlow(EstudoTimerSnapshot())
    val snapshot: StateFlow<EstudoTimerSnapshot> = _snapshot.asStateFlow()

    fun update(transform: (EstudoTimerSnapshot) -> EstudoTimerSnapshot) {
        _snapshot.update(transform)
    }
}
