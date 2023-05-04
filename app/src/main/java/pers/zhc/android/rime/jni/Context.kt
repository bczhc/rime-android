package pers.zhc.android.rime.jni

class Context(private val addr: Long) {
    fun getPreedit(): String? {
        return Rime.getPreedit(addr)
    }

    @Suppress("ArrayInDataClass")
    data class Candidates(
        val candidates: Array<Rime.Companion.Candidate>,
        val selectedPos: Int,
    )

    fun getCandidates(): Candidates {
        val candidates = Rime.getCandidates(addr, Rime.PHANTOM_CANDIDATE)
        val selectedPos = Rime.getSelectedCandidatesPos(addr)
        return Candidates(candidates, selectedPos)
    }
}
