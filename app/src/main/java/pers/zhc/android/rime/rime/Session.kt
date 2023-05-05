package pers.zhc.android.rime.rime

class Session(private val addr: Long, private val engine: Engine) {
    var closed = false
    private fun checkEngineClosed() {
        if (engine.closed) {
            throw RuntimeException("Engine has been closed")
        }
    }

    fun getContext(): Context? {
        checkEngineClosed()
        return Context(JNI.getContext(addr).also {
            if (it == 0L) {
                return null
            }
        }, this)
    }

    fun getCommit(): String? {
        checkEngineClosed()
        return JNI.getCommit(addr)
    }

    fun processKey(event: KeyEvent): KeyStatus {
        checkEngineClosed()
        return when (JNI.processKey(addr, event.keyCode, event.modifier)) {
            true -> KeyStatus.ACCEPTED
            false -> KeyStatus.PASS
        }
    }

    protected fun finalize() {
        checkEngineClosed()
        println("Finalize Session")
        JNI.closeSession(addr)
        closed = true
    }
}
