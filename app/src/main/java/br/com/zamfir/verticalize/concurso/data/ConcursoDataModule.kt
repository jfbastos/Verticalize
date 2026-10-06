package br.com.zamfir.verticalize.concurso.data

import br.com.zamfir.verticalize.concurso.domain.ConcursoRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val concursoDataModule = module {
    singleOf(::RoomConcursoRepository) { bind<ConcursoRepository>() }
}
