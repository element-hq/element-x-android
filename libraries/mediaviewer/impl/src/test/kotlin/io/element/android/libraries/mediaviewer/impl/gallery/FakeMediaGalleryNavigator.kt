/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.tests.testutils.lambda.lambdaError

class FakeMediaGalleryNavigator(
    private val onViewInTimelineClickLambda: (EventId) -> Unit = { lambdaError() },
    private val onForwardClickLambda: (EventId) -> Unit = { lambdaError() },
) : MediaGalleryNavigator {
    override fun onViewInTimelineClick(eventId: EventId) {
        onViewInTimelineClickLambda(eventId)
    }

    override fun onForwardClick(eventId: EventId) {
        onForwardClickLambda(eventId)
    }
}
