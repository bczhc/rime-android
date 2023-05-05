package pers.zhc.android.rime.rime

class Session(private val addr: Long) {
    fun getContext(): Context? {
        return Context(JNI.getContext(addr).also {
            if (it == 0L) {
                return null
            }
        })
    }

    fun getCommit(): String? {
        return JNI.getCommit(addr)
    }

    fun processKey(event: KeyEvent): KeyStatus {
        return when (JNI.processKey(addr, event.keyCode, event.modifier)) {
            true -> KeyStatus.ACCEPTED
            false -> KeyStatus.PASS
        }
    }

    protected fun finalize() {
        JNI.closeSession(addr)
    }
}
