package br.com.zamfir.verticalize.core.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.zamfir.verticalize.core.database.MIGRATION_1_2
import br.com.zamfir.verticalize.core.database.MIGRATION_2_3
import br.com.zamfir.verticalize.core.database.MIGRATION_3_4
import br.com.zamfir.verticalize.core.database.MIGRATION_4_5
import br.com.zamfir.verticalize.core.database.MIGRATION_5_6
import br.com.zamfir.verticalize.core.database.MIGRATION_6_7
import br.com.zamfir.verticalize.core.database.VerticalizeDatabase
import br.com.zamfir.verticalize.sync.data.criarEstruturaDeSync
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            VerticalizeDatabase::class.java,
            VerticalizeDatabase.DATABASE_NAME
        )
            .addMigrations(
                MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7
            )
            // Ao abrir (e não só ao criar): cobre instalação nova, a migração 6→7 e futuras migrações que
            // recriem tabelas, o que apaga os triggers delas. Tudo ali é idempotente.
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    criarEstruturaDeSync(db)
                }
            })
            .build()
    }
    single { get<VerticalizeDatabase>().concursoDao() }
    single { get<VerticalizeDatabase>().conteudoDao() }
    single { get<VerticalizeDatabase>().registroEstudoDao() }
    single { get<VerticalizeDatabase>().importDao() }
    single { get<VerticalizeDatabase>().syncDao() }
}
