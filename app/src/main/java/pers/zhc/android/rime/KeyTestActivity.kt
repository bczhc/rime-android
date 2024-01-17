package pers.zhc.android.rime

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import pers.zhc.android.rime.databinding.ImeCandidatesViewBinding
import pers.zhc.android.rime.databinding.KeyTestActivityBinding
import pers.zhc.android.rime.rime.Context
import pers.zhc.tools.utils.setLinearLayoutManager

class KeyTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bindings = KeyTestActivityBinding.inflate(layoutInflater)
        setContentView(bindings.root)

        val listAdapter = CandidatesListAdapter()
        val icvBindings = ImeCandidatesViewBinding.inflate(layoutInflater).apply {
            recyclerView.setLinearLayoutManager()
            recyclerView.adapter = listAdapter
        }
        bindings.container.addView(icvBindings.root)

        val inputET = bindings.inputEt
        val commitTV = bindings.commitTv

        var textWatcher: TextWatcher? = null
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                inputET.removeTextChangedListener(textWatcher!!)
                inputET.setText("")

                run {
                    val char = s!!.toString().lastOrNull() ?: return@run
                    val session = Session.tryGetSession() ?: return@run
                    session.simulateKeys(char.toString())
                    val context = session.getContext() ?: return@run
                    val commit = session.getCommit()
                    val candidates = context.getCandidates().candidates

                    icvBindings.preeditView.text = context.getPreedit() ?: ""
                    commit?.let { commitTV.text.append(it) }
                    listAdapter.update(Context.Candidates(candidates, -1))
                }

                inputET.addTextChangedListener(textWatcher!!)
            }
        }

        inputET.addTextChangedListener(textWatcher)
    }
}
