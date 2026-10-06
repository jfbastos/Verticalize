package br.com.zamfir.verticalize.concurso.presentation

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import br.com.zamfir.verticalize.conteudo.domain.Prioridade
import br.com.zamfir.verticalize.conteudo.presentation.ConteudoAgrupamento
import br.com.zamfir.verticalize.conteudo.presentation.ConteudoOrdenacao
import br.com.zamfir.verticalize.conteudo.presentation.ConteudoUi
import org.junit.jupiter.api.Test

class ConcursoDetailStateTest {

    @Test
    fun `sem filtro nem ordenacao ativos retorna a lista original`() {
        val conteudos = listOf(
            sampleConteudo(id = 1L, eixo = 2, bloco = 1),
            sampleConteudo(id = 2L, eixo = 1, bloco = 2)
        )
        val state = ConcursoDetailState(conteudos = conteudos)

        assertThat(state.conteudosExibidos.map { it.id }).containsExactly(1L, 2L)
    }

    @Test
    fun `filtro por um unico eixo retorna so os conteudos daquele eixo`() {
        val conteudos = listOf(
            sampleConteudo(id = 1L, eixo = 1),
            sampleConteudo(id = 2L, eixo = 2),
            sampleConteudo(id = 3L, eixo = 1)
        )
        val state = ConcursoDetailState(conteudos = conteudos, conteudoFiltroEixosSelecionados = setOf(1))

        assertThat(state.conteudosExibidos.map { it.id }).containsExactly(1L, 3L)
    }

    @Test
    fun `filtro por multiplos eixos simultaneamente funciona como uniao`() {
        val conteudos = listOf(
            sampleConteudo(id = 1L, eixo = 1),
            sampleConteudo(id = 2L, eixo = 2),
            sampleConteudo(id = 3L, eixo = 3)
        )
        val state = ConcursoDetailState(conteudos = conteudos, conteudoFiltroEixosSelecionados = setOf(1, 3))

        assertThat(state.conteudosExibidos.map { it.id }).containsExactly(1L, 3L)
    }

    @Test
    fun `filtro por eixo e bloco simultaneamente funciona como intersecao`() {
        val conteudos = listOf(
            sampleConteudo(id = 1L, eixo = 1, bloco = 1),
            sampleConteudo(id = 2L, eixo = 1, bloco = 2),
            sampleConteudo(id = 3L, eixo = 2, bloco = 1)
        )
        val state = ConcursoDetailState(
            conteudos = conteudos,
            conteudoFiltroEixosSelecionados = setOf(1),
            conteudoFiltroBlocosSelecionados = setOf(1)
        )

        assertThat(state.conteudosExibidos.map { it.id }).containsExactly(1L)
    }

    @Test
    fun `selecionados vazios nao filtram por aquele campo`() {
        val conteudos = listOf(
            sampleConteudo(id = 1L, eixo = 1, bloco = 1),
            sampleConteudo(id = 2L, eixo = 2, bloco = 2)
        )
        val state = ConcursoDetailState(conteudos = conteudos)

        assertThat(state.conteudosExibidos.map { it.id }).containsExactly(1L, 2L)
    }

    @Test
    fun `ordenacao por eixo crescente coloca nulos primeiro`() {
        val conteudos = listOf(
            sampleConteudo(id = 1L, eixo = 2),
            sampleConteudo(id = 2L, eixo = null),
            sampleConteudo(id = 3L, eixo = 1)
        )
        val state = ConcursoDetailState(conteudos = conteudos, conteudoOrdenacao = ConteudoOrdenacao.EIXO_ASC)

        assertThat(state.conteudosExibidos.map { it.id }).containsExactly(2L, 3L, 1L)
    }

    @Test
    fun `ordenacao por eixo decrescente coloca nulos por ultimo`() {
        val conteudos = listOf(
            sampleConteudo(id = 1L, eixo = 2),
            sampleConteudo(id = 2L, eixo = null),
            sampleConteudo(id = 3L, eixo = 1)
        )
        val state = ConcursoDetailState(conteudos = conteudos, conteudoOrdenacao = ConteudoOrdenacao.EIXO_DESC)

        assertThat(state.conteudosExibidos.map { it.id }).containsExactly(1L, 3L, 2L)
    }

    @Test
    fun `ordenacao por bloco crescente e decrescente`() {
        val conteudos = listOf(
            sampleConteudo(id = 1L, bloco = 3),
            sampleConteudo(id = 2L, bloco = 1),
            sampleConteudo(id = 3L, bloco = 2)
        )

        val asc = ConcursoDetailState(conteudos = conteudos, conteudoOrdenacao = ConteudoOrdenacao.BLOCO_ASC)
        val desc = ConcursoDetailState(conteudos = conteudos, conteudoOrdenacao = ConteudoOrdenacao.BLOCO_DESC)

        assertThat(asc.conteudosExibidos.map { it.id }).containsExactly(2L, 3L, 1L)
        assertThat(desc.conteudosExibidos.map { it.id }).containsExactly(1L, 3L, 2L)
    }

    @Test
    fun `ordenacao por data da ultima revisao crescente e decrescente`() {
        val conteudos = listOf(
            sampleConteudo(id = 1L, dataUltimaRevisaoMillis = 3_000L),
            sampleConteudo(id = 2L, dataUltimaRevisaoMillis = 1_000L),
            sampleConteudo(id = 3L, dataUltimaRevisaoMillis = 2_000L)
        )

        val asc = ConcursoDetailState(conteudos = conteudos, conteudoOrdenacao = ConteudoOrdenacao.REVISAO_ASC)
        val desc = ConcursoDetailState(conteudos = conteudos, conteudoOrdenacao = ConteudoOrdenacao.REVISAO_DESC)

        assertThat(asc.conteudosExibidos.map { it.id }).containsExactly(2L, 3L, 1L)
        assertThat(desc.conteudosExibidos.map { it.id }).containsExactly(1L, 3L, 2L)
    }

    @Test
    fun `ordenacao por tempo total de aulas usa quantidade vezes tempo medio`() {
        val conteudos = listOf(
            sampleConteudo(id = 1L, quantidadeAulas = 2, tempoMedioAulaMinutos = 30), // 60
            sampleConteudo(id = 2L, quantidadeAulas = 5, tempoMedioAulaMinutos = 10), // 50
            sampleConteudo(id = 3L, quantidadeAulas = 10, tempoMedioAulaMinutos = 10) // 100
        )

        val asc = ConcursoDetailState(conteudos = conteudos, conteudoOrdenacao = ConteudoOrdenacao.TEMPO_AULA_ASC)
        val desc = ConcursoDetailState(conteudos = conteudos, conteudoOrdenacao = ConteudoOrdenacao.TEMPO_AULA_DESC)

        assertThat(asc.conteudosExibidos.map { it.id }).containsExactly(2L, 1L, 3L)
        assertThat(desc.conteudosExibidos.map { it.id }).containsExactly(3L, 1L, 2L)
    }

    @Test
    fun `filtro e ordenacao combinados`() {
        val conteudos = listOf(
            sampleConteudo(id = 1L, eixo = 1, bloco = 3),
            sampleConteudo(id = 2L, eixo = 2, bloco = 1),
            sampleConteudo(id = 3L, eixo = 1, bloco = 1)
        )
        val state = ConcursoDetailState(
            conteudos = conteudos,
            conteudoFiltroEixosSelecionados = setOf(1),
            conteudoOrdenacao = ConteudoOrdenacao.BLOCO_ASC
        )

        assertThat(state.conteudosExibidos.map { it.id }).containsExactly(3L, 1L)
    }

    @Test
    fun `filtro por uma unica prioridade retorna so os conteudos daquela prioridade`() {
        val conteudos = listOf(
            sampleConteudo(id = 1L, prioridade = Prioridade.ALTA),
            sampleConteudo(id = 2L, prioridade = Prioridade.BAIXA),
            sampleConteudo(id = 3L, prioridade = Prioridade.ALTA)
        )
        val state = ConcursoDetailState(
            conteudos = conteudos,
            conteudoFiltroPrioridadesSelecionados = setOf(Prioridade.ALTA)
        )

        assertThat(state.conteudosExibidos.map { it.id }).containsExactly(1L, 3L)
    }

    @Test
    fun `ordenacao por prioridade crescente e decrescente`() {
        val conteudos = listOf(
            sampleConteudo(id = 1L, prioridade = Prioridade.BAIXA),
            sampleConteudo(id = 2L, prioridade = Prioridade.ALTA),
            sampleConteudo(id = 3L, prioridade = Prioridade.OPCIONAL)
        )

        val asc = ConcursoDetailState(conteudos = conteudos, conteudoOrdenacao = ConteudoOrdenacao.PRIORIDADE_ASC)
        val desc = ConcursoDetailState(conteudos = conteudos, conteudoOrdenacao = ConteudoOrdenacao.PRIORIDADE_DESC)

        assertThat(asc.conteudosExibidos.map { it.id }).containsExactly(2L, 1L, 3L)
        assertThat(desc.conteudosExibidos.map { it.id }).containsExactly(3L, 1L, 2L)
    }

    @Test
    fun `isConteudoFiltroAtivo reflete selecoes de eixo, bloco ou prioridade`() {
        val vazio = ConcursoDetailState()
        val comEixo = ConcursoDetailState(conteudoFiltroEixosSelecionados = setOf(1))
        val comBloco = ConcursoDetailState(conteudoFiltroBlocosSelecionados = setOf(1))
        val comPrioridade = ConcursoDetailState(conteudoFiltroPrioridadesSelecionados = setOf(Prioridade.ALTA))

        assertThat(vazio.isConteudoFiltroAtivo).isFalse()
        assertThat(comEixo.isConteudoFiltroAtivo).isTrue()
        assertThat(comBloco.isConteudoFiltroAtivo).isTrue()
        assertThat(comPrioridade.isConteudoFiltroAtivo).isTrue()
    }

    @Test
    fun `eixos e blocos disponiveis para filtro sao distintos e ordenados`() {
        val conteudos = listOf(
            sampleConteudo(id = 1L, eixo = 3, bloco = 2),
            sampleConteudo(id = 2L, eixo = 1, bloco = null),
            sampleConteudo(id = 3L, eixo = 1, bloco = 1)
        )
        val state = ConcursoDetailState(conteudos = conteudos)

        assertThat(state.eixosDisponiveisParaFiltro).isEqualTo(listOf(1, 3))
        assertThat(state.blocosDisponiveisParaFiltro).isEqualTo(listOf(1, 2))
    }

    @Test
    fun `sem agrupamento ha um unico grupo com todos os conteudos exibidos`() {
        val conteudos = listOf(sampleConteudo(id = 1L, eixo = 2), sampleConteudo(id = 2L, eixo = 1))
        val state = ConcursoDetailState(conteudos = conteudos)

        val grupos = state.conteudosAgrupados
        assertThat(grupos.map { it.numero }).isEqualTo(listOf(null))
        assertThat(grupos.single().itens.map { it.id }).isEqualTo(listOf(1L, 2L))
    }

    @Test
    fun `agrupar por eixo ordena os grupos e deixa sem eixo por ultimo`() {
        val conteudos = listOf(
            sampleConteudo(id = 1L, eixo = 2),
            sampleConteudo(id = 2L, eixo = null),
            sampleConteudo(id = 3L, eixo = 1),
            sampleConteudo(id = 4L, eixo = 1)
        )
        val state = ConcursoDetailState(conteudos = conteudos, conteudoAgrupamento = ConteudoAgrupamento.EIXO)

        val grupos = state.conteudosAgrupados
        assertThat(grupos.map { it.numero }).isEqualTo(listOf(1, 2, null))
        assertThat(grupos[0].itens.map { it.id }).isEqualTo(listOf(3L, 4L))
        assertThat(grupos[1].itens.map { it.id }).isEqualTo(listOf(1L))
        assertThat(grupos[2].itens.map { it.id }).isEqualTo(listOf(2L))
    }

    @Test
    fun `agrupar por bloco funciona como o agrupamento por eixo`() {
        val conteudos = listOf(sampleConteudo(id = 1L, bloco = 2), sampleConteudo(id = 2L, bloco = 1))
        val state = ConcursoDetailState(conteudos = conteudos, conteudoAgrupamento = ConteudoAgrupamento.BLOCO)

        val grupos = state.conteudosAgrupados
        assertThat(grupos.map { it.numero }).isEqualTo(listOf(1, 2))
        assertThat(grupos[0].itens.map { it.id }).isEqualTo(listOf(2L))
        assertThat(grupos[1].itens.map { it.id }).isEqualTo(listOf(1L))
    }

    @Test
    fun `agrupamento respeita filtro e ordenacao ja aplicados`() {
        val conteudos = listOf(
            sampleConteudo(id = 1L, eixo = 1, quantidadeAulas = 1, tempoMedioAulaMinutos = 10),
            sampleConteudo(id = 2L, eixo = 1, quantidadeAulas = 5, tempoMedioAulaMinutos = 10),
            sampleConteudo(id = 3L, eixo = 2, quantidadeAulas = 1, tempoMedioAulaMinutos = 1)
        )
        val state = ConcursoDetailState(
            conteudos = conteudos,
            conteudoFiltroEixosSelecionados = setOf(1),
            conteudoOrdenacao = ConteudoOrdenacao.TEMPO_AULA_DESC,
            conteudoAgrupamento = ConteudoAgrupamento.EIXO
        )

        val grupos = state.conteudosAgrupados
        assertThat(grupos.map { it.numero }).isEqualTo(listOf(1))
        assertThat(grupos.single().itens.map { it.id }).isEqualTo(listOf(2L, 1L))
    }

    @Test
    fun `agrupar por materia ordena alfabeticamente e deixa sem materia por ultimo`() {
        val conteudos = listOf(
            sampleConteudo(id = 1L, materia = "Português"),
            sampleConteudo(id = 2L, materia = ""),
            sampleConteudo(id = 3L, materia = "Direito"),
            sampleConteudo(id = 4L, materia = "Direito")
        )
        val state = ConcursoDetailState(conteudos = conteudos, conteudoAgrupamento = ConteudoAgrupamento.MATERIA)

        val grupos = state.conteudosAgrupados
        assertThat(grupos.map { it.materia }).isEqualTo(listOf("Direito", "Português", null))
        assertThat(grupos[0].itens.map { it.id }).isEqualTo(listOf(3L, 4L))
        assertThat(grupos[1].itens.map { it.id }).isEqualTo(listOf(1L))
        assertThat(grupos[2].itens.map { it.id }).isEqualTo(listOf(2L))
    }

    @Test
    fun `chave do grupo distingue sem eixo, sem bloco e sem materia entre si`() {
        val semEixo = ConteudoGrupo(numero = null, itens = emptyList())
        val semBloco = ConteudoGrupo(numero = null, itens = emptyList())
        val semMateria = ConteudoGrupo(materia = null, itens = emptyList())

        val chaveEixo = semEixo.chave(ConteudoAgrupamento.EIXO)
        val chaveBloco = semBloco.chave(ConteudoAgrupamento.BLOCO)
        val chaveMateria = semMateria.chave(ConteudoAgrupamento.MATERIA)

        assertThat(setOf(chaveEixo, chaveBloco, chaveMateria)).hasSize(3)
    }

    private fun sampleConteudo(
        id: Long,
        eixo: Int? = null,
        bloco: Int? = null,
        materia: String = "Matéria $id",
        quantidadeAulas: Int = 0,
        tempoMedioAulaMinutos: Int = 0,
        dataUltimaRevisaoMillis: Long? = null,
        prioridade: Prioridade = Prioridade.MEDIA
    ) = ConteudoUi(
        id = id,
        materia = materia,
        descricao = "Descrição $id",
        eixo = eixo,
        bloco = bloco,
        concluido = false,
        quantidadeAulas = quantidadeAulas,
        quantidadeQuestoes = 0,
        tempoMedioAulaMinutos = tempoMedioAulaMinutos,
        dataUltimaRevisaoFormatted = null,
        dataUltimaRevisaoMillis = dataUltimaRevisaoMillis,
        prioridade = prioridade
    )
}
