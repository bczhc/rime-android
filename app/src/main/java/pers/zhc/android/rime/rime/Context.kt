package pers.zhc.android.rime.rime

class Context(private val addr: Long) {
    fun getPreedit(): String? {
        return JNI.getPreedit(addr)
    }

    @Suppress("ArrayInDataClass")
    data class Candidates(
        val candidates: Array<JNI.Candidate>,
        val selectedPos: Int,
    )

    fun getCandidates(): Candidates {
        val candidates = JNI.getCandidates(addr, JNI.PHANTOM_CANDIDATE)
        val selectedPos = JNI.getSelectedCandidatesPos(addr)
        return Candidates(candidates, selectedPos)
    }

    protected fun finalize() {
        JNI.releaseContext(addr)
    }
}
