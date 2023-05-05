package pers.zhc.android.rime.rime

import pers.zhc.android.rime.annotation.CheckReturnValue

// TODO: release resource
class Engine(private val addr: Long) {
    fun createSession(): Session {
        val sessionAddr = JNI.createSession(addr)
        return Session(sessionAddr)
    }

    @CheckReturnValue
    fun waitForDeployment(): DeployStatus {
        return when (JNI.waitForDeployment(addr)) {
            true -> DeployStatus.SUCCESS
            false -> DeployStatus.FAILURE
        }
    }

    companion object {
        fun create(userDataDir: String, sharedDataDir: String?): Engine {
            val addr = JNI.createEngine(userDataDir, sharedDataDir)
            return Engine(addr)
        }

        fun deploy(userDataDir: String, sharedDataDir: String?): DeployStatus {
            create(userDataDir, sharedDataDir).let {
                return it.waitForDeployment()
            }
        }

        enum class DeployStatus {
            SUCCESS,
            FAILURE,
        }
    }
}
