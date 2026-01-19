package ru.abdulkhalikov.ftpclient.presentation.ui.files

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

object AddFileContract : ActivityResultContract<Unit, Uri?>() {

    override fun createIntent(
        context: Context,
        input: Unit
    ): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
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