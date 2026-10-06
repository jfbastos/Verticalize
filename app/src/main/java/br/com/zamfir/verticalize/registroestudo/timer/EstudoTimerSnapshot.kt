package br.com.zamfir.verticalize.registroestudo.timer

enum class EstudoTimerStatus {
    IDLE,
    RUNNING,
    PAUSED
}

/**
 * Estado do cronômetro global (só existe um por vez, controlado pelo [EstudoTimerService]).
 * [accumulatedMillis] é o tempo ativo já contado antes do trecho atual; enquanto [status] é
 * [EstudoTimerStatus.RUNNING], o tempo decorrido do trecho atual é somado a partir de [segmentStartEpochMillis].
 */
data class EstudoTimerSnapshot(
    val status: EstudoTimerStatus = EstudoTimerStatus.IDLE,
    val concursoId: Long = 0L,
    val materia: String = "",
    val startEpochMillis: Long = 0L,
    val accumulatedMillis: Long = 0L,
    val segmentStartEpochMillis: Long = 0L
)

fun EstudoTimerSnapshot.elapsedMillis(nowMillis: Long = System.currentTimeMillis()): Long {
    val emAndamento = if (status == EstudoTimerStatus.RUNNING) nowMillis - segmentStartEpochMillis else 0L
    return accumulatedMillis + emAndamento
}

/** "12:34" (minutos:segundos) ou "1:02:34" (horas:minutos:segundos) quando passa de uma hora. */
fun formatElapsedClock(millis: Long): String {
    val totalSegundos = millis / 1000
    val horas = totalSegundos / 3600
    val minutos = (totalSegundos % 3600) / 60
    val segundos = totalSegundos % 60
    return if (horas > 0) {
        "%d:%02d:%02d".format(horas, minutos, segundos)
    } else {
        "%02d:%02d".format(minutos, segundos)
    }
}
