package br.com.zamfir.verticalize.registroestudo.data

import br.com.zamfir.verticalize.registroestudo.domain.RegistroEstudo

fun RegistroEstudoEntity.toRegistroEstudo(): RegistroEstudo {
    return RegistroEstudo(
        id = id,
        concursoId = concursoId,
        materia = materia,
        data = data,
        horaInicioMinutos = horaInicioMinutos,
        horaFimMinutos = horaFimMinutos
    )
}

fun RegistroEstudo.toRegistroEstudoEntity(): RegistroEstudoEntity {
    return RegistroEstudoEntity(
        id = id,
        concursoId = concursoId,
        materia = materia,
        data = data,
        horaInicioMinutos = horaInicioMinutos,
        horaFimMinutos = horaFimMinutos
    )
}
