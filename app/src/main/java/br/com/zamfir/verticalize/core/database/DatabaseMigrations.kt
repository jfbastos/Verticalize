package br.com.zamfir.verticalize.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `conteudos` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`concursoId` INTEGER NOT NULL, `nome` TEXT NOT NULL, `eixo` INTEGER, `bloco` INTEGER, " +
                "`quantidadeAulas` INTEGER NOT NULL, `tempoMedioAulaMinutos` INTEGER NOT NULL, " +
                "`concluido` INTEGER NOT NULL, `dataUltimaRevisao` INTEGER, " +
                "`quantidadeQuestoesRealizadas` INTEGER NOT NULL, " +
                "FOREIGN KEY(`concursoId`) REFERENCES `concursos`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_conteudos_concursoId` ON `conteudos` (`concursoId`)")
    }
}

// SQLite ALTER TABLE ... RENAME COLUMN só é confiável a partir da 3.25 (não garantido no SQLite do
// sistema em minSdk 28), então a coluna "nome" é migrada para "descricao" recriando a tabela.
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `conteudos_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`concursoId` INTEGER NOT NULL, `materia` TEXT NOT NULL, `descricao` TEXT NOT NULL, " +
                "`eixo` INTEGER, `bloco` INTEGER, `quantidadeAulas` INTEGER NOT NULL, " +
                "`tempoMedioAulaMinutos` INTEGER NOT NULL, `concluido` INTEGER NOT NULL, " +
                "`dataUltimaRevisao` INTEGER, `quantidadeQuestoesRealizadas` INTEGER NOT NULL, " +
                "FOREIGN KEY(`concursoId`) REFERENCES `concursos`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL(
            "INSERT INTO `conteudos_new` (id, concursoId, materia, descricao, eixo, bloco, quantidadeAulas, " +
                "tempoMedioAulaMinutos, concluido, dataUltimaRevisao, quantidadeQuestoesRealizadas) " +
                "SELECT id, concursoId, '', nome, eixo, bloco, quantidadeAulas, tempoMedioAulaMinutos, " +
                "concluido, dataUltimaRevisao, quantidadeQuestoesRealizadas FROM `conteudos`"
        )
        db.execSQL("DROP TABLE `conteudos`")
        db.execSQL("ALTER TABLE `conteudos_new` RENAME TO `conteudos`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_conteudos_concursoId` ON `conteudos` (`concursoId`)")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `registros_estudo` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`concursoId` INTEGER NOT NULL, `materia` TEXT NOT NULL, `data` INTEGER NOT NULL, " +
                "`horaInicioMinutos` INTEGER NOT NULL, `horaFimMinutos` INTEGER NOT NULL, " +
                "FOREIGN KEY(`concursoId`) REFERENCES `concursos`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_registros_estudo_concursoId` ON `registros_estudo` (`concursoId`)")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `conteudos` ADD COLUMN `prioridade` TEXT NOT NULL DEFAULT 'MEDIA'")
    }
}

// SQLite não permite remover a restrição NOT NULL de uma coluna com ALTER TABLE, então a tabela
// é recriada, como já feito na MIGRATION_2_3.
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `concursos_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`nome` TEXT NOT NULL, `nivel` TEXT NOT NULL, `dataProva` INTEGER, " +
                "`valorInscricaoCentavos` INTEGER NOT NULL, `banca` TEXT NOT NULL, " +
                "`horasEstudadas` INTEGER NOT NULL, `percentualCompletude` INTEGER NOT NULL)"
        )
        db.execSQL(
            "INSERT INTO `concursos_new` (id, nome, nivel, dataProva, valorInscricaoCentavos, banca, " +
                "horasEstudadas, percentualCompletude) " +
                "SELECT id, nome, nivel, dataProva, valorInscricaoCentavos, banca, horasEstudadas, " +
                "percentualCompletude FROM `concursos`"
        )
        db.execSQL("DROP TABLE `concursos`")
        db.execSQL("ALTER TABLE `concursos_new` RENAME TO `concursos`")
    }
}

// Tabelas da sincronização com a nuvem. Os triggers que as alimentam são criados em
// criarEstruturaDeSync, chamado ao abrir o banco (ver CoreModule).
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `sync_pendencias` (`tabela` TEXT NOT NULL, " +
                "`registroId` INTEGER NOT NULL, `excluido` INTEGER NOT NULL, `seq` INTEGER NOT NULL, " +
                "PRIMARY KEY(`tabela`, `registroId`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `sync_controle` (`id` INTEGER NOT NULL, " +
                "`aplicandoRemoto` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
    }
}
