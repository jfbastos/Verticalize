package br.com.zamfir.verticalize.importacao.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import br.com.zamfir.verticalize.concurso.presentation.labelRes
import br.com.zamfir.verticalize.core.presentation.ObserveAsEvents
import br.com.zamfir.verticalize.core.presentation.UiText
import br.com.zamfir.verticalize.core.presentation.asString
import br.com.zamfir.verticalize.core.presentation.components.EmptyState
import br.com.zamfir.verticalize.core.presentation.components.InfoChip
import br.com.zamfir.verticalize.core.presentation.components.SectionHeader
import br.com.zamfir.verticalize.core.presentation.components.VerticalizeCard
import br.com.zamfir.verticalize.importacao.domain.ImportFileError
import br.com.zamfir.verticalize.importacao.domain.ImportSummary
import br.com.zamfir.verticalize.ui.theme.VerticalizeTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ImportRoot(
    fileUri: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ImportViewModel = koinViewModel { parametersOf(fileUri) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ImportEvent.ShowError -> scope.launch {
                snackbarHostState.showSnackbar(event.message.asString(context))
            }
            ImportEvent.NavigateBack -> onNavigateBack()
        }
    }

    ImportScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    state: ImportState,
    onAction: (ImportAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.import_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(ImportAction.OnCancelClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_nav_back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { ImportBottomBar(state = state, onAction = onAction) }
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(R.string.import_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            state.resultado != null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                ImportResultado(resultado = state.resultado)
            }

            else -> ImportContent(state = state, innerPadding = innerPadding)
        }
    }
}

@Composable
private fun ImportBottomBar(
    state: ImportState,
    onAction: (ImportAction) -> Unit
) {
    if (state.isLoading) return

    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                state.resultado != null -> Button(
                    onClick = { onAction(ImportAction.OnDoneClick) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.import_action_done))
                }

                state.fileError != null -> Button(
                    onClick = { onAction(ImportAction.OnCancelClick) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.import_action_back))
                }

                else -> {
                    OutlinedButton(
                        onClick = { onAction(ImportAction.OnCancelClick) },
                        enabled = !state.isImporting,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    Button(
                        onClick = { onAction(ImportAction.OnConfirmClick) },
                        enabled = state.isConfirmEnabled,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (state.isImporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(stringResource(R.string.import_action_import))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ImportContent(
    state: ImportState,
    innerPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding() + 8.dp,
            bottom = innerPadding.calculateBottomPadding() + 16.dp,
            start = 16.dp,
            end = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.fileError != null) {
            item(key = "file-error") {
                EmptyState(
                    icon = Icons.Filled.ErrorOutline,
                    title = stringResource(state.fileError.messageRes()),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            item(key = "resumo") { ResumoCard(resumo = state.resumo) }

            if (!state.resumo.temNovidades) {
                item(key = "nada-novo") {
                    Text(
                        text = stringResource(R.string.import_nada_novo),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            item(key = "secao-concursos") {
                SectionHeader(
                    title = stringResource(R.string.import_section_concursos),
                    trailing = state.concursos.size.toString(),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            // O mesmo nome pode aparecer com datas de prova diferentes, então a posição é a chave estável.
            itemsIndexed(
                items = state.concursos,
                key = { indice, _ -> "concurso-$indice" }
            ) { _, concurso ->
                ConcursoPreviewCard(concurso = concurso)
            }
        }

        if (state.errors.isNotEmpty()) {
            item(key = "secao-erros") {
                SectionHeader(
                    title = stringResource(R.string.import_section_erros),
                    trailing = state.errors.size.toString(),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            itemsIndexed(
                items = state.errors,
                key = { indice, _ -> "erro-$indice" }
            ) { _, erro ->
                ErroDeLinhaCard(erro = erro)
            }
        }
    }
}

@Composable
private fun ResumoCard(resumo: ImportResumoUi) {
    VerticalizeCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ResumoStat(
                    value = resumo.concursosNovos,
                    label = stringResource(R.string.import_resumo_concursos_novos),
                    highlight = true,
                    modifier = Modifier.weight(1f)
                )
                ResumoStat(
                    value = resumo.conteudosNovos,
                    label = stringResource(R.string.import_resumo_conteudos_novos),
                    highlight = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ResumoStat(
                    value = resumo.concursosExistentes,
                    label = stringResource(R.string.import_resumo_concursos_existentes),
                    highlight = false,
                    modifier = Modifier.weight(1f)
                )
                ResumoStat(
                    value = resumo.conteudosDuplicados,
                    label = stringResource(R.string.import_resumo_conteudos_duplicados),
                    highlight = false,
                    modifier = Modifier.weight(1f)
                )
            }
            // Horários de estudo só aparecem quando o arquivo traz um bloco [ESTUDOS].
            if (resumo.temEstudos) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ResumoStat(
                        value = resumo.estudosNovos,
                        label = stringResource(R.string.import_resumo_estudos_novos),
                        highlight = true,
                        modifier = Modifier.weight(1f)
                    )
                    ResumoStat(
                        value = resumo.estudosDuplicados,
                        label = stringResource(R.string.import_resumo_estudos_duplicados),
                        highlight = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ResumoStat(
    value: Int,
    label: String,
    highlight: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConcursoPreviewCard(concurso: ImportConcursoUi) {
    VerticalizeCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = concurso.nome,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (concurso.isNovo) {
                    InfoChip(
                        text = stringResource(R.string.import_badge_novo),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                } else {
                    InfoChip(text = stringResource(R.string.import_badge_existente))
                }
                InfoChip(text = stringResource(concurso.nivel.labelRes()))
                InfoChip(
                    text = pluralStringResource(
                        R.plurals.import_conteudos_novos_format,
                        concurso.conteudosNovos,
                        concurso.conteudosNovos
                    )
                )
                if (concurso.estudosNovos > 0) {
                    InfoChip(
                        text = pluralStringResource(
                            R.plurals.import_estudos_novos_format,
                            concurso.estudosNovos,
                            concurso.estudosNovos
                        )
                    )
                }
                if (concurso.conteudosDuplicados > 0) {
                    InfoChip(
                        text = pluralStringResource(
                            R.plurals.import_conteudos_duplicados_format,
                            concurso.conteudosDuplicados,
                            concurso.conteudosDuplicados
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ErroDeLinhaCard(erro: ImportLineErrorUi) {
    VerticalizeCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoChip(
                text = stringResource(R.string.import_linha_format, erro.linha),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = erro.mensagem.asString(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ImportResultado(resultado: ImportSummary) {
    val concursos = pluralStringResource(
        R.plurals.import_resultado_concursos_format,
        resultado.concursosCriados,
        resultado.concursosCriados
    )
    val conteudos = pluralStringResource(
        R.plurals.import_resultado_conteudos_format,
        resultado.conteudosCriados,
        resultado.conteudosCriados
    )
    val estudos = if (resultado.estudosCriados > 0) {
        pluralStringResource(
            R.plurals.import_resultado_estudos_format,
            resultado.estudosCriados,
            resultado.estudosCriados
        )
    } else {
        null
    }
    EmptyState(
        icon = Icons.Filled.CheckCircle,
        title = stringResource(R.string.import_concluido_title),
        message = listOfNotNull(concursos, conteudos, estudos).joinToString("\n")
    )
}

@Preview(showBackground = true)
@Composable
private fun ImportScreenPreview() {
    VerticalizeTheme {
        ImportScreen(
            state = ImportState(
                isLoading = false,
                resumo = ImportResumoUi(
                    concursosNovos = 1,
                    concursosExistentes = 1,
                    conteudosNovos = 12,
                    conteudosDuplicados = 3
                ),
                concursos = listOf(
                    ImportConcursoUi("TRT 2ª Região", Nivel.SUPERIOR, isNovo = true, conteudosNovos = 9, conteudosDuplicados = 0),
                    ImportConcursoUi("Banco do Brasil", Nivel.MEDIO, isNovo = false, conteudosNovos = 3, conteudosDuplicados = 3)
                ),
                errors = listOf(
                    ImportLineErrorUi(14, UiText.DynamicString("Data da prova inválida. Use o formato dd/mm/aaaa."))
                )
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ImportScreenErrorPreview() {
    VerticalizeTheme {
        ImportScreen(
            state = ImportState(isLoading = false, fileError = ImportFileError.NENHUM_CONCURSO),
            onAction = {}
        )
    }
}
