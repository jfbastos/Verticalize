package br.com.zamfir.verticalize

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import br.com.zamfir.verticalize.concurso.presentation.ConcursoDetailRoot
import br.com.zamfir.verticalize.concurso.presentation.ConcursoDetailRoute
import br.com.zamfir.verticalize.concurso.presentation.ConcursoListRoot
import br.com.zamfir.verticalize.concurso.presentation.ConcursoListRoute
import br.com.zamfir.verticalize.importacao.presentation.ImportRoot
import br.com.zamfir.verticalize.importacao.presentation.ImportacaoRoute
import br.com.zamfir.verticalize.ui.theme.VerticalizeTheme

private const val NAV_TRANSITION_DURATION_MILLIS = 300

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VerticalizeTheme {
                VerticalizeNavDisplay()
            }
        }
    }
}

@Composable
private fun VerticalizeNavDisplay() {
    val backStack = rememberNavBackStack(ConcursoListRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = {
            fadeIn(tween(NAV_TRANSITION_DURATION_MILLIS)) togetherWith
                fadeOut(tween(NAV_TRANSITION_DURATION_MILLIS))
        },
        popTransitionSpec = {
            fadeIn(tween(NAV_TRANSITION_DURATION_MILLIS)) togetherWith
                fadeOut(tween(NAV_TRANSITION_DURATION_MILLIS))
        },
        predictivePopTransitionSpec = {
            fadeIn(tween(NAV_TRANSITION_DURATION_MILLIS)) togetherWith
                fadeOut(tween(NAV_TRANSITION_DURATION_MILLIS))
        },
        entryProvider = entryProvider {
            entry<ConcursoListRoute> {
                ConcursoListRoot(
                    onNavigateToDetail = { id -> backStack.add(ConcursoDetailRoute(id)) },
                    onNavigateToImport = { fileUri -> backStack.add(ImportacaoRoute(fileUri)) }
                )
            }
            entry<ConcursoDetailRoute> { route ->
                ConcursoDetailRoot(
                    concursoId = route.concursoId,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }
            entry<ImportacaoRoute> { route ->
                ImportRoot(
                    fileUri = route.fileUri,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
