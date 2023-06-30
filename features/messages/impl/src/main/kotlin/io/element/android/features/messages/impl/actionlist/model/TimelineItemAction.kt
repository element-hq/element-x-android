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

package io.element.android.features.messages.impl.actionlist.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import io.element.android.libraries.designsystem.VectorIcons
import io.element.android.libraries.ui.strings.CommonStrings

@Immutable
sealed class TimelineItemAction(
    @StringRes val titleRes: Int,
    @DrawableRes val icon: Int,
    val destructive: Boolean = false
) {
    object Forward : TimelineItemAction(CommonStrings.action_forward, VectorIcons.Forward)
    object Copy : TimelineItemAction(CommonStrings.action_copy, VectorIcons.Copy)
    object Redact : TimelineItemAction(CommonStrings.action_remove, VectorIcons.Delete, destructive = true)
    object Reply : TimelineItemAction(CommonStrings.action_reply, VectorIcons.Reply)
    object Edit : TimelineItemAction(CommonStrings.action_edit, VectorIcons.Edit)
    object Developer : TimelineItemAction(CommonStrings.action_view_source, VectorIcons.DeveloperMode)
    object ReportContent : TimelineItemAction(CommonStrings.action_report_content, VectorIcons.ReportContent, destructive = true)
}
