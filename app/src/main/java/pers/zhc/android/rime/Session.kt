package pers.zhc.android.rime

import pers.zhc.android.rime.rime.JNI
import pers.zhc.android.rime.rime.Rime
import pers.zhc.android.rime.rime.Session
import pers.zhc.android.rime.util.ToastUtils

object Session {
    var SESSION: Session? = null

    fun tryGetSession(): Session? {
        trySetupSession()
        return SESSION
    }

    fun trySetupSession() {
        if (SESSION != null) {
            return
        }
        // when full-check deployment is in progress, prevent IME from creating sessions
        if (ImeSettingsActivity.FULL_DEPLOYING) {
            return
        }
        if (!Rime.initialized) {
            val configs = ImeSettingsActivity.getConfigs()
            val userDataDir = configs?.userDataDir ?: ""
            val sharedDataDir = configs?.sharedDataDir ?: ""
            Rime.reinitialize(userDataDir, sharedDataDir)
            setUpOnOptionChangedHandler()
        }
        try {
            JNI.deploy()
        } catch (_: Exception) {
        }
        SESSION = try {
            Session.create()
        } catch (_: Exception) {
            null
        }
    }

    fun resetSession() {
        SESSION?.let {
            it.close()
            SESSION = null
        }
    }

    private fun setUpOnOptionChangedHandler() {
        val appContext = MyApplication.CONTEXT
        Rime.setNotificationHandler { messageType, messageValue ->
            println("Message: " + Pair(messageType, messageValue))
            if (messageType == "option") {
                if (messageValue.startsWith('!')) {
                    val option = messageValue.substring(1)
                    ToastUtils.show(appContext, "$option: off")
                } else {
                    ToastUtils.show(appContext, "$messageValue: on")
                }
            }
        }
    }
}
