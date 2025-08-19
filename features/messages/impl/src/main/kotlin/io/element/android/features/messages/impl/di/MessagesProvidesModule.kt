/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.features.messages.impl.timeline.di.LiveTimeline
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.timeline.Timeline

@ContributesTo(RoomScope::class)
@Module
object MessagesProvidesModule {
    @Provides
    @LiveTimeline
    fun provideLiveTimeline(joinedRoom: JoinedRoom): Timeline = joinedRoom.liveTimeline
}
