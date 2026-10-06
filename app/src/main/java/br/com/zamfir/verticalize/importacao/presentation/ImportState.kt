package br.com.zamfir.verticalize.importacao.presentation

import androidx.compose.runtime.Stable
import br.com.zamfir.verticalize.importacao.domain.ImportFileError
import br.com.zamfir.verticalize.importacao.domain.ImportSummary

@Stable
data class ImportState(
    val isLoading: Boolean = true,
    /** Não nulo quando o arquivo não pôde ser importado; [errors] ainda pode explicar o motivo. */
    val fileError: ImportFileError? = null,
    val resumo: ImportResumoUi = ImportResumoUi(),
    val concursos: List<ImportConcursoUi> = emptyList(),
    val errors: List<ImportLineErrorUi> = emptyList(),
    val isImporting: Boolean = false,
    /** Preenchido depois que a importação foi gravada com sucesso. */
    val resultado: ImportSummary? = null
)

val ImportState.isConfirmEnabled: Boolean
    get() = !isLoading && !isImporting && fileError == null && resultado == null && resumo.temNovidades
