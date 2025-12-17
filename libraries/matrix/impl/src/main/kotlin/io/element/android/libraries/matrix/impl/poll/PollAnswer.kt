/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.poll

import io.element.android.libraries.matrix.api.poll.PollAnswer
import org.matrix.rustcomponents.sdk.PollAnswer as RustPollAnswer

fun RustPollAnswer.map(): PollAnswer = PollAnswer(
    id = id,
    text = text,
)
