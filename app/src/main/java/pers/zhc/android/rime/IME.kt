package pers.zhc.android.rime

import android.annotation.SuppressLint
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import pers.zhc.android.rime.ImeSettingsActivity.Companion.CONFIGS_FILE
import pers.zhc.android.rime.MyApplication.Companion.GSON
import pers.zhc.android.rime.databinding.ImeCandidateViewBinding
import pers.zhc.android.rime.databinding.ImeCandidatesViewBinding
import pers.zhc.android.rime.rime.*
import pers.zhc.android.rime.util.fromJsonOrNull
import kotlin.concurrent.thread

class IME : InputMethodService() {
    private var candidatesViewBinding: ImeCandidatesViewBinding? = null
    private var ic: InputConnection? = null

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        ic = currentInputConnection
    }

    override fun onCreate() {
        super.onCreate()
        candidatesViewBinding = ImeCandidatesViewBinding.inflate(layoutInflater)
        setupSession()
    }

    private fun onKey(event: KeyEvent): Boolean {
        val session = SESSION ?: return false
        val ic = ic ?: return false
        val candidatesViewBinding = candidatesViewBinding ?: return false

        val rimeKeyEvent = toRimeKey(event)
        val keyStatus = session.processKey(rimeKeyEvent)
        if (keyStatus == KeyStatus.PASS) {
            return false
        }

        val context = session.getContext()
        if (context != null) {
            candidatesViewBinding.setPreedit(context.getPreedit() ?: "")
            candidatesViewBinding.setCandidates(context.getCandidates())
        }
        val commit = session.getCommit()
        if (commit != null) {
            ic.commit(commit)
        }

        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return onKey(event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return onKey(event)
    }

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return true
    }

    override fun onCreateCandidatesView(): View {
        setCandidatesViewShown(true)
        return candidatesViewBinding!!.root
    }

    companion object {
        private var SESSION: Session? = null

        private fun setupSession() {
            if (SESSION != null) {
                return
            }
            val configs = GSON.fromJsonOrNull(CONFIGS_FILE.readText(), RimeConfigs::class.java)
            val userDataDir = configs?.userDataDir ?: ""
            val sharedDataDir = configs?.sharedDataDir ?: ""
            val engine = Engine.create(userDataDir, sharedDataDir)
            thread {
                val result = engine.waitForDeployment()
                if (result == Engine.Companion.DeployStatus.SUCCESS) {
                    SESSION = engine.createSession()
                }
            }
        }
    }
}

fun ImeCandidatesViewBinding.setPreedit(text: String) {
    this.preeditView.text = text
}

@SuppressLint("SetTextI18n")
fun ImeCandidatesViewBinding.setCandidates(candidates: Context.Candidates) {
    val candidatesLL = this.candidatesLl
    candidatesLL.removeAllViews()
    for (candidate in candidates.candidates) {
        val candidateView = ImeCandidateViewBinding.inflate(LayoutInflater.from(this.root.context)).apply {
            var text = candidate.text
            candidate.comment?.let { text += " $it" }
            candidateView.text = text
        }.root
        candidatesLL.addView(candidateView)
    }
}

fun InputConnection.commit(text: String) {
    this.commitText(text, 1 /* a value > 0 */)
}
