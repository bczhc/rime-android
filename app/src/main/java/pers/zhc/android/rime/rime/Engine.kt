package pers.zhc.android.rime.rime

import pers.zhc.android.rime.annotation.CheckReturnValue

class Engine(private val addr: Long) {
    fun createSession(): Session {
        val sessionAddr = JNI.createSession(addr)
        return Session(sessionAddr)
    }

    @CheckReturnValue
    fun waitForDeployment(): Boolean {
        return JNI.waitForDeployment(addr)
    }

    companion object {
        fun create(userDataDir: String, sharedDataDir: String?): Engine {
            val addr = JNI.createEngine(userDataDir, sharedDataDir)
            return Engine(addr)
        }
    }
}
