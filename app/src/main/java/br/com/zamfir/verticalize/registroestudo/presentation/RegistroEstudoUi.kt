package br.com.zamfir.verticalize.registroestudo.presentation

import br.com.zamfir.verticalize.conteudo.presentation.toHorasMinutosFormatted
import br.com.zamfir.verticalize.core.domain.formatDate
import br.com.zamfir.verticalize.registroestudo.domain.RegistroEstudo

data class RegistroEstudoUi(
    val id: Long,
    val materia: String,
    val dataFormatted: String,
    val duracaoFormatted: String,
    /** Epoch millis à meia-noite UTC; usado para agrupar os registros por dia na visão de calendário. */
    val data: Long = 0L
)

fun RegistroEstudo.toRegistroEstudoUi(): RegistroEstudoUi {
    return RegistroEstudoUi(
        id = id,
        materia = materia,
        dataFormatted = formatDate(data),
        duracaoFormatted = (horaFimMinutos - horaInicioMinutos).toHorasMinutosFormatted(),
        data = data
    )
}
