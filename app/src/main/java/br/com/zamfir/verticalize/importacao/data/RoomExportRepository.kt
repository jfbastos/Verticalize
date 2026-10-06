package br.com.zamfir.verticalize.importacao.data

import br.com.zamfir.verticalize.concurso.data.ConcursoEntity
import br.com.zamfir.verticalize.concurso.domain.Nivel
import br.com.zamfir.verticalize.conteudo.data.ConteudoEntity
import br.com.zamfir.verticalize.conteudo.domain.Prioridade
import br.com.zamfir.verticalize.core.domain.DataError
import br.com.zamfir.verticalize.core.domain.Result
import br.com.zamfir.verticalize.core.domain.safeCall
import br.com.zamfir.verticalize.importacao.domain.ExportRepository
import br.com.zamfir.verticalize.importacao.domain.ImportedConcurso
import br.com.zamfir.verticalize.importacao.domain.ImportedConteudo
import br.com.zamfir.verticalize.importacao.domain.ImportedEstudo
import br.com.zamfir.verticalize.registroestudo.data.RegistroEstudoEntity

class RoomExportRepository(
    private val dao: ImportDao
) : ExportRepository {

    override suspend fun carregarConcursos(): Result<List<ImportedConcurso>, DataError.Local> {
        return safeCall {
            val conteudosPorConcurso = dao.getAllConteudos()
                .sortedBy { it.id }
                .groupBy { it.concursoId }
            val estudosPorConcurso = dao.getAllEstudos().groupBy { it.concursoId }

            dao.getAllConcursos()
                .sortedWith(compareBy<ConcursoEntity> { it.dataProva }.thenBy { it.id })
                .map { concurso ->
                    concurso.toArquivo(
                        conteudos = conteudosPorConcurso[concurso.id].orEmpty(),
                        estudos = estudosPorConcurso[concurso.id].orEmpty()
                    )
                }
        }
    }

    override suspend fun carregarConcurso(concursoId: Long): Result<ImportedConcurso, DataError.Local> {
        return when (val resultado = safeCall { dao.getConcursoById(concursoId) }) {
            is Result.Error -> resultado
            is Result.Success -> {
                val concurso = resultado.data ?: return Result.Error(DataError.Local.NOT_FOUND)
                safeCall {
                    concurso.toArquivo(
                        conteudos = dao.getConteudosByConcursoId(concursoId),
                        estudos = dao.getEstudosByConcursoId(concursoId)
                    )
                }
            }
        }
    }
}

private fun ConcursoEntity.toArquivo(
    conteudos: List<ConteudoEntity>,
    estudos: List<RegistroEstudoEntity>
): ImportedConcurso {
    return ImportedConcurso(
        nome = nome,
        nivel = Nivel.valueOf(nivel),
        dataProva = dataProva,
        valorInscricaoCentavos = valorInscricaoCentavos,
        banca = banca,
        conteudos = conteudos.map { it.toArquivo() },
        estudos = estudos.map { it.toArquivo() }
    )
}

private fun ConteudoEntity.toArquivo(): ImportedConteudo {
    return ImportedConteudo(
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

private fun RegistroEstudoEntity.toArquivo(): ImportedEstudo {
    return ImportedEstudo(
        materia = materia,
        data = data,
        horaInicioMinutos = horaInicioMinutos,
        horaFimMinutos = horaFimMinutos
    )
}
