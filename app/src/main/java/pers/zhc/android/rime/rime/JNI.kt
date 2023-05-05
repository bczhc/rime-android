package pers.zhc.android.rime.rime

import pers.zhc.android.rime.annotation.CheckReturnValue

object JNI {
    init {
        System.loadLibrary("rime")
        System.loadLibrary("rime_jni")
        initModules()
    }

    @JvmStatic
    private external fun initModules()

    @JvmStatic
    external fun getRimeVersion(): String

    @JvmStatic
    external fun createEngine(
        userDataDir: String,
        sharedDataDir: String?,
    ): Long

    /**
     * this function will block
     *
     * @return true on success, false otherwise
     */
    @JvmStatic
    @CheckReturnValue
    external fun waitForDeployment(engineAddr: Long): Boolean

    @JvmStatic
    external fun createSession(engineAddr: Long): Long

    /**
     * @return true: accepted; false: pass
     */
    @JvmStatic
    @CheckReturnValue
    external fun processKey(sessionAddr: Long, keyCode: Int, modifier: Int): Boolean

    @JvmStatic
    external fun closeSession(sessionAddr: Long)

    @JvmStatic
    external fun releaseEngine(engineAddr: Long)

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
    external fun getCandidates(contextAddr: Long, phantomCandidate: Candidate): Array<Candidate>

    @JvmStatic
    external fun getSelectedCandidatesPos(contextAddr: Long): Int

    data class Candidate(
        val selectLabel: String?,
        val text: String,
        val comment: String?,
    )

    val PHANTOM_CANDIDATE = Candidate(null, "", null)

    @JvmStatic
    external fun getCommit(sessionAddr: Long): String?
}
