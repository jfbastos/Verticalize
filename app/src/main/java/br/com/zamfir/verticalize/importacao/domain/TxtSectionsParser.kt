package br.com.zamfir.verticalize.importacao.domain

import br.com.zamfir.verticalize.concurso.domain.Nivel
import br.com.zamfir.verticalize.conteudo.domain.Prioridade

private const val BOM = "\uFEFF"
private const val SEPARADOR_DE_CAMPOS = ';'

private enum class Secao { NENHUMA, CONCURSO, CONTEUDOS, ESTUDOS, IGNORAR }

private enum class Coluna(vararg val apelidos: String) {
    MATERIA("materia", "disciplina"),
    DESCRICAO("descricao", "assunto"),
    EIXO("eixo"),
    BLOCO("bloco"),
    AULAS("aulas", "quantidade_aulas", "qtd_aulas"),
    MINUTOS_POR_AULA("min_por_aula", "minutos_por_aula", "tempo_medio_aula", "tempo_aula"),
    CONCLUIDO("concluido", "feito"),
    ULTIMA_REVISAO("ultima_revisao", "data_ultima_revisao", "revisao"),
    QUESTOES("questoes", "quantidade_questoes", "qtd_questoes"),
    PRIORIDADE("prioridade"),
    IGNORAR;

    companion object {
        fun dePalavra(chave: String): Coluna? = entries.firstOrNull { chave in it.apelidos }
    }
}

private val COLUNAS_PADRAO = listOf(
    Coluna.MATERIA,
    Coluna.DESCRICAO,
    Coluna.EIXO,
    Coluna.BLOCO,
    Coluna.AULAS,
    Coluna.MINUTOS_POR_AULA,
    Coluna.CONCLUIDO,
    Coluna.ULTIMA_REVISAO,
    Coluna.QUESTOES,
    Coluna.PRIORIDADE
)

private enum class ColunaEstudo(vararg val apelidos: String) {
    DATA("data", "dia"),
    HORA_INICIO("hora_inicio", "inicio"),
    HORA_FIM("hora_fim", "fim"),
    MATERIA("materia", "disciplina"),
    IGNORAR;

    companion object {
        fun dePalavra(chave: String): ColunaEstudo? = entries.firstOrNull { chave in it.apelidos }
    }
}

private val COLUNAS_ESTUDO_PADRAO = listOf(
    ColunaEstudo.DATA,
    ColunaEstudo.HORA_INICIO,
    ColunaEstudo.HORA_FIM,
    ColunaEstudo.MATERIA
)

private sealed interface LeituraConteudo {
    data class Ok(val conteudo: ImportedConteudo) : LeituraConteudo
    data class Falha(val motivo: ImportLineReason) : LeituraConteudo
}

private sealed interface LeituraEstudo {
    data class Ok(val estudo: ImportedEstudo) : LeituraEstudo
    data class Falha(val motivo: ImportLineReason) : LeituraEstudo
}

/**
 * Lê o formato TXT com seções:
 *
 * ```
 * [CONCURSO]
 * nome: TRT 2ª Região
 * nivel: superior
 * data_prova: 15/03/2027
 *
 * [CONTEUDOS]
 * materia;descricao;eixo;bloco;aulas;min_por_aula;concluido;ultima_revisao;questoes;prioridade
 * Direito Constitucional;Princípios fundamentais;1;1;5;50;nao;;0;alta
 *
 * [ESTUDOS]
 * data;hora_inicio;hora_fim;materia
 * 17/09/2026;08:00;10:30;Direito Constitucional
 * ```
 *
 * As seções [CONTEUDOS] e [ESTUDOS] são opcionais e pertencem ao `[CONCURSO]` imediatamente anterior.
 * Erros em uma linha são registrados em [ParsedImport.errors] e a leitura continua nas demais.
 */
class TxtSectionsParser : ImportFileParser {

    override fun parse(text: String): ParsedImport {
        val concursos = mutableListOf<ImportedConcurso>()
        val errors = mutableListOf<ImportLineError>()

        var bloco: BlocoConcurso? = null
        var secao = Secao.NENHUMA
        var colunas = COLUNAS_PADRAO
        var colunasEstudo = COLUNAS_ESTUDO_PADRAO
        var cabecalhoVerificado = false
        var linhaForaDeSecaoReportada = false

        fun fecharBloco() {
            bloco?.finalizar(errors)?.let(concursos::add)
            bloco = null
        }

        text.removePrefix(BOM).lineSequence().forEachIndexed { indice, bruta ->
            val numero = indice + 1
            val linha = bruta.trim()
            if (linha.isEmpty() || linha.startsWith('#')) return@forEachIndexed

            val nomeDaSecao = linha.nomeDeSecao()
            if (nomeDaSecao != null) {
                when (nomeDaSecao.paraChave()) {
                    "concurso" -> {
                        fecharBloco()
                        bloco = BlocoConcurso(numero)
                        secao = Secao.CONCURSO
                    }
                    "conteudos", "conteudo" -> {
                        if (bloco == null) {
                            errors += ImportLineError(numero, ImportLineReason.CONTEUDO_SEM_CONCURSO)
                            secao = Secao.IGNORAR
                        } else {
                            secao = Secao.CONTEUDOS
                            colunas = COLUNAS_PADRAO
                            cabecalhoVerificado = false
                        }
                    }
                    "estudos", "estudo" -> {
                        if (bloco == null) {
                            errors += ImportLineError(numero, ImportLineReason.ESTUDO_SEM_CONCURSO)
                            secao = Secao.IGNORAR
                        } else {
                            secao = Secao.ESTUDOS
                            colunasEstudo = COLUNAS_ESTUDO_PADRAO
                            cabecalhoVerificado = false
                        }
                    }
                    else -> {
                        errors += ImportLineError(numero, ImportLineReason.SECAO_DESCONHECIDA)
                        secao = Secao.IGNORAR
                    }
                }
                return@forEachIndexed
            }

            when (secao) {
                Secao.NENHUMA -> if (!linhaForaDeSecaoReportada) {
                    linhaForaDeSecaoReportada = true
                    errors += ImportLineError(numero, ImportLineReason.LINHA_FORA_DE_SECAO)
                }
                Secao.IGNORAR -> Unit
                Secao.CONCURSO -> bloco?.definirCampo(numero, linha, errors)
                Secao.CONTEUDOS -> {
                    val atual = bloco ?: return@forEachIndexed
                    val campos = linha.split(SEPARADOR_DE_CAMPOS).map { it.trim() }

                    if (!cabecalhoVerificado) {
                        cabecalhoVerificado = true
                        val cabecalho = lerCabecalho(campos)
                        if (cabecalho != null) {
                            if (Coluna.DESCRICAO in cabecalho) {
                                colunas = cabecalho
                            } else {
                                errors += ImportLineError(numero, ImportLineReason.CABECALHO_SEM_DESCRICAO)
                                secao = Secao.IGNORAR
                            }
                            return@forEachIndexed
                        }
                    }

                    when (val leitura = lerConteudo(campos, colunas)) {
                        is LeituraConteudo.Ok -> atual.conteudos += leitura.conteudo
                        is LeituraConteudo.Falha -> errors += ImportLineError(numero, leitura.motivo)
                    }
                }
                Secao.ESTUDOS -> {
                    val atual = bloco ?: return@forEachIndexed
                    val campos = linha.split(SEPARADOR_DE_CAMPOS).map { it.trim() }

                    if (!cabecalhoVerificado) {
                        cabecalhoVerificado = true
                        val cabecalho = lerCabecalhoDeEstudo(campos)
                        if (cabecalho != null) {
                            if (COLUNAS_ESTUDO_PADRAO.all { it in cabecalho }) {
                                colunasEstudo = cabecalho
                            } else {
                                errors += ImportLineError(numero, ImportLineReason.CABECALHO_ESTUDO_INCOMPLETO)
                                secao = Secao.IGNORAR
                            }
                            return@forEachIndexed
                        }
                    }

                    when (val leitura = lerEstudo(campos, colunasEstudo)) {
                        is LeituraEstudo.Ok -> atual.estudos += leitura.estudo
                        is LeituraEstudo.Falha -> errors += ImportLineError(numero, leitura.motivo)
                    }
                }
            }
        }
        fecharBloco()

        return ParsedImport(concursos = concursos, errors = errors)
    }

    /** "[CONCURSO]" -> "CONCURSO"; null se a linha não for um cabeçalho de seção. */
    private fun String.nomeDeSecao(): String? {
        return if (startsWith('[') && endsWith(']')) substring(1, length - 1) else null
    }

    /** Uma linha é cabeçalho se todos os nomes forem colunas conhecidas e houver matéria ou descrição. */
    private fun lerCabecalho(campos: List<String>): List<Coluna>? {
        val colunas = campos.map { campo ->
            if (campo.isEmpty()) Coluna.IGNORAR else Coluna.dePalavra(campo.paraChave()) ?: return null
        }
        val temColunaPrincipal = Coluna.MATERIA in colunas || Coluna.DESCRICAO in colunas
        return colunas.takeIf { temColunaPrincipal }
    }

    /** Cabeçalho de estudos: todos os nomes conhecidos e a coluna `data` presente (uma linha de dados nunca passa nisso). */
    private fun lerCabecalhoDeEstudo(campos: List<String>): List<ColunaEstudo>? {
        val colunas = campos.map { campo ->
            if (campo.isEmpty()) ColunaEstudo.IGNORAR else ColunaEstudo.dePalavra(campo.paraChave()) ?: return null
        }
        return colunas.takeIf { ColunaEstudo.DATA in colunas }
    }

    private fun lerConteudo(campos: List<String>, colunas: List<Coluna>): LeituraConteudo {
        if (campos.size > colunas.size) return LeituraConteudo.Falha(ImportLineReason.MUITOS_CAMPOS)

        val valores = HashMap<Coluna, String>()
        colunas.forEachIndexed { indice, coluna ->
            if (coluna != Coluna.IGNORAR) valores[coluna] = campos.getOrNull(indice).orEmpty()
        }
        fun valor(coluna: Coluna) = valores[coluna].orEmpty()

        val descricao = valor(Coluna.DESCRICAO)
        if (descricao.isEmpty()) return LeituraConteudo.Falha(ImportLineReason.DESCRICAO_OBRIGATORIA)

        val eixoTexto = valor(Coluna.EIXO)
        val eixo = if (eixoTexto.isEmpty()) null else eixoTexto.toIntNoIntervalo(1)
            ?: return LeituraConteudo.Falha(ImportLineReason.EIXO_INVALIDO)

        val blocoTexto = valor(Coluna.BLOCO)
        val bloco = if (blocoTexto.isEmpty()) null else blocoTexto.toIntNoIntervalo(1)
            ?: return LeituraConteudo.Falha(ImportLineReason.BLOCO_INVALIDO)

        val aulasTexto = valor(Coluna.AULAS)
        val aulas = if (aulasTexto.isEmpty()) 0 else aulasTexto.toIntNoIntervalo(0)
            ?: return LeituraConteudo.Falha(ImportLineReason.AULAS_INVALIDAS)

        val minutosTexto = valor(Coluna.MINUTOS_POR_AULA)
        val minutos = if (minutosTexto.isEmpty()) 0 else minutosTexto.toIntNoIntervalo(0)
            ?: return LeituraConteudo.Falha(ImportLineReason.MINUTOS_INVALIDOS)

        val concluido = parseBooleano(valor(Coluna.CONCLUIDO))
            ?: return LeituraConteudo.Falha(ImportLineReason.CONCLUIDO_INVALIDO)

        val revisaoTexto = valor(Coluna.ULTIMA_REVISAO)
        val revisao = if (revisaoTexto.isEmpty()) null else parseDataEmMillis(revisaoTexto)
            ?: return LeituraConteudo.Falha(ImportLineReason.REVISAO_INVALIDA)

        val questoesTexto = valor(Coluna.QUESTOES)
        val questoes = if (questoesTexto.isEmpty()) 0 else questoesTexto.toIntNoIntervalo(0)
            ?: return LeituraConteudo.Falha(ImportLineReason.QUESTOES_INVALIDAS)

        val prioridadeTexto = valor(Coluna.PRIORIDADE)
        val prioridade = if (prioridadeTexto.isEmpty()) Prioridade.MEDIA else parsePrioridade(prioridadeTexto)
            ?: return LeituraConteudo.Falha(ImportLineReason.PRIORIDADE_INVALIDA)

        return LeituraConteudo.Ok(
            ImportedConteudo(
                materia = valor(Coluna.MATERIA),
                descricao = descricao,
                eixo = eixo,
                bloco = bloco,
                quantidadeAulas = aulas,
                tempoMedioAulaMinutos = minutos,
                concluido = concluido,
                dataUltimaRevisao = revisao,
                quantidadeQuestoesRealizadas = questoes,
                prioridade = prioridade
            )
        )
    }

    /** Mesmas regras do cadastro de horário de estudo: matéria e data obrigatórias e fim depois do início. */
    private fun lerEstudo(campos: List<String>, colunas: List<ColunaEstudo>): LeituraEstudo {
        if (campos.size > colunas.size) return LeituraEstudo.Falha(ImportLineReason.MUITOS_CAMPOS)

        val valores = HashMap<ColunaEstudo, String>()
        colunas.forEachIndexed { indice, coluna ->
            if (coluna != ColunaEstudo.IGNORAR) valores[coluna] = campos.getOrNull(indice).orEmpty()
        }
        fun valor(coluna: ColunaEstudo) = valores[coluna].orEmpty()

        val materia = valor(ColunaEstudo.MATERIA)
        if (materia.isEmpty()) return LeituraEstudo.Falha(ImportLineReason.ESTUDO_MATERIA_OBRIGATORIA)

        val data = parseDataEmMillis(valor(ColunaEstudo.DATA))
            ?: return LeituraEstudo.Falha(ImportLineReason.ESTUDO_DATA_INVALIDA)
        val inicio = parseHoraEmMinutos(valor(ColunaEstudo.HORA_INICIO))
            ?: return LeituraEstudo.Falha(ImportLineReason.ESTUDO_HORA_INICIO_INVALIDA)
        val fim = parseHoraEmMinutos(valor(ColunaEstudo.HORA_FIM))
            ?: return LeituraEstudo.Falha(ImportLineReason.ESTUDO_HORA_FIM_INVALIDA)
        if (fim <= inicio) return LeituraEstudo.Falha(ImportLineReason.ESTUDO_INTERVALO_INVALIDO)

        return LeituraEstudo.Ok(
            ImportedEstudo(materia = materia, data = data, horaInicioMinutos = inicio, horaFimMinutos = fim)
        )
    }
}

/** Um bloco `[CONCURSO]` em construção, junto com os conteúdos e estudos lidos abaixo dele. */
private class BlocoConcurso(private val linhaDoCabecalho: Int) {
    private var nome: String? = null
    private var nivel: Nivel? = null
    private var dataProva: Long? = null
    private var valorInscricaoCentavos = 0L
    private var banca = ""

    // Campos obrigatórios cujo valor foi informado mas era inválido (já reportados linha a linha).
    private val camposComErro = mutableSetOf<String>()
    private var invalido = false

    val conteudos = mutableListOf<ImportedConteudo>()
    val estudos = mutableListOf<ImportedEstudo>()

    fun definirCampo(numero: Int, linha: String, errors: MutableList<ImportLineError>) {
        val separador = linha.indexOf(':')
        if (separador < 0) {
            errors += ImportLineError(numero, ImportLineReason.LINHA_INVALIDA)
            return
        }
        val chave = linha.substring(0, separador).paraChave()
        val valor = linha.substring(separador + 1).trim()

        fun falha(campo: String?, motivo: ImportLineReason) {
            invalido = true
            if (campo != null) camposComErro += campo
            errors += ImportLineError(numero, motivo)
        }

        when (chave) {
            "nome" -> if (valor.isEmpty()) falha("nome", ImportLineReason.NOME_OBRIGATORIO) else nome = valor
            "nivel" -> parseNivel(valor)?.let { nivel = it } ?: falha("nivel", ImportLineReason.NIVEL_INVALIDO)
            // "data_da_prova" e "valor_da_inscricao" são os rótulos usados na própria tela de cadastro.
            "data_prova", "data_da_prova" -> parseDataEmMillis(valor)?.let { dataProva = it }
                ?: falha("data_prova", ImportLineReason.DATA_PROVA_INVALIDA)
            "banca" -> banca = valor
            "valor_inscricao", "valor_da_inscricao", "valor" -> if (valor.isEmpty()) {
                valorInscricaoCentavos = 0L
            } else {
                parseValorEmCentavos(valor)?.let { valorInscricaoCentavos = it }
                    ?: falha(null, ImportLineReason.VALOR_INSCRICAO_INVALIDO)
            }
            // Chave desconhecida (provável erro de digitação): a linha é reportada, mas o bloco segue válido.
            else -> errors += ImportLineError(numero, ImportLineReason.CHAVE_DESCONHECIDA)
        }
    }

    /** Devolve o concurso completo, ou null (com os motivos em [errors]) se o bloco não puder ser importado. */
    fun finalizar(errors: MutableList<ImportLineError>): ImportedConcurso? {
        if (nome == null && "nome" !in camposComErro) {
            invalido = true
            errors += ImportLineError(linhaDoCabecalho, ImportLineReason.NOME_OBRIGATORIO)
        }
        if (nivel == null && "nivel" !in camposComErro) {
            invalido = true
            errors += ImportLineError(linhaDoCabecalho, ImportLineReason.NIVEL_OBRIGATORIO)
        }
        val nomeFinal = nome
        val nivelFinal = nivel
        val dataFinal = dataProva
        if (invalido || nomeFinal == null || nivelFinal == null) return null

        return ImportedConcurso(
            nome = nomeFinal,
            nivel = nivelFinal,
            dataProva = dataFinal,
            valorInscricaoCentavos = valorInscricaoCentavos,
            banca = banca,
            conteudos = conteudos.toList(),
            estudos = estudos.toList()
        )
    }
}
