package br.com.zamfir.verticalize.importacao.domain

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import br.com.zamfir.verticalize.concurso.domain.Nivel
import br.com.zamfir.verticalize.core.domain.Result
import br.com.zamfir.verticalize.importacao.data.FakeExportFileWriter
import br.com.zamfir.verticalize.importacao.data.FakeExportRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneOffset

class ConcursoExporterTest {

    private lateinit var repository: FakeExportRepository
    private lateinit var fileWriter: FakeExportFileWriter
    private lateinit var exporter: ConcursoExporter

    private val formatter = TxtSectionsFormatter()

    // As datas do app vêm do seletor de data, sempre em meia-noite UTC; só assim a ida e volta é exata.
    private fun meiaNoiteUtc(ano: Int, mes: Int, dia: Int): Long =
        LocalDate.of(ano, mes, dia).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    private val concursos = listOf(
        ImportedConcurso(
            nome = "TRT 2ª Região",
            nivel = Nivel.SUPERIOR,
            dataProva = meiaNoiteUtc(2027, 3, 15),
            conteudos = listOf(ImportedConteudo(descricao = "A"), ImportedConteudo(descricao = "B"))
        ),
        ImportedConcurso(nome = "Banco do Brasil", nivel = Nivel.MEDIO, dataProva = meiaNoiteUtc(2027, 6, 20))
    )

    @BeforeEach
    fun setup() {
        repository = FakeExportRepository()
        fileWriter = FakeExportFileWriter()
        exporter = ConcursoExporter(repository, formatter, fileWriter)
    }

    @Test
    fun `exporta gravando o texto formatado no arquivo escolhido e resume as contagens`() = runTest {
        repository.concursos = concursos

        val resultado = exporter.exportar("content://destino.txt")

        assertThat(resultado).isEqualTo(Result.Success(ExportSummary(concursos = 2, conteudos = 2)))
        assertThat(fileWriter.lastUri).isEqualTo("content://destino.txt")
        assertThat(fileWriter.lastText).isEqualTo(formatter.format(concursos))
        assertThat(fileWriter.lastText!!).contains("[CONCURSO]")
    }

    @Test
    fun `o resumo e o arquivo incluem os horarios de estudo`() = runTest {
        val comEstudos = concursos[0].copy(
            estudos = listOf(
                ImportedEstudo("Direito", meiaNoiteUtc(2026, 9, 17), 8 * 60, 10 * 60),
                ImportedEstudo("Português", meiaNoiteUtc(2026, 9, 18), 14 * 60, 15 * 60)
            )
        )
        repository.concursos = listOf(comEstudos, concursos[1])

        val resultado = exporter.exportar("content://destino.txt")

        assertThat(resultado).isEqualTo(Result.Success(ExportSummary(concursos = 2, conteudos = 2, estudos = 2)))
        assertThat(fileWriter.lastText!!).contains("[ESTUDOS]")
        assertThat(fileWriter.lastText!!).contains("17/09/2026;08:00;10:00;Direito")
    }

    @Test
    fun `sem concursos nao grava nada`() = runTest {
        val resultado = exporter.exportar("content://destino.txt")

        assertThat(resultado).isEqualTo(Result.Error(ExportError.SEM_DADOS))
        assertThat(fileWriter.lastText).isNull()
    }

    @Test
    fun `falha ao ler os dados nao grava nada`() = runTest {
        repository.concursos = concursos
        repository.shouldReturnError = true

        val resultado = exporter.exportar("content://destino.txt")

        assertThat(resultado).isEqualTo(Result.Error(ExportError.FALHA_AO_LER))
        assertThat(fileWriter.lastText).isNull()
    }

    @Test
    fun `falha ao gravar o arquivo devolve o erro de gravacao`() = runTest {
        repository.concursos = concursos
        fileWriter.shouldFail = true

        val resultado = exporter.exportar("content://destino.txt")

        assertThat(resultado).isEqualTo(Result.Error(ExportError.FALHA_AO_GRAVAR))
    }

    @Test
    fun `exportar um concurso grava so ele no arquivo`() = runTest {
        repository.concursosPorId = mapOf(7L to concursos[0], 8L to concursos[1])

        val resultado = exporter.exportarConcurso(7L, "content://destino.txt")

        assertThat(resultado).isEqualTo(Result.Success(ExportSummary(concursos = 1, conteudos = 2)))
        assertThat(fileWriter.lastUri).isEqualTo("content://destino.txt")
        assertThat(fileWriter.lastText).isEqualTo(formatter.format(listOf(concursos[0])))
        assertThat(fileWriter.lastText!!).doesNotContain("Banco do Brasil")
    }

    @Test
    fun `exportar um concurso que nao existe mais devolve erro de leitura e nao grava`() = runTest {
        repository.concursosPorId = mapOf(7L to concursos[0])

        val resultado = exporter.exportarConcurso(99L, "content://destino.txt")

        assertThat(resultado).isEqualTo(Result.Error(ExportError.FALHA_AO_LER))
        assertThat(fileWriter.lastText).isNull()
    }

    @Test
    fun `falha ao gravar ao exportar um concurso devolve o erro de gravacao`() = runTest {
        repository.concursosPorId = mapOf(7L to concursos[0])
        fileWriter.shouldFail = true

        val resultado = exporter.exportarConcurso(7L, "content://destino.txt")

        assertThat(resultado).isEqualTo(Result.Error(ExportError.FALHA_AO_GRAVAR))
    }

    @Test
    fun `o arquivo de um concurso exportado e importado de volta pelo parser`() = runTest {
        repository.concursosPorId = mapOf(7L to concursos[0])

        exporter.exportarConcurso(7L, "content://destino.txt")

        val parsed = TxtSectionsParser().parse(fileWriter.lastText!!)
        assertThat(parsed.errors).isEmpty()
        assertThat(parsed.concursos).isEqualTo(listOf(concursos[0]))
    }
}
