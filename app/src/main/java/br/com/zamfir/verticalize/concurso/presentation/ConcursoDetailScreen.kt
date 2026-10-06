package br.com.zamfir.verticalize.concurso.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.concurso.domain.Nivel
import br.com.zamfir.verticalize.concurso.presentation.components.ConcursoDetailHeaderCard
import br.com.zamfir.verticalize.conteudo.presentation.ConteudoAgrupamento
import br.com.zamfir.verticalize.conteudo.presentation.ConteudoUi
import br.com.zamfir.verticalize.conteudo.presentation.components.ConteudoAgrupamentoBottomSheet
import br.com.zamfir.verticalize.conteudo.presentation.components.ConteudoDetailDialog
import br.com.zamfir.verticalize.conteudo.presentation.components.ConteudoFiltroBottomSheet
import br.com.zamfir.verticalize.conteudo.presentation.components.ConteudoFormDialog
import br.com.zamfir.verticalize.conteudo.presentation.components.ConteudoListItem
import br.com.zamfir.verticalize.conteudo.presentation.components.ConteudoOrdenacaoBottomSheet
import br.com.zamfir.verticalize.core.presentation.ObserveAsEvents
import br.com.zamfir.verticalize.core.presentation.asString
import br.com.zamfir.verticalize.core.presentation.components.EmptyState
import br.com.zamfir.verticalize.core.presentation.components.SectionHeader
import br.com.zamfir.verticalize.core.presentation.components.VerticalizeDeleteDialog
import br.com.zamfir.verticalize.importacao.presentation.components.rememberExportFilePicker
import br.com.zamfir.verticalize.registroestudo.presentation.RegistroEstudoListAction
import br.com.zamfir.verticalize.registroestudo.presentation.RegistroEstudoListContent
import br.com.zamfir.verticalize.registroestudo.presentation.RegistroEstudoListEvent
import br.com.zamfir.verticalize.registroestudo.presentation.RegistroEstudoListState
import br.com.zamfir.verticalize.registroestudo.presentation.RegistroEstudoListViewModel
import br.com.zamfir.verticalize.registroestudo.presentation.components.EstudoTimerStartDialog
import br.com.zamfir.verticalize.registroestudo.presentation.components.RegistroEstudoDeleteConfirmationDialog
import br.com.zamfir.verticalize.registroestudo.presentation.components.RegistroEstudoFormDialog
import br.com.zamfir.verticalize.registroestudo.presentation.components.RegistroOpcaoDialog
import br.com.zamfir.verticalize.ui.theme.VerticalizeTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private const val HEADER_ITEM_KEY = "header"
private const val SECTION_ITEM_KEY = "section"
private const val EMPTY_ITEM_KEY = "empty"

@Composable
fun ConcursoDetailRoot(
    concursoId: Long,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConcursoDetailViewModel = koinViewModel { parametersOf(concursoId) },
    registroEstudoViewModel: RegistroEstudoListViewModel = koinViewModel { parametersOf(concursoId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val registroEstudoState by registroEstudoViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val openExportPicker = rememberExportFilePicker { uri ->
        viewModel.onAction(ConcursoDetailAction.OnExportFileSelected(uri))
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ConcursoDetailEvent.ShowError -> scope.launch {
                snackbarHostState.showSnackbar(event.message.asString(context))
            }
            is ConcursoDetailEvent.ShowMessage -> scope.launch {
                snackbarHostState.showSnackbar(event.message.asString(context))
            }
            is ConcursoDetailEvent.OpenExportPicker -> openExportPicker(event.fileName)
        }
    }

    ObserveAsEvents(registroEstudoViewModel.events) { event ->
        when (event) {
            is RegistroEstudoListEvent.ShowError -> scope.launch {
                snackbarHostState.showSnackbar(event.message.asString(context))
            }
            is RegistroEstudoListEvent.ShowMessage -> scope.launch {
                snackbarHostState.showSnackbar(event.message.asString(context))
            }
        }
    }

    ConcursoDetailScreen(
        state = state,
        onAction = viewModel::onAction,
        registroEstudoState = registroEstudoState,
        onRegistroEstudoAction = registroEstudoViewModel::onAction,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConcursoDetailScreen(
    state: ConcursoDetailState,
    onAction: (ConcursoDetailAction) -> Unit,
    registroEstudoState: RegistroEstudoListState,
    onRegistroEstudoAction: (RegistroEstudoListAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    val registroEstudoListState = rememberLazyListState()
    val isFabExpanded by remember {
        derivedStateOf {
            val activeListState = if (state.selectedTab == ConcursoDetailTab.CONTEUDOS) listState else registroEstudoListState
            !activeListState.canScrollBackward
        }
    }

    val quantidadeSelecionada = when (state.selectedTab) {
        ConcursoDetailTab.CONTEUDOS -> state.conteudosSelecionados.size
        ConcursoDetailTab.HORAS_ESTUDO -> registroEstudoState.registrosSelecionados.size
    }
    val emSelecao = quantidadeSelecionada > 0
    val cancelarSelecao = {
        when (state.selectedTab) {
            ConcursoDetailTab.CONTEUDOS -> onAction(ConcursoDetailAction.OnConteudoSelecaoCancelar)
            ConcursoDetailTab.HORAS_ESTUDO -> onRegistroEstudoAction(RegistroEstudoListAction.OnSelecaoCancelar)
        }
    }


    val selecionarAba = { tab: ConcursoDetailTab ->
        onAction(ConcursoDetailAction.OnTabSelected(tab))
        onRegistroEstudoAction(RegistroEstudoListAction.OnSelecaoCancelar)
    }

    BackHandler(enabled = emSelecao, onBack = cancelarSelecao)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (emSelecao) {
                SelecaoTopAppBar(
                    quantidade = quantidadeSelecionada,
                    onCancelar = cancelarSelecao,
                    onSelecionarTodos = {
                        when (state.selectedTab) {
                            ConcursoDetailTab.CONTEUDOS -> onAction(ConcursoDetailAction.OnConteudoSelecionarTodosClick)
                            ConcursoDetailTab.HORAS_ESTUDO ->
                                onRegistroEstudoAction(RegistroEstudoListAction.OnSelecionarTodosClick)
                        }
                    },
                    onExcluir = {
                        when (state.selectedTab) {
                            ConcursoDetailTab.CONTEUDOS -> onAction(ConcursoDetailAction.OnConteudoExcluirSelecionadosClick)
                            ConcursoDetailTab.HORAS_ESTUDO ->
                                onRegistroEstudoAction(RegistroEstudoListAction.OnExcluirSelecionadosClick)
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            } else {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            text = state.concurso?.nome.orEmpty(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_nav_back)
                            )
                        }
                    },
                    actions = {
                        if (state.concurso != null) {
                            IconButton(onClick = { onAction(ConcursoDetailAction.OnConteudoFiltroClick) }) {
                                Icon(
                                    imageVector = Icons.Filled.FilterList,
                                    contentDescription = stringResource(R.string.conteudo_filtro_cd)
                                )
                            }
                            IconButton(onClick = { onAction(ConcursoDetailAction.OnConteudoOrdenacaoClick) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = stringResource(R.string.conteudo_ordenacao_cd)
                                )
                            }

                            Box {
                                IconButton(onClick = { onAction(ConcursoDetailAction.OnMoreMenuToggle) }) {
                                    Icon(
                                        imageVector = Icons.Filled.MoreVert,
                                        contentDescription = stringResource(R.string.concurso_detail_more_cd)
                                    )
                                }
                                DropdownMenu(
                                    expanded = state.isMoreMenuExpanded,
                                    onDismissRequest = { onAction(ConcursoDetailAction.OnMoreMenuDismiss) }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.concurso_detail_agrupar_menu)) },
                                        leadingIcon = {
                                            Icon(imageVector = Icons.Filled.Layers, contentDescription = null)
                                        },
                                        onClick = { onAction(ConcursoDetailAction.OnConteudoAgrupamentoClick) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.concurso_detail_export_menu)) },
                                        leadingIcon = {
                                            Icon(imageVector = Icons.Filled.FileUpload, contentDescription = null)
                                        },
                                        onClick = { onAction(ConcursoDetailAction.OnExportClick) }
                                    )
                                }
                            }
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (state.concurso != null) {
                NavigationBar {
                    NavigationBarItem(
                        selected = state.selectedTab == ConcursoDetailTab.CONTEUDOS,
                        onClick = { selecionarAba(ConcursoDetailTab.CONTEUDOS) },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(R.string.concurso_detail_tab_conteudos)) }
                    )
                    NavigationBarItem(
                        selected = state.selectedTab == ConcursoDetailTab.HORAS_ESTUDO,
                        onClick = { selecionarAba(ConcursoDetailTab.HORAS_ESTUDO) },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(R.string.concurso_detail_tab_horas_estudo)) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (state.concurso != null && !emSelecao) {
                when (state.selectedTab) {
                    ConcursoDetailTab.CONTEUDOS -> ExtendedFloatingActionButton(
                        onClick = { onAction(ConcursoDetailAction.OnConteudoFabClick) },
                        expanded = isFabExpanded,
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = stringResource(R.string.conteudo_fab_add_cd)
                            )
                        },
                        text = { Text(stringResource(R.string.conteudo_fab_extended_label)) }
                    )

                    ConcursoDetailTab.HORAS_ESTUDO -> ExtendedFloatingActionButton(
                        onClick = { onRegistroEstudoAction(RegistroEstudoListAction.OnFabClick) },
                        expanded = isFabExpanded,
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = stringResource(R.string.registro_estudo_fab_cd)
                            )
                        },
                        text = { Text(stringResource(R.string.registro_estudo_fab_extended_label)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            state.concurso == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.error_not_found),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            state.selectedTab == ConcursoDetailTab.HORAS_ESTUDO -> RegistroEstudoListContent(
                state = registroEstudoState,
                onAction = onRegistroEstudoAction,
                contentPadding = innerPadding,
                listState = registroEstudoListState
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 88.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = HEADER_ITEM_KEY) {
                    ConcursoDetailHeaderCard(
                        concurso = state.concurso,
                        eixos = state.eixos,
                        totalMinutosAula = state.totalMinutosAula,
                        totalMinutosAulaAssistidos = state.totalMinutosAulaAssistidos,
                        isExpanded = state.isHeaderExpanded,
                        onToggleExpand = { onAction(ConcursoDetailAction.OnHeaderExpandToggle) }
                    )
                }

                item(key = SECTION_ITEM_KEY) {
                    SectionHeader(
                        title = stringResource(R.string.conteudo_section_title),
                        trailing = if (state.conteudos.isEmpty()) {
                            null
                        } else {
                            pluralStringResource(
                                R.plurals.conteudo_count_format,
                                state.conteudosExibidos.size,
                                state.conteudosExibidos.size
                            )
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (state.conteudos.isEmpty()) {
                    item(key = EMPTY_ITEM_KEY) {
                        EmptyState(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            title = stringResource(R.string.conteudo_empty_state_message),
                            message = stringResource(R.string.conteudo_empty_state_hint),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else if (state.conteudosExibidos.isEmpty()) {
                    item(key = EMPTY_ITEM_KEY) {
                        EmptyState(
                            icon = Icons.Filled.FilterList,
                            title = stringResource(R.string.conteudo_empty_filtro_message),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    state.conteudosAgrupados.forEach { grupo ->
                        val agrupado = state.conteudoAgrupamento != ConteudoAgrupamento.NENHUM
                        val chave = if (agrupado) grupo.chave(state.conteudoAgrupamento) else null
                        val recolhido = chave != null && chave !in state.conteudoGruposExpandidos

                        if (chave != null) {
                            item(key = "$SECTION_ITEM_KEY-$chave") {
                                SectionHeader(
                                    title = conteudoGrupoTitulo(grupo, state.conteudoAgrupamento),
                                    expanded = !recolhido,
                                    onExpandToggle = { onAction(ConcursoDetailAction.OnConteudoGrupoToggle(chave)) },
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                        if (!recolhido) {
                            items(grupo.itens, key = { it.id }) { conteudo ->
                                ConteudoListItem(
                                    conteudo = conteudo,
                                    onTapped = { onAction(ConcursoDetailAction.OnConteudoTapped(conteudo.id)) },
                                    onConcluidoToggle = { checked ->
                                        onAction(ConcursoDetailAction.OnConteudoConcluidoToggle(conteudo.id, checked))
                                    },
                                    onLongPress = { onAction(ConcursoDetailAction.OnConteudoLongPress(conteudo.id)) },
                                    emSelecao = state.isConteudoSelecaoAtiva,
                                    selecionado = conteudo.id in state.conteudosSelecionados,
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    val conteudoDetalhe = state.conteudoDetalhe
    if (conteudoDetalhe != null && !state.isConteudoDialogVisible) {
        ConteudoDetailDialog(
            conteudo = conteudoDetalhe,
            onEdit = { onAction(ConcursoDetailAction.OnConteudoDetalheEditClick) },
            onDismiss = { onAction(ConcursoDetailAction.OnConteudoDetalheDismiss) }
        )
    }

    if (state.isConteudoDialogVisible) {
        ConteudoFormDialog(state = state, onAction = onAction)
    }

    if (state.isConteudoFiltroSheetVisible) {
        ConteudoFiltroBottomSheet(state = state, onAction = onAction)
    }

    if (state.isConteudoOrdenacaoSheetVisible) {
        ConteudoOrdenacaoBottomSheet(state = state, onAction = onAction)
    }

    if (state.isConteudoAgrupamentoSheetVisible) {
        ConteudoAgrupamentoBottomSheet(state = state, onAction = onAction)
    }

    if (registroEstudoState.isDialogVisible) {
        RegistroEstudoFormDialog(state = registroEstudoState, onAction = onRegistroEstudoAction)
    }

    if (registroEstudoState.isDeleteConfirmationVisible) {
        RegistroEstudoDeleteConfirmationDialog(
            onConfirm = { onRegistroEstudoAction(RegistroEstudoListAction.OnDeleteConfirm) },
            onDismiss = { onRegistroEstudoAction(RegistroEstudoListAction.OnDeleteDismiss) }
        )
    }

    if (state.isConteudoExcluirSelecionadosVisible) {
        val quantidade = state.conteudosSelecionados.size
        VerticalizeDeleteDialog(
            title = pluralStringResource(R.plurals.conteudo_excluir_selecionados_title, quantidade, quantidade),
            message = stringResource(R.string.selecao_excluir_message),
            onConfirm = { onAction(ConcursoDetailAction.OnConteudoExcluirSelecionadosConfirm) },
            onDismiss = { onAction(ConcursoDetailAction.OnConteudoExcluirSelecionadosDismiss) }
        )
    }

    if (registroEstudoState.isExcluirSelecionadosVisible) {
        val quantidade = registroEstudoState.registrosSelecionados.size
        VerticalizeDeleteDialog(
            title = pluralStringResource(R.plurals.registro_estudo_excluir_selecionados_title, quantidade, quantidade),
            message = stringResource(R.string.selecao_excluir_message),
            onConfirm = { onRegistroEstudoAction(RegistroEstudoListAction.OnExcluirSelecionadosConfirm) },
            onDismiss = { onRegistroEstudoAction(RegistroEstudoListAction.OnExcluirSelecionadosDismiss) }
        )
    }

    if (registroEstudoState.isRegistroOpcaoSheetVisible) {
        RegistroOpcaoDialog(
            onManualClick = { onRegistroEstudoAction(RegistroEstudoListAction.OnRegistroManualOptionClick) },
            onCronometroClick = { onRegistroEstudoAction(RegistroEstudoListAction.OnRegistroCronometroOptionClick) },
            onDismiss = { onRegistroEstudoAction(RegistroEstudoListAction.OnRegistroOpcaoDismiss) }
        )
    }

    if (registroEstudoState.isTimerStartDialogVisible) {
        EstudoTimerStartDialog(state = registroEstudoState, onAction = onRegistroEstudoAction)
    }
}

/** Barra contextual do modo de seleção: quantos itens estão marcados e as ações sobre eles. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelecaoTopAppBar(
    quantidade: Int,
    onCancelar: () -> Unit,
    onSelecionarTodos: () -> Unit,
    onExcluir: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            scrolledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        title = { Text(pluralStringResource(R.plurals.selecao_count_format, quantidade, quantidade)) },
        navigationIcon = {
            IconButton(onClick = onCancelar) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.selecao_cancelar_cd)
                )
            }
        },
        actions = {
            IconButton(onClick = onSelecionarTodos) {
                Icon(
                    imageVector = Icons.Filled.SelectAll,
                    contentDescription = stringResource(R.string.selecao_selecionar_todos_cd)
                )
            }
            IconButton(onClick = onExcluir) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.selecao_excluir_cd)
                )
            }
        }
    )
}

/** Título do cabeçalho de um grupo: "Eixo N"/"Bloco N"/a matéria, ou "Sem eixo/bloco/matéria". */
@Composable
private fun conteudoGrupoTitulo(grupo: ConteudoGrupo, agrupamento: ConteudoAgrupamento): String {
    return when (agrupamento) {
        ConteudoAgrupamento.EIXO -> grupo.numero?.let { stringResource(R.string.eixo_label_format, it) }
            ?: stringResource(R.string.conteudo_grupo_sem_eixo)
        ConteudoAgrupamento.BLOCO -> grupo.numero?.let { stringResource(R.string.bloco_label_format, it) }
            ?: stringResource(R.string.conteudo_grupo_sem_bloco)
        ConteudoAgrupamento.MATERIA -> grupo.materia ?: stringResource(R.string.conteudo_grupo_sem_materia)
        ConteudoAgrupamento.NENHUM -> ""
    }
}

@Preview(showBackground = true)
@Composable
private fun ConcursoDetailScreenPreview() {
    VerticalizeTheme {
        ConcursoDetailScreen(
            state = ConcursoDetailState(
                isLoading = false,
                concurso = ConcursoDetailUi(
                    id = 1L,
                    nome = "Concurso TRT 2ª Região",
                    nivel = Nivel.SUPERIOR,
                    dataProvaFormatted = "15/03/2027",
                    diasParaProva = 42L,
                    horasEstudadas = 12,
                    percentualCompletude = 35
                ),
                eixos = listOf(EixoUi(numero = 1, percentualCompletude = 40), EixoUi(numero = 2, percentualCompletude = 65)),
                isHeaderExpanded = true,
                totalMinutosAula = 600,
                totalMinutosAulaAssistidos = 180,
                conteudos = listOf(
                    ConteudoUi(id = 1L, materia = "Direito Administrativo", descricao = "Princípios administrativos", eixo = 1, bloco = 2, concluido = false, quantidadeAulas = 5, tempoMedioAulaMinutos = 50, dataUltimaRevisaoFormatted = null, quantidadeQuestoes = 0),
                    ConteudoUi(id = 2L, materia = "Direito Constitucional", descricao = "Controle de constitucionalidade", eixo = 1, bloco = 1, concluido = true, quantidadeAulas = 3, tempoMedioAulaMinutos = 60, dataUltimaRevisaoFormatted = "10/09/2026", dataUltimaRevisaoMillis = 1_757_500_000_000L, quantidadeQuestoes = 0)
                )
            ),
            onAction = {},
            registroEstudoState = RegistroEstudoListState(),
            onRegistroEstudoAction = {},
            onNavigateBack = {}
        )
    }
}
