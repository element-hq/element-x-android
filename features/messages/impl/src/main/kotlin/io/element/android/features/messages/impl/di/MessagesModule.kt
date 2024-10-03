/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import io.element.android.features.messages.impl.crypto.sendfailure.resolve.ResolveVerifiedUserSendFailurePresenter
import io.element.android.features.messages.impl.crypto.sendfailure.resolve.ResolveVerifiedUserSendFailureState
import io.element.android.features.messages.impl.pinned.banner.PinnedMessagesBannerPresenter
import io.element.android.features.messages.impl.pinned.banner.PinnedMessagesBannerState
import io.element.android.features.messages.impl.typing.TypingNotificationPresenter
import io.element.android.features.messages.impl.typing.TypingNotificationState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.RoomScope

@ContributesTo(RoomScope::class)
@Module
interface MessagesModule {
    @Binds
    fun bindPinnedMessagesBannerPresenter(presenter: PinnedMessagesBannerPresenter): Presenter<PinnedMessagesBannerState>

    @Binds
    fun bindResolveVerifiedUserSendFailurePresenter(presenter: ResolveVerifiedUserSendFailurePresenter): Presenter<ResolveVerifiedUserSendFailureState>

    @Binds
    fun bindTypingNotificationPresenter(presenter: TypingNotificationPresenter): Presenter<TypingNotificationState>
}
