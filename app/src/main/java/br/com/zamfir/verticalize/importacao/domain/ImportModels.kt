package br.com.zamfir.verticalize.importacao.domain

import br.com.zamfir.verticalize.concurso.domain.Nivel
import br.com.zamfir.verticalize.conteudo.domain.Prioridade

data class ImportedConteudo(
    val materia: String = "",
    val descricao: String,
    val eixo: Int? = null,
    val bloco: Int? = null,
    val quantidadeAulas: Int = 0,
    val tempoMedioAulaMinutos: Int = 0,
    val concluido: Boolean = false,
    val dataUltimaRevisao: Long? = null,
    val quantidadeQuestoesRealizadas: Int = 0,
    val prioridade: Prioridade = Prioridade.MEDIA
)

/** Um horário de estudo: [data] em meia-noite UTC e o intervalo em minutos desde 00:00 (fim > início). */
data class ImportedEstudo(
    val materia: String,
    val data: Long,
    val horaInicioMinutos: Int,
    val horaFimMinutos: Int
)

data class ImportedConcurso(
    val nome: String,
    val nivel: Nivel,
    val dataProva: Long? = null,
    val valorInscricaoCentavos: Long = 0L,
    val banca: String = "",
    val conteudos: List<ImportedConteudo> = emptyList(),
    val estudos: List<ImportedEstudo> = emptyList()
)

/** Resultado da leitura do arquivo: o que foi entendido e as linhas que foram ignoradas. */
data class ParsedImport(
    val concursos: List<ImportedConcurso>,
    val errors: List<ImportLineError>
)

data class ImportSummary(
    val concursosCriados: Int,
    val conteudosCriados: Int,
    val estudosCriados: Int = 0
)
