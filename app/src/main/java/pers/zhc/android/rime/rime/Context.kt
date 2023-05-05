package pers.zhc.android.rime.rime

class Context(private val addr: Long, private val session: Session) {
    private fun checkSessionClosed() {
        if (session.closed) {
            throw RuntimeException("Session has been closed")
        }
    }

    fun getPreedit(): String? {
        checkSessionClosed()
        return JNI.getPreedit(addr)
    }

    @Suppress("ArrayInDataClass")
    data class Candidates(
        val candidates: Array<JNI.Candidate>,
        val selectedPos: Int,
    )

    fun getCandidates(): Candidates {
        checkSessionClosed()
        val candidates = JNI.getCandidates(addr, JNI.PHANTOM_CANDIDATE)
        val selectedPos = JNI.getSelectedCandidatesPos(addr)
        return Candidates(candidates, selectedPos)
    }

    protected fun finalize() {
        checkSessionClosed()
        println("Finalize Context")
        JNI.releaseContext(addr)
    }
}
