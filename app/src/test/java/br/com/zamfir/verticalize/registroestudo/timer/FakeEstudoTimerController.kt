package br.com.zamfir.verticalize.registroestudo.timer

class FakeEstudoTimerController : EstudoTimerController {
    var startCalls = mutableListOf<Pair<Long, String>>()
    var pauseCallCount = 0
    var resumeCallCount = 0
    var registrarCallCount = 0
    var descartarCallCount = 0

    override fun start(concursoId: Long, materia: String) {
        startCalls.add(concursoId to materia)
    }

    override fun pause() {
        pauseCallCount++
    }

    override fun resume() {
        resumeCallCount++
    }

    override fun registrar() {
        registrarCallCount++
    }

    override fun descartar() {
        descartarCallCount++
    }
}
