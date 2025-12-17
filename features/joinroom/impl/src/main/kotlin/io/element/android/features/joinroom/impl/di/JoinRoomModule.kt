/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.joinroom.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteState
import io.element.android.features.joinroom.impl.JoinRoomPresenter
import io.element.android.features.roomdirectory.api.RoomDescription
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.room.join.JoinRoom
import java.util.Optional

@BindingContainer
@ContributesTo(SessionScope::class)
object JoinRoomModule {
    @Provides
    fun providesJoinRoomPresenterFactory(
        client: MatrixClient,
        joinRoom: JoinRoom,
        knockRoom: KnockRoom,
        cancelKnockRoom: CancelKnockRoom,
        forgetRoom: ForgetRoom,
        acceptDeclineInvitePresenter: Presenter<AcceptDeclineInviteState>,
        buildMeta: BuildMeta,
        seenInvitesStore: SeenInvitesStore,
    ): JoinRoomPresenter.Factory {
        return object : JoinRoomPresenter.Factory {
            override fun create(
                roomId: RoomId,
                roomIdOrAlias: RoomIdOrAlias,
                roomDescription: Optional<RoomDescription>,
                serverNames: List<String>,
                trigger: JoinedRoom.Trigger,
            ): JoinRoomPresenter {
                return JoinRoomPresenter(
                    roomId = roomId,
                    roomIdOrAlias = roomIdOrAlias,
                    roomDescription = roomDescription,
                    serverNames = serverNames,
                    trigger = trigger,
                    matrixClient = client,
                    joinRoom = joinRoom,
                    knockRoom = knockRoom,
                    forgetRoom = forgetRoom,
                    cancelKnockRoom = cancelKnockRoom,
                    acceptDeclineInvitePresenter = acceptDeclineInvitePresenter,
                    buildMeta = buildMeta,
                    seenInvitesStore = seenInvitesStore,
                )
            }
        }
    }
}
