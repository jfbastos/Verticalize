package br.com.zamfir.verticalize.registroestudo.presentation

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import br.com.zamfir.verticalize.core.presentation.UiText
import br.com.zamfir.verticalize.registroestudo.data.FakeRegistroEstudoRepository
import br.com.zamfir.verticalize.registroestudo.domain.RegistroEstudo
import br.com.zamfir.verticalize.registroestudo.timer.EstudoTimerSnapshot
import br.com.zamfir.verticalize.registroestudo.timer.EstudoTimerState
import br.com.zamfir.verticalize.registroestudo.timer.EstudoTimerStatus
import br.com.zamfir.verticalize.registroestudo.timer.FakeEstudoTimerController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegistroEstudoListViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeRegistroEstudoRepository
    private lateinit var timerController: FakeEstudoTimerController
    private lateinit var viewModel: RegistroEstudoListViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(dispatcher)
        EstudoTimerState.update { EstudoTimerSnapshot() }
        repository = FakeRegistroEstudoRepository()
        timerController = FakeEstudoTimerController()
        viewModel = RegistroEstudoListViewModel(1L, repository, timerController)
    }

    @AfterEach
    fun tearDown() {
        // Cancelar antes de tocar no Main/no estado global evita que o collector do cronômetro
        // (ainda ativo no viewModelScope) seja notificado depois que o Main dispatcher já foi resetado.
        viewModel.viewModelScope.cancel()
        EstudoTimerState.update { EstudoTimerSnapshot() }
        Dispatchers.resetMain()
    }

    @Test
    fun `lista reflete os registros emitidos pelo repositorio`() {
        repository.setRegistros(
            listOf(
                sampleRegistro(id = 1L, materia = "Direito Administrativo", horaInicioMinutos = 480, horaFimMinutos = 1380)
            )
        )

        assertThat(viewModel.state.value.registros.map { it.materia }).isEqualTo(listOf("Direito Administrativo"))
    }

    @Test
    fun `fab click abre o dialog com os defaults`() {
        viewModel.onAction(RegistroEstudoListAction.OnFabClick)
        viewModel.onAction(RegistroEstudoListAction.OnRegistroManualOptionClick)

        val state = viewModel.state.value
        assertThat(state.isDialogVisible).isTrue()
        assertThat(state.materia).isEqualTo("")
        assertThat(state.dataMillis).isNull()
        assertThat(state.horaInicioMinutos).isEqualTo(480)
        assertThat(state.horaFimMinutos).isEqualTo(1380)
    }

    @Test
    fun `isSaveEnabled fica falso quando hora fim nao e maior que hora inicio`() {
        viewModel.onAction(RegistroEstudoListAction.OnFabClick)
        viewModel.onAction(RegistroEstudoListAction.OnMateriaChanged("Direito Administrativo"))
        viewModel.onAction(RegistroEstudoListAction.OnDataPickerConfirm(1_700_000_000_000L))

        viewModel.onAction(RegistroEstudoListAction.OnHoraInicioChanged(600))
        viewModel.onAction(RegistroEstudoListAction.OnHoraFimChanged(600))

        assertThat(viewModel.state.value.isSaveEnabled).isFalse()
    }

    @Test
    fun `salvar registro valido persiste e fecha o dialog`() {
        viewModel.onAction(RegistroEstudoListAction.OnFabClick)
        viewModel.onAction(RegistroEstudoListAction.OnMateriaChanged("Direito Administrativo"))
        viewModel.onAction(RegistroEstudoListAction.OnDataPickerConfirm(1_700_000_000_000L))

        viewModel.onAction(RegistroEstudoListAction.OnSaveClick)

        val state = viewModel.state.value
        assertThat(state.isDialogVisible).isFalse()
        assertThat(state.registros.map { it.materia }).isEqualTo(listOf("Direito Administrativo"))
    }

    @Test
    fun `erro do repositorio ao salvar emite ShowError`() = runTest {
        repository.shouldReturnError = true
        viewModel.onAction(RegistroEstudoListAction.OnFabClick)
        viewModel.onAction(RegistroEstudoListAction.OnMateriaChanged("Direito Administrativo"))
        viewModel.onAction(RegistroEstudoListAction.OnDataPickerConfirm(1_700_000_000_000L))

        viewModel.events.test {
            viewModel.onAction(RegistroEstudoListAction.OnSaveClick)
            assertThat(awaitItem()).isInstanceOf(RegistroEstudoListEvent.ShowError::class)
        }
    }

    @Test
    fun `swipe direita para editar abre o dialog preenchido com os dados do registro`() {
        repository.setRegistros(
            listOf(sampleRegistro(id = 7L, materia = "Direito Constitucional", horaInicioMinutos = 600, horaFimMinutos = 720))
        )

        viewModel.onAction(RegistroEstudoListAction.OnEditSwipe(7L))

        val state = viewModel.state.value
        assertThat(state.isDialogVisible).isTrue()
        assertThat(state.editingId).isEqualTo(7L)
        assertThat(state.materia).isEqualTo("Direito Constitucional")
        assertThat(state.horaInicioMinutos).isEqualTo(600)
        assertThat(state.horaFimMinutos).isEqualTo(720)
    }

    @Test
    fun `salvar edicao atualiza o registro existente em vez de criar um novo`() {
        repository.setRegistros(listOf(sampleRegistro(id = 7L, materia = "Direito Constitucional")))

        viewModel.onAction(RegistroEstudoListAction.OnEditSwipe(7L))
        viewModel.onAction(RegistroEstudoListAction.OnMateriaChanged("Direito Administrativo"))
        viewModel.onAction(RegistroEstudoListAction.OnSaveClick)

        val state = viewModel.state.value
        assertThat(state.isDialogVisible).isFalse()
        assertThat(state.registros.map { it.id to it.materia }).isEqualTo(listOf(7L to "Direito Administrativo"))
    }

    @Test
    fun `editar um registro inexistente emite ShowError`() = runTest {
        viewModel.events.test {
            viewModel.onAction(RegistroEstudoListAction.OnEditSwipe(999L))
            assertThat(awaitItem()).isInstanceOf(RegistroEstudoListEvent.ShowError::class)
        }
    }

    @Test
    fun `swipe esquerda para excluir abre a confirmacao sem remover o registro`() {
        repository.setRegistros(listOf(sampleRegistro(id = 7L)))

        viewModel.onAction(RegistroEstudoListAction.OnDeleteSwipe(7L))

        val state = viewModel.state.value
        assertThat(state.isDeleteConfirmationVisible).isTrue()
        assertThat(state.registros.map { it.id }).isEqualTo(listOf(7L))
    }

    @Test
    fun `cancelar a confirmacao mantem o registro`() {
        repository.setRegistros(listOf(sampleRegistro(id = 7L)))
        viewModel.onAction(RegistroEstudoListAction.OnDeleteSwipe(7L))

        viewModel.onAction(RegistroEstudoListAction.OnDeleteDismiss)

        val state = viewModel.state.value
        assertThat(state.isDeleteConfirmationVisible).isFalse()
        assertThat(state.registros.map { it.id }).isEqualTo(listOf(7L))
    }

    @Test
    fun `confirmar a exclusao remove o registro e fecha a confirmacao`() {
        repository.setRegistros(listOf(sampleRegistro(id = 7L)))
        viewModel.onAction(RegistroEstudoListAction.OnDeleteSwipe(7L))

        viewModel.onAction(RegistroEstudoListAction.OnDeleteConfirm)

        val state = viewModel.state.value
        assertThat(state.isDeleteConfirmationVisible).isFalse()
        assertThat(state.registros).isEqualTo(emptyList())
    }

    @Test
    fun `fab abre a escolha entre manual e cronometro`() {
        viewModel.onAction(RegistroEstudoListAction.OnFabClick)

        assertThat(viewModel.state.value.isRegistroOpcaoSheetVisible).isTrue()
    }

    @Test
    fun `escolher manual fecha a escolha e abre o formulario`() {
        viewModel.onAction(RegistroEstudoListAction.OnFabClick)

        viewModel.onAction(RegistroEstudoListAction.OnRegistroManualOptionClick)

        val state = viewModel.state.value
        assertThat(state.isRegistroOpcaoSheetVisible).isFalse()
        assertThat(state.isDialogVisible).isTrue()
    }

    @Test
    fun `escolher cronometro com nenhum em andamento abre o dialog de iniciar`() {
        viewModel.onAction(RegistroEstudoListAction.OnFabClick)

        viewModel.onAction(RegistroEstudoListAction.OnRegistroCronometroOptionClick)

        val state = viewModel.state.value
        assertThat(state.isRegistroOpcaoSheetVisible).isFalse()
        assertThat(state.isTimerStartDialogVisible).isTrue()
    }

    @Test
    fun `escolher cronometro com um ja em andamento emite erro e nao abre o dialog`() = runTest {
        EstudoTimerState.update { it.copy(status = EstudoTimerStatus.RUNNING, concursoId = 1L, materia = "Outra matéria") }

        viewModel.events.test {
            viewModel.onAction(RegistroEstudoListAction.OnRegistroCronometroOptionClick)
            assertThat(awaitItem()).isInstanceOf(RegistroEstudoListEvent.ShowError::class)
        }
        assertThat(viewModel.state.value.isTimerStartDialogVisible).isFalse()
    }

    @Test
    fun `confirmar o inicio do cronometro chama o controller e fecha o dialog`() {
        viewModel.onAction(RegistroEstudoListAction.OnRegistroCronometroOptionClick)
        viewModel.onAction(RegistroEstudoListAction.OnTimerMateriaChanged("Direito Administrativo"))

        viewModel.onAction(RegistroEstudoListAction.OnTimerStartConfirm)

        assertThat(viewModel.state.value.isTimerStartDialogVisible).isFalse()
        assertThat(timerController.startCalls).isEqualTo(listOf(1L to "Direito Administrativo"))
    }

    @Test
    fun `confirmar o inicio sem materia nao chama o controller`() {
        viewModel.onAction(RegistroEstudoListAction.OnRegistroCronometroOptionClick)

        viewModel.onAction(RegistroEstudoListAction.OnTimerStartConfirm)

        assertThat(timerController.startCalls).isEqualTo(emptyList())
    }

    @Test
    fun `pausar retomar registrar e descartar delegam ao controller`() {
        viewModel.onAction(RegistroEstudoListAction.OnTimerPauseClick)
        viewModel.onAction(RegistroEstudoListAction.OnTimerResumeClick)
        viewModel.onAction(RegistroEstudoListAction.OnTimerRegistrarClick)
        viewModel.onAction(RegistroEstudoListAction.OnTimerDescartarClick)

        assertThat(timerController.pauseCallCount).isEqualTo(1)
        assertThat(timerController.resumeCallCount).isEqualTo(1)
        assertThat(timerController.registrarCallCount).isEqualTo(1)
        assertThat(timerController.descartarCallCount).isEqualTo(1)
    }

    @Test
    fun `estado reflete o cronometro global ativo neste concurso`() {
        EstudoTimerState.update {
            it.copy(status = EstudoTimerStatus.RUNNING, concursoId = 1L, materia = "Direito Administrativo")
        }

        val state = viewModel.state.value
        assertThat(state.isTimerAtivoNesteConcurso).isTrue()
        assertThat(state.isTimerAtivoEmOutroConcurso).isFalse()
    }

    @Test
    fun `estado reflete o cronometro global ativo em outro concurso`() {
        EstudoTimerState.update {
            it.copy(status = EstudoTimerStatus.RUNNING, concursoId = 2L, materia = "Direito Administrativo")
        }

        val state = viewModel.state.value
        assertThat(state.isTimerAtivoNesteConcurso).isFalse()
        assertThat(state.isTimerAtivoEmOutroConcurso).isTrue()
    }

    @Test
    fun `toque fora do modo de selecao nao seleciona nada`() {
        repository.setRegistros(listOf(sampleRegistro(id = 1L)))

        viewModel.onAction(RegistroEstudoListAction.OnRegistroTapped(1L))

        assertThat(viewModel.state.value.isSelecaoAtiva).isFalse()
    }

    @Test
    fun `toque longo entra na selecao e toques seguintes marcam e desmarcam`() {
        repository.setRegistros(listOf(sampleRegistro(id = 1L), sampleRegistro(id = 2L)))

        viewModel.onAction(RegistroEstudoListAction.OnRegistroLongPress(1L))
        viewModel.onAction(RegistroEstudoListAction.OnRegistroTapped(2L))
        assertThat(viewModel.state.value.registrosSelecionados).isEqualTo(setOf(1L, 2L))

        viewModel.onAction(RegistroEstudoListAction.OnRegistroTapped(1L))
        assertThat(viewModel.state.value.registrosSelecionados).isEqualTo(setOf(2L))
    }

    @Test
    fun `selecionar todos marca todos os registros`() {
        repository.setRegistros(listOf(sampleRegistro(id = 1L), sampleRegistro(id = 2L), sampleRegistro(id = 3L)))
        viewModel.onAction(RegistroEstudoListAction.OnRegistroLongPress(1L))

        viewModel.onAction(RegistroEstudoListAction.OnSelecionarTodosClick)

        assertThat(viewModel.state.value.registrosSelecionados).isEqualTo(setOf(1L, 2L, 3L))
    }

    @Test
    fun `cancelar a selecao e trocar para o calendario limpam os selecionados`() {
        repository.setRegistros(listOf(sampleRegistro(id = 1L)))
        viewModel.onAction(RegistroEstudoListAction.OnRegistroLongPress(1L))
        viewModel.onAction(RegistroEstudoListAction.OnSelecaoCancelar)
        assertThat(viewModel.state.value.isSelecaoAtiva).isFalse()

        viewModel.onAction(RegistroEstudoListAction.OnRegistroLongPress(1L))
        viewModel.onAction(RegistroEstudoListAction.OnViewModeChanged(RegistroEstudoViewMode.CALENDARIO))
        assertThat(viewModel.state.value.isSelecaoAtiva).isFalse()
    }

    @Test
    fun `excluir selecionados pede confirmacao e cancelar mantem tudo`() {
        repository.setRegistros(listOf(sampleRegistro(id = 1L), sampleRegistro(id = 2L)))
        viewModel.onAction(RegistroEstudoListAction.OnRegistroLongPress(1L))

        viewModel.onAction(RegistroEstudoListAction.OnExcluirSelecionadosClick)
        assertThat(viewModel.state.value.isExcluirSelecionadosVisible).isTrue()

        viewModel.onAction(RegistroEstudoListAction.OnExcluirSelecionadosDismiss)
        val state = viewModel.state.value
        assertThat(state.isExcluirSelecionadosVisible).isFalse()
        assertThat(state.registrosSelecionados).isEqualTo(setOf(1L))
        assertThat(state.registros.map { it.id }).isEqualTo(listOf(1L, 2L))
    }

    @Test
    fun `confirmar remove so os selecionados e avisa quantos foram excluidos`() = runTest {
        repository.setRegistros(listOf(sampleRegistro(id = 1L), sampleRegistro(id = 2L), sampleRegistro(id = 3L)))
        viewModel.onAction(RegistroEstudoListAction.OnRegistroLongPress(1L))
        viewModel.onAction(RegistroEstudoListAction.OnRegistroTapped(3L))
        viewModel.onAction(RegistroEstudoListAction.OnExcluirSelecionadosClick)

        viewModel.events.test {
            viewModel.onAction(RegistroEstudoListAction.OnExcluirSelecionadosConfirm)

            val message = (awaitItem() as RegistroEstudoListEvent.ShowMessage).message as UiText.PluralResource
            assertThat(message.quantity).isEqualTo(2)
        }
        val state = viewModel.state.value
        assertThat(state.registros.map { it.id }).isEqualTo(listOf(2L))
        assertThat(state.isSelecaoAtiva).isFalse()
        assertThat(state.isExcluirSelecionadosVisible).isFalse()
    }

    @Test
    fun `falha ao excluir selecionados mantem a selecao e emite ShowError`() = runTest {
        repository.setRegistros(listOf(sampleRegistro(id = 1L)))
        viewModel.onAction(RegistroEstudoListAction.OnRegistroLongPress(1L))
        viewModel.onAction(RegistroEstudoListAction.OnExcluirSelecionadosClick)
        repository.shouldReturnError = true

        viewModel.events.test {
            viewModel.onAction(RegistroEstudoListAction.OnExcluirSelecionadosConfirm)
            assertThat(awaitItem()).isInstanceOf(RegistroEstudoListEvent.ShowError::class)
        }
        assertThat(viewModel.state.value.registrosSelecionados).isEqualTo(setOf(1L))
    }

    private fun sampleRegistro(
        id: Long,
        materia: String = "Matéria $id",
        horaInicioMinutos: Int = 480,
        horaFimMinutos: Int = 1380
    ) = RegistroEstudo(
        id = id,
        concursoId = 1L,
        materia = materia,
        data = 1_700_000_000_000L,
        horaInicioMinutos = horaInicioMinutos,
        horaFimMinutos = horaFimMinutos
    )
}
