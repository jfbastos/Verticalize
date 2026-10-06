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
import br.com.zamfir.verticalize.concurso.data.FakeConcursoRepository
import br.com.zamfir.verticalize.concurso.domain.Concurso
import br.com.zamfir.verticalize.concurso.domain.Nivel
import br.com.zamfir.verticalize.conteudo.data.FakeConteudoRepository
import br.com.zamfir.verticalize.conteudo.domain.Conteudo
import br.com.zamfir.verticalize.conteudo.domain.Prioridade
import br.com.zamfir.verticalize.conteudo.presentation.ConteudoAgrupamento
import br.com.zamfir.verticalize.conteudo.presentation.ConteudoOrdenacao
import br.com.zamfir.verticalize.core.presentation.UiText
import br.com.zamfir.verticalize.importacao.data.FakeExportFileWriter
import br.com.zamfir.verticalize.importacao.data.FakeExportRepository
import br.com.zamfir.verticalize.importacao.domain.ConcursoExporter
import br.com.zamfir.verticalize.importacao.domain.ImportedConcurso
import br.com.zamfir.verticalize.importacao.domain.ImportedConteudo
import br.com.zamfir.verticalize.importacao.domain.TxtSectionsFormatter
import br.com.zamfir.verticalize.registroestudo.data.FakeRegistroEstudoRepository
import br.com.zamfir.verticalize.registroestudo.domain.RegistroEstudo
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
class ConcursoDetailViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var concursoRepository: FakeConcursoRepository
    private lateinit var conteudoRepository: FakeConteudoRepository
    private lateinit var registroEstudoRepository: FakeRegistroEstudoRepository
    private lateinit var exportRepository: FakeExportRepository
    private lateinit var exportFileWriter: FakeExportFileWriter
    private lateinit var viewModel: ConcursoDetailViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(dispatcher)
        concursoRepository = FakeConcursoRepository()
        conteudoRepository = FakeConteudoRepository()
        registroEstudoRepository = FakeRegistroEstudoRepository()
        exportRepository = FakeExportRepository()
        exportFileWriter = FakeExportFileWriter()
        concursoRepository.setConcursos(listOf(sampleConcurso(id = 1L)))
        conteudoRepository.setConteudos(
            listOf(
                sampleConteudo(id = 1L, eixo = 1),
                sampleConteudo(id = 2L, eixo = 2)
            )
        )
        viewModel = ConcursoDetailViewModel(
            concursoId = 1L,
            concursoRepository = concursoRepository,
            conteudoRepository = conteudoRepository,
            registroEstudoRepository = registroEstudoRepository,
            exporter = ConcursoExporter(exportRepository, TxtSectionsFormatter(), exportFileWriter)
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `filtro click abre o sheet e dismiss fecha`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoFiltroClick)
        assertThat(viewModel.state.value.isConteudoFiltroSheetVisible).isTrue()

        viewModel.onAction(ConcursoDetailAction.OnConteudoFiltroDismiss)
        assertThat(viewModel.state.value.isConteudoFiltroSheetVisible).isFalse()
    }

    @Test
    fun `toggle de eixo adiciona e depois remove do set de selecionados`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoFiltroEixoToggle(1))
        assertThat(viewModel.state.value.conteudoFiltroEixosSelecionados).isEqualTo(setOf(1))

        viewModel.onAction(ConcursoDetailAction.OnConteudoFiltroEixoToggle(1))
        assertThat(viewModel.state.value.conteudoFiltroEixosSelecionados).isEmpty()
    }

    @Test
    fun `toggle de bloco adiciona e depois remove do set de selecionados`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoFiltroBlocoToggle(2))
        assertThat(viewModel.state.value.conteudoFiltroBlocosSelecionados).isEqualTo(setOf(2))

        viewModel.onAction(ConcursoDetailAction.OnConteudoFiltroBlocoToggle(2))
        assertThat(viewModel.state.value.conteudoFiltroBlocosSelecionados).isEmpty()
    }

    @Test
    fun `toggle de prioridade adiciona e depois remove do set de selecionados`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoFiltroPrioridadeToggle(Prioridade.ALTA))
        assertThat(viewModel.state.value.conteudoFiltroPrioridadesSelecionados).isEqualTo(setOf(Prioridade.ALTA))

        viewModel.onAction(ConcursoDetailAction.OnConteudoFiltroPrioridadeToggle(Prioridade.ALTA))
        assertThat(viewModel.state.value.conteudoFiltroPrioridadesSelecionados).isEmpty()
    }

    @Test
    fun `limpar filtros zera eixos, blocos e prioridades selecionados`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoFiltroEixoToggle(1))
        viewModel.onAction(ConcursoDetailAction.OnConteudoFiltroBlocoToggle(2))
        viewModel.onAction(ConcursoDetailAction.OnConteudoFiltroPrioridadeToggle(Prioridade.ALTA))

        viewModel.onAction(ConcursoDetailAction.OnConteudoFiltroLimparClick)

        assertThat(viewModel.state.value.conteudoFiltroEixosSelecionados).isEmpty()
        assertThat(viewModel.state.value.conteudoFiltroBlocosSelecionados).isEmpty()
        assertThat(viewModel.state.value.conteudoFiltroPrioridadesSelecionados).isEmpty()
    }

    @Test
    fun `ordenacao click abre o sheet e dismiss fecha`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoOrdenacaoClick)
        assertThat(viewModel.state.value.isConteudoOrdenacaoSheetVisible).isTrue()

        viewModel.onAction(ConcursoDetailAction.OnConteudoOrdenacaoDismiss)
        assertThat(viewModel.state.value.isConteudoOrdenacaoSheetVisible).isFalse()
    }

    @Test
    fun `selecionar ordenacao atualiza o criterio e fecha o sheet`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoOrdenacaoClick)

        viewModel.onAction(ConcursoDetailAction.OnConteudoOrdenacaoSelected(ConteudoOrdenacao.EIXO_DESC))

        assertThat(viewModel.state.value.conteudoOrdenacao).isEqualTo(ConteudoOrdenacao.EIXO_DESC)
        assertThat(viewModel.state.value.isConteudoOrdenacaoSheetVisible).isFalse()
    }

    @Test
    fun `agrupamento click abre o sheet e dismiss fecha`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoAgrupamentoClick)
        assertThat(viewModel.state.value.isConteudoAgrupamentoSheetVisible).isTrue()

        viewModel.onAction(ConcursoDetailAction.OnConteudoAgrupamentoDismiss)
        assertThat(viewModel.state.value.isConteudoAgrupamentoSheetVisible).isFalse()
    }

    @Test
    fun `abrir o agrupamento pelo menu de mais opcoes fecha o menu`() {
        viewModel.onAction(ConcursoDetailAction.OnMoreMenuToggle)

        viewModel.onAction(ConcursoDetailAction.OnConteudoAgrupamentoClick)

        assertThat(viewModel.state.value.isConteudoAgrupamentoSheetVisible).isTrue()
        assertThat(viewModel.state.value.isMoreMenuExpanded).isFalse()
    }

    @Test
    fun `selecionar agrupamento atualiza o criterio e fecha o sheet`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoAgrupamentoClick)

        viewModel.onAction(ConcursoDetailAction.OnConteudoAgrupamentoSelected(ConteudoAgrupamento.EIXO))

        assertThat(viewModel.state.value.conteudoAgrupamento).isEqualTo(ConteudoAgrupamento.EIXO)
        assertThat(viewModel.state.value.isConteudoAgrupamentoSheetVisible).isFalse()
    }

    @Test
    fun `toggle de grupo expande e depois retrai de volta`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoGrupoToggle("EIXO:1:null"))
        assertThat(viewModel.state.value.conteudoGruposExpandidos).isEqualTo(setOf("EIXO:1:null"))

        viewModel.onAction(ConcursoDetailAction.OnConteudoGrupoToggle("EIXO:1:null"))
        assertThat(viewModel.state.value.conteudoGruposExpandidos).isEmpty()
    }

    @Test
    fun `escolher um agrupamento comeca com todos os grupos retraidos`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoAgrupamentoSelected(ConteudoAgrupamento.EIXO))
        viewModel.onAction(ConcursoDetailAction.OnConteudoGrupoToggle("EIXO:1:null"))

        viewModel.onAction(ConcursoDetailAction.OnConteudoAgrupamentoSelected(ConteudoAgrupamento.BLOCO))
        viewModel.onAction(ConcursoDetailAction.OnConteudoAgrupamentoSelected(ConteudoAgrupamento.EIXO))

        assertThat(viewModel.state.value.conteudoGruposExpandidos).isEmpty()
    }

    @Test
    fun `selecionar aba atualiza a aba selecionada`() {
        assertThat(viewModel.state.value.selectedTab).isEqualTo(ConcursoDetailTab.CONTEUDOS)

        viewModel.onAction(ConcursoDetailAction.OnTabSelected(ConcursoDetailTab.HORAS_ESTUDO))

        assertThat(viewModel.state.value.selectedTab).isEqualTo(ConcursoDetailTab.HORAS_ESTUDO)
    }

    @Test
    fun `total de horas aula soma todos os conteudos, ja as assistidas so os concluidos`() {
        conteudoRepository.setConteudos(
            listOf(
                Conteudo(
                    id = 1L,
                    concursoId = 1L,
                    descricao = "Concluído",
                    quantidadeAulas = 2,
                    tempoMedioAulaMinutos = 30,
                    concluido = true
                ),
                Conteudo(
                    id = 2L,
                    concursoId = 1L,
                    descricao = "Pendente",
                    quantidadeAulas = 3,
                    tempoMedioAulaMinutos = 20,
                    concluido = false
                )
            )
        )

        assertThat(viewModel.state.value.totalMinutosAula).isEqualTo(120)
        assertThat(viewModel.state.value.totalMinutosAulaAssistidos).isEqualTo(60)
    }

    @Test
    fun `horas estudadas do header e recalculada a partir dos registros de estudo`() {
        registroEstudoRepository.setRegistros(
            listOf(
                sampleRegistroEstudo(id = 1L, horaInicioMinutos = 660, horaFimMinutos = 1380)
            )
        )

        assertThat(viewModel.state.value.concurso?.horasEstudadas).isEqualTo(12)
    }

    private fun sampleRegistroEstudo(
        id: Long,
        horaInicioMinutos: Int,
        horaFimMinutos: Int
    ) = RegistroEstudo(
        id = id,
        concursoId = 1L,
        materia = "Matéria $id",
        data = 1_700_000_000_000L,
        horaInicioMinutos = horaInicioMinutos,
        horaFimMinutos = horaFimMinutos
    )

    @Test
    fun `tocar no conteudo abre os detalhes e nao abre a edicao`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoTapped(1L))

        val state = viewModel.state.value
        assertThat(state.conteudoDetalheId).isEqualTo(1L)
        assertThat(state.conteudoDetalhe?.descricao).isEqualTo("Descrição 1")
        assertThat(state.isConteudoDialogVisible).isFalse()
        assertThat(state.editingConteudoId).isNull()
    }

    @Test
    fun `dispensar os detalhes fecha a dialog`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoTapped(1L))

        viewModel.onAction(ConcursoDetailAction.OnConteudoDetalheDismiss)

        assertThat(viewModel.state.value.conteudoDetalheId).isNull()
        assertThat(viewModel.state.value.conteudoDetalhe).isNull()
    }

    @Test
    fun `tocar em um conteudo inexistente nao mostra os detalhes`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoTapped(999L))

        assertThat(viewModel.state.value.conteudoDetalhe).isNull()
    }

    @Test
    fun `o lapis abre a edicao preenchida e mantem os detalhes para voltar a eles`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoTapped(1L))

        viewModel.onAction(ConcursoDetailAction.OnConteudoDetalheEditClick)

        val state = viewModel.state.value
        assertThat(state.isConteudoDialogVisible).isTrue()
        assertThat(state.editingConteudoId).isEqualTo(1L)
        assertThat(state.conteudoMateria).isEqualTo("Matéria 1")
        assertThat(state.conteudoDescricao).isEqualTo("Descrição 1")
        assertThat(state.conteudoEixoInput).isEqualTo("1")
        assertThat(state.conteudoDetalheId).isEqualTo(1L)
    }

    @Test
    fun `cancelar a edicao fecha so o formulario e os detalhes continuam`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoTapped(1L))
        viewModel.onAction(ConcursoDetailAction.OnConteudoDetalheEditClick)

        viewModel.onAction(ConcursoDetailAction.OnConteudoDismissDialog)

        val state = viewModel.state.value
        assertThat(state.isConteudoDialogVisible).isFalse()
        assertThat(state.conteudoDetalheId).isEqualTo(1L)
        assertThat(state.conteudoDetalhe?.descricao).isEqualTo("Descrição 1")
    }

    @Test
    fun `salvar a edicao fecha o formulario e os detalhes mostram os dados atualizados`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoTapped(1L))
        viewModel.onAction(ConcursoDetailAction.OnConteudoDetalheEditClick)
        viewModel.onAction(ConcursoDetailAction.OnConteudoDescricaoChanged("Descrição atualizada"))

        viewModel.onAction(ConcursoDetailAction.OnConteudoSaveClick)

        val state = viewModel.state.value
        assertThat(state.isConteudoDialogVisible).isFalse()
        assertThat(state.conteudoDetalheId).isEqualTo(1L)
        assertThat(state.conteudoDetalhe?.descricao).isEqualTo("Descrição atualizada")
    }

    @Test
    fun `alterar a prioridade no formulario e salvar reflete nos detalhes`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoTapped(1L))
        viewModel.onAction(ConcursoDetailAction.OnConteudoDetalheEditClick)
        assertThat(viewModel.state.value.conteudoPrioridade).isEqualTo(Prioridade.MEDIA)

        viewModel.onAction(ConcursoDetailAction.OnConteudoPrioridadeChanged(Prioridade.ALTA))
        viewModel.onAction(ConcursoDetailAction.OnConteudoSaveClick)

        assertThat(viewModel.state.value.conteudoDetalhe?.prioridade).isEqualTo(Prioridade.ALTA)
    }

    @Test
    fun `o lapis sem nenhum detalhe aberto nao faz nada`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoDetalheEditClick)

        assertThat(viewModel.state.value.isConteudoDialogVisible).isFalse()
    }

    @Test
    fun `criar um conteudo pelo fab abre o formulario vazio sem passar pelos detalhes`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoFabClick)

        val state = viewModel.state.value
        assertThat(state.isConteudoDialogVisible).isTrue()
        assertThat(state.editingConteudoId).isNull()
        assertThat(state.conteudoDetalheId).isNull()
    }

    @Test
    fun `menu de mais opcoes abre e fecha`() {
        viewModel.onAction(ConcursoDetailAction.OnMoreMenuToggle)
        assertThat(viewModel.state.value.isMoreMenuExpanded).isTrue()

        viewModel.onAction(ConcursoDetailAction.OnMoreMenuDismiss)
        assertThat(viewModel.state.value.isMoreMenuExpanded).isFalse()
    }

    @Test
    fun `exportar fecha o menu e abre o seletor sugerindo o nome do concurso`() = runTest {
        viewModel.onAction(ConcursoDetailAction.OnMoreMenuToggle)

        viewModel.events.test {
            viewModel.onAction(ConcursoDetailAction.OnExportClick)
            assertThat(awaitItem()).isEqualTo(
                ConcursoDetailEvent.OpenExportPicker("verticalize-concurso-de-teste.txt")
            )
        }

        assertThat(viewModel.state.value.isMoreMenuExpanded).isFalse()
    }

    @Test
    fun `escolher o destino exporta so este concurso e avisa o sucesso`() = runTest {
        exportRepository.concursosPorId = mapOf(
            1L to ImportedConcurso(
                nome = "Concurso de teste",
                nivel = Nivel.MEDIO,
                dataProva = 1_700_000_000_000L,
                conteudos = listOf(ImportedConteudo(materia = "Matéria 1", descricao = "Descrição 1", eixo = 1))
            )
        )

        viewModel.events.test {
            viewModel.onAction(ConcursoDetailAction.OnExportFileSelected("content://destino.txt"))
            assertThat(awaitItem()).isInstanceOf(ConcursoDetailEvent.ShowMessage::class)
        }

        assertThat(exportFileWriter.lastUri).isEqualTo("content://destino.txt")
        assertThat(exportFileWriter.lastText!!).contains("nome: Concurso de teste")
        assertThat(exportFileWriter.lastText!!).contains("Matéria 1;Descrição 1;1;;0;0;nao;;0;media")
    }

    @Test
    fun `falha ao exportar emite ShowError`() = runTest {
        exportRepository.concursosPorId = mapOf(
            1L to ImportedConcurso(nome = "Concurso de teste", nivel = Nivel.MEDIO, dataProva = 1_700_000_000_000L)
        )
        exportFileWriter.shouldFail = true

        viewModel.events.test {
            viewModel.onAction(ConcursoDetailAction.OnExportFileSelected("content://destino.txt"))
            assertThat(awaitItem()).isInstanceOf(ConcursoDetailEvent.ShowError::class)
        }
    }

    @Test
    fun `toque longo entra na selecao e o toque seguinte marca outro item em vez de abrir os detalhes`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoLongPress(1L))
        viewModel.onAction(ConcursoDetailAction.OnConteudoTapped(2L))

        val state = viewModel.state.value
        assertThat(state.conteudosSelecionados).isEqualTo(setOf(1L, 2L))
        assertThat(state.conteudoDetalheId).isNull()
    }

    @Test
    fun `desmarcar o ultimo item sai do modo de selecao`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoLongPress(1L))
        viewModel.onAction(ConcursoDetailAction.OnConteudoTapped(1L))

        assertThat(viewModel.state.value.isConteudoSelecaoAtiva).isFalse()
    }

    @Test
    fun `selecionar todos marca so os conteudos exibidos pelo filtro`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoFiltroEixoToggle(2))
        viewModel.onAction(ConcursoDetailAction.OnConteudoLongPress(2L))

        viewModel.onAction(ConcursoDetailAction.OnConteudoSelecionarTodosClick)

        assertThat(viewModel.state.value.conteudosSelecionados).isEqualTo(setOf(2L))
    }

    @Test
    fun `cancelar a selecao e trocar de aba limpam os selecionados`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoLongPress(1L))
        viewModel.onAction(ConcursoDetailAction.OnConteudoSelecaoCancelar)
        assertThat(viewModel.state.value.conteudosSelecionados).isEmpty()

        viewModel.onAction(ConcursoDetailAction.OnConteudoLongPress(1L))
        viewModel.onAction(ConcursoDetailAction.OnTabSelected(ConcursoDetailTab.HORAS_ESTUDO))
        assertThat(viewModel.state.value.conteudosSelecionados).isEmpty()
    }

    @Test
    fun `excluir selecionados pede confirmacao antes de remover`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoLongPress(1L))

        viewModel.onAction(ConcursoDetailAction.OnConteudoExcluirSelecionadosClick)

        val state = viewModel.state.value
        assertThat(state.isConteudoExcluirSelecionadosVisible).isTrue()
        assertThat(state.conteudos.map { it.id }).isEqualTo(listOf(1L, 2L))
    }

    @Test
    fun `cancelar a confirmacao mantem os conteudos e a selecao`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoLongPress(1L))
        viewModel.onAction(ConcursoDetailAction.OnConteudoExcluirSelecionadosClick)

        viewModel.onAction(ConcursoDetailAction.OnConteudoExcluirSelecionadosDismiss)

        val state = viewModel.state.value
        assertThat(state.isConteudoExcluirSelecionadosVisible).isFalse()
        assertThat(state.conteudosSelecionados).isEqualTo(setOf(1L))
        assertThat(state.conteudos.map { it.id }).isEqualTo(listOf(1L, 2L))
    }

    @Test
    fun `confirmar remove os selecionados, sai da selecao e avisa quantos foram excluidos`() = runTest {
        viewModel.onAction(ConcursoDetailAction.OnConteudoLongPress(1L))
        viewModel.onAction(ConcursoDetailAction.OnConteudoTapped(2L))
        viewModel.onAction(ConcursoDetailAction.OnConteudoExcluirSelecionadosClick)

        viewModel.events.test {
            viewModel.onAction(ConcursoDetailAction.OnConteudoExcluirSelecionadosConfirm)

            val message = (awaitItem() as ConcursoDetailEvent.ShowMessage).message as UiText.PluralResource
            assertThat(message.quantity).isEqualTo(2)
        }
        val state = viewModel.state.value
        assertThat(state.conteudos).isEmpty()
        assertThat(state.conteudosSelecionados).isEmpty()
        assertThat(state.isConteudoExcluirSelecionadosVisible).isFalse()
    }

    @Test
    fun `falha ao excluir selecionados mantem a selecao e emite ShowError`() = runTest {
        viewModel.onAction(ConcursoDetailAction.OnConteudoLongPress(1L))
        viewModel.onAction(ConcursoDetailAction.OnConteudoExcluirSelecionadosClick)
        conteudoRepository.shouldReturnError = true

        viewModel.events.test {
            viewModel.onAction(ConcursoDetailAction.OnConteudoExcluirSelecionadosConfirm)
            assertThat(awaitItem()).isInstanceOf(ConcursoDetailEvent.ShowError::class)
        }
        assertThat(viewModel.state.value.conteudosSelecionados).isEqualTo(setOf(1L))
    }

    @Test
    fun `conteudo removido por fora sai da selecao`() {
        viewModel.onAction(ConcursoDetailAction.OnConteudoLongPress(1L))
        viewModel.onAction(ConcursoDetailAction.OnConteudoTapped(2L))

        conteudoRepository.setConteudos(listOf(sampleConteudo(id = 2L, eixo = 2)))

        assertThat(viewModel.state.value.conteudosSelecionados).isEqualTo(setOf(2L))
    }

    private fun sampleConcurso(
        id: Long = 1L,
        nome: String = "Concurso de teste",
        nivel: Nivel = Nivel.MEDIO,
        dataProva: Long = 1_700_000_000_000L
    ) = Concurso(id = id, nome = nome, nivel = nivel, dataProva = dataProva)

    private fun sampleConteudo(
        id: Long,
        eixo: Int? = null,
        bloco: Int? = null
    ) = Conteudo(
        id = id,
        concursoId = 1L,
        materia = "Matéria $id",
        descricao = "Descrição $id",
        eixo = eixo,
        bloco = bloco
    )
}
