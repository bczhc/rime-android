package pers.zhc.android.rime.rime

import kotlin.jvm.Throws

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

    fun close() {
        println("Close session $addr")
        JNI.closeSession(addr)
    }

    fun getStatus(): JNI.Status {
        return JNI.getStatus(addr, JNI.DUMMY_STATUS)
    }

    fun simulateKeys(keySequence: String) {
        return JNI.simulateKeys(addr, keySequence)
    }

    companion object {
        @Throws(RuntimeException::class)
        fun create(): Session {
            return Session(JNI.createSession())
        }
    }
}
