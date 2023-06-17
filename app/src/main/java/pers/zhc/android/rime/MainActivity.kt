package pers.zhc.android.rime

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import pers.zhc.android.rime.databinding.ActivityMainBinding
import pers.zhc.android.rime.rime.JNI
import pers.zhc.android.rime.util.ToastUtils
import pers.zhc.tools.utils.RecyclerViewUtils
import pers.zhc.tools.utils.addDividerLines
import pers.zhc.tools.utils.setLinearLayoutManager

class MainActivity : AppCompatActivity() {
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (!it) {
            ToastUtils.show(this, R.string.grant_permission_request_toast)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bindings = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindings.root)

        ToastUtils.show(this, JNI.getRimeVersion())

        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)

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

                3 -> {
                    // input test
                    startActivity(Intent(this, InputTestActivity::class.java))
                }

                4 -> {
                    // dictionary test
                    startActivity(Intent(this, KeyTestActivity::class.java))
                }
            }
        }
    }
}
