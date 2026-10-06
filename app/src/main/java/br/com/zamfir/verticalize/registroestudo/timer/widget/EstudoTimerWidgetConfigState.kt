package br.com.zamfir.verticalize.registroestudo.timer.widget

data class ConcursoOptionUi(val id: Long, val nome: String)

data class EstudoTimerWidgetConfigState(
    val isLoading: Boolean = true,
    val concursos: List<ConcursoOptionUi> = emptyList(),
    val concursoIdSelecionado: Long? = null,
    val materia: String = ""
)

val EstudoTimerWidgetConfigState.isStartEnabled: Boolean
    get() = concursoIdSelecionado != null && materia.isNotBlank()
