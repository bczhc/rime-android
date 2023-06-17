package pers.zhc.android.rime

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import pers.zhc.android.rime.databinding.KeyTestActivityBinding
import pers.zhc.tools.utils.setLinearLayoutManager

class KeyTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bindings = KeyTestActivityBinding.inflate(layoutInflater)
        setContentView(bindings.root)

        val inputET = bindings.inputEt
        val recyclerView = bindings.recyclerView
        val preeditTV = bindings.preeditView
        val commitTV = bindings.commitTv

        val listAdapter = Adapter()
        recyclerView.setLinearLayoutManager()
        recyclerView.adapter = listAdapter

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
                    val candidates = context.getCandidates().candidates.mapIndexed { i, it ->
                        "${it.selectLabel ?: (i + 1).toString()} ${it.text} ${it.comment ?: ""}"
                    }

                    preeditTV.text = context.getPreedit() ?: ""
                    commit?.let { commitTV.text.append(it) }
                    listAdapter.update(candidates)
                }

                inputET.addTextChangedListener(textWatcher!!)
            }
        }

        inputET.addTextChangedListener(textWatcher)
    }

    class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>() {
        private var candidates: List<String>? = null

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView = view.findViewById<TextView>(android.R.id.text1)!!
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                android.R.layout.simple_list_item_1, parent, false
            )!!
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = this.candidates!![position]
        }

        override fun getItemCount(): Int {
            return (candidates ?: return 0).size
        }

        @SuppressLint("NotifyDataSetChanged")
        fun update(candidates: List<String>) {
            this.candidates = candidates
            notifyDataSetChanged()
        }
    }
}
