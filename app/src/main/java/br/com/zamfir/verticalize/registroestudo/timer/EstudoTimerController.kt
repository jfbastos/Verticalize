package br.com.zamfir.verticalize.registroestudo.timer

import android.content.Context

/** Ponto único por onde a presentation layer controla o cronômetro, sem depender do Android Service/Context direto. */
interface EstudoTimerController {
    fun start(concursoId: Long, materia: String)
    fun pause()
    fun resume()
    fun registrar()
    fun descartar()
}

class AndroidEstudoTimerController(private val context: Context) : EstudoTimerController {
    override fun start(concursoId: Long, materia: String) = EstudoTimerService.start(context, concursoId, materia)
    override fun pause() = EstudoTimerService.pause(context)
    override fun resume() = EstudoTimerService.resume(context)
    override fun registrar() = EstudoTimerService.registrar(context)
    override fun descartar() = EstudoTimerService.descartar(context)
}
