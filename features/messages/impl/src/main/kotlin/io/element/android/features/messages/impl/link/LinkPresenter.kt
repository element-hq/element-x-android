/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.link

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.wysiwyg.link.Link

@Inject
class LinkPresenter(
    private val linkChecker: LinkChecker,
) : Presenter<LinkState> {
    @Composable
    override fun present(): LinkState {
        val linkClick: MutableState<AsyncAction<Link>> = remember { mutableStateOf(AsyncAction.Uninitialized) }

        fun handleEvent(event: LinkEvents) {
            when (event) {
                is LinkEvents.OnLinkClick -> {
                    linkClick.value = AsyncAction.Loading
                    val result = linkChecker.isSafe(event.link)
                    if (result) {
                        linkClick.value = AsyncAction.Success(event.link)
                    } else {
                        // Confirm first
                        linkClick.value = ConfirmingLinkClick(event.link)
                    }
                }
                LinkEvents.Confirm -> {
                    linkClick.value = (linkClick.value as? ConfirmingLinkClick)
                        ?.let { AsyncAction.Success(it.link) }
                        ?: AsyncAction.Uninitialized
                }
                LinkEvents.Cancel -> {
                    linkClick.value = AsyncAction.Uninitialized
                }
            }
        }
        return LinkState(
            linkClick = linkClick.value,
            eventSink = ::handleEvent,
        )
    }
}
