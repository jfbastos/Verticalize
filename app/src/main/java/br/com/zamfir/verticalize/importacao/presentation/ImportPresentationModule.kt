package br.com.zamfir.verticalize.importacao.presentation

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val importacaoPresentationModule = module {
    viewModel { (fileUri: String) -> ImportViewModel(fileUri, get(), get(), get()) }
}
