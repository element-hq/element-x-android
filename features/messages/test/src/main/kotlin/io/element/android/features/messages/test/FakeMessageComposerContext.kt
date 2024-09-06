/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.test

import io.element.android.features.messages.api.MessageComposerContext
import io.element.android.libraries.textcomposer.model.MessageComposerMode

class FakeMessageComposerContext(
    override var composerMode: MessageComposerMode = MessageComposerMode.Normal
) : MessageComposerContext
