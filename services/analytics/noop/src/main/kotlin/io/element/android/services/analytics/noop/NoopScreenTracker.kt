/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.noop

import androidx.compose.runtime.Composable
import com.squareup.anvil.annotations.ContributesBinding
import im.vector.app.features.analytics.plan.MobileScreen
import io.element.android.libraries.di.AppScope
import io.element.android.services.analytics.api.ScreenTracker
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class NoopScreenTracker @Inject constructor() : ScreenTracker {
    @Composable
    override fun TrackScreen(screen: MobileScreen.ScreenName) = Unit
}
