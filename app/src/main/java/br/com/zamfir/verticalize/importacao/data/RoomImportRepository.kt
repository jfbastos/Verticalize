package br.com.zamfir.verticalize.importacao.data

import br.com.zamfir.verticalize.concurso.data.ConcursoEntity
import br.com.zamfir.verticalize.concurso.data.toConcurso
import br.com.zamfir.verticalize.conteudo.data.ConteudoEntity
import br.com.zamfir.verticalize.conteudo.data.toConteudo
import br.com.zamfir.verticalize.core.domain.DataError
import br.com.zamfir.verticalize.core.domain.Result
import br.com.zamfir.verticalize.core.domain.map
import br.com.zamfir.verticalize.core.domain.safeCall
import br.com.zamfir.verticalize.importacao.domain.ImportPlan
import br.com.zamfir.verticalize.importacao.domain.ImportPlanner
import br.com.zamfir.verticalize.importacao.domain.ImportRepository
import br.com.zamfir.verticalize.importacao.domain.ImportSummary
import br.com.zamfir.verticalize.importacao.domain.ParsedImport
import br.com.zamfir.verticalize.importacao.domain.PlannedConcurso
import br.com.zamfir.verticalize.registroestudo.data.RegistroEstudoEntity
import br.com.zamfir.verticalize.registroestudo.data.toRegistroEstudo

class RoomImportRepository(
    private val dao: ImportDao,
    private val planner: ImportPlanner
) : ImportRepository {

    override suspend fun analisar(parsed: ParsedImport): Result<ImportPlan, DataError.Local> {
        return safeCall {
            val concursos = dao.getAllConcursos().map { it.toConcurso() }
            val conteudos = dao.getAllConteudos().map { it.toConteudo() }
            val estudos = dao.getAllEstudos().map { it.toRegistroEstudo() }
            planner.plan(parsed, concursos, conteudos, estudos)
        }
    }

    override suspend fun aplicar(plan: ImportPlan): Result<ImportSummary, DataError.Local> {
        // Concurso já cadastrado sem nada novo não precisa de nenhuma escrita.
        val itens = plan.concursos
            .filter { it.isNovo || it.novosConteudos.isNotEmpty() || it.novosEstudos.isNotEmpty() }
            .map { it.toEntities() }

        return safeCall { dao.aplicar(itens) }.map {
            ImportSummary(
                concursosCriados = plan.concursosNovos,
                conteudosCriados = plan.conteudosNovos,
                estudosCriados = plan.estudosNovos
            )
        }
    }
}

private fun PlannedConcurso.toEntities(): ConcursoImportEntities {
    return ConcursoImportEntities(
        existingId = existingConcursoId,
        concurso = ConcursoEntity(
            nome = nome,
            nivel = nivel.name,
            dataProva = dataProva,
            valorInscricaoCentavos = valorInscricaoCentavos,
            banca = banca,
            horasEstudadas = horasEstudadas,
            percentualCompletude = percentualCompletude
        ),
        conteudos = novosConteudos.map { conteudo ->
            ConteudoEntity(
                // Preenchido pelo ImportDao depois que o concurso tem id.
                concursoId = 0L,
                materia = conteudo.materia,
                descricao = conteudo.descricao,
                eixo = conteudo.eixo,
                bloco = conteudo.bloco,
                quantidadeAulas = conteudo.quantidadeAulas,
                tempoMedioAulaMinutos = conteudo.tempoMedioAulaMinutos,
                concluido = conteudo.concluido,
                dataUltimaRevisao = conteudo.dataUltimaRevisao,
                quantidadeQuestoesRealizadas = conteudo.quantidadeQuestoesRealizadas,
                prioridade = conteudo.prioridade.name
            )
        },
        estudos = novosEstudos.map { estudo ->
            RegistroEstudoEntity(
                concursoId = 0L,
                materia = estudo.materia,
                data = estudo.data,
                horaInicioMinutos = estudo.horaInicioMinutos,
                horaFimMinutos = estudo.horaFimMinutos
            )
        }
    )
}
