package br.com.zamfir.verticalize.concurso.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.auth.data.GoogleIdTokenRequester
import br.com.zamfir.verticalize.auth.domain.AuthError
import br.com.zamfir.verticalize.auth.presentation.components.ContaActionButton
import br.com.zamfir.verticalize.core.domain.onError
import br.com.zamfir.verticalize.core.domain.onSuccess
import br.com.zamfir.verticalize.concurso.domain.Nivel
import br.com.zamfir.verticalize.concurso.presentation.components.ConcursoDeleteConfirmationDialog
import br.com.zamfir.verticalize.concurso.presentation.components.ConcursoFormDialog
import br.com.zamfir.verticalize.concurso.presentation.components.ConcursoListItem
import br.com.zamfir.verticalize.core.presentation.ObserveAsEvents
import br.com.zamfir.verticalize.core.presentation.asString
import br.com.zamfir.verticalize.core.presentation.components.EmptyState
import br.com.zamfir.verticalize.importacao.presentation.components.ImportFormatDialog
import br.com.zamfir.verticalize.importacao.presentation.components.rememberExportFilePicker
import br.com.zamfir.verticalize.sync.presentation.components.SyncConflitoDialog
import br.com.zamfir.verticalize.ui.theme.VerticalizeTheme
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import org.koin.compose.koinInject
import org.koin.androidx.compose.koinViewModel

// Alguns gerenciadores de arquivos reportam .txt/.csv com tipos diferentes; "text/*" cobre todos.
private const val IMPORT_FILE_MIME_TYPE = "text/*"

@Composable
fun ConcursoListRoot(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToImport: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConcursoListViewModel = koinViewModel(),
    googleIdTokenRequester: GoogleIdTokenRequester = koinInject()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // O resultado do seletor de arquivos é só navegação: não passa pelo ViewModel.
    val importFilePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) onNavigateToImport(uri.toString())
    }

    // O usuário escolhe onde salvar; a gravação em si é feita pelo ViewModel.
    val openExportPicker = rememberExportFilePicker { uri ->
        viewModel.onAction(ConcursoListAction.OnExportFileSelected(uri))
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ConcursoListEvent.ShowError -> scope.launch {
                snackbarHostState.showSnackbar(event.message.asString(context))
            }
            is ConcursoListEvent.ShowMessage -> scope.launch {
                snackbarHostState.showSnackbar(event.message.asString(context))
            }
            is ConcursoListEvent.NavigateToDetail -> onNavigateToDetail(event.concursoId)
            ConcursoListEvent.OpenImportPicker -> importFilePicker.launch(arrayOf(IMPORT_FILE_MIME_TYPE))
            is ConcursoListEvent.OpenExportPicker -> openExportPicker(event.fileName)
            // O seletor de contas precisa da Activity; o token obtido segue para o ViewModel autenticar.
            ConcursoListEvent.LaunchGoogleSignIn -> scope.launch {
                try {
                    googleIdTokenRequester.requestIdToken(context)
                        .onSuccess { viewModel.onAction(ConcursoListAction.OnGoogleIdTokenReceived(it)) }
                        .onError { viewModel.onAction(ConcursoListAction.OnGoogleSignInFailed(it)) }
                } catch (e: CancellationException) {
                    // A tela saiu de composição (ex.: rotação) com o seletor aberto: libera o botão de login.
                    viewModel.onAction(ConcursoListAction.OnGoogleSignInFailed(AuthError.CANCELADO))
                    throw e
                }
            }
        }
    }

    ConcursoListScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConcursoListScreen(
    state: ConcursoListState,
    onAction: (ConcursoListAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()
    // O FAB começa estendido (com texto) e recolhe para só o ícone assim que a lista rola.
    val isFabExpanded by remember { derivedStateOf { !listState.canScrollBackward } }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.concurso_list_title)) },
                actions = {
                    ContaActionButton(
                        conta = state.conta,
                        // A sincronização logo após o login conta como parte do "entrando".
                        isSigningIn = state.isSigningIn || state.isSincronizando,
                        isMenuExpanded = state.isContaMenuExpanded,
                        onClick = { onAction(ConcursoListAction.OnContaClick) },
                        onMenuDismiss = { onAction(ConcursoListAction.OnContaMenuDismiss) },
                        onSignOutClick = { onAction(ConcursoListAction.OnSignOutClick) }
                    )
                    // Importar/exportar são ações pouco frequentes: ficam num menu para não lotar a barra.
                    Box {
                        IconButton(onClick = { onAction(ConcursoListAction.OnMoreMenuToggle) }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = stringResource(R.string.concurso_list_more_cd)
                            )
                        }
                        DropdownMenu(
                            expanded = state.isMoreMenuExpanded,
                            onDismissRequest = { onAction(ConcursoListAction.OnMoreMenuDismiss) }
                        ) {
                            // Seta para baixo = trazer dados para o app (importar); seta para cima = levar para fora (exportar).
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.concurso_import_menu)) },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Filled.FileDownload, contentDescription = null)
                                },
                                onClick = { onAction(ConcursoListAction.OnImportClick) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.concurso_export_menu)) },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Filled.FileUpload, contentDescription = null)
                                },
                                onClick = { onAction(ConcursoListAction.OnExportClick) }
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAction(ConcursoListAction.OnFabClick) },
                expanded = isFabExpanded,
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.concurso_fab_add_cd)
                    )
                },
                text = { Text(stringResource(R.string.concurso_fab_extended_label)) }
            )
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

            state.concursos.isEmpty() -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = Icons.Filled.School,
                    title = stringResource(R.string.empty_state_message),
                    message = stringResource(R.string.empty_state_hint)
                )
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    // Espaço extra embaixo para o último card não ficar escondido atrás do FAB.
                    bottom = innerPadding.calculateBottomPadding() + 88.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.concursos, key = { it.id }) { concurso ->
                    ConcursoListItem(
                        concurso = concurso,
                        onEditRequested = { onAction(ConcursoListAction.OnConcursoSwipeEdit(concurso.id)) },
                        onDeleteRequested = { onAction(ConcursoListAction.OnConcursoSwipeDelete(concurso.id)) },
                        onDetailRequested = { onAction(ConcursoListAction.OnConcursoTapped(concurso.id)) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }

    if (state.isDialogVisible) {
        ConcursoFormDialog(state = state, onAction = onAction)
    }

    if (state.isDeleteConfirmationVisible) {
        ConcursoDeleteConfirmationDialog(
            onConfirm = { onAction(ConcursoListAction.OnDeleteConfirm) },
            onDismiss = { onAction(ConcursoListAction.OnDeleteDismiss) }
        )
    }

    if (state.isImportFormatDialogVisible) {
        ImportFormatDialog(
            onPickFile = { onAction(ConcursoListAction.OnImportPickFileClick) },
            onDismiss = { onAction(ConcursoListAction.OnImportDismiss) }
        )
    }

    state.conflitoSync?.let { conflito ->
        SyncConflitoDialog(
            conflito = conflito,
            onUsarNuvem = { onAction(ConcursoListAction.OnSyncConflitoUsarNuvem) },
            onManterAparelho = { onAction(ConcursoListAction.OnSyncConflitoManterAparelho) }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConcursoListScreenPreview() {
    VerticalizeTheme {
        ConcursoListScreen(
            state = ConcursoListState(
                isLoading = false,
                concursos = listOf(
                    ConcursoUi(
                        id = 1L,
                        nome = "Concurso TRT 2ª Região",
                        nivel = Nivel.SUPERIOR,
                        horasEstudadas = 12,
                        percentualCompletude = 35
                    ),
                    ConcursoUi(
                        id = 2L,
                        nome = "Banco do Brasil - Escriturário",
                        nivel = Nivel.MEDIO,
                        horasEstudadas = 48,
                        percentualCompletude = 72
                    )
                )
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConcursoListScreenEmptyPreview() {
    VerticalizeTheme {
        ConcursoListScreen(
            state = ConcursoListState(isLoading = false),
            onAction = {}
        )
    }
}
