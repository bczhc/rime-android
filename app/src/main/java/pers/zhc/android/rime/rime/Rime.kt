package pers.zhc.android.rime.rime

object Rime {
    var initialized = false

    enum class DeployStatus {
        SUCCESS, FAILURE
    }

    fun reinitialize(userDataDir: String, sharedDataDir: String) {
        if (initialized) {
            JNI.finalize()
            initialized = false
        }
        JNI.initialize(userDataDir, sharedDataDir)
        initialized = true
    }

    fun fullDeployAndWait(): DeployStatus {
        return when (JNI.fullDeployAndWait()) {
            true -> DeployStatus.SUCCESS
            false -> DeployStatus.FAILURE
        }
    }

    fun setNotificationHandler(handler: ((messageType: String, messageValue: String) -> Unit)?) {
        if (handler == null) {
            JNI.setNotificationHandler(null)
            return
        }
        JNI.setNotificationHandler(object : JNI.NotificationHandlerCallback {
            override fun onMessage(messageType: String, messageValue: String) {
                handler(messageType, messageValue)
            }
        })
    }
}
