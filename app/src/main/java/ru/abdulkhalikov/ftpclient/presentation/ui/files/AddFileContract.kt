package ru.abdulkhalikov.ftpclient.presentation.ui.files

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract

class AddFileContract : ActivityResultContract<Unit, Uri?>() {

    override fun createIntent(
        context: Context,
        input: Unit
    ): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
        }
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): Uri? {
        return if (resultCode == RESULT_OK) {
            intent?.data
        } else {
            null
        }
    }
}