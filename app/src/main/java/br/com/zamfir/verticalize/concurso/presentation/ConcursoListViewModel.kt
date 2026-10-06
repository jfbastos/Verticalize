package br.com.zamfir.verticalize.concurso.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.auth.domain.AuthError
import br.com.zamfir.verticalize.auth.domain.AuthRepository
import br.com.zamfir.verticalize.auth.presentation.messageRes
import br.com.zamfir.verticalize.auth.presentation.toContaUi
import br.com.zamfir.verticalize.concurso.domain.Concurso
import br.com.zamfir.verticalize.concurso.domain.ConcursoRepository
import br.com.zamfir.verticalize.concurso.domain.Nivel
import br.com.zamfir.verticalize.core.domain.Result
import br.com.zamfir.verticalize.core.domain.onError
import br.com.zamfir.verticalize.core.domain.onSuccess
import br.com.zamfir.verticalize.core.presentation.UiText
import br.com.zamfir.verticalize.core.presentation.filterDigits
import br.com.zamfir.verticalize.core.domain.formatDate
import br.com.zamfir.verticalize.importacao.domain.ConcursoExporter
import br.com.zamfir.verticalize.importacao.domain.ExportError
import br.com.zamfir.verticalize.importacao.domain.NOME_DO_ARQUIVO_DE_TODOS_OS_CONCURSOS
import br.com.zamfir.verticalize.importacao.presentation.ExportFeedback
import br.com.zamfir.verticalize.importacao.presentation.messageRes
import br.com.zamfir.verticalize.importacao.presentation.toFeedback
import br.com.zamfir.verticalize.sync.domain.AcaoSyncInicial
import br.com.zamfir.verticalize.sync.domain.CloudSyncRepository
import br.com.zamfir.verticalize.sync.domain.EscolhaConflito
import br.com.zamfir.verticalize.sync.domain.ResultadoSyncInicial
import br.com.zamfir.verticalize.sync.domain.SyncError
import br.com.zamfir.verticalize.sync.presentation.ConflitoSyncUi
import br.com.zamfir.verticalize.sync.presentation.messageRes
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConcursoListViewModel(
    private val repository: ConcursoRepository,
    private val exporter: ConcursoExporter,
    private val authRepository: AuthRepository,
    private val cloudSync: CloudSyncRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ConcursoListState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<ConcursoListEvent>()
    val events = eventChannel.receiveAsFlow()

    /** Usuário logado no momento; null sem conta. */
    private var uidAtual: String? = null

    private var syncJob: Job? = null

    init {
        viewModelScope.launch {
            repository.observeConcursos().collect { concursos ->
                _state.update {
                    it.copy(
                        concursos = concursos.map(Concurso::toConcursoUi),
                        isLoading = false
                    )
                }
            }
        }
        viewModelScope.launch {
            authRepository.observeCurrentUser().collect { user ->
                uidAtual = user?.id
                _state.update { it.copy(conta = user?.toContaUi()) }
                // Cobre o login e também a reabertura do app quando a conciliação anterior falhou.
                if (user != null && !cloudSync.isSincronizacaoAtiva(user.id)) {
                    prepararSincronizacao(user.id)
                }
            }
        }
    }

    fun onAction(action: ConcursoListAction) {
        when (action) {
            ConcursoListAction.OnFabClick -> openCreateDialog()
            is ConcursoListAction.OnConcursoSwipeEdit -> openEditDialog(action.id)
            ConcursoListAction.OnMoreMenuToggle -> _state.update { it.copy(isMoreMenuExpanded = !it.isMoreMenuExpanded) }
            ConcursoListAction.OnMoreMenuDismiss -> _state.update { it.copy(isMoreMenuExpanded = false) }
            ConcursoListAction.OnImportClick -> _state.update {
                it.copy(isImportFormatDialogVisible = true, isMoreMenuExpanded = false)
            }
            ConcursoListAction.OnImportDismiss -> _state.update { it.copy(isImportFormatDialogVisible = false) }
            ConcursoListAction.OnImportPickFileClick -> openImportPicker()
            ConcursoListAction.OnExportClick -> requestExport()
            is ConcursoListAction.OnExportFileSelected -> export(action.uri)
            is ConcursoListAction.OnConcursoSwipeDelete -> _state.update {
                it.copy(isDeleteConfirmationVisible = true, concursoPendingDeleteId = action.id)
            }
            ConcursoListAction.OnDeleteConfirm -> deletePendingConcurso()
            ConcursoListAction.OnDeleteDismiss -> _state.update {
                it.copy(isDeleteConfirmationVisible = false, concursoPendingDeleteId = null)
            }
            is ConcursoListAction.OnConcursoTapped -> navigateToDetail(action.id)
            ConcursoListAction.OnDismissDialog -> _state.update { it.copy(isDialogVisible = false) }
            is ConcursoListAction.OnNomeChanged -> _state.update { it.copy(nome = action.value) }
            ConcursoListAction.OnNivelDropdownToggle -> _state.update {
                it.copy(isNivelDropdownExpanded = !it.isNivelDropdownExpanded)
            }
            is ConcursoListAction.OnNivelSelected -> _state.update {
                it.copy(nivel = action.nivel, isNivelDropdownExpanded = false)
            }
            ConcursoListAction.OnDataProvaFieldClick -> _state.update { it.copy(isDatePickerVisible = true) }
            is ConcursoListAction.OnDatePickerConfirm -> _state.update {
                it.copy(
                    dataProvaMillis = action.millis,
                    dataProvaFormatted = formatDate(action.millis),
                    isDatePickerVisible = false
                )
            }
            ConcursoListAction.OnDataProvaClearClick -> _state.update {
                it.copy(dataProvaMillis = null, dataProvaFormatted = "")
            }
            ConcursoListAction.OnDatePickerDismiss -> _state.update { it.copy(isDatePickerVisible = false) }
            is ConcursoListAction.OnValorInscricaoChanged -> _state.update {
                it.copy(valorInscricaoInput = action.rawInput.filterDigits(10))
            }
            is ConcursoListAction.OnBancaChanged -> _state.update { it.copy(banca = action.value) }
            ConcursoListAction.OnSaveClick -> save()
            ConcursoListAction.OnContaClick -> onContaClick()
            ConcursoListAction.OnContaMenuDismiss -> _state.update { it.copy(isContaMenuExpanded = false) }
            ConcursoListAction.OnSignOutClick -> signOut()
            is ConcursoListAction.OnGoogleIdTokenReceived -> signInWithGoogle(action.idToken)
            is ConcursoListAction.OnGoogleSignInFailed -> onSignInFailed(action.error)
            ConcursoListAction.OnSyncConflitoUsarNuvem -> resolverConflito(EscolhaConflito.USAR_NUVEM)
            ConcursoListAction.OnSyncConflitoManterAparelho -> resolverConflito(EscolhaConflito.MANTER_APARELHO)
        }
    }

    private fun prepararSincronizacao(uid: String) {
        if (syncJob?.isActive == true) return
        syncJob = viewModelScope.launch {
            _state.update { it.copy(isSincronizando = true) }
            val resultado = cloudSync.prepararSincronizacao(uid)
            _state.update { it.copy(isSincronizando = false) }
            when (resultado) {
                is Result.Error -> showSyncError(resultado.error)
                is Result.Success -> when (val r = resultado.data) {
                    is ResultadoSyncInicial.Concluida -> showSyncConcluida(r.acao)
                    is ResultadoSyncInicial.Conflito -> _state.update {
                        it.copy(conflitoSync = ConflitoSyncUi(aparelho = r.aparelho, nuvem = r.nuvem))
                    }
                }
            }
        }
    }

    private fun resolverConflito(escolha: EscolhaConflito) {
        val uid = uidAtual ?: return
        if (syncJob?.isActive == true) return
        _state.update { it.copy(conflitoSync = null) }
        syncJob = viewModelScope.launch {
            _state.update { it.copy(isSincronizando = true) }
            val resultado = cloudSync.resolverConflito(uid, escolha)
            _state.update { it.copy(isSincronizando = false) }
            resultado
                .onSuccess { showSyncConcluida(it) }
                .onError { showSyncError(it) }
        }
    }

    private suspend fun showSyncConcluida(acao: AcaoSyncInicial) {
        val messageRes = acao.messageRes() ?: return
        eventChannel.send(ConcursoListEvent.ShowMessage(UiText.StringResource(messageRes)))
    }

    private suspend fun showSyncError(error: SyncError) {
        eventChannel.send(ConcursoListEvent.ShowError(UiText.StringResource(error.messageRes())))
    }

    private fun onContaClick() {
        val current = _state.value
        when {
            current.isSigningIn -> Unit
            current.conta != null -> _state.update { it.copy(isContaMenuExpanded = true) }
            else -> {
                _state.update { it.copy(isSigningIn = true) }
                viewModelScope.launch { eventChannel.send(ConcursoListEvent.LaunchGoogleSignIn) }
            }
        }
    }

    private fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            authRepository.signInWithGoogle(idToken)
                .onSuccess { user ->
                    // A conta em si chega ao estado pelo observeCurrentUser; aqui só confirma para o usuário.
                    val nome = user.nome
                    val message = if (nome.isNullOrBlank()) {
                        UiText.StringResource(R.string.conta_login_sucesso)
                    } else {
                        UiText.StringResource(R.string.conta_login_sucesso_format, listOf(nome))
                    }
                    _state.update { it.copy(isSigningIn = false) }
                    eventChannel.send(ConcursoListEvent.ShowMessage(message))
                }
                .onError { onSignInFailed(it) }
        }
    }

    private fun onSignInFailed(error: AuthError) {
        _state.update { it.copy(isSigningIn = false) }
        val messageRes = error.messageRes() ?: return
        viewModelScope.launch {
            eventChannel.send(ConcursoListEvent.ShowError(UiText.StringResource(messageRes)))
        }
    }

    private fun signOut() {
        syncJob?.cancel()
        _state.update { it.copy(isContaMenuExpanded = false, conflitoSync = null, isSincronizando = false) }
        // Antes do signOut: nenhuma alteração local depois daqui deve ir para a nuvem desta conta.
        cloudSync.desativar()
        viewModelScope.launch {
            authRepository.signOut()
            eventChannel.send(ConcursoListEvent.ShowMessage(UiText.StringResource(R.string.conta_logout_sucesso)))
        }
    }

    private fun openImportPicker() {
        _state.update { it.copy(isImportFormatDialogVisible = false) }
        viewModelScope.launch {
            eventChannel.send(ConcursoListEvent.OpenImportPicker)
        }
    }

    private fun requestExport() {
        _state.update { it.copy(isMoreMenuExpanded = false) }
        viewModelScope.launch {
            // Sem concursos não há o que exportar: avisa antes de abrir o seletor de destino.
            if (_state.value.concursos.isEmpty()) {
                eventChannel.send(ConcursoListEvent.ShowError(UiText.StringResource(ExportError.SEM_DADOS.messageRes())))
            } else {
                eventChannel.send(ConcursoListEvent.OpenExportPicker(NOME_DO_ARQUIVO_DE_TODOS_OS_CONCURSOS))
            }
        }
    }

    private fun export(uri: String) {
        viewModelScope.launch {
            val feedback = exporter.exportar(uri).toFeedback { resumo ->
                UiText.StringResource(R.string.export_success_format, listOf(resumo.concursos, resumo.conteudos, resumo.estudos))
            }
            eventChannel.send(
                when (feedback) {
                    is ExportFeedback.Success -> ConcursoListEvent.ShowMessage(feedback.message)
                    is ExportFeedback.Failure -> ConcursoListEvent.ShowError(feedback.message)
                }
            )
        }
    }

    private fun navigateToDetail(id: Long) {
        viewModelScope.launch {
            eventChannel.send(ConcursoListEvent.NavigateToDetail(id))
        }
    }

    private fun openCreateDialog() {
        _state.update {
            it.copy(
                isDialogVisible = true,
                editingId = null,
                editingHorasEstudadas = 0,
                editingPercentualCompletude = 0,
                nome = "",
                nivel = Nivel.MEDIO,
                isNivelDropdownExpanded = false,
                dataProvaMillis = null,
                dataProvaFormatted = "",
                valorInscricaoInput = "",
                banca = ""
            )
        }
    }

    private fun openEditDialog(id: Long) {
        viewModelScope.launch {
            repository.getConcursoById(id)
                .onSuccess { concurso ->
                    _state.update {
                        it.copy(
                            isDialogVisible = true,
                            editingId = concurso.id,
                            editingHorasEstudadas = concurso.horasEstudadas,
                            editingPercentualCompletude = concurso.percentualCompletude,
                            nome = concurso.nome,
                            nivel = concurso.nivel,
                            dataProvaMillis = concurso.dataProva,
                            dataProvaFormatted = concurso.dataProva?.let(::formatDate) ?: "",
                            valorInscricaoInput = concurso.valorInscricaoCentavos.toString(),
                            banca = concurso.banca
                        )
                    }
                }
                .onError {
                    eventChannel.send(ConcursoListEvent.ShowError(UiText.StringResource(R.string.error_not_found)))
                }
        }
    }

    private fun deletePendingConcurso() {
        val id = _state.value.concursoPendingDeleteId ?: return

        viewModelScope.launch {
            repository.deleteConcurso(id)
                .onSuccess {
                    _state.update { it.copy(isDeleteConfirmationVisible = false, concursoPendingDeleteId = null) }
                }
                .onError {
                    _state.update { it.copy(isDeleteConfirmationVisible = false, concursoPendingDeleteId = null) }
                    eventChannel.send(ConcursoListEvent.ShowError(UiText.StringResource(R.string.error_unknown)))
                }
        }
    }

    private fun save() {
        val current = _state.value
        if (!current.isSaveEnabled) return

        viewModelScope.launch {
            // percentualCompletude agora é recalculado em segundo plano (ver ConcursoDetailViewModel)
            // a partir dos conteúdos, então busca o valor mais recente aqui em vez de reusar o
            // snapshot capturado quando o diálogo foi aberto, que pode estar desatualizado.
            val percentualAtual = current.editingId?.let { id ->
                when (val result = repository.getConcursoById(id)) {
                    is Result.Success -> result.data.percentualCompletude
                    is Result.Error -> current.editingPercentualCompletude
                }
            } ?: current.editingPercentualCompletude

            val concurso = Concurso(
                id = current.editingId ?: 0L,
                nome = current.nome.trim(),
                nivel = current.nivel,
                dataProva = current.dataProvaMillis,
                valorInscricaoCentavos = current.valorInscricaoInput.toLongOrNull() ?: 0L,
                banca = current.banca.trim(),
                horasEstudadas = current.editingHorasEstudadas,
                percentualCompletude = percentualAtual
            )

            repository.upsertConcurso(concurso)
                .onSuccess { _state.update { it.copy(isDialogVisible = false) } }
                .onError {
                    eventChannel.send(ConcursoListEvent.ShowError(UiText.StringResource(R.string.error_unknown)))
                }
        }
    }
}
