package br.com.zamfir.verticalize.registroestudo.presentation

import br.com.zamfir.verticalize.registroestudo.timer.AndroidEstudoTimerController
import br.com.zamfir.verticalize.registroestudo.timer.EstudoTimerController
import br.com.zamfir.verticalize.registroestudo.timer.widget.EstudoTimerWidgetConfigViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val registroEstudoPresentationModule = module {
    singleOf(::AndroidEstudoTimerController) { bind<EstudoTimerController>() }
    viewModel { (concursoId: Long) -> RegistroEstudoListViewModel(concursoId, get(), get()) }
    viewModelOf(::EstudoTimerWidgetConfigViewModel)
}
