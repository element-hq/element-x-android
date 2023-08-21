/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.textcomposer.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.element.android.libraries.textcomposer.InnerTextComposerState
import io.element.android.libraries.textcomposer.TextComposerState

@Composable
fun rememberFakeTextComposerState(): TextComposerState {
    val inner = remember { FakeInnerTextComposerState() }

    return remember(
        inner.messageHtml,
        inner.canSendMessage,
        inner.hasFocus,
        inner.lineCount
    ) { TextComposerState(inner) }
}

internal class FakeInnerTextComposerState : InnerTextComposerState {
    override var messageHtml: String by mutableStateOf("")
    override var messageMarkdown: String by mutableStateOf("")
        private set

    override var hasFocus: Boolean by mutableStateOf(false)
        private set

    override val lineCount: Int
        by derivedStateOf { messageHtml.count { it == '\n' } + 1 }

    override val canSendMessage: Boolean
        get() = messageHtml.isNotEmpty()

    override fun setHtml(value: String) {
        messageHtml = value
        messageMarkdown = value
    }

    override fun requestFocus(): Boolean {
        hasFocus = true
        return true
    }
}

