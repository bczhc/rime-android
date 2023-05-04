package pers.zhc.android.rime.jni

import pers.zhc.android.rime.annotation.CheckReturnValue

class Engine(private val addr: Long) {
    fun createSession(): Session {
        val sessionAddr = Rime.createSession(addr)
        return Session(sessionAddr)
    }

    @CheckReturnValue
    fun waitForDeployment(): Boolean {
        return Rime.waitForDeployment(addr)
    }

    companion object {
        fun create(userDataDir: String, sharedDataDir: String?): Engine {
            val addr = Rime.createEngine(userDataDir, sharedDataDir)
            return Engine(addr)
        }
    }
}
