/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.noop

import androidx.compose.runtime.Composable
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import im.vector.app.features.analytics.plan.MobileScreen
import io.element.android.services.analytics.api.ScreenTracker

@ContributesBinding(AppScope::class)
@Inject
class NoopScreenTracker : ScreenTracker {
    @Composable
    override fun TrackScreen(screen: MobileScreen.ScreenName) = Unit
}
