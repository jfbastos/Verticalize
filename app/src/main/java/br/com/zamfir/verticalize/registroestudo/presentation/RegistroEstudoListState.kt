package br.com.zamfir.verticalize.registroestudo.presentation

import br.com.zamfir.verticalize.registroestudo.timer.EstudoTimerSnapshot
import br.com.zamfir.verticalize.registroestudo.timer.EstudoTimerStatus
import java.time.YearMonth
import java.time.ZoneOffset

enum class RegistroEstudoViewMode {
    LISTA,
    CALENDARIO
}

data class RegistroEstudoListState(
    val concursoId: Long = 0L,
    val registros: List<RegistroEstudoUi> = emptyList(),
    val isDialogVisible: Boolean = false,
    val editingId: Long? = null,
    val materia: String = "",
    val dataMillis: Long? = null,
    val dataFormatted: String = "",
    val isDatePickerVisible: Boolean = false,
    val horaInicioMinutos: Int = 480,
    val horaFimMinutos: Int = 1380,
    val isDeleteConfirmationVisible: Boolean = false,
    val registroPendingDeleteId: Long? = null,
    val viewMode: RegistroEstudoViewMode = RegistroEstudoViewMode.LISTA,
    // As datas do app são sempre meia-noite UTC; usar o mesmo fuso aqui evita o mês "errado" perto da virada do dia.
    val calendarMonth: YearMonth = YearMonth.now(ZoneOffset.UTC),

    val isRegistroOpcaoSheetVisible: Boolean = false,
    val isTimerStartDialogVisible: Boolean = false,
    val timerMateriaInput: String = "",
    // Estado global do cronômetro (só existe um por vez no app); ver EstudoTimerState.
    val timerSnapshot: EstudoTimerSnapshot = EstudoTimerSnapshot(),

    /** Seleção múltipla (só na visualização em lista): vazia = fora do modo de seleção. */
    val registrosSelecionados: Set<Long> = emptySet(),
    val isExcluirSelecionadosVisible: Boolean = false
)

val RegistroEstudoListState.isSelecaoAtiva: Boolean
    get() = registrosSelecionados.isNotEmpty()

val RegistroEstudoListState.isSaveEnabled: Boolean
    get() = materia.isNotBlank() && dataMillis != null && horaFimMinutos > horaInicioMinutos

val RegistroEstudoListState.isTimerStartSaveEnabled: Boolean
    get() = timerMateriaInput.isNotBlank()

/** Se o cronômetro ativo (rodando ou pausado) pertence a este concurso. */
val RegistroEstudoListState.isTimerAtivoNesteConcurso: Boolean
    get() = timerSnapshot.status != EstudoTimerStatus.IDLE && timerSnapshot.concursoId == concursoId

/** Se há um cronômetro ativo, mas de outro concurso — mostrado como aviso, sem controles aqui. */
val RegistroEstudoListState.isTimerAtivoEmOutroConcurso: Boolean
    get() = timerSnapshot.status != EstudoTimerStatus.IDLE && timerSnapshot.concursoId != concursoId
