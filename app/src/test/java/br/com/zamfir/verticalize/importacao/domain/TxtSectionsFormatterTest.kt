package br.com.zamfir.verticalize.importacao.domain

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import br.com.zamfir.verticalize.concurso.domain.Nivel
import br.com.zamfir.verticalize.conteudo.domain.Prioridade
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneOffset

class TxtSectionsFormatterTest {

    private val formatter = TxtSectionsFormatter()
    private val parser = TxtSectionsParser()

    private fun millis(ano: Int, mes: Int, dia: Int): Long =
        LocalDate.of(ano, mes, dia).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    private val cabecalho = "# Exportado pelo Verticalize. Este arquivo pode ser importado de volta pelo app."

    @Test
    fun `formata um concurso completo no formato de importacao`() {
        val texto = formatter.format(
            listOf(
                ImportedConcurso(
                    nome = "TRT 2ª Região",
                    nivel = Nivel.SUPERIOR,
                    dataProva = millis(2027, 3, 15),
                    valorInscricaoCentavos = 12050L,
                    banca = "FCC",
                    conteudos = listOf(
                        ImportedConteudo(
                            materia = "Direito Constitucional",
                            descricao = "Princípios",
                            eixo = 1,
                            bloco = 1,
                            quantidadeAulas = 5,
                            tempoMedioAulaMinutos = 50
                        ),
                        ImportedConteudo(
                            descricao = "Sem matéria",
                            concluido = true,
                            dataUltimaRevisao = millis(2026, 9, 10),
                            quantidadeQuestoesRealizadas = 30
                        )
                    )
                )
            )
        )

        val esperado = listOf(
            cabecalho,
            "",
            "[CONCURSO]",
            "nome: TRT 2ª Região",
            "nivel: superior",
            "data_prova: 15/03/2027",
            "banca: FCC",
            "valor_inscricao: 120,50",
            "",
            "[CONTEUDOS]",
            "materia;descricao;eixo;bloco;aulas;min_por_aula;concluido;ultima_revisao;questoes;prioridade",
            "Direito Constitucional;Princípios;1;1;5;50;nao;;0;media",
            ";Sem matéria;;;0;0;sim;10/09/2026;30;media"
        ).joinToString("\n") + "\n"

        assertThat(texto).isEqualTo(esperado)
    }

    @Test
    fun `omite banca valor e secao de conteudos quando nao ha o que escrever`() {
        val texto = formatter.format(
            listOf(ImportedConcurso(nome = "Concurso A", nivel = Nivel.MEDIO, dataProva = millis(2027, 1, 2)))
        )

        assertThat(texto).doesNotContain("banca:")
        assertThat(texto).doesNotContain("valor_inscricao:")
        assertThat(texto).doesNotContain("[CONTEUDOS]")
        assertThat(texto).contains("nivel: medio")
    }

    @Test
    fun `lista vazia gera so o comentario de cabecalho`() {
        assertThat(formatter.format(emptyList())).isEqualTo("$cabecalho\n")
    }

    @Test
    fun `valor em centavos vira reais com duas casas`() {
        fun valorExportado(centavos: Long): String {
            val texto = formatter.format(
                listOf(
                    ImportedConcurso(
                        nome = "A",
                        nivel = Nivel.MEDIO,
                        dataProva = millis(2027, 1, 2),
                        valorInscricaoCentavos = centavos
                    )
                )
            )
            return texto.lines().first { it.startsWith("valor_inscricao:") }
        }

        assertThat(valorExportado(5L)).isEqualTo("valor_inscricao: 0,05")
        assertThat(valorExportado(100_000L)).isEqualTo("valor_inscricao: 1000,00")
        assertThat(valorExportado(120_050L)).isEqualTo("valor_inscricao: 1200,50")
    }

    @Test
    fun `ponto e virgula e quebras de linha nos textos nao quebram o formato`() {
        val texto = formatter.format(
            listOf(
                ImportedConcurso(
                    nome = "Edital A;B\nSegunda linha",
                    nivel = Nivel.MEDIO,
                    dataProva = millis(2027, 1, 2),
                    conteudos = listOf(
                        ImportedConteudo(materia = "#Tópico", descricao = "Item 1; item 2\r\nfim")
                    )
                )
            )
        )

        assertThat(texto).contains("nome: Edital A,B Segunda linha\n")
        assertThat(texto).contains("\nTópico;Item 1, item 2 fim;;;0;0;nao;;0;media\n")
    }

    @Test
    fun `escreve o bloco de estudos em ordem cronologica com horas hh mm`() {
        val texto = formatter.format(
            listOf(
                ImportedConcurso(
                    nome = "Concurso A",
                    nivel = Nivel.MEDIO,
                    dataProva = millis(2027, 1, 2),
                    estudos = listOf(
                        ImportedEstudo("Português", millis(2026, 9, 18), 9 * 60 + 5, 10 * 60),
                        ImportedEstudo("Direito; Penal", millis(2026, 9, 17), 14 * 60, 15 * 60 + 30),
                        ImportedEstudo("Informática", millis(2026, 9, 17), 8 * 60, 9 * 60)
                    )
                )
            )
        )

        val esperado = listOf(
            cabecalho,
            "",
            "[CONCURSO]",
            "nome: Concurso A",
            "nivel: medio",
            "data_prova: 02/01/2027",
            "",
            "[ESTUDOS]",
            "data;hora_inicio;hora_fim;materia",
            "17/09/2026;08:00;09:00;Informática",
            "17/09/2026;14:00;15:30;Direito, Penal",
            "18/09/2026;09:05;10:00;Português"
        ).joinToString("\n") + "\n"

        assertThat(texto).isEqualTo(esperado)
    }

    @Test
    fun `omite o bloco de estudos quando nao ha horarios`() {
        val texto = formatter.format(
            listOf(ImportedConcurso(nome = "A", nivel = Nivel.MEDIO, dataProva = millis(2027, 1, 2)))
        )

        assertThat(texto).doesNotContain("[ESTUDOS]")
    }

    @Test
    fun `conteudos e estudos saem no mesmo concurso e voltam sem perdas`() {
        val original = listOf(
            ImportedConcurso(
                nome = "TRT 2ª Região",
                nivel = Nivel.SUPERIOR,
                dataProva = millis(2027, 3, 15),
                conteudos = listOf(ImportedConteudo(materia = "Direito", descricao = "Tema A", eixo = 1)),
                estudos = listOf(
                    ImportedEstudo("Direito", millis(2026, 9, 17), 8 * 60, 10 * 60 + 30),
                    ImportedEstudo("Português", millis(2026, 9, 18), 0, 1439)
                )
            ),
            ImportedConcurso(
                nome = "Só horários",
                nivel = Nivel.MEDIO,
                dataProva = millis(2027, 5, 1),
                estudos = listOf(ImportedEstudo("Matemática", millis(2026, 10, 1), 19 * 60, 20 * 60))
            )
        )

        val parsed = parser.parse(formatter.format(original))

        assertThat(parsed.errors).isEmpty()
        assertThat(parsed.concursos).isEqualTo(original)
    }

    @Test
    fun `o arquivo exportado e importado de volta sem perdas`() {
        val original = listOf(
            ImportedConcurso(
                nome = "TRT 2ª Região - Analista: Judiciário",
                nivel = Nivel.SUPERIOR,
                dataProva = millis(2027, 3, 15),
                valorInscricaoCentavos = 12050L,
                banca = "FCC",
                conteudos = listOf(
                    ImportedConteudo(
                        materia = "Direito Constitucional",
                        descricao = "Princípios fundamentais",
                        eixo = 1,
                        bloco = 2,
                        quantidadeAulas = 5,
                        tempoMedioAulaMinutos = 50,
                        concluido = true,
                        dataUltimaRevisao = millis(2026, 9, 10),
                        quantidadeQuestoesRealizadas = 30
                    ),
                    ImportedConteudo(descricao = "Só a descrição")
                )
            ),
            ImportedConcurso(nome = "Sem conteúdos", nivel = Nivel.MEDIO, dataProva = millis(2027, 12, 31)),
            ImportedConcurso(
                nome = "Outro",
                nivel = Nivel.MEDIO,
                dataProva = millis(2028, 2, 29),
                conteudos = listOf(ImportedConteudo(materia = "Português", descricao = "Crase", eixo = 3))
            )
        )

        val parsed = parser.parse(formatter.format(original))

        assertThat(parsed.errors).isEmpty()
        assertThat(parsed.concursos).isEqualTo(original)
    }

    @Test
    fun `exporta a prioridade de cada conteudo`() {
        val texto = formatter.format(
            listOf(
                ImportedConcurso(
                    nome = "A",
                    nivel = Nivel.MEDIO,
                    dataProva = millis(2027, 1, 2),
                    conteudos = listOf(
                        ImportedConteudo(descricao = "Alta", prioridade = Prioridade.ALTA),
                        ImportedConteudo(descricao = "Baixa", prioridade = Prioridade.BAIXA),
                        ImportedConteudo(descricao = "Opcional", prioridade = Prioridade.OPCIONAL)
                    )
                )
            )
        )

        assertThat(texto).contains(";Alta;;;0;0;nao;;0;alta\n")
        assertThat(texto).contains(";Baixa;;;0;0;nao;;0;baixa\n")
        assertThat(texto).contains(";Opcional;;;0;0;nao;;0;opcional\n")
    }

    @Test
    fun `omite a data da prova quando o concurso nao tem uma, e volta null ao reimportar`() {
        val original = ImportedConcurso(nome = "Sem data", nivel = Nivel.MEDIO, dataProva = null)

        val texto = formatter.format(listOf(original))
        assertThat(texto).doesNotContain("data_prova:")

        val parsed = parser.parse(texto)
        assertThat(parsed.errors).isEmpty()
        assertThat(parsed.concursos).isEqualTo(listOf(original))
    }
}
