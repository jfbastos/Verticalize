package br.com.zamfir.verticalize.concurso.presentation

import br.com.zamfir.verticalize.auth.domain.AuthError
import br.com.zamfir.verticalize.concurso.domain.Nivel

sealed interface ConcursoListAction {
    data object OnFabClick : ConcursoListAction
    data class OnConcursoSwipeEdit(val id: Long) : ConcursoListAction
    data object OnMoreMenuToggle : ConcursoListAction
    data object OnMoreMenuDismiss : ConcursoListAction
    data object OnImportClick : ConcursoListAction
    data object OnImportDismiss : ConcursoListAction
    data object OnImportPickFileClick : ConcursoListAction
    data object OnExportClick : ConcursoListAction
    data class OnExportFileSelected(val uri: String) : ConcursoListAction
    data class OnConcursoSwipeDelete(val id: Long) : ConcursoListAction
    data object OnDeleteConfirm : ConcursoListAction
    data object OnDeleteDismiss : ConcursoListAction
    data class OnConcursoTapped(val id: Long) : ConcursoListAction
    data object OnDismissDialog : ConcursoListAction
    data class OnNomeChanged(val value: String) : ConcursoListAction
    data object OnNivelDropdownToggle : ConcursoListAction
    data class OnNivelSelected(val nivel: Nivel) : ConcursoListAction
    data object OnDataProvaFieldClick : ConcursoListAction
    data object OnDataProvaClearClick : ConcursoListAction
    data class OnDatePickerConfirm(val millis: Long) : ConcursoListAction
    data object OnDatePickerDismiss : ConcursoListAction
    data class OnValorInscricaoChanged(val rawInput: String) : ConcursoListAction
    data class OnBancaChanged(val value: String) : ConcursoListAction
    data object OnSaveClick : ConcursoListAction
    data object OnContaClick : ConcursoListAction
    data object OnContaMenuDismiss : ConcursoListAction
    data object OnSignOutClick : ConcursoListAction
    data class OnGoogleIdTokenReceived(val idToken: String) : ConcursoListAction
    data class OnGoogleSignInFailed(val error: AuthError) : ConcursoListAction
    data object OnSyncConflitoUsarNuvem : ConcursoListAction
    data object OnSyncConflitoManterAparelho : ConcursoListAction
}
