package pers.zhc.android.rime

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pers.zhc.android.rime.databinding.DeployingDialogBinding
import pers.zhc.android.rime.databinding.ImeSettingsActivityBinding
import pers.zhc.android.rime.rime.Engine
import kotlin.concurrent.thread

class ImeSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bindings = ImeSettingsActivityBinding.inflate(layoutInflater)
        setContentView(bindings.root)

        bindings.deployButton.setOnClickListener {
            val userDataDir = bindings.userDataDirEt.text.toString()
            val sharedDataDir = bindings.sharedDataDirEt.text.toString().ifEmpty { null }
            val dialog = Dialog(this).apply {
                val view = DeployingDialogBinding.inflate(layoutInflater).root
                setContentView(view)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
            }.also { it.show() }
            thread {
                val result = Engine.deploy(userDataDir, sharedDataDir)
                runOnUiThread {
                    dialog.dismiss()
                    val resultStringRes = when (result) {
                        Engine.Companion.DeployStatus.SUCCESS -> R.string.deploy_success_dialog
                        Engine.Companion.DeployStatus.FAILURE -> R.string.deploy_failure_dialog
                    }
                    MaterialAlertDialogBuilder(this)
                        .setTitle(resultStringRes)
                        .setPositiveButton(R.string.done_button, null)
                        .show()
                }
            }
        }
    }
}
