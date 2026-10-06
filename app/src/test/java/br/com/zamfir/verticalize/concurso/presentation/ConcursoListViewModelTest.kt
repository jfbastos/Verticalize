package br.com.zamfir.verticalize.concurso.presentation

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import br.com.zamfir.verticalize.auth.data.FakeAuthRepository
import br.com.zamfir.verticalize.auth.domain.AuthError
import br.com.zamfir.verticalize.auth.domain.AuthUser
import br.com.zamfir.verticalize.auth.presentation.ContaUi
import br.com.zamfir.verticalize.concurso.data.FakeConcursoRepository
import br.com.zamfir.verticalize.concurso.domain.Concurso
import br.com.zamfir.verticalize.concurso.domain.Nivel
import br.com.zamfir.verticalize.importacao.data.FakeExportFileWriter
import br.com.zamfir.verticalize.importacao.data.FakeExportRepository
import br.com.zamfir.verticalize.importacao.domain.ConcursoExporter
import br.com.zamfir.verticalize.importacao.domain.ImportedConcurso
import br.com.zamfir.verticalize.importacao.domain.TxtSectionsFormatter
import br.com.zamfir.verticalize.core.domain.Result
import br.com.zamfir.verticalize.sync.data.FakeCloudSyncRepository
import br.com.zamfir.verticalize.sync.domain.AcaoSyncInicial
import br.com.zamfir.verticalize.sync.domain.EscolhaConflito
import br.com.zamfir.verticalize.sync.domain.ResultadoSyncInicial
import br.com.zamfir.verticalize.sync.domain.ResumoDados
import br.com.zamfir.verticalize.sync.domain.SyncError
import br.com.zamfir.verticalize.sync.presentation.ConflitoSyncUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConcursoListViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeConcursoRepository
    private lateinit var exportRepository: FakeExportRepository
    private lateinit var exportFileWriter: FakeExportFileWriter
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var cloudSync: FakeCloudSyncRepository
    private lateinit var viewModel: ConcursoListViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repository = FakeConcursoRepository()
        exportRepository = FakeExportRepository()
        exportFileWriter = FakeExportFileWriter()
        authRepository = FakeAuthRepository()
        cloudSync = FakeCloudSyncRepository()
        viewModel = ConcursoListViewModel(
            repository = repository,
            exporter = ConcursoExporter(exportRepository, TxtSectionsFormatter(), exportFileWriter),
            authRepository = authRepository,
            cloudSync = cloudSync
        )
    }

    @Test
    fun `logging in prepares cloud sync for the user`() {
        authRepository.setCurrentUser(sampleUser())

        assertThat(cloudSync.prepararChamadas).isEqualTo(1)
        assertThat(cloudSync.isSincronizacaoAtiva("uid-1")).isTrue()
        assertThat(viewModel.state.value.isSincronizando).isFalse()
    }

    @Test
    fun `sync is not prepared again when already active for the user`() {
        cloudSync.uidAtivo = "uid-1"

        authRepository.setCurrentUser(sampleUser())

        assertThat(cloudSync.prepararChamadas).isEqualTo(0)
    }

    @Test
    fun `downloading data from the cloud on login shows a message`() = runTest {
        cloudSync.resultadoPreparar = Result.Success(ResultadoSyncInicial.Concluida(AcaoSyncInicial.BAIXOU_DA_NUVEM))

        viewModel.events.test {
            authRepository.setCurrentUser(sampleUser())

            assertThat(awaitItem()).isInstanceOf(ConcursoListEvent.ShowMessage::class)
        }
    }

    @Test
    fun `sync failure on login shows an error and keeps sync inactive`() = runTest {
        cloudSync.resultadoPreparar = Result.Error(SyncError.FALHA_NUVEM)

        viewModel.events.test {
            authRepository.setCurrentUser(sampleUser())

            assertThat(awaitItem()).isInstanceOf(ConcursoListEvent.ShowError::class)
            assertThat(cloudSync.isSincronizacaoAtiva("uid-1")).isFalse()
        }
    }

    @Test
    fun `different data on device and cloud shows the conflict dialog`() {
        val aparelho = ResumoDados(concursos = 1, conteudos = 2, estudos = 0)
        val nuvem = ResumoDados(concursos = 3, conteudos = 5, estudos = 1, atualizadoEm = 1_000L)
        cloudSync.resultadoPreparar = Result.Success(ResultadoSyncInicial.Conflito(aparelho, nuvem))

        authRepository.setCurrentUser(sampleUser())

        assertThat(viewModel.state.value.conflitoSync).isEqualTo(ConflitoSyncUi(aparelho, nuvem))
    }

    @Test
    fun `choosing cloud data in the conflict resolves it and closes the dialog`() = runTest {
        cloudSync.resultadoPreparar = Result.Success(
            ResultadoSyncInicial.Conflito(ResumoDados(1, 0, 0), ResumoDados(2, 0, 0))
        )
        authRepository.setCurrentUser(sampleUser())

        viewModel.events.test {
            viewModel.onAction(ConcursoListAction.OnSyncConflitoUsarNuvem)

            assertThat(awaitItem()).isInstanceOf(ConcursoListEvent.ShowMessage::class)
            assertThat(cloudSync.ultimaEscolha).isEqualTo(EscolhaConflito.USAR_NUVEM)
            assertThat(viewModel.state.value.conflitoSync).isNull()
        }
    }

    @Test
    fun `signing out disables cloud sync`() = runTest {
        authRepository.setCurrentUser(sampleUser())

        viewModel.events.test {
            viewModel.onAction(ConcursoListAction.OnSignOutClick)
            awaitItem()

            assertThat(cloudSync.isSincronizacaoAtiva("uid-1")).isFalse()
        }
    }

    private fun sampleUser() = AuthUser(id = "uid-1", nome = "Maria", email = "maria@gmail.com", fotoUrl = null)

    @Test
    fun `app starts without an account`() {
        assertThat(viewModel.state.value.conta).isNull()
    }

    @Test
    fun `tapping the account button while logged out launches google sign-in`() = runTest {
        viewModel.events.test {
            viewModel.onAction(ConcursoListAction.OnContaClick)

            assertThat(awaitItem()).isEqualTo(ConcursoListEvent.LaunchGoogleSignIn)
            assertThat(viewModel.state.value.isSigningIn).isTrue()
        }
    }

    @Test
    fun `receiving a google id token signs in and shows the account`() = runTest {
        viewModel.events.test {
            viewModel.onAction(ConcursoListAction.OnContaClick)
            awaitItem()

            viewModel.onAction(ConcursoListAction.OnGoogleIdTokenReceived("token-123"))

            assertThat(awaitItem()).isInstanceOf(ConcursoListEvent.ShowMessage::class)
            assertThat(authRepository.lastIdToken).isEqualTo("token-123")
            val state = viewModel.state.value
            assertThat(state.isSigningIn).isFalse()
            assertThat(state.conta).isEqualTo(ContaUi(nome = "Maria Souza", email = "maria@gmail.com"))
        }
    }

    @Test
    fun `cancelling the account picker stops signing in without showing an error`() = runTest {
        viewModel.events.test {
            viewModel.onAction(ConcursoListAction.OnContaClick)
            awaitItem()

            viewModel.onAction(ConcursoListAction.OnGoogleSignInFailed(AuthError.CANCELADO))

            assertThat(viewModel.state.value.isSigningIn).isFalse()
            expectNoEvents()
        }
    }

    @Test
    fun `firebase sign-in failure shows an error`() = runTest {
        authRepository.signInError = AuthError.SEM_CONEXAO

        viewModel.events.test {
            viewModel.onAction(ConcursoListAction.OnGoogleIdTokenReceived("token-123"))

            assertThat(awaitItem()).isInstanceOf(ConcursoListEvent.ShowError::class)
            assertThat(viewModel.state.value.conta).isNull()
        }
    }

    @Test
    fun `tapping the account button while logged in opens the account menu`() {
        authRepository.setCurrentUser(AuthUser(id = "uid-1", nome = "Maria", email = "maria@gmail.com", fotoUrl = null))

        viewModel.onAction(ConcursoListAction.OnContaClick)

        assertThat(viewModel.state.value.isContaMenuExpanded).isTrue()
    }

    @Test
    fun `signing out clears the account and closes the menu`() = runTest {
        authRepository.setCurrentUser(AuthUser(id = "uid-1", nome = "Maria", email = "maria@gmail.com", fotoUrl = null))
        viewModel.onAction(ConcursoListAction.OnContaClick)

        viewModel.events.test {
            viewModel.onAction(ConcursoListAction.OnSignOutClick)

            assertThat(awaitItem()).isInstanceOf(ConcursoListEvent.ShowMessage::class)
            assertThat(viewModel.state.value.conta).isNull()
            assertThat(viewModel.state.value.isContaMenuExpanded).isFalse()
        }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has no concursos and is not loading after collection`() {
        assertThat(viewModel.state.value.isLoading).isFalse()
        assertThat(viewModel.state.value.concursos).isEmpty()
    }

    @Test
    fun `list reflects concursos emitted by the repository`() {
        repository.setConcursos(listOf(sampleConcurso(id = 1L, nome = "TRT 2ª Região")))

        assertThat(viewModel.state.value.concursos).isEqualTo(
            listOf(sampleConcurso(id = 1L, nome = "TRT 2ª Região").toConcursoUi())
        )
    }

    @Test
    fun `fab click resets the form to create mode`() {
        repository.setConcursos(listOf(sampleConcurso(id = 1L)))
        viewModel.onAction(ConcursoListAction.OnConcursoTapped(1L))

        viewModel.onAction(ConcursoListAction.OnFabClick)

        val state = viewModel.state.value
        assertThat(state.isDialogVisible).isTrue()
        assertThat(state.editingId).isNull()
        assertThat(state.nome).isEqualTo("")
        assertThat(state.nivel).isEqualTo(Nivel.MEDIO)
    }

    @Test
    fun `swiping a concurso to edit opens dialog in edit mode with fields prefilled`() {
        repository.setConcursos(listOf(sampleConcurso(id = 7L, nome = "Banco Central", nivel = Nivel.SUPERIOR)))

        viewModel.onAction(ConcursoListAction.OnConcursoSwipeEdit(7L))

        val state = viewModel.state.value
        assertThat(state.isDialogVisible).isTrue()
        assertThat(state.editingId).isEqualTo(7L)
        assertThat(state.nome).isEqualTo("Banco Central")
        assertThat(state.nivel).isEqualTo(Nivel.SUPERIOR)
    }

    @Test
    fun `swiping an unknown concurso to edit emits an error event`() = runTest {
        viewModel.events.test {
            viewModel.onAction(ConcursoListAction.OnConcursoSwipeEdit(999L))
            assertThat(awaitItem()).isInstanceOf(ConcursoListEvent.ShowError::class)
        }
    }

    @Test
    fun `valor inscricao input filters non digit characters`() {
        viewModel.onAction(ConcursoListAction.OnValorInscricaoChanged("R$15a0,00"))

        assertThat(viewModel.state.value.valorInscricaoInput).isEqualTo("15000")
    }

    @Test
    fun `save is disabled without a name, but exam date is optional`() {
        viewModel.onAction(ConcursoListAction.OnFabClick)
        assertThat(viewModel.state.value.isSaveEnabled).isFalse()

        viewModel.onAction(ConcursoListAction.OnNomeChanged("Novo concurso"))
        assertThat(viewModel.state.value.isSaveEnabled).isTrue()
    }

    @Test
    fun `saving a valid concurso closes the dialog and persists it`() {
        viewModel.onAction(ConcursoListAction.OnFabClick)
        viewModel.onAction(ConcursoListAction.OnNomeChanged("Novo concurso"))
        viewModel.onAction(ConcursoListAction.OnDatePickerConfirm(1_700_000_000_000L))

        viewModel.onAction(ConcursoListAction.OnSaveClick)

        assertThat(viewModel.state.value.isDialogVisible).isFalse()
        assertThat(viewModel.state.value.concursos).isEqualTo(
            listOf(sampleConcurso(id = 1L, nome = "Novo concurso", dataProva = 1_700_000_000_000L).toConcursoUi())
        )
    }

    @Test
    fun `saving without an exam date persists it as null`() {
        viewModel.onAction(ConcursoListAction.OnFabClick)
        viewModel.onAction(ConcursoListAction.OnNomeChanged("Concurso sem data"))

        viewModel.onAction(ConcursoListAction.OnSaveClick)

        assertThat(viewModel.state.value.isDialogVisible).isFalse()
        assertThat(viewModel.state.value.concursos).isEqualTo(
            listOf(sampleConcurso(id = 1L, nome = "Concurso sem data", dataProva = null).toConcursoUi())
        )
    }

    @Test
    fun `remover a data na edicao salva o concurso sem data da prova`() {
        repository.setConcursos(listOf(sampleConcurso(id = 7L, nome = "Com data", dataProva = 1_700_000_000_000L)))
        viewModel.onAction(ConcursoListAction.OnConcursoSwipeEdit(7L))

        viewModel.onAction(ConcursoListAction.OnDataProvaClearClick)
        assertThat(viewModel.state.value.dataProvaMillis).isNull()
        assertThat(viewModel.state.value.dataProvaFormatted).isEqualTo("")

        viewModel.onAction(ConcursoListAction.OnSaveClick)
        assertThat(viewModel.state.value.concursos).isEqualTo(
            listOf(sampleConcurso(id = 7L, nome = "Com data", dataProva = null).toConcursoUi())
        )
    }

    @Test
    fun `swipe esquerda para excluir abre a confirmacao sem remover o concurso`() {
        repository.setConcursos(listOf(sampleConcurso(id = 7L)))

        viewModel.onAction(ConcursoListAction.OnConcursoSwipeDelete(7L))

        val state = viewModel.state.value
        assertThat(state.isDeleteConfirmationVisible).isTrue()
        assertThat(state.concursos.map { it.id }).isEqualTo(listOf(7L))
    }

    @Test
    fun `cancelar a confirmacao mantem o concurso`() {
        repository.setConcursos(listOf(sampleConcurso(id = 7L)))
        viewModel.onAction(ConcursoListAction.OnConcursoSwipeDelete(7L))

        viewModel.onAction(ConcursoListAction.OnDeleteDismiss)

        val state = viewModel.state.value
        assertThat(state.isDeleteConfirmationVisible).isFalse()
        assertThat(state.concursoPendingDeleteId).isNull()
        assertThat(state.concursos.map { it.id }).isEqualTo(listOf(7L))
    }

    @Test
    fun `confirmar a exclusao remove o concurso e fecha a confirmacao`() {
        repository.setConcursos(listOf(sampleConcurso(id = 7L), sampleConcurso(id = 8L)))
        viewModel.onAction(ConcursoListAction.OnConcursoSwipeDelete(7L))

        viewModel.onAction(ConcursoListAction.OnDeleteConfirm)

        val state = viewModel.state.value
        assertThat(state.isDeleteConfirmationVisible).isFalse()
        assertThat(state.concursoPendingDeleteId).isNull()
        assertThat(state.concursos.map { it.id }).isEqualTo(listOf(8L))
    }

    @Test
    fun `falha ao excluir fecha a confirmacao e emite ShowError`() = runTest {
        repository.setConcursos(listOf(sampleConcurso(id = 7L)))
        viewModel.onAction(ConcursoListAction.OnConcursoSwipeDelete(7L))
        repository.shouldReturnError = true

        viewModel.events.test {
            viewModel.onAction(ConcursoListAction.OnDeleteConfirm)
            assertThat(awaitItem()).isInstanceOf(ConcursoListEvent.ShowError::class)
        }

        assertThat(viewModel.state.value.isDeleteConfirmationVisible).isFalse()
        assertThat(viewModel.state.value.concursos.map { it.id }).isEqualTo(listOf(7L))
    }

    @Test
    fun `menu de mais opcoes abre e fecha`() {
        viewModel.onAction(ConcursoListAction.OnMoreMenuToggle)
        assertThat(viewModel.state.value.isMoreMenuExpanded).isTrue()

        viewModel.onAction(ConcursoListAction.OnMoreMenuDismiss)
        assertThat(viewModel.state.value.isMoreMenuExpanded).isFalse()
    }

    @Test
    fun `importar pelo menu fecha o menu e abre o dialogo de formato`() {
        viewModel.onAction(ConcursoListAction.OnMoreMenuToggle)

        viewModel.onAction(ConcursoListAction.OnImportClick)

        assertThat(viewModel.state.value.isMoreMenuExpanded).isFalse()
        assertThat(viewModel.state.value.isImportFormatDialogVisible).isTrue()
    }

    @Test
    fun `exportar pelo menu fecha o menu`() {
        viewModel.onAction(ConcursoListAction.OnMoreMenuToggle)

        viewModel.onAction(ConcursoListAction.OnExportClick)

        assertThat(viewModel.state.value.isMoreMenuExpanded).isFalse()
    }

    @Test
    fun `clicar em importar abre o dialogo com o formato do arquivo`() {
        viewModel.onAction(ConcursoListAction.OnImportClick)

        assertThat(viewModel.state.value.isImportFormatDialogVisible).isTrue()
    }

    @Test
    fun `dispensar o dialogo de importacao o fecha`() {
        viewModel.onAction(ConcursoListAction.OnImportClick)

        viewModel.onAction(ConcursoListAction.OnImportDismiss)

        assertThat(viewModel.state.value.isImportFormatDialogVisible).isFalse()
    }

    @Test
    fun `escolher arquivo fecha o dialogo e pede para abrir o seletor`() = runTest {
        viewModel.onAction(ConcursoListAction.OnImportClick)

        viewModel.events.test {
            viewModel.onAction(ConcursoListAction.OnImportPickFileClick)
            assertThat(awaitItem()).isEqualTo(ConcursoListEvent.OpenImportPicker)
        }

        assertThat(viewModel.state.value.isImportFormatDialogVisible).isFalse()
    }

    @Test
    fun `exportar com concursos cadastrados abre o seletor de destino`() = runTest {
        repository.setConcursos(listOf(sampleConcurso(id = 1L)))

        viewModel.events.test {
            viewModel.onAction(ConcursoListAction.OnExportClick)
            assertThat(awaitItem()).isEqualTo(ConcursoListEvent.OpenExportPicker("verticalize-concursos.txt"))
        }
    }

    @Test
    fun `exportar sem concursos avisa e nao abre o seletor`() = runTest {
        viewModel.events.test {
            viewModel.onAction(ConcursoListAction.OnExportClick)
            assertThat(awaitItem()).isInstanceOf(ConcursoListEvent.ShowError::class)
        }
    }

    @Test
    fun `escolher o destino grava o arquivo e avisa o sucesso`() = runTest {
        exportRepository.concursos = listOf(
            ImportedConcurso(nome = "TRT 2ª Região", nivel = Nivel.SUPERIOR, dataProva = 1_800_000_000_000L)
        )

        viewModel.events.test {
            viewModel.onAction(ConcursoListAction.OnExportFileSelected("content://destino.txt"))
            assertThat(awaitItem()).isInstanceOf(ConcursoListEvent.ShowMessage::class)
        }

        assertThat(exportFileWriter.lastUri).isEqualTo("content://destino.txt")
        assertThat(exportFileWriter.lastText!!).contains("nome: TRT 2ª Região")
    }

    @Test
    fun `falha ao gravar a exportacao emite ShowError`() = runTest {
        exportRepository.concursos = listOf(
            ImportedConcurso(nome = "TRT 2ª Região", nivel = Nivel.SUPERIOR, dataProva = 1_800_000_000_000L)
        )
        exportFileWriter.shouldFail = true

        viewModel.events.test {
            viewModel.onAction(ConcursoListAction.OnExportFileSelected("content://destino.txt"))
            assertThat(awaitItem()).isInstanceOf(ConcursoListEvent.ShowError::class)
        }
    }

    private fun sampleConcurso(
        id: Long = 1L,
        nome: String = "Concurso de teste",
        nivel: Nivel = Nivel.MEDIO,
        dataProva: Long? = 1_700_000_000_000L
    ) = Concurso(
        id = id,
        nome = nome,
        nivel = nivel,
        dataProva = dataProva
    )
}
