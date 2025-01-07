/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.viewer

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.tests.testutils.lambda.lambdaError

class FakeMediaViewerNavigator(
    private val onViewInTimelineClickLambda: (EventId) -> Unit = { lambdaError() },
    private val onItemDeletedLambda: () -> Unit = { lambdaError() },
) : MediaViewerNavigator {
    override fun onViewInTimelineClick(eventId: EventId) {
        onViewInTimelineClickLambda(eventId)
    }

    override fun onItemDeleted() {
        onItemDeletedLambda()
    }
}
