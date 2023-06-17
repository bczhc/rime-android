package pers.zhc.android.rime

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.RecyclerView
import pers.zhc.android.rime.ImeSettingsActivity.Companion.getConfigs
import pers.zhc.android.rime.databinding.ImeCandidateItemBinding
import pers.zhc.android.rime.databinding.ImeCandidatesViewBinding
import pers.zhc.android.rime.rime.*
import pers.zhc.android.rime.util.ToastUtils
import pers.zhc.tools.utils.setLinearLayoutManager

class IME : InputMethodService() {
    private var candidatesViewBinding: ImeCandidatesViewBinding? = null
    private var ic: InputConnection? = null
    private var candidatesAdapter: CandidatesListAdapter? = null

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        ic = currentInputConnection
        Session.trySetupSession()

        getConfigs()?.let {
            runCatching {
                candidatesAdapter!!.candidatesTypeface = Typeface.createFromFile(it.customFontPath)
            }.onFailure {
                ToastUtils.show(this, R.string.invalid_font_toast)
                candidatesAdapter!!.candidatesTypeface = null
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val themedContext = ContextThemeWrapper(this, R.style.Theme_Main)
        candidatesViewBinding = ImeCandidatesViewBinding.inflate(LayoutInflater.from(themedContext))
        candidatesAdapter = CandidatesListAdapter()
        Session.trySetupSession()
    }

    private fun onKey(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && isInputViewShown) {
            hideWindow()
            return true
        }

        Session.trySetupSession()

        val session = Session.SESSION ?: return false
        val ic = ic ?: return false
        val candidatesViewBinding = candidatesViewBinding ?: return false

        val rimeKeyEvent = toRimeKey(event) ?: return false
        val keyStatus = session.processKey(rimeKeyEvent)
        if (keyStatus == KeyStatus.PASS) {
            return false
        }

        val context = session.getContext()
        if (context != null) {
            candidatesViewBinding.setPreedit(context.getPreedit() ?: "")
            val candidates = context.getCandidates()
            candidatesAdapter!!.update(candidates)
        }
        val commit = session.getCommit()
        if (commit != null) {
            ic.commit(commit)
        }
        println(session.getStatus())

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
        candidatesViewBinding!!.recyclerView.apply {
            adapter = candidatesAdapter!!
            setLinearLayoutManager()
        }
        return candidatesViewBinding!!.root
    }

    override fun onComputeInsets(outInsets: Insets) {
        super.onComputeInsets(outInsets)
        if (!isFullscreenMode) {
            outInsets.contentTopInsets = outInsets.visibleTopInsets
        }
    }
}

fun ImeCandidatesViewBinding.setPreedit(text: String) {
    this.preeditView.text = text
}

fun InputConnection.commit(text: String) {
    this.commitText(text, 1 /* a value > 0 */)
}

class CandidatesListAdapter : RecyclerView.Adapter<CandidatesListAdapter.MyViewHolder>() {
    private var candidates: Context.Candidates? = null
    var candidatesTypeface: Typeface? = null
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class MyViewHolder(bindings: ImeCandidateItemBinding) : RecyclerView.ViewHolder(bindings.root) {
        val candidateTV = bindings.candidateView
        val selectLabelTV = bindings.selectLabelTv
        val rootRL = bindings.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val bindings = ImeCandidateItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(bindings)
    }

    override fun getItemCount(): Int {
        return (candidates ?: return 0).candidates.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // when candidates is null, `getItemCount()` gets 0; we assert it's not null here
        val candidates = candidates!!
        val (selectLabel, candidateText, comment) = candidates.candidates[position]
        holder.selectLabelTV.text = selectLabel ?: (position + 1).toString()
        var text = candidateText
        comment?.let { text += " $it" }
        holder.candidateTV.text = text
        holder.candidateTV.typeface = candidatesTypeface
        if (position == candidates.selectedPos) {
            holder.rootRL.setBackgroundColor(Color.LTGRAY)
        } else {
            holder.rootRL.setBackgroundColor(Color.WHITE)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(candidates: Context.Candidates) {
        this.candidates = candidates
        notifyDataSetChanged()
    }
}
