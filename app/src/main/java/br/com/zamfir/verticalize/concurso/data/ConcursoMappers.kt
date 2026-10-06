package br.com.zamfir.verticalize.concurso.data

import br.com.zamfir.verticalize.concurso.domain.Concurso
import br.com.zamfir.verticalize.concurso.domain.Nivel

fun ConcursoEntity.toConcurso(): Concurso {
    return Concurso(
        id = id,
        nome = nome,
        nivel = Nivel.valueOf(nivel),
        dataProva = dataProva,
        valorInscricaoCentavos = valorInscricaoCentavos,
        banca = banca,
        horasEstudadas = horasEstudadas,
        percentualCompletude = percentualCompletude
    )
}

fun Concurso.toConcursoEntity(): ConcursoEntity {
    return ConcursoEntity(
        id = id,
        nome = nome,
        nivel = nivel.name,
        dataProva = dataProva,
        valorInscricaoCentavos = valorInscricaoCentavos,
        banca = banca,
        horasEstudadas = horasEstudadas,
        percentualCompletude = percentualCompletude
    )
}
