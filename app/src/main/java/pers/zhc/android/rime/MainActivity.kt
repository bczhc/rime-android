package pers.zhc.android.rime

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import pers.zhc.android.rime.databinding.ActivityMainBinding
import pers.zhc.tools.utils.RecyclerViewUtils
import pers.zhc.tools.utils.addDividerLines
import pers.zhc.tools.utils.setLinearLayoutManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bindings = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindings.root)

        val recyclerView = bindings.recyclerView

        val menuTextList = resources.getStringArray(R.array.ime_main).toList()
        val adapter = RecyclerViewUtils.buildSimpleItem1ListAdapter(this, menuTextList)
        recyclerView.adapter = adapter

        recyclerView.setLinearLayoutManager()
        recyclerView.addDividerLines()

        adapter.setOnItemClickListener { position, _ ->
            when (position) {
                0 -> {
                    // enable IME
                    startActivity(Intent("android.settings.INPUT_METHOD_SETTINGS"))
                }

                1 -> {
                    // select IME
                    (applicationContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
                }

                2 -> {
                    // rime settings
                    startActivity(Intent(this, ImeSettingsActivity::class.java))
                }
            }
        }
    }
}
