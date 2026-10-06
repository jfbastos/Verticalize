package br.com.zamfir.verticalize.concurso.presentation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ConcursoListRoute : NavKey

@Serializable
data class ConcursoDetailRoute(val concursoId: Long) : NavKey
