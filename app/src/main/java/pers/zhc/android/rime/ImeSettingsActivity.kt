package pers.zhc.android.rime

import android.app.Dialog
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import pers.zhc.android.rime.MyApplication.Companion.GSON
import pers.zhc.android.rime.databinding.DeployingDialogBinding
import pers.zhc.android.rime.databinding.ImeSettingsActivityBinding
import pers.zhc.android.rime.rime.Rime
import pers.zhc.android.rime.util.fromJsonOrNull
import java.io.File
import kotlin.concurrent.thread

class ImeSettingsActivity : AppCompatActivity() {
    private lateinit var sharedDataDirEt: TextInputEditText
    private lateinit var userDataDirEt: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bindings = ImeSettingsActivityBinding.inflate(layoutInflater)
        setContentView(bindings.root)

        userDataDirEt = bindings.userDataDirEt
        sharedDataDirEt = bindings.sharedDataDirEt
        GSON.fromJsonOrNull(CONFIGS_FILE.readText(), RimeConfigs::class.java)?.let { configs ->
            userDataDirEt.setText(configs.userDataDir)
            sharedDataDirEt.setText(configs.sharedDataDir)
        }

        bindings.deployButton.setOnClickListener {
            val userDataDir = userDataDirEt.text.toString()
            val sharedDataDir = sharedDataDirEt.text.toString()
            val dialog = Dialog(this).apply {
                val view = DeployingDialogBinding.inflate(layoutInflater).root
                setContentView(view)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
            }.also { it.show() }
            thread {
                Rime.reinitialize(userDataDir, sharedDataDir)
                val result = Rime.fullDeployAndWait()
                runOnUiThread {
                    dialog.dismiss()
                    val resultStringRes = when (result) {
                        Rime.DeployStatus.SUCCESS -> R.string.deploy_success_dialog
                        Rime.DeployStatus.FAILURE -> R.string.deploy_failure_dialog
                    }
                    MaterialAlertDialogBuilder(this)
                        .setTitle(resultStringRes)
                        .setPositiveButton(R.string.done_button, null)
                        .show()
                }
            }
        }

        onBackPressedDispatcher.addCallback {
            handleBackPressed()
        }
    }

    private fun handleBackPressed() {
        val configs = RimeConfigs(userDataDirEt.text.toString(), sharedDataDirEt.text.toString())
        val json = GSON.toJson(configs)
        CONFIGS_FILE.writeText(json)
        finish()
    }

    companion object {
        val CONFIGS_FILE by lazy {
            File(MyApplication.CONTEXT.filesDir, "configs.json").also {
                if (!it.exists()) {
                    it.createNewFile()
                }
            }
        }
    }
}
