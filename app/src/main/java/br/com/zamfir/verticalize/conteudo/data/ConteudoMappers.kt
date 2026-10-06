package br.com.zamfir.verticalize.conteudo.data

import br.com.zamfir.verticalize.conteudo.domain.Conteudo
import br.com.zamfir.verticalize.conteudo.domain.Prioridade

fun ConteudoEntity.toConteudo(): Conteudo {
    return Conteudo(
        id = id,
        concursoId = concursoId,
        materia = materia,
        descricao = descricao,
        eixo = eixo,
        bloco = bloco,
        quantidadeAulas = quantidadeAulas,
        tempoMedioAulaMinutos = tempoMedioAulaMinutos,
        concluido = concluido,
        dataUltimaRevisao = dataUltimaRevisao,
        quantidadeQuestoesRealizadas = quantidadeQuestoesRealizadas,
        prioridade = Prioridade.valueOf(prioridade)
    )
}

fun Conteudo.toConteudoEntity(): ConteudoEntity {
    return ConteudoEntity(
        id = id,
        concursoId = concursoId,
        materia = materia,
        descricao = descricao,
        eixo = eixo,
        bloco = bloco,
        quantidadeAulas = quantidadeAulas,
        tempoMedioAulaMinutos = tempoMedioAulaMinutos,
        concluido = concluido,
        dataUltimaRevisao = dataUltimaRevisao,
        quantidadeQuestoesRealizadas = quantidadeQuestoesRealizadas,
        prioridade = prioridade.name
    )
}
