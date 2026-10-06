package br.com.zamfir.verticalize.sync.data

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isTrue
import br.com.zamfir.verticalize.concurso.data.ConcursoEntity
import br.com.zamfir.verticalize.conteudo.data.ConteudoEntity
import br.com.zamfir.verticalize.registroestudo.data.RegistroEstudoEntity
import org.junit.jupiter.api.Test

class PlanoAplicacaoTest {

    @Test
    fun `identical device and cloud produce an empty plan`() {
        val dados = snapshot(concursos = listOf(concurso(1)), conteudos = listOf(conteudo(10, concursoId = 1)))

        assertThat(planejarAplicacao(dados, dados, emptySet()).isVazio).isTrue()
    }

    @Test
    fun `records created or changed on another device are written locally`() {
        val local = snapshot(concursos = listOf(concurso(1, nome = "Antigo")))
        val remoto = snapshot(
            concursos = listOf(concurso(1, nome = "Novo"), concurso(2)),
            estudos = listOf(estudo(30, concursoId = 2))
        )

        val plano = planejarAplicacao(local, remoto, emptySet())

        assertThat(plano.concursosGravar).containsExactly(concurso(1, nome = "Novo"), concurso(2))
        assertThat(plano.estudosGravar).containsExactly(estudo(30, concursoId = 2))
        assertThat(plano.concursosExcluir).isEmpty()
    }

    @Test
    fun `records missing from the cloud are deleted locally`() {
        val local = snapshot(
            concursos = listOf(concurso(1), concurso(2)),
            conteudos = listOf(conteudo(10, concursoId = 2))
        )
        val remoto = snapshot(concursos = listOf(concurso(1)))

        val plano = planejarAplicacao(local, remoto, emptySet())

        assertThat(plano.concursosExcluir).containsExactly(2L)
        assertThat(plano.conteudosExcluir).containsExactly(10L)
    }

    @Test
    fun `records with local changes not yet sent are left untouched`() {
        val local = snapshot(concursos = listOf(concurso(1, nome = "Editado aqui"), concurso(2)))
        val remoto = snapshot(concursos = listOf(concurso(1, nome = "Versão da nuvem")))
        val pendentes = setOf(RegistroRef(TABELA_CONCURSOS, 1), RegistroRef(TABELA_CONCURSOS, 2))

        assertThat(planejarAplicacao(local, remoto, pendentes).isVazio).isTrue()
    }

    @Test
    fun `children of a concurso that will not exist locally are skipped`() {
        // Concurso 1 foi excluído aqui (pendente); outro aparelho acabou de criar um conteúdo nele.
        val local = snapshot()
        val remoto = snapshot(concursos = listOf(concurso(1)), conteudos = listOf(conteudo(10, concursoId = 1)))
        val pendentes = setOf(RegistroRef(TABELA_CONCURSOS, 1))

        val plano = planejarAplicacao(local, remoto, pendentes)

        assertThat(plano.concursosGravar).isEmpty()
        assertThat(plano.conteudosGravar).isEmpty()
    }

    private fun snapshot(
        concursos: List<ConcursoEntity> = emptyList(),
        conteudos: List<ConteudoEntity> = emptyList(),
        estudos: List<RegistroEstudoEntity> = emptyList()
    ) = BackupSnapshot(concursos, conteudos, estudos)

    private fun concurso(id: Long, nome: String = "Concurso $id") =
        ConcursoEntity(id = id, nome = nome, nivel = "MEDIO")

    private fun conteudo(id: Long, concursoId: Long) = ConteudoEntity(
        id = id,
        concursoId = concursoId,
        descricao = "Conteúdo $id",
        eixo = null,
        bloco = null,
        dataUltimaRevisao = null
    )

    private fun estudo(id: Long, concursoId: Long) = RegistroEstudoEntity(
        id = id,
        concursoId = concursoId,
        materia = "Português",
        data = 0L,
        horaInicioMinutos = 600,
        horaFimMinutos = 660
    )
}
