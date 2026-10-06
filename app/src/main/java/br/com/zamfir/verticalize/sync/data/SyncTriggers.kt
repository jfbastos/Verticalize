package br.com.zamfir.verticalize.sync.data

import androidx.sqlite.db.SupportSQLiteDatabase

const val TABELA_CONCURSOS = "concursos"
const val TABELA_CONTEUDOS = "conteudos"
const val TABELA_ESTUDOS = "registros_estudo"

private val TABELAS_SINCRONIZADAS = listOf(TABELA_CONCURSOS, TABELA_CONTEUDOS, TABELA_ESTUDOS)

/**
 * Triggers que registram em `sync_pendencias` toda escrita nas tabelas sincronizadas. Ficam no banco
 * (e não nos DAOs) para pegar qualquer origem: telas, importação, cronômetro, widget e as exclusões em
 * cascata das foreign keys, que o SQLite executa sem passar pelo app.
 */
private fun triggerSql(tabela: String, evento: String): String {
    val linha = if (evento == "DELETE") "OLD" else "NEW"
    val excluido = if (evento == "DELETE") 1 else 0
    return "CREATE TRIGGER IF NOT EXISTS `sync_${tabela}_${evento.lowercase()}` " +
        "AFTER $evento ON `$tabela` " +
        "WHEN IFNULL((SELECT aplicandoRemoto FROM sync_controle WHERE id = $SYNC_CONTROLE_ID), 0) = 0 " +
        "BEGIN " +
        "INSERT OR REPLACE INTO sync_pendencias (tabela, registroId, excluido, seq) " +
        "VALUES ('$tabela', $linha.id, $excluido, (SELECT IFNULL(MAX(seq), 0) + 1 FROM sync_pendencias)); " +
        "END"
}

private val SYNC_TRIGGERS_SQL: List<String> = TABELAS_SINCRONIZADAS.flatMap { tabela ->
    listOf("INSERT", "UPDATE", "DELETE").map { evento -> triggerSql(tabela, evento) }
}

/** Cria os triggers e a linha de controle. Idempotente: usado na migração e em instalações novas. */
fun criarEstruturaDeSync(db: SupportSQLiteDatabase) {
    db.execSQL("INSERT OR IGNORE INTO sync_controle (id, aplicandoRemoto) VALUES ($SYNC_CONTROLE_ID, 0)")
    SYNC_TRIGGERS_SQL.forEach(db::execSQL)
}
