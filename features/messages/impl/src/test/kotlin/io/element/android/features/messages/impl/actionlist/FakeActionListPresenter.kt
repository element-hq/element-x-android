/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.actionlist

import androidx.compose.runtime.Composable
import io.element.android.features.messages.impl.actionlist.model.TimelineItemActionPostProcessor

class FakeActionListPresenter(private val eventSink: (ActionListEvents) -> Unit = {}) : ActionListPresenter {
    class Factory(private val eventSink: (ActionListEvents) -> Unit = {}) : ActionListPresenter.Factory {
        override fun create(postProcessor: TimelineItemActionPostProcessor): ActionListPresenter {
            return FakeActionListPresenter(eventSink)
        }
    }

    @Composable
    override fun present(): ActionListState {
        return anActionListState(eventSink = eventSink)
    }
}
