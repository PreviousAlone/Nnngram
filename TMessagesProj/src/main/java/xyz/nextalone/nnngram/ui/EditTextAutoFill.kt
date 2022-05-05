package xyz.nextalone.nnngram.ui

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import org.telegram.ui.Components.EditTextBoldCursor

class EditTextAutoFill(context: Context?) : EditTextBoldCursor(context) {
    init {
        if (Build.VERSION.SDK_INT >= 26) {
            importantForAutofill = IMPORTANT_FOR_AUTOFILL_YES
            setAutofillHints(AUTOFILL_HINT_PASSWORD)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    override fun getAutofillType(): Int {
        return AUTOFILL_TYPE_TEXT
    }
}
