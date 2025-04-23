/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.data

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.room.JoinedRoom

@Module
@ContributesTo(RoomScope::class)
object KnockRequestsModule {
    @Provides
    @SingleIn(RoomScope::class)
    fun knockRequestsService(room: JoinedRoom, featureFlagService: FeatureFlagService): KnockRequestsService {
        return KnockRequestsService(
            knockRequestsFlow = room.knockRequestsFlow,
            permissionsFlow = room.knockRequestPermissionsFlow(),
            isKnockFeatureEnabledFlow = featureFlagService.isFeatureEnabledFlow(FeatureFlags.Knock),
            coroutineScope = room.roomCoroutineScope
        )
    }
}
