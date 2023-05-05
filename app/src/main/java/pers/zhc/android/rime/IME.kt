package pers.zhc.android.rime

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import pers.zhc.android.rime.ImeSettingsActivity.Companion.CONFIGS_FILE
import pers.zhc.android.rime.MyApplication.Companion.GSON
import pers.zhc.android.rime.databinding.ImeCandidatesViewBinding
import pers.zhc.android.rime.rime.Engine
import pers.zhc.android.rime.rime.Session
import pers.zhc.android.rime.rime.toRimeKey
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
        val keyCode = event.keyCode
//        val session = SESSION!!

//        val context = session.getContext()
//        ToastUtils.show(this, context!!.getCandidates().toString())

        println(Pair(event.keyCode, event.unicodeChar))
        println(toRimeKey(event))

        return false
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
