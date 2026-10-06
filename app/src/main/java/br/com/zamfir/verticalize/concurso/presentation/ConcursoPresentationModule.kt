package br.com.zamfir.verticalize.concurso.presentation

import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val concursoPresentationModule = module {
    viewModelOf(::ConcursoListViewModel)
    viewModel { (concursoId: Long) -> ConcursoDetailViewModel(concursoId, get(), get(), get(), get()) }
}
