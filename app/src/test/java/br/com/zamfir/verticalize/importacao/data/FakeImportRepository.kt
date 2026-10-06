package br.com.zamfir.verticalize.importacao.data

import br.com.zamfir.verticalize.concurso.domain.Concurso
import br.com.zamfir.verticalize.conteudo.domain.Conteudo
import br.com.zamfir.verticalize.core.domain.DataError
import br.com.zamfir.verticalize.core.domain.Result
import br.com.zamfir.verticalize.importacao.domain.ImportPlan
import br.com.zamfir.verticalize.importacao.domain.ImportPlanner
import br.com.zamfir.verticalize.importacao.domain.ImportRepository
import br.com.zamfir.verticalize.importacao.domain.ImportSummary
import br.com.zamfir.verticalize.importacao.domain.ParsedImport

class FakeImportRepository(
    private val planner: ImportPlanner = ImportPlanner()
) : ImportRepository {
    var concursosExistentes: List<Concurso> = emptyList()
    var conteudosExistentes: List<Conteudo> = emptyList()
    var shouldFailAnalisar: Boolean = false
    var shouldFailAplicar: Boolean = false

    /** Plano recebido por [aplicar]; nulo se nada foi gravado. */
    var planoAplicado: ImportPlan? = null
        private set

    override suspend fun analisar(parsed: ParsedImport): Result<ImportPlan, DataError.Local> {
        if (shouldFailAnalisar) return Result.Error(DataError.Local.UNKNOWN)
        return Result.Success(planner.plan(parsed, concursosExistentes, conteudosExistentes))
    }

    override suspend fun aplicar(plan: ImportPlan): Result<ImportSummary, DataError.Local> {
        if (shouldFailAplicar) return Result.Error(DataError.Local.UNKNOWN)
        planoAplicado = plan
        return Result.Success(ImportSummary(plan.concursosNovos, plan.conteudosNovos, plan.estudosNovos))
    }
}
