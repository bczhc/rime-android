package pers.zhc.android.rime.rime

class Session(private val addr: Long, private val engine: Engine) {
    init {
        // prevent engine being finalized first than this
        // (JVM finalizers are unordered)
        // https://www.hboehm.info/misc_slides/java_finalizers.pdf
        engineRefMap[addr] = engine
    }

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
        })
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
        println("Finalize Session")
        checkEngineClosed()
        JNI.closeSession(addr)
        // remove engine; now Engine can be finalized
        engineRefMap.remove(addr)
    }

    companion object {
        val engineRefMap = HashMap<Long, Engine>()
    }
}
