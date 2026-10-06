package br.com.zamfir.verticalize.sync.data

import br.com.zamfir.verticalize.concurso.data.ConcursoEntity
import br.com.zamfir.verticalize.concurso.domain.Nivel
import br.com.zamfir.verticalize.conteudo.data.ConteudoEntity
import br.com.zamfir.verticalize.conteudo.domain.Prioridade
import br.com.zamfir.verticalize.registroestudo.data.RegistroEstudoEntity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ServerValue

/*
 * Formato no Realtime Database, em users/{uid}:
 *
 *   versao: 1
 *   atualizadoEm: <timestamp do servidor da última alteração>
 *   dados/concursos/c{id}: { id, nome, nivel, ... }
 *   dados/conteudos/c{id}: { id, concursoId, ... }
 *   dados/estudos/e{id}:   { id, concursoId, ... }
 *
 * Um nó por registro, para cada aparelho alterar só o que mudou (sem sobrescrever o trabalho do outro).
 * As chaves têm prefixo de letra de propósito: com chaves numéricas o Realtime Database pode devolver o
 * nó como lista em vez de mapa.
 */

const val BACKUP_VERSAO = 1L

const val CAMPO_VERSAO = "versao"
const val CAMPO_ATUALIZADO_EM = "atualizadoEm"
const val NO_DADOS = "dados"
private const val NO_CONCURSOS = "concursos"
private const val NO_CONTEUDOS = "conteudos"
private const val NO_ESTUDOS = "estudos"

/** Formato inválido no backup da nuvem. */
class BackupInvalidoException(message: String) : IllegalArgumentException(message)

private fun noDaTabela(tabela: String): String = when (tabela) {
    TABELA_CONCURSOS -> NO_CONCURSOS
    TABELA_CONTEUDOS -> NO_CONTEUDOS
    TABELA_ESTUDOS -> NO_ESTUDOS
    else -> throw IllegalArgumentException("Tabela não sincronizada: $tabela")
}

private fun chaveDoRegistro(tabela: String, id: Long): String =
    if (tabela == TABELA_ESTUDOS) "e$id" else "c$id"

/** Caminho de um registro dentro de `dados`, a partir da tabela local e do id. */
fun caminhoDoRegistro(tabela: String, id: Long): String = "${noDaTabela(tabela)}/${chaveDoRegistro(tabela, id)}"

/** Conteúdo completo do nó `dados`. */
fun BackupSnapshot.toFirebaseDados(): Map<String, Any> = mapOf(
    NO_CONCURSOS to concursos.associate { chaveDoRegistro(TABELA_CONCURSOS, it.id) to it.toFirebaseMap() },
    NO_CONTEUDOS to conteudos.associate { chaveDoRegistro(TABELA_CONTEUDOS, it.id) to it.toFirebaseMap() },
    NO_ESTUDOS to estudos.associate { chaveDoRegistro(TABELA_ESTUDOS, it.id) to it.toFirebaseMap() }
)

/** Metadados gravados junto de qualquer alteração em `dados`. */
fun metadadosDeAlteracao(): Map<String, Any> = mapOf(
    CAMPO_VERSAO to BACKUP_VERSAO,
    CAMPO_ATUALIZADO_EM to ServerValue.TIMESTAMP
)

/**
 * Lê o nó `users/{uid}` inteiro.
 * @throws BackupInvalidoException se algum campo obrigatório faltar ou tiver valor desconhecido.
 */
fun DataSnapshot.toBackupRemoto(): BackupRemoto {
    val versao = child(CAMPO_VERSAO).getValue(Long::class.java)
    if (versao != null && versao > BACKUP_VERSAO) {
        throw BackupInvalidoException("Backup na versão $versao, mais nova que a suportada ($BACKUP_VERSAO)")
    }
    return BackupRemoto(
        snapshot = child(NO_DADOS).toBackupSnapshot(),
        atualizadoEm = child(CAMPO_ATUALIZADO_EM).getValue(Long::class.java)
    )
}

/**
 * Lê o nó `dados` (vazio quando ele não existe: o Realtime Database apaga nós sem filhos).
 * @throws BackupInvalidoException se algum campo obrigatório faltar ou tiver valor desconhecido.
 */
fun DataSnapshot.toBackupSnapshot(): BackupSnapshot = BackupSnapshot(
    concursos = child(NO_CONCURSOS).children.map { it.toConcursoEntity() },
    conteudos = child(NO_CONTEUDOS).children.map { it.toConteudoEntity() },
    estudos = child(NO_ESTUDOS).children.map { it.toEstudoEntity() }
).normalizado()

fun ConcursoEntity.toFirebaseMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "nome" to nome,
    "nivel" to nivel,
    "dataProva" to dataProva,
    "valorInscricaoCentavos" to valorInscricaoCentavos,
    "banca" to banca,
    "horasEstudadas" to horasEstudadas,
    "percentualCompletude" to percentualCompletude
)

fun ConteudoEntity.toFirebaseMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "concursoId" to concursoId,
    "materia" to materia,
    "descricao" to descricao,
    "eixo" to eixo,
    "bloco" to bloco,
    "quantidadeAulas" to quantidadeAulas,
    "tempoMedioAulaMinutos" to tempoMedioAulaMinutos,
    "concluido" to concluido,
    "dataUltimaRevisao" to dataUltimaRevisao,
    "quantidadeQuestoesRealizadas" to quantidadeQuestoesRealizadas,
    "prioridade" to prioridade
)

fun RegistroEstudoEntity.toFirebaseMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "concursoId" to concursoId,
    "materia" to materia,
    "data" to data,
    "horaInicioMinutos" to horaInicioMinutos,
    "horaFimMinutos" to horaFimMinutos
)

private fun DataSnapshot.toConcursoEntity(): ConcursoEntity = ConcursoEntity(
    id = long("id"),
    nome = string("nome"),
    nivel = enumName<Nivel>("nivel"),
    dataProva = longOrNull("dataProva"),
    valorInscricaoCentavos = longOrNull("valorInscricaoCentavos") ?: 0L,
    banca = stringOrNull("banca") ?: "",
    horasEstudadas = intOrNull("horasEstudadas") ?: 0,
    percentualCompletude = intOrNull("percentualCompletude") ?: 0
)

private fun DataSnapshot.toConteudoEntity(): ConteudoEntity = ConteudoEntity(
    id = long("id"),
    concursoId = long("concursoId"),
    materia = stringOrNull("materia") ?: "",
    descricao = string("descricao"),
    eixo = intOrNull("eixo"),
    bloco = intOrNull("bloco"),
    quantidadeAulas = intOrNull("quantidadeAulas") ?: 0,
    tempoMedioAulaMinutos = intOrNull("tempoMedioAulaMinutos") ?: 0,
    concluido = child("concluido").getValue(Boolean::class.java) ?: false,
    dataUltimaRevisao = longOrNull("dataUltimaRevisao"),
    quantidadeQuestoesRealizadas = intOrNull("quantidadeQuestoesRealizadas") ?: 0,
    prioridade = if (child("prioridade").exists()) enumName<Prioridade>("prioridade") else Prioridade.MEDIA.name
)

private fun DataSnapshot.toEstudoEntity(): RegistroEstudoEntity = RegistroEstudoEntity(
    id = long("id"),
    concursoId = long("concursoId"),
    materia = string("materia"),
    data = long("data"),
    horaInicioMinutos = int("horaInicioMinutos"),
    horaFimMinutos = int("horaFimMinutos")
)

private fun DataSnapshot.longOrNull(campo: String): Long? = child(campo).getValue(Long::class.java)

private fun DataSnapshot.intOrNull(campo: String): Int? = longOrNull(campo)?.toInt()

private fun DataSnapshot.stringOrNull(campo: String): String? = child(campo).getValue(String::class.java)

private fun DataSnapshot.long(campo: String): Long = longOrNull(campo) ?: faltando(campo)

private fun DataSnapshot.int(campo: String): Int = intOrNull(campo) ?: faltando(campo)

private fun DataSnapshot.string(campo: String): String = stringOrNull(campo) ?: faltando(campo)

/** Confere o valor contra o enum agora, para um backup corrompido não derrubar o app depois. */
private inline fun <reified E : Enum<E>> DataSnapshot.enumName(campo: String): String {
    val valor = string(campo)
    if (enumValues<E>().none { it.name == valor }) {
        throw BackupInvalidoException("Valor '$valor' inválido em $key/$campo")
    }
    return valor
}

private fun DataSnapshot.faltando(campo: String): Nothing =
    throw BackupInvalidoException("Campo '$campo' ausente em $key")
