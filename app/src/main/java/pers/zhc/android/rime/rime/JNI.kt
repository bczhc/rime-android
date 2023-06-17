package pers.zhc.android.rime.rime

import pers.zhc.android.rime.annotation.CheckReturnValue

object JNI {
    init {
        System.loadLibrary("rime")
        System.loadLibrary("rime_jni")
        jniInit()
    }

    @JvmStatic
    private external fun jniInit()

    @JvmStatic
    external fun initialize(userDataDir: String, sharedDataDir: String)

    @JvmStatic
    external fun finalize()

    @JvmStatic
    external fun getRimeVersion(): String

    @JvmStatic
    external fun deploy()

    /**
     * Perform a full-check deployment. This function will block.
     *
     * @return true on success, false otherwise
     */
    @JvmStatic
    external fun fullDeployAndWait(): Boolean

    @JvmStatic
    external fun createSession(): Long

    /**
     * @return true: accepted; false: pass
     */
    @JvmStatic
    @CheckReturnValue
    external fun processKey(sessionAddr: Long, keyCode: Int, modifier: Int): Boolean

    @JvmStatic
    external fun closeSession(sessionAddr: Long)

    @JvmStatic
    external fun releaseContext(contextAddr: Long)

    /**
     * @return 0 for None
     */
    @JvmStatic
    external fun getContext(sessionAddr: Long): Long

    @JvmStatic
    external fun getPreedit(contextAddr: Long): String?

    @JvmStatic
    external fun getCandidates(contextAddr: Long, dummyCandidate: Candidate): Array<Candidate>

    @JvmStatic
    external fun getSelectedCandidatesPos(contextAddr: Long): Int

    data class Candidate(
        val selectLabel: String?,
        val text: String,
        val comment: String?,
    )

    val DUMMY_CANDIDATE = Candidate(null, "", null)

    @JvmStatic
    external fun getCommit(sessionAddr: Long): String?

    interface NotificationHandlerCallback {
        fun onMessage(messageType: String, messageValue: String)
    }

    @JvmStatic
    external fun setNotificationHandler(callback: NotificationHandlerCallback?)

    data class Status(
        val schemaName: String,
        val schemaId: String,
        val isDisabled: Boolean,
        val isComposing: Boolean,
        val isAsciiMode: Boolean,
        val isFullShape: Boolean,
        val isSimplified: Boolean,
        val isTraditional: Boolean,
        val isAsciiPunct: Boolean,
    )

    @Suppress("BooleanLiteralArgument")
    val DUMMY_STATUS = Status("", "", false, false, false,
        false, false, false, false)

    @JvmStatic
    external fun getStatus(sessionAddr: Long, dummyStatus: Status): Status

    @JvmStatic
    external fun simulateKeys(sessionAddr: Long, keySequence: String)
}
