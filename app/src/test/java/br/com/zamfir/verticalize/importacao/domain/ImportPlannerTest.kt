package br.com.zamfir.verticalize.importacao.domain

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import br.com.zamfir.verticalize.concurso.domain.Concurso
import br.com.zamfir.verticalize.concurso.domain.Nivel
import br.com.zamfir.verticalize.conteudo.domain.Conteudo
import br.com.zamfir.verticalize.registroestudo.domain.RegistroEstudo
import org.junit.jupiter.api.Test

class ImportPlannerTest {

    private val planner = ImportPlanner()

    private val dataProva = 1_800_000_000_000L

    private fun importado(
        nome: String = "TRT 2ª Região",
        data: Long = dataProva,
        conteudos: List<ImportedConteudo> = emptyList(),
        estudos: List<ImportedEstudo> = emptyList()
    ) = ImportedConcurso(nome = nome, nivel = Nivel.SUPERIOR, dataProva = data, conteudos = conteudos, estudos = estudos)

    private fun estudo(materia: String = "Direito", data: Long = 1_700_000_000_000L, inicio: Int, fim: Int) =
        ImportedEstudo(materia = materia, data = data, horaInicioMinutos = inicio, horaFimMinutos = fim)

    private fun conteudo(materia: String = "Direito", descricao: String, eixo: Int? = null, concluido: Boolean = false) =
        ImportedConteudo(materia = materia, descricao = descricao, eixo = eixo, concluido = concluido)

    private fun parsed(vararg concursos: ImportedConcurso) = ParsedImport(concursos.toList(), emptyList())

    @Test
    fun `concurso inexistente e todos os conteudos sao novos`() {
        val plano = planner.plan(
            parsed(importado(conteudos = listOf(conteudo(descricao = "A"), conteudo(descricao = "B")))),
            concursosExistentes = emptyList(),
            conteudosExistentes = emptyList()
        )

        val concurso = plano.concursos.single()
        assertThat(concurso.isNovo).isTrue()
        assertThat(concurso.existingConcursoId).isNull()
        assertThat(concurso.novosConteudos).hasSize(2)
        assertThat(concurso.conteudosDuplicados).isEqualTo(0)
        assertThat(plano.concursosNovos).isEqualTo(1)
        assertThat(plano.conteudosNovos).isEqualTo(2)
        assertThat(plano.temNovidades).isTrue()
    }

    @Test
    fun `concurso existente e reaproveitado sem alterar seus dados e recebe so os conteudos novos`() {
        // Nome, nível e banca diferentes do arquivo: só nome (sem caixa/acento) + data da prova identificam o concurso.
        val existente = Concurso(id = 5L, nome = "trt 2ª regiao", nivel = Nivel.MEDIO, dataProva = dataProva, banca = "Outra")
        val jaCadastrado = Conteudo(id = 1L, concursoId = 5L, materia = "DIREITO", descricao = "Tema  A")

        val plano = planner.plan(
            parsed(importado(conteudos = listOf(conteudo(descricao = "tema a"), conteudo(descricao = "Tema B")))),
            concursosExistentes = listOf(existente),
            conteudosExistentes = listOf(jaCadastrado)
        )

        val concurso = plano.concursos.single()
        assertThat(concurso.isNovo).isFalse()
        assertThat(concurso.existingConcursoId).isEqualTo(5L)
        assertThat(concurso.novosConteudos.map { it.descricao }).isEqualTo(listOf("Tema B"))
        assertThat(concurso.conteudosDuplicados).isEqualTo(1)
        assertThat(plano.concursosNovos).isEqualTo(0)
        assertThat(plano.concursosExistentes).isEqualTo(1)
    }

    @Test
    fun `mesmo nome com outra data de prova e outro concurso`() {
        val existente = Concurso(id = 5L, nome = "TRT 2ª Região", nivel = Nivel.SUPERIOR, dataProva = dataProva)

        val plano = planner.plan(
            parsed(importado(data = dataProva + 86_400_000L)),
            concursosExistentes = listOf(existente),
            conteudosExistentes = emptyList()
        )

        assertThat(plano.concursos.single().isNovo).isTrue()
    }

    @Test
    fun `duplicatas dentro do proprio arquivo sao ignoradas`() {
        val plano = planner.plan(
            parsed(
                importado(conteudos = listOf(conteudo(descricao = "A"), conteudo(descricao = "a"))),
                importado(nome = "TRT 2ª REGIÃO", conteudos = listOf(conteudo(descricao = "A"), conteudo(descricao = "C")))
            ),
            concursosExistentes = emptyList(),
            conteudosExistentes = emptyList()
        )

        val concurso = plano.concursos.single()
        assertThat(concurso.novosConteudos.map { it.descricao }).isEqualTo(listOf("A", "C"))
        assertThat(concurso.conteudosDuplicados).isEqualTo(2)
    }

    @Test
    fun `nada de novo quando tudo ja esta cadastrado`() {
        val existente = Concurso(id = 5L, nome = "TRT 2ª Região", nivel = Nivel.SUPERIOR, dataProva = dataProva)
        val jaCadastrado = Conteudo(id = 1L, concursoId = 5L, materia = "Direito", descricao = "A")

        val plano = planner.plan(
            parsed(importado(conteudos = listOf(conteudo(descricao = "A")))),
            concursosExistentes = listOf(existente),
            conteudosExistentes = listOf(jaCadastrado)
        )

        assertThat(plano.temNovidades).isFalse()
        assertThat(plano.conteudosDuplicados).isEqualTo(1)
    }

    @Test
    fun `percentual usa a mesma formula do detalhe - media dos percentuais por eixo`() {
        val plano = planner.plan(
            parsed(
                importado(
                    conteudos = listOf(
                        conteudo(descricao = "A", eixo = 1, concluido = true),
                        conteudo(descricao = "B", eixo = 1, concluido = false),
                        conteudo(descricao = "C", eixo = 2, concluido = true),
                        conteudo(descricao = "D", eixo = null, concluido = true)
                    )
                )
            ),
            concursosExistentes = emptyList(),
            conteudosExistentes = emptyList()
        )

        // eixo 1 = 50%, eixo 2 = 100%, conteúdo sem eixo não conta -> (50 + 100) / 2 = 75
        assertThat(plano.concursos.single().percentualCompletude).isEqualTo(75)
    }

    @Test
    fun `percentual de concurso existente considera os conteudos ja cadastrados e os novos`() {
        val existente = Concurso(id = 5L, nome = "TRT 2ª Região", nivel = Nivel.SUPERIOR, dataProva = dataProva)
        val jaCadastrado = Conteudo(id = 1L, concursoId = 5L, descricao = "A", eixo = 1, concluido = true)

        val plano = planner.plan(
            parsed(importado(conteudos = listOf(conteudo(descricao = "B", eixo = 1, concluido = false)))),
            concursosExistentes = listOf(existente),
            conteudosExistentes = listOf(jaCadastrado)
        )

        assertThat(plano.concursos.single().percentualCompletude).isEqualTo(50)
    }

    @Test
    fun `horarios de estudo de concurso novo sao todos novos e somam as horas estudadas`() {
        val plano = planner.plan(
            parsed(
                importado(
                    estudos = listOf(
                        estudo(inicio = 8 * 60, fim = 10 * 60 + 30),
                        estudo(materia = "Português", inicio = 14 * 60, fim = 15 * 60 + 30)
                    )
                )
            ),
            concursosExistentes = emptyList(),
            conteudosExistentes = emptyList()
        )

        val concurso = plano.concursos.single()
        assertThat(concurso.novosEstudos).hasSize(2)
        assertThat(concurso.estudosDuplicados).isEqualTo(0)
        assertThat(concurso.horasEstudadas).isEqualTo(4) // 150 + 90 = 240 min
        assertThat(plano.estudosNovos).isEqualTo(2)
        assertThat(plano.temNovidades).isTrue()
    }

    @Test
    fun `horario de estudo ja cadastrado e ignorado e as horas somam os existentes e os novos`() {
        val existente = Concurso(id = 5L, nome = "TRT 2ª Região", nivel = Nivel.SUPERIOR, dataProva = dataProva)
        val jaCadastrado = RegistroEstudo(
            id = 1L,
            concursoId = 5L,
            materia = "DIREITO",
            data = 1_700_000_000_000L,
            horaInicioMinutos = 8 * 60,
            horaFimMinutos = 10 * 60
        )

        val plano = planner.plan(
            parsed(
                importado(
                    estudos = listOf(
                        estudo(materia = "direito", inicio = 8 * 60, fim = 10 * 60),
                        estudo(materia = "Direito", inicio = 14 * 60, fim = 16 * 60)
                    )
                )
            ),
            concursosExistentes = listOf(existente),
            conteudosExistentes = emptyList(),
            estudosExistentes = listOf(jaCadastrado)
        )

        val concurso = plano.concursos.single()
        assertThat(concurso.novosEstudos.map { it.horaInicioMinutos }).isEqualTo(listOf(14 * 60))
        assertThat(concurso.estudosDuplicados).isEqualTo(1)
        assertThat(concurso.horasEstudadas).isEqualTo(4) // 120 existentes + 120 novos
    }

    @Test
    fun `mesmo horario em outro dia ou com outra materia nao e duplicado`() {
        val plano = planner.plan(
            parsed(
                importado(
                    estudos = listOf(
                        estudo(data = 1_700_000_000_000L, inicio = 480, fim = 600),
                        estudo(data = 1_700_086_400_000L, inicio = 480, fim = 600),
                        estudo(materia = "Português", data = 1_700_000_000_000L, inicio = 480, fim = 600),
                        estudo(data = 1_700_000_000_000L, inicio = 480, fim = 600)
                    )
                )
            ),
            concursosExistentes = emptyList(),
            conteudosExistentes = emptyList()
        )

        assertThat(plano.concursos.single().novosEstudos).hasSize(3)
        assertThat(plano.estudosDuplicados).isEqualTo(1)
    }

    @Test
    fun `so ha novidades quando existe algo novo incluindo apenas horarios`() {
        val existente = Concurso(id = 5L, nome = "TRT 2ª Região", nivel = Nivel.SUPERIOR, dataProva = dataProva)

        val plano = planner.plan(
            parsed(importado(estudos = listOf(estudo(inicio = 480, fim = 600)))),
            concursosExistentes = listOf(existente),
            conteudosExistentes = emptyList()
        )

        assertThat(plano.concursosNovos).isEqualTo(0)
        assertThat(plano.conteudosNovos).isEqualTo(0)
        assertThat(plano.estudosNovos).isEqualTo(1)
        assertThat(plano.temNovidades).isTrue()
    }

}
