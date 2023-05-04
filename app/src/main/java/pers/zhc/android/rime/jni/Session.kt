package pers.zhc.android.rime.jni

class Session(private val addr: Long) {
    fun getContext(): Context? {
        return Context(Rime.getContext(addr).also {
            if (it == 0L) {
                return null
            }
        })
    }

    fun getCommit(): String? {
        return Rime.getCommit(addr)
    }

    fun processKey(event: KeyEvent): KeyStatus {
        return when (Rime.processKey(addr, event.keyCode, event.modifier)) {
            true -> KeyStatus.ACCEPTED
            false -> KeyStatus.PASS
        }
    }
}
