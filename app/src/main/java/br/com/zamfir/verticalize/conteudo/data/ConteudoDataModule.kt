package br.com.zamfir.verticalize.conteudo.data

import br.com.zamfir.verticalize.conteudo.domain.ConteudoRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val conteudoDataModule = module {
    singleOf(::RoomConteudoRepository) { bind<ConteudoRepository>() }
}
