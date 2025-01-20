/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.tests.testutils.lambda.lambdaError

class FakeMediaGalleryNavigator(
    private val onViewInTimelineClickLambda: (EventId) -> Unit = { lambdaError() }
) : MediaGalleryNavigator {
    override fun onViewInTimelineClick(eventId: EventId) {
        onViewInTimelineClickLambda(eventId)
    }
}
