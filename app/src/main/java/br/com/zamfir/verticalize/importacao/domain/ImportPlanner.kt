package br.com.zamfir.verticalize.importacao.domain

import br.com.zamfir.verticalize.concurso.domain.Concurso
import br.com.zamfir.verticalize.concurso.domain.Nivel
import br.com.zamfir.verticalize.concurso.domain.calcularHorasEstudadas
import br.com.zamfir.verticalize.concurso.domain.calcularPercentualCompletude
import br.com.zamfir.verticalize.conteudo.domain.Conteudo
import br.com.zamfir.verticalize.registroestudo.domain.RegistroEstudo

/**
 * Um concurso do arquivo já cruzado com o que existe no app.
 * Se [existingConcursoId] não for nulo o concurso já estava cadastrado: ele **não é alterado**,
 * apenas recebe os [novosConteudos] e [novosEstudos] que ainda não tinha.
 */
data class PlannedConcurso(
    val nome: String,
    val nivel: Nivel,
    val dataProva: Long?,
    val valorInscricaoCentavos: Long,
    val banca: String,
    val existingConcursoId: Long?,
    val novosConteudos: List<ImportedConteudo>,
    val conteudosDuplicados: Int,
    val percentualCompletude: Int,
    val novosEstudos: List<ImportedEstudo> = emptyList(),
    val estudosDuplicados: Int = 0,
    val horasEstudadas: Int = 0
) {
    val isNovo: Boolean get() = existingConcursoId == null
}

data class ImportPlan(val concursos: List<PlannedConcurso>) {
    val concursosNovos: Int get() = concursos.count { it.isNovo }
    val concursosExistentes: Int get() = concursos.count { !it.isNovo }
    val conteudosNovos: Int get() = concursos.sumOf { it.novosConteudos.size }
    val conteudosDuplicados: Int get() = concursos.sumOf { it.conteudosDuplicados }
    val estudosNovos: Int get() = concursos.sumOf { it.novosEstudos.size }
    val estudosDuplicados: Int get() = concursos.sumOf { it.estudosDuplicados }
    val temNovidades: Boolean get() = concursosNovos > 0 || conteudosNovos > 0 || estudosNovos > 0
}

/** Decide, sem tocar no banco, o que será criado e o que será pulado por já existir. */
class ImportPlanner {

    fun plan(
        parsed: ParsedImport,
        concursosExistentes: List<Concurso>,
        conteudosExistentes: List<Conteudo>,
        estudosExistentes: List<RegistroEstudo> = emptyList()
    ): ImportPlan {
        val existentesPorChave = concursosExistentes
            .groupBy { chaveDoConcurso(it.nome, it.dataProva) }
            .mapValues { (_, iguais) -> iguais.first() }
        val conteudosPorConcurso = conteudosExistentes.groupBy { it.concursoId }
        val estudosPorConcurso = estudosExistentes.groupBy { it.concursoId }

        // Um mesmo concurso pode aparecer em mais de um bloco do arquivo; os itens são somados.
        val acumuladores = LinkedHashMap<String, Acumulador>()
        for (importado in parsed.concursos) {
            val chave = chaveDoConcurso(importado.nome, importado.dataProva)
            val acumulador = acumuladores.getOrPut(chave) {
                val existente = existentesPorChave[chave]
                Acumulador(
                    importado = importado,
                    existente = existente,
                    conteudosJaCadastrados = existente?.let { conteudosPorConcurso[it.id] }.orEmpty(),
                    estudosJaCadastrados = existente?.let { estudosPorConcurso[it.id] }.orEmpty()
                )
            }
            acumulador.adicionarConteudos(importado.conteudos)
            acumulador.adicionarEstudos(importado.estudos)
        }

        return ImportPlan(acumuladores.values.map { it.paraPlano() })
    }

    private class Acumulador(
        private val importado: ImportedConcurso,
        private val existente: Concurso?,
        private val conteudosJaCadastrados: List<Conteudo>,
        private val estudosJaCadastrados: List<RegistroEstudo>
    ) {
        private val chavesDeConteudo = conteudosJaCadastrados
            .mapTo(mutableSetOf()) { chaveDoConteudo(it.materia, it.descricao) }
        private val chavesDeEstudo = estudosJaCadastrados
            .mapTo(mutableSetOf()) { chaveDoEstudo(it.materia, it.data, it.horaInicioMinutos, it.horaFimMinutos) }

        private val conteudosNovos = mutableListOf<ImportedConteudo>()
        private var conteudosDuplicados = 0
        private val estudosNovos = mutableListOf<ImportedEstudo>()
        private var estudosDuplicados = 0

        fun adicionarConteudos(conteudos: List<ImportedConteudo>) {
            for (conteudo in conteudos) {
                if (chavesDeConteudo.add(chaveDoConteudo(conteudo.materia, conteudo.descricao))) {
                    conteudosNovos += conteudo
                } else {
                    conteudosDuplicados++
                }
            }
        }

        fun adicionarEstudos(estudos: List<ImportedEstudo>) {
            for (estudo in estudos) {
                val chave = chaveDoEstudo(estudo.materia, estudo.data, estudo.horaInicioMinutos, estudo.horaFimMinutos)
                if (chavesDeEstudo.add(chave)) {
                    estudosNovos += estudo
                } else {
                    estudosDuplicados++
                }
            }
        }

        fun paraPlano(): PlannedConcurso {
            val itensDeProgresso = conteudosJaCadastrados.map { it.eixo to it.concluido } +
                conteudosNovos.map { it.eixo to it.concluido }
            val minutosEstudados = estudosJaCadastrados.sumOf { it.horaFimMinutos - it.horaInicioMinutos } +
                estudosNovos.sumOf { it.horaFimMinutos - it.horaInicioMinutos }

            return PlannedConcurso(
                nome = importado.nome,
                nivel = importado.nivel,
                dataProva = importado.dataProva,
                valorInscricaoCentavos = importado.valorInscricaoCentavos,
                banca = importado.banca,
                existingConcursoId = existente?.id,
                novosConteudos = conteudosNovos.toList(),
                conteudosDuplicados = conteudosDuplicados,
                percentualCompletude = calcularPercentualCompletude(itensDeProgresso),
                novosEstudos = estudosNovos.toList(),
                estudosDuplicados = estudosDuplicados,
                horasEstudadas = calcularHorasEstudadas(minutosEstudados)
            )
        }
    }
}

private fun chaveDoConcurso(nome: String, dataProva: Long?) = "${nome.normalizado()}|$dataProva"

private fun chaveDoConteudo(materia: String, descricao: String) = "${materia.normalizado()}|${descricao.normalizado()}"

private fun chaveDoEstudo(materia: String, data: Long, inicioMinutos: Int, fimMinutos: Int) =
    "${materia.normalizado()}|$data|$inicioMinutos|$fimMinutos"
