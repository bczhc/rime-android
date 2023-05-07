package pers.zhc.android.rime.rime

import pers.zhc.android.rime.annotation.CheckReturnValue

// TODO: release resource
class Engine(private val addr: Long) {
    var closed = false

    fun createSession(): Session {
        val sessionAddr = JNI.createSession(addr)
        return Session(sessionAddr, this)
    }

    @CheckReturnValue
    fun waitForDeployment(): DeployStatus {
        return when (JNI.waitForDeployment(addr)) {
            true -> DeployStatus.SUCCESS
            false -> DeployStatus.FAILURE
        }
    }

    protected fun finalize() {
        println("Finalize Engine")
        JNI.releaseEngine(addr)
        closed = true
    }

    fun setNotificationHandler(callback: ((messageType: String, messageValue: String) -> Unit)?) {
        if (callback == null) {
            JNI.setNotificationHandler(addr, null)
        }
        JNI.setNotificationHandler(addr, object : JNI.NotificationHandlerCallback {
            override fun onMessage(messageType: String, messageValue: String) {
                callback!!(messageType, messageValue)
            }
        })
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
