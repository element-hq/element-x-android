/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.forward

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId

data class ForwardMessagesState(
    val forwardAction: AsyncAction<List<RoomId>>,
    val eventSink: (ForwardMessagesEvents) -> Unit
)
