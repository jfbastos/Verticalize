package br.com.zamfir.verticalize.importacao.data

import br.com.zamfir.verticalize.importacao.domain.ConcursoExporter
import br.com.zamfir.verticalize.importacao.domain.ExportFileWriter
import br.com.zamfir.verticalize.importacao.domain.ExportRepository
import br.com.zamfir.verticalize.importacao.domain.ImportFileParser
import br.com.zamfir.verticalize.importacao.domain.ImportFileReader
import br.com.zamfir.verticalize.importacao.domain.ImportPlanner
import br.com.zamfir.verticalize.importacao.domain.ImportRepository
import br.com.zamfir.verticalize.importacao.domain.TxtSectionsFormatter
import br.com.zamfir.verticalize.importacao.domain.TxtSectionsParser
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val importacaoDataModule = module {
    factoryOf(::ImportPlanner)
    factoryOf(::TxtSectionsParser) { bind<ImportFileParser>() }
    single<ImportFileReader> { ContentResolverImportFileReader(androidContext().contentResolver) }
    singleOf(::RoomImportRepository) { bind<ImportRepository>() }

    factoryOf(::TxtSectionsFormatter)
    single<ExportFileWriter> { ContentResolverExportFileWriter(androidContext().contentResolver) }
    singleOf(::RoomExportRepository) { bind<ExportRepository>() }
    factoryOf(::ConcursoExporter)
}
