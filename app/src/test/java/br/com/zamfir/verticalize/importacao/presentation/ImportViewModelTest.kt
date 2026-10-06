package br.com.zamfir.verticalize.importacao.presentation

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import br.com.zamfir.verticalize.concurso.domain.Concurso
import br.com.zamfir.verticalize.concurso.domain.Nivel
import br.com.zamfir.verticalize.conteudo.domain.Conteudo
import br.com.zamfir.verticalize.importacao.data.FakeImportFileReader
import br.com.zamfir.verticalize.importacao.data.FakeImportRepository
import br.com.zamfir.verticalize.importacao.domain.ImportFileError
import br.com.zamfir.verticalize.importacao.domain.TxtSectionsParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class ImportViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var fileReader: FakeImportFileReader
    private lateinit var repository: FakeImportRepository

    private val arquivoValido = """
        [CONCURSO]
        nome: TRT 2ª Região
        nivel: superior
        data_prova: 15/03/2027

        [CONTEUDOS]
        Direito;Tema A;1
        Direito;Tema B;1
        Linha ruim;;1
    """.trimIndent()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(dispatcher)
        fileReader = FakeImportFileReader()
        repository = FakeImportRepository()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = ImportViewModel(
        fileUri = "content://arquivo.txt",
        fileReader = fileReader,
        parser = TxtSectionsParser(),
        repository = repository
    )

    @Test
    fun `arquivo valido carrega a previa com resumo concursos e linhas ignoradas`() {
        fileReader.text = arquivoValido

        val state = createViewModel().state.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.fileError).isNull()
        assertThat(state.resumo).isEqualTo(
            ImportResumoUi(concursosNovos = 1, concursosExistentes = 0, conteudosNovos = 2, conteudosDuplicados = 0)
        )
        assertThat(state.concursos).isEqualTo(
            listOf(ImportConcursoUi("TRT 2ª Região", Nivel.SUPERIOR, isNovo = true, conteudosNovos = 2, conteudosDuplicados = 0))
        )
        assertThat(state.errors).hasSize(1)
        assertThat(state.errors.single().linha).isEqualTo(9)
        assertThat(state.isConfirmEnabled).isTrue()
    }

    @Test
    fun `arquivo com estudos mostra os horarios no resumo e grava ao confirmar`() {
        fileReader.text = """
            [CONCURSO]
            nome: TRT 2ª Região
            nivel: superior
            data_prova: 15/03/2027
            [ESTUDOS]
            17/09/2026;08:00;10:00;Direito
            17/09/2026;14:00;15:00;Português
        """.trimIndent()
        val viewModel = createViewModel()

        val resumo = viewModel.state.value.resumo
        assertThat(resumo.estudosNovos).isEqualTo(2)
        assertThat(resumo.temEstudos).isTrue()
        assertThat(viewModel.state.value.concursos.single().estudosNovos).isEqualTo(2)

        viewModel.onAction(ImportAction.OnConfirmClick)

        assertThat(viewModel.state.value.resultado!!.estudosCriados).isEqualTo(2)
        assertThat(repository.planoAplicado!!.concursos.single().horasEstudadas).isEqualTo(3)
    }

    @Test
    fun `arquivo ilegivel mostra erro de arquivo`() {
        fileReader.error = ImportFileError.ARQUIVO_ILEGIVEL

        val state = createViewModel().state.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.fileError).isEqualTo(ImportFileError.ARQUIVO_ILEGIVEL)
        assertThat(state.isConfirmEnabled).isFalse()
    }

    @Test
    fun `arquivo em branco mostra erro de arquivo vazio`() {
        fileReader.text = "  \n \n"

        assertThat(createViewModel().state.value.fileError).isEqualTo(ImportFileError.ARQUIVO_VAZIO)
    }

    @Test
    fun `arquivo sem concurso valido mostra erro e mantem as linhas ignoradas`() {
        fileReader.text = "texto qualquer"

        val state = createViewModel().state.value

        assertThat(state.fileError).isEqualTo(ImportFileError.NENHUM_CONCURSO)
        assertThat(state.errors).isNotEmpty()
        assertThat(state.isConfirmEnabled).isFalse()
    }

    @Test
    fun `falha ao analisar no repositorio vira erro de arquivo`() {
        fileReader.text = arquivoValido
        repository.shouldFailAnalisar = true

        assertThat(createViewModel().state.value.fileError).isEqualTo(ImportFileError.FALHA_AO_ANALISAR)
    }

    @Test
    fun `itens ja cadastrados sao contados como existentes e duplicados`() {
        fileReader.text = arquivoValido
        val dataProva = LocalDate.of(2027, 3, 15).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        repository.concursosExistentes = listOf(
            Concurso(id = 9L, nome = "TRT 2ª Região", nivel = Nivel.SUPERIOR, dataProva = dataProva)
        )
        repository.conteudosExistentes = listOf(Conteudo(id = 1L, concursoId = 9L, materia = "Direito", descricao = "Tema A"))

        val state = createViewModel().state.value

        assertThat(state.resumo).isEqualTo(
            ImportResumoUi(concursosNovos = 0, concursosExistentes = 1, conteudosNovos = 1, conteudosDuplicados = 1)
        )
        assertThat(state.concursos.single().isNovo).isFalse()
        assertThat(state.isConfirmEnabled).isTrue()
    }

    @Test
    fun `confirmar grava o plano e mostra o resultado`() {
        fileReader.text = arquivoValido
        val viewModel = createViewModel()

        viewModel.onAction(ImportAction.OnConfirmClick)

        val state = viewModel.state.value
        assertThat(state.isImporting).isFalse()
        assertThat(state.resultado).isNotNull()
        assertThat(state.resultado!!.concursosCriados).isEqualTo(1)
        assertThat(state.resultado!!.conteudosCriados).isEqualTo(2)
        assertThat(repository.planoAplicado).isNotNull()
        assertThat(state.isConfirmEnabled).isFalse()
    }

    @Test
    fun `nao confirma quando tudo do arquivo ja esta cadastrado`() {
        fileReader.text = """
            [CONCURSO]
            nome: TRT 2ª Região
            nivel: superior
            data_prova: 15/03/2027
            [CONTEUDOS]
            Direito;Tema A
        """.trimIndent()
        val dataProva = LocalDate.of(2027, 3, 15).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        repository.concursosExistentes = listOf(
            Concurso(id = 9L, nome = "TRT 2ª Região", nivel = Nivel.SUPERIOR, dataProva = dataProva)
        )
        repository.conteudosExistentes = listOf(Conteudo(id = 1L, concursoId = 9L, materia = "Direito", descricao = "Tema A"))
        val viewModel = createViewModel()

        assertThat(viewModel.state.value.isConfirmEnabled).isFalse()
        viewModel.onAction(ImportAction.OnConfirmClick)

        assertThat(repository.planoAplicado).isNull()
        assertThat(viewModel.state.value.resultado).isNull()
    }

    @Test
    fun `falha ao gravar emite ShowError e nao mostra resultado`() = runTest {
        fileReader.text = arquivoValido
        repository.shouldFailAplicar = true
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(ImportAction.OnConfirmClick)
            assertThat(awaitItem()).isInstanceOf(ImportEvent.ShowError::class)
        }

        assertThat(viewModel.state.value.isImporting).isFalse()
        assertThat(viewModel.state.value.resultado).isNull()
        assertThat(repository.planoAplicado).isNull()
        assertThat(viewModel.state.value.errors).hasSize(1)
    }

    @Test
    fun `cancelar e concluir navegam de volta`() = runTest {
        fileReader.text = arquivoValido
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(ImportAction.OnCancelClick)
            assertThat(awaitItem()).isEqualTo(ImportEvent.NavigateBack)

            viewModel.onAction(ImportAction.OnDoneClick)
            assertThat(awaitItem()).isEqualTo(ImportEvent.NavigateBack)
        }
        assertThat(repository.planoAplicado).isNull()
    }
}
