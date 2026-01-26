/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import io.element.android.features.messages.impl.crypto.identity.IdentityChangeState
import io.element.android.features.messages.impl.crypto.identity.IdentityChangeStatePresenter
import io.element.android.features.messages.impl.crypto.sendfailure.resolve.ResolveVerifiedUserSendFailurePresenter
import io.element.android.features.messages.impl.crypto.sendfailure.resolve.ResolveVerifiedUserSendFailureState
import io.element.android.features.messages.impl.link.LinkPresenter
import io.element.android.features.messages.impl.link.LinkState
import io.element.android.features.messages.impl.pinned.banner.PinnedMessagesBannerPresenter
import io.element.android.features.messages.impl.pinned.banner.PinnedMessagesBannerState
import io.element.android.features.messages.impl.timeline.components.customreaction.CustomReactionPresenter
import io.element.android.features.messages.impl.timeline.components.customreaction.CustomReactionState
import io.element.android.features.messages.impl.timeline.components.reactionsummary.ReactionSummaryPresenter
import io.element.android.features.messages.impl.timeline.components.reactionsummary.ReactionSummaryState
import io.element.android.features.messages.impl.timeline.components.receipt.bottomsheet.ReadReceiptBottomSheetPresenter
import io.element.android.features.messages.impl.timeline.components.receipt.bottomsheet.ReadReceiptBottomSheetState
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionPresenter
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionState
import io.element.android.features.messages.impl.typing.TypingNotificationPresenter
import io.element.android.features.messages.impl.typing.TypingNotificationState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.RoomScope

@ContributesTo(RoomScope::class)
@BindingContainer
interface MessagesBindsModule {
    @Binds
    fun bindPinnedMessagesBannerPresenter(presenter: PinnedMessagesBannerPresenter): Presenter<PinnedMessagesBannerState>

    @Binds
    fun bindResolveVerifiedUserSendFailurePresenter(presenter: ResolveVerifiedUserSendFailurePresenter): Presenter<ResolveVerifiedUserSendFailureState>

    @Binds
    fun bindTypingNotificationPresenter(presenter: TypingNotificationPresenter): Presenter<TypingNotificationState>

    @Binds
    fun bindTimelineProtectionPresenter(presenter: TimelineProtectionPresenter): Presenter<TimelineProtectionState>

    @Binds
    fun bindLinkPresenter(presenter: LinkPresenter): Presenter<LinkState>

    @Binds
    fun bindCustomReactionPresenter(presenter: CustomReactionPresenter): Presenter<CustomReactionState>

    @Binds
    fun bindReactionSummaryPresenter(presenter: ReactionSummaryPresenter): Presenter<ReactionSummaryState>

    @Binds
    fun bindReadReceiptBottomSheetPresenter(presenter: ReadReceiptBottomSheetPresenter): Presenter<ReadReceiptBottomSheetState>

    @Binds
    fun bindIdentityChangeStatePresenter(presenter: IdentityChangeStatePresenter): Presenter<IdentityChangeState>
}
