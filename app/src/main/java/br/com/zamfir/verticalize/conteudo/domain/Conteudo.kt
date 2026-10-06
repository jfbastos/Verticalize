package br.com.zamfir.verticalize.conteudo.domain

data class Conteudo(
    val id: Long = 0L,
    val concursoId: Long,
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
