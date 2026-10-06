package br.com.zamfir.verticalize.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import br.com.zamfir.verticalize.concurso.data.ConcursoDao
import br.com.zamfir.verticalize.concurso.data.ConcursoEntity
import br.com.zamfir.verticalize.conteudo.data.ConteudoDao
import br.com.zamfir.verticalize.conteudo.data.ConteudoEntity
import br.com.zamfir.verticalize.importacao.data.ImportDao
import br.com.zamfir.verticalize.registroestudo.data.RegistroEstudoDao
import br.com.zamfir.verticalize.registroestudo.data.RegistroEstudoEntity
import br.com.zamfir.verticalize.sync.data.SyncDao
import br.com.zamfir.verticalize.sync.data.SyncControleEntity
import br.com.zamfir.verticalize.sync.data.SyncPendenciaEntity

@Database(
    entities = [
        ConcursoEntity::class,
        ConteudoEntity::class,
        RegistroEstudoEntity::class,
        SyncPendenciaEntity::class,
        SyncControleEntity::class
    ],
    version = 7,
    exportSchema = true
)
abstract class VerticalizeDatabase : RoomDatabase() {
    abstract fun concursoDao(): ConcursoDao
    abstract fun conteudoDao(): ConteudoDao
    abstract fun registroEstudoDao(): RegistroEstudoDao
    abstract fun importDao(): ImportDao
    abstract fun syncDao(): SyncDao

    companion object {
        const val DATABASE_NAME = "verticalize.db"
    }
}
