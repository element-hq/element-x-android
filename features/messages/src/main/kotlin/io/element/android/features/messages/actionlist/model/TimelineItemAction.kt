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

package io.element.android.features.messages.actionlist.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import io.element.android.libraries.designsystem.VectorIcons

@Immutable
sealed class TimelineItemAction(
    val title: String,
    @DrawableRes val icon: Int,
    val destructive: Boolean = false
) {
    object Forward : TimelineItemAction("Forward", VectorIcons.ArrowForward)
    object Copy : TimelineItemAction("Copy", VectorIcons.Copy)
    object Redact : TimelineItemAction("Redact", VectorIcons.Delete, destructive = true)
    object Reply : TimelineItemAction("Reply", VectorIcons.Reply)
    object Edit : TimelineItemAction("Edit", VectorIcons.Edit)
}
