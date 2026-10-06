package br.com.zamfir.verticalize.concurso.presentation

import androidx.compose.runtime.Stable
import br.com.zamfir.verticalize.auth.presentation.ContaUi
import br.com.zamfir.verticalize.concurso.domain.Nivel
import br.com.zamfir.verticalize.sync.presentation.ConflitoSyncUi

@Stable
data class ConcursoListState(
    val concursos: List<ConcursoUi> = emptyList(),
    val isLoading: Boolean = true,
    val isDialogVisible: Boolean = false,
    val editingId: Long? = null,
    val editingHorasEstudadas: Int = 0,
    val editingPercentualCompletude: Int = 0,
    val nome: String = "",
    val nivel: Nivel = Nivel.MEDIO,
    val isNivelDropdownExpanded: Boolean = false,
    val dataProvaMillis: Long? = null,
    val dataProvaFormatted: String = "",
    val isDatePickerVisible: Boolean = false,
    val valorInscricaoInput: String = "",
    val banca: String = "",
    val isDeleteConfirmationVisible: Boolean = false,
    val concursoPendingDeleteId: Long? = null,
    val isMoreMenuExpanded: Boolean = false,
    val isImportFormatDialogVisible: Boolean = false,
    // null = app sendo usado sem conta (o login é opcional).
    val conta: ContaUi? = null,
    val isSigningIn: Boolean = false,
    val isContaMenuExpanded: Boolean = false,
    /** Baixando ou enviando os dados da conta logo após o login. */
    val isSincronizando: Boolean = false,
    val conflitoSync: ConflitoSyncUi? = null
)

val ConcursoListState.isSaveEnabled: Boolean
    get() = nome.isNotBlank()
