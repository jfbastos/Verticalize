package br.com.zamfir.verticalize.registroestudo.data

import br.com.zamfir.verticalize.registroestudo.domain.RegistroEstudoRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val registroEstudoDataModule = module {
    singleOf(::RoomRegistroEstudoRepository) { bind<RegistroEstudoRepository>() }
}
