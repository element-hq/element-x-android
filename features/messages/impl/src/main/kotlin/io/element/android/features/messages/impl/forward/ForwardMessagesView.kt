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

package io.element.android.features.messages.impl.forward

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialogDefaults
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.collections.immutable.ImmutableList

@Composable
fun ForwardMessagesView(
    state: ForwardMessagesState,
    onForwardingSucceeded: (ImmutableList<RoomId>) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.forwardingSucceeded != null) {
        onForwardingSucceeded(state.forwardingSucceeded)
        return
    }

    if (state.isForwarding) {
        ProgressDialog(modifier)
    }

    if (state.error != null) {
        ForwardingErrorDialog(
            modifier = modifier,
            onDismiss = { state.eventSink(ForwardMessagesEvents.ClearError) },
        )
    }
}

@Composable
private fun ForwardingErrorDialog(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    ErrorDialog(
        content = ErrorDialogDefaults.title,
        onDismiss = onDismiss,
        modifier = modifier,
    )
}

@PreviewsDayNight
@Composable
internal fun ForwardMessagesViewPreview(@PreviewParameter(ForwardMessagesStateProvider::class) state: ForwardMessagesState) = ElementPreview {
    ForwardMessagesView(
        state = state,
        onForwardingSucceeded = {}
    )
}
