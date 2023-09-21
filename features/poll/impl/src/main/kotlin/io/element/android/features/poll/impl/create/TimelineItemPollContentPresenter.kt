/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.poll.impl.create

import androidx.compose.runtime.Composable
import com.squareup.anvil.annotations.ContributesMultibinding
import io.element.android.features.messages.api.TimelineItemPresenter
import io.element.android.features.messages.api.TimelineItemPresenterKey
import io.element.android.features.poll.api.TimelineItemPollContentState
import io.element.android.libraries.di.RoomScope

@ContributesMultibinding(RoomScope::class)
@TimelineItemPresenterKey("TimelineItemPollContent")
class TimelineItemPollContentPresenter : TimelineItemPresenter {
    @Composable
    override fun present(): TimelineItemPollContentState {
        TODO("Not yet implemented")
    }
}
