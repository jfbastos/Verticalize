package br.com.zamfir.verticalize.importacao.domain

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import br.com.zamfir.verticalize.concurso.domain.Nivel
import br.com.zamfir.verticalize.conteudo.domain.Prioridade
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneOffset

class TxtSectionsParserTest {

    private val parser = TxtSectionsParser()

    private fun millis(ano: Int, mes: Int, dia: Int): Long =
        LocalDate.of(ano, mes, dia).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    private fun ParsedImport.erros() = errors.map { it.linha to it.motivo }

    @Test
    fun `arquivo valido com dois concursos e seus conteudos`() {
        val parsed = parser.parse(
            """
            [CONCURSO]
            nome: TRT 2ª Região
            nivel: superior
            data_prova: 15/03/2027
            banca: FCC
            valor_inscricao: 120,00

            [CONTEUDOS]
            materia;descricao;eixo;bloco;aulas;min_por_aula;concluido;ultima_revisao;questoes
            Direito Constitucional;Princípios fundamentais;1;1;5;50;nao;;0
            Direito Administrativo;Atos administrativos;1;2;8;45;sim;10/09/2026;30

            [CONCURSO]
            nome: Banco do Brasil
            nivel: medio
            data_prova: 20/05/2027

            [CONTEUDOS]
            Português;Interpretação de texto
            """.trimIndent()
        )

        assertThat(parsed.errors).isEmpty()
        assertThat(parsed.concursos).hasSize(2)

        val trt = parsed.concursos[0]
        assertThat(trt.nome).isEqualTo("TRT 2ª Região")
        assertThat(trt.nivel).isEqualTo(Nivel.SUPERIOR)
        assertThat(trt.dataProva).isEqualTo(millis(2027, 3, 15))
        assertThat(trt.banca).isEqualTo("FCC")
        assertThat(trt.valorInscricaoCentavos).isEqualTo(12000L)
        assertThat(trt.conteudos).isEqualTo(
            listOf(
                ImportedConteudo(
                    materia = "Direito Constitucional",
                    descricao = "Princípios fundamentais",
                    eixo = 1,
                    bloco = 1,
                    quantidadeAulas = 5,
                    tempoMedioAulaMinutos = 50
                ),
                ImportedConteudo(
                    materia = "Direito Administrativo",
                    descricao = "Atos administrativos",
                    eixo = 1,
                    bloco = 2,
                    quantidadeAulas = 8,
                    tempoMedioAulaMinutos = 45,
                    concluido = true,
                    dataUltimaRevisao = millis(2026, 9, 10),
                    quantidadeQuestoesRealizadas = 30
                )
            )
        )

        val bb = parsed.concursos[1]
        assertThat(bb.nivel).isEqualTo(Nivel.MEDIO)
        assertThat(bb.banca).isEqualTo("")
        assertThat(bb.valorInscricaoCentavos).isEqualTo(0L)
        assertThat(bb.conteudos).isEqualTo(listOf(ImportedConteudo(materia = "Português", descricao = "Interpretação de texto")))
    }

    @Test
    fun `cabecalho de colunas pode reordenar e omitir colunas`() {
        val parsed = parser.parse(
            """
            [CONCURSO]
            nome: Concurso A
            nivel: medio
            data_prova: 01/01/2027
            [CONTEUDOS]
            descricao;concluido;eixo;materia
            Tema 1;sim;2;Matéria X
            """.trimIndent()
        )

        assertThat(parsed.errors).isEmpty()
        assertThat(parsed.concursos.single().conteudos).isEqualTo(
            listOf(ImportedConteudo(materia = "Matéria X", descricao = "Tema 1", eixo = 2, concluido = true))
        )
    }

    @Test
    fun `cabecalho sem a coluna descricao e reportado e as linhas seguintes sao ignoradas`() {
        val parsed = parser.parse(
            """
            [CONCURSO]
            nome: Concurso A
            nivel: medio
            data_prova: 01/01/2027
            [CONTEUDOS]
            materia;eixo
            Matéria X;1
            """.trimIndent()
        )

        assertThat(parsed.erros()).isEqualTo(listOf(6 to ImportLineReason.CABECALHO_SEM_DESCRICAO))
        assertThat(parsed.concursos.single().conteudos).isEmpty()
    }

    @Test
    fun `comentarios linhas em branco BOM e CRLF sao tratados`() {
        val texto = "\uFEFF# meu arquivo\r\n\r\n[CONCURSO]\r\nnome: Concurso A\r\nnivel: medio\r\n" +
            "data_prova: 01/01/2027\r\n# comentario\r\n[CONTEUDOS]\r\nMatéria;Tema 1\r\n"

        val parsed = parser.parse(texto)

        assertThat(parsed.errors).isEmpty()
        assertThat(parsed.concursos.single().nome).isEqualTo("Concurso A")
        assertThat(parsed.concursos.single().conteudos).hasSize(1)
    }

    @Test
    fun `chaves e secoes ignoram acentos caixa e espacos`() {
        val parsed = parser.parse(
            """
            [concurso]
            Nome: Concurso A
            Nível: Médio
            Data da Prova: 5/3/2027
            Valor da Inscrição: R${'$'} 1.200,50
            [CONTEÚDOS]
            Mat;Tema 1
            """.trimIndent()
        )

        assertThat(parsed.errors).isEmpty()
        val concurso = parsed.concursos.single()
        assertThat(concurso.nivel).isEqualTo(Nivel.MEDIO)
        assertThat(concurso.dataProva).isEqualTo(millis(2027, 3, 5))
        assertThat(concurso.valorInscricaoCentavos).isEqualTo(120050L)
        assertThat(concurso.conteudos).hasSize(1)
    }

    @Test
    fun `nome pode conter dois pontos porque so o primeiro separa a chave`() {
        val parsed = parser.parse(
            """
            [CONCURSO]
            nome: Edital 1/2027: Analista
            nivel: superior
            data_prova: 01/02/2027
            """.trimIndent()
        )

        assertThat(parsed.errors).isEmpty()
        assertThat(parsed.concursos.single().nome).isEqualTo("Edital 1/2027: Analista")
    }

    @Test
    fun `aceita data com dia e mes de um digito e nivel com acento`() {
        val parsed = parser.parse(
            """
            [CONCURSO]
            nome: A
            nivel: Médio
            data_prova: 5/3/2027
            """.trimIndent()
        )

        assertThat(parsed.errors).isEmpty()
        assertThat(parsed.concursos.single().dataProva).isEqualTo(millis(2027, 3, 5))
        assertThat(parsed.concursos.single().nivel).isEqualTo(Nivel.MEDIO)
    }

    @Test
    fun `data que nao existe descarta o concurso e reporta a linha`() {
        val parsed = parser.parse(
            """
            [CONCURSO]
            nome: Concurso A
            nivel: medio
            data_prova: 31/02/2027
            [CONTEUDOS]
            Mat;Tema 1
            """.trimIndent()
        )

        assertThat(parsed.concursos).isEmpty()
        assertThat(parsed.erros()).isEqualTo(listOf(4 to ImportLineReason.DATA_PROVA_INVALIDA))
    }

    @Test
    fun `campos obrigatorios ausentes sao reportados na linha do cabecalho do concurso`() {
        val parsed = parser.parse(
            """
            # comentario
            [CONCURSO]
            nome: Concurso A
            """.trimIndent()
        )

        assertThat(parsed.concursos).isEmpty()
        assertThat(parsed.erros()).isEqualTo(listOf(2 to ImportLineReason.NIVEL_OBRIGATORIO))
    }

    @Test
    fun `data da prova e banca sao opcionais`() {
        val parsed = parser.parse(
            """
            [CONCURSO]
            nome: Concurso sem data nem banca
            nivel: medio
            """.trimIndent()
        )

        assertThat(parsed.errors).isEmpty()
        val concurso = parsed.concursos.single()
        assertThat(concurso.dataProva).isNull()
        assertThat(concurso.banca).isEqualTo("")
    }

    @Test
    fun `nivel invalido nao gera tambem o erro de nivel obrigatorio`() {
        val parsed = parser.parse(
            """
            [CONCURSO]
            nome: Concurso A
            nivel: fundamental
            data_prova: 01/01/2027
            """.trimIndent()
        )

        assertThat(parsed.concursos).isEmpty()
        assertThat(parsed.erros()).isEqualTo(listOf(3 to ImportLineReason.NIVEL_INVALIDO))
    }

    @Test
    fun `valor de inscricao invalido descarta o concurso`() {
        val parsed = parser.parse(
            """
            [CONCURSO]
            nome: Concurso A
            nivel: medio
            data_prova: 01/01/2027
            valor_inscricao: 12a
            """.trimIndent()
        )

        assertThat(parsed.concursos).isEmpty()
        assertThat(parsed.erros()).isEqualTo(listOf(5 to ImportLineReason.VALOR_INSCRICAO_INVALIDO))
    }

    @Test
    fun `chave desconhecida e reportada mas o concurso continua valido`() {
        val parsed = parser.parse(
            """
            [CONCURSO]
            nome: Concurso A
            nivel: medio
            data_prova: 01/01/2027
            bancaa: FCC
            """.trimIndent()
        )

        assertThat(parsed.concursos).hasSize(1)
        assertThat(parsed.erros()).isEqualTo(listOf(5 to ImportLineReason.CHAVE_DESCONHECIDA))
    }

    @Test
    fun `linha de conteudo invalida e ignorada e as demais sao mantidas`() {
        val parsed = parser.parse(
            """
            [CONCURSO]
            nome: Concurso A
            nivel: medio
            data_prova: 01/01/2027
            [CONTEUDOS]
            Mat;Tema bom;1
            Mat;Eixo zero;0
            Mat;Eixo texto;abc
            Mat;;1
            Mat;Bloco ruim;1;x
            Mat;Aulas ruins;1;1;-3
            Mat;Concluido ruim;1;1;1;1;talvez
            Mat;Revisao ruim;1;1;1;1;sim;32/13/2026
            Mat;Prioridade ruim;1;1;1;1;sim;;0;urgente
            Mat;Muitos;1;1;1;1;sim;;0;alta;extra
            """.trimIndent()
        )

        assertThat(parsed.concursos.single().conteudos.map { it.descricao }).isEqualTo(listOf("Tema bom"))
        assertThat(parsed.erros()).isEqualTo(
            listOf(
                7 to ImportLineReason.EIXO_INVALIDO,
                8 to ImportLineReason.EIXO_INVALIDO,
                9 to ImportLineReason.DESCRICAO_OBRIGATORIA,
                10 to ImportLineReason.BLOCO_INVALIDO,
                11 to ImportLineReason.AULAS_INVALIDAS,
                12 to ImportLineReason.CONCLUIDO_INVALIDO,
                13 to ImportLineReason.REVISAO_INVALIDA,
                14 to ImportLineReason.PRIORIDADE_INVALIDA,
                15 to ImportLineReason.MUITOS_CAMPOS
            )
        )
    }

    @Test
    fun `conteudos antes de qualquer concurso sao reportados uma vez`() {
        val parsed = parser.parse(
            """
            [CONTEUDOS]
            Mat;Tema 1
            Mat;Tema 2
            """.trimIndent()
        )

        assertThat(parsed.concursos).isEmpty()
        assertThat(parsed.erros()).isEqualTo(listOf(1 to ImportLineReason.CONTEUDO_SEM_CONCURSO))
    }

    @Test
    fun `texto fora de secao e reportado apenas na primeira linha`() {
        val parsed = parser.parse("linha solta\noutra linha solta")

        assertThat(parsed.concursos).isEmpty()
        assertThat(parsed.erros()).isEqualTo(listOf(1 to ImportLineReason.LINHA_FORA_DE_SECAO))
    }

    @Test
    fun `secao desconhecida e ignorada com seu conteudo`() {
        val parsed = parser.parse(
            """
            [CONCURSO]
            nome: Concurso A
            nivel: medio
            data_prova: 01/01/2027
            [OUTRA]
            qualquer coisa
            [CONTEUDOS]
            Mat;Tema 1
            """.trimIndent()
        )

        assertThat(parsed.erros()).isEqualTo(listOf(5 to ImportLineReason.SECAO_DESCONHECIDA))
        assertThat(parsed.concursos.single().conteudos).hasSize(1)
    }

    @Test
    fun `secao de estudos le data horas e materia`() {
        val parsed = parser.parse(
            """
            [CONCURSO]
            nome: Concurso A
            nivel: medio
            data_prova: 01/01/2027
            [ESTUDOS]
            data;hora_inicio;hora_fim;materia
            17/09/2026;08:00;10:30;Direito Constitucional
            18/09/2026;9:15;23:59;Português
            """.trimIndent()
        )

        assertThat(parsed.errors).isEmpty()
        assertThat(parsed.concursos.single().estudos).isEqualTo(
            listOf(
                ImportedEstudo("Direito Constitucional", millis(2026, 9, 17), 8 * 60, 10 * 60 + 30),
                ImportedEstudo("Português", millis(2026, 9, 18), 9 * 60 + 15, 23 * 60 + 59)
            )
        )
    }

    @Test
    fun `estudos aceitam cabecalho reordenado ou nenhum cabecalho`() {
        val reordenado = parser.parse(
            """
            [CONCURSO]
            nome: A
            nivel: medio
            data_prova: 01/01/2027
            [ESTUDOS]
            materia;fim;inicio;data
            Direito;10:00;08:00;17/09/2026
            """.trimIndent()
        )
        val semCabecalho = parser.parse(
            """
            [CONCURSO]
            nome: A
            nivel: medio
            data_prova: 01/01/2027
            [ESTUDOS]
            17/09/2026;08:00;10:00;Direito
            """.trimIndent()
        )

        val esperado = listOf(ImportedEstudo("Direito", millis(2026, 9, 17), 480, 600))
        assertThat(reordenado.errors).isEmpty()
        assertThat(reordenado.concursos.single().estudos).isEqualTo(esperado)
        assertThat(semCabecalho.errors).isEmpty()
        assertThat(semCabecalho.concursos.single().estudos).isEqualTo(esperado)
    }

    @Test
    fun `linhas de estudo invalidas sao ignoradas e as demais mantidas`() {
        val parsed = parser.parse(
            """
            [CONCURSO]
            nome: A
            nivel: medio
            data_prova: 01/01/2027
            [ESTUDOS]
            17/09/2026;08:00;10:00;Bom
            17/09/2026;10:00;10:00;Fim igual ao inicio
            17/09/2026;10:00;09:00;Fim antes do inicio
            31/02/2026;08:00;09:00;Data ruim
            17/09/2026;8h;09:00;Inicio ruim
            17/09/2026;08:00;25:00;Fim ruim
            17/09/2026;08:00;09:00;
            17/09/2026;08:00;09:00;Mat;extra
            """.trimIndent()
        )

        assertThat(parsed.concursos.single().estudos.map { it.materia }).isEqualTo(listOf("Bom"))
        assertThat(parsed.erros()).isEqualTo(
            listOf(
                7 to ImportLineReason.ESTUDO_INTERVALO_INVALIDO,
                8 to ImportLineReason.ESTUDO_INTERVALO_INVALIDO,
                9 to ImportLineReason.ESTUDO_DATA_INVALIDA,
                10 to ImportLineReason.ESTUDO_HORA_INICIO_INVALIDA,
                11 to ImportLineReason.ESTUDO_HORA_FIM_INVALIDA,
                12 to ImportLineReason.ESTUDO_MATERIA_OBRIGATORIA,
                13 to ImportLineReason.MUITOS_CAMPOS
            )
        )
    }

    @Test
    fun `cabecalho de estudos sem todas as colunas e reportado`() {
        val parsed = parser.parse(
            """
            [CONCURSO]
            nome: A
            nivel: medio
            data_prova: 01/01/2027
            [ESTUDOS]
            data;materia
            17/09/2026;Direito
            """.trimIndent()
        )

        assertThat(parsed.erros()).isEqualTo(listOf(6 to ImportLineReason.CABECALHO_ESTUDO_INCOMPLETO))
        assertThat(parsed.concursos.single().estudos).isEmpty()
    }

    @Test
    fun `estudos antes de qualquer concurso sao reportados uma vez`() {
        val parsed = parser.parse(
            """
            [ESTUDOS]
            17/09/2026;08:00;10:00;Direito
            17/09/2026;10:00;11:00;Português
            """.trimIndent()
        )

        assertThat(parsed.concursos).isEmpty()
        assertThat(parsed.erros()).isEqualTo(listOf(1 to ImportLineReason.ESTUDO_SEM_CONCURSO))
    }

    @Test
    fun `conteudos e estudos podem vir juntos no mesmo concurso`() {
        val parsed = parser.parse(
            """
            [CONCURSO]
            nome: A
            nivel: medio
            data_prova: 01/01/2027
            [CONTEUDOS]
            Mat;Tema 1
            [ESTUDOS]
            17/09/2026;08:00;10:00;Mat
            [CONCURSO]
            nome: B
            nivel: superior
            data_prova: 02/02/2027
            """.trimIndent()
        )

        assertThat(parsed.errors).isEmpty()
        assertThat(parsed.concursos.map { it.conteudos.size to it.estudos.size }).isEqualTo(listOf(1 to 1, 0 to 0))
    }

    @Test
    fun `arquivo vazio nao gera concursos nem erros`() {
        val parsed = parser.parse("")

        assertThat(parsed.concursos).isEmpty()
        assertThat(parsed.errors).isEmpty()
    }

    @Test
    fun `concluido aceita variacoes de sim e nao`() {
        val parsed = parser.parse(
            """
            [CONCURSO]
            nome: A
            nivel: medio
            data_prova: 01/01/2027
            [CONTEUDOS]
            M;t1;;;;;S
            M;t2;;;;;true
            M;t3;;;;;x
            M;t4;;;;;NÃO
            M;t5;;;;;0
            M;t6
            """.trimIndent()
        )

        assertThat(parsed.errors).isEmpty()
        assertThat(parsed.concursos.single().conteudos.map { it.concluido })
            .isEqualTo(listOf(true, true, true, false, false, false))
    }

    @Test
    fun `conteudos sem eixo ou bloco ficam nulos`() {
        val parsed = parser.parse(
            """
            [CONCURSO]
            nome: A
            nivel: medio
            data_prova: 01/01/2027
            [CONTEUDOS]
            M;t1
            """.trimIndent()
        )

        val conteudo = parsed.concursos.single().conteudos.single()
        assertThat(conteudo.eixo).isNull()
        assertThat(conteudo.bloco).isNull()
        assertThat(conteudo.concluido).isFalse()
        assertThat(conteudo.dataUltimaRevisao).isNull()
        assertThat(parsed.errors.isEmpty()).isTrue()
    }

    @Test
    fun `conteudo sem prioridade informada fica com prioridade media`() {
        val parsed = parser.parse(
            """
            [CONCURSO]
            nome: A
            nivel: medio
            [CONTEUDOS]
            M;t1
            """.trimIndent()
        )

        assertThat(parsed.errors).isEmpty()
        assertThat(parsed.concursos.single().conteudos.single().prioridade).isEqualTo(Prioridade.MEDIA)
    }

    @Test
    fun `prioridade aceita os quatro niveis, acentos e caixa`() {
        val parsed = parser.parse(
            """
            [CONCURSO]
            nome: A
            nivel: medio
            [CONTEUDOS]
            materia;descricao;eixo;bloco;aulas;min_por_aula;concluido;ultima_revisao;questoes;prioridade
            M;t1;;;;;;;;alta
            M;t2;;;;;;;;MÉDIA
            M;t3;;;;;;;;Baixa
            M;t4;;;;;;;;opcional
            """.trimIndent()
        )

        assertThat(parsed.errors).isEmpty()
        assertThat(parsed.concursos.single().conteudos.map { it.prioridade }).isEqualTo(
            listOf(Prioridade.ALTA, Prioridade.MEDIA, Prioridade.BAIXA, Prioridade.OPCIONAL)
        )
    }

    @Test
    fun `prioridade invalida descarta so aquela linha`() {
        val parsed = parser.parse(
            """
            [CONCURSO]
            nome: A
            nivel: medio
            [CONTEUDOS]
            materia;descricao;eixo;bloco;aulas;min_por_aula;concluido;ultima_revisao;questoes;prioridade
            M;t1;;;;;;;;urgente
            """.trimIndent()
        )

        assertThat(parsed.concursos.single().conteudos).isEmpty()
        assertThat(parsed.erros()).isEqualTo(listOf(6 to ImportLineReason.PRIORIDADE_INVALIDA))
    }
}
