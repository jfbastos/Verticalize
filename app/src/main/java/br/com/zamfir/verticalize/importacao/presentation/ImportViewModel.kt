package br.com.zamfir.verticalize.importacao.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.core.domain.onError
import br.com.zamfir.verticalize.core.domain.onSuccess
import br.com.zamfir.verticalize.core.presentation.UiText
import br.com.zamfir.verticalize.importacao.domain.ImportFileError
import br.com.zamfir.verticalize.importacao.domain.ImportFileParser
import br.com.zamfir.verticalize.importacao.domain.ImportFileReader
import br.com.zamfir.verticalize.importacao.domain.ImportLineError
import br.com.zamfir.verticalize.importacao.domain.ImportPlan
import br.com.zamfir.verticalize.importacao.domain.ImportRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ImportViewModel(
    private val fileUri: String,
    private val fileReader: ImportFileReader,
    private val parser: ImportFileParser,
    private val repository: ImportRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ImportState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<ImportEvent>()
    val events = eventChannel.receiveAsFlow()

    // O plano fica só no ViewModel (não no state): a UI mostra apenas o resumo dele.
    private var plan: ImportPlan? = null

    init {
        viewModelScope.launch { carregarArquivo() }
    }

    fun onAction(action: ImportAction) {
        when (action) {
            ImportAction.OnConfirmClick -> confirmar()
            ImportAction.OnCancelClick, ImportAction.OnDoneClick -> viewModelScope.launch {
                eventChannel.send(ImportEvent.NavigateBack)
            }
        }
    }

    private suspend fun carregarArquivo() {
        fileReader.readText(fileUri)
            .onSuccess { texto -> analisar(texto) }
            .onError { erro -> mostrarErroDeArquivo(erro) }
    }

    private suspend fun analisar(texto: String) {
        if (texto.isBlank()) {
            mostrarErroDeArquivo(ImportFileError.ARQUIVO_VAZIO)
            return
        }

        val parsed = parser.parse(texto)
        val errosUi = parsed.errors.map(ImportLineError::toImportLineErrorUi)
        if (parsed.concursos.isEmpty()) {
            mostrarErroDeArquivo(ImportFileError.NENHUM_CONCURSO, errosUi)
            return
        }

        repository.analisar(parsed)
            .onSuccess { planoAnalisado ->
                plan = planoAnalisado
                _state.update {
                    it.copy(
                        isLoading = false,
                        resumo = planoAnalisado.toImportResumoUi(),
                        concursos = planoAnalisado.concursos.map { concurso -> concurso.toImportConcursoUi() },
                        errors = errosUi
                    )
                }
            }
            .onError { mostrarErroDeArquivo(ImportFileError.FALHA_AO_ANALISAR, errosUi) }
    }

    private fun mostrarErroDeArquivo(erro: ImportFileError, errors: List<ImportLineErrorUi> = emptyList()) {
        _state.update { it.copy(isLoading = false, fileError = erro, errors = errors) }
    }

    private fun confirmar() {
        val planoAtual = plan ?: return
        if (!_state.value.isConfirmEnabled) return

        _state.update { it.copy(isImporting = true) }
        viewModelScope.launch {
            repository.aplicar(planoAtual)
                .onSuccess { resumo -> _state.update { it.copy(isImporting = false, resultado = resumo) } }
                .onError {
                    _state.update { it.copy(isImporting = false) }
                    eventChannel.send(ImportEvent.ShowError(UiText.StringResource(R.string.import_error_aplicar)))
                }
        }
    }
}
