package io.element.android.x.textcomposer.tools

import android.text.Spanned
import android.widget.EditText

fun EditText.setTextIfDifferent(newText: CharSequence?): Boolean {
    if (!isTextDifferent(newText, text)) {
        // Previous text is the same. No op
        return false
    }
    setText(newText)
    // Since the text changed we move the cursor to the end of the new text.
    // This allows us to fill in text programmatically with a different value,
    // but if the user is typing and the view is rebound we won't lose their cursor position.
    setSelection(newText?.length ?: 0)
    return true
}

private fun isTextDifferent(str1: CharSequence?, str2: CharSequence?): Boolean {
    if (str1 === str2) {
        return false
    }
    if (str1 == null || str2 == null) {
        return true
    }
    val length = str1.length
    if (length != str2.length) {
        return true
    }
    if (str1 is Spanned) {
        return str1 != str2
    }
    for (i in 0 until length) {
        if (str1[i] != str2[i]) {
            return true
        }
    }
    return false
}
