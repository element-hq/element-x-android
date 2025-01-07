/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.messagecomposer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.messages.api.MessageComposerContext
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import javax.inject.Inject

@SingleIn(RoomScope::class)
@ContributesBinding(RoomScope::class)
class DefaultMessageComposerContext @Inject constructor() : MessageComposerContext {
    override var composerMode: MessageComposerMode by mutableStateOf(MessageComposerMode.Normal)
        internal set
}
