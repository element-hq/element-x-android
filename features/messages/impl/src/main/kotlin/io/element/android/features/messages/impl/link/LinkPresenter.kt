/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.link

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.wysiwyg.link.Link
import javax.inject.Inject

class LinkPresenter @Inject constructor(
    private val linkChecker: LinkChecker,
) : Presenter<LinkState> {
    @Composable
    override fun present(): LinkState {
        val linkClick: MutableState<AsyncAction<Link>> = remember { mutableStateOf(AsyncAction.Uninitialized) }

        fun handleEvents(linkEvents: LinkEvents) {
            when (linkEvents) {
                is LinkEvents.OnLinkClick -> {
                    linkClick.value = AsyncAction.Loading
                    val result = linkChecker.isSafe(linkEvents.link)
                    if (result) {
                        linkClick.value = AsyncAction.Success(linkEvents.link)
                    } else {
                        // Confirm first
                        linkClick.value = ConfirmingLinkClick(linkEvents.link)
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
            eventSink = ::handleEvents,
        )
    }
}
