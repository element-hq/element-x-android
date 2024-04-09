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
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.ui.strings.CommonStrings

@Immutable
sealed class TimelineItemAction(
    @StringRes val titleRes: Int,
    @DrawableRes val icon: Int,
    val destructive: Boolean = false
) {
    data object Forward : TimelineItemAction(CommonStrings.action_forward, CompoundDrawables.ic_compound_forward)
    data object Copy : TimelineItemAction(CommonStrings.action_copy, CompoundDrawables.ic_compound_copy)
    data object CopyLink : TimelineItemAction(CommonStrings.action_copy_link_to_message, CompoundDrawables.ic_compound_link)
    data object Redact : TimelineItemAction(CommonStrings.action_remove, CompoundDrawables.ic_compound_delete, destructive = true)
    data object Reply : TimelineItemAction(CommonStrings.action_reply, CompoundDrawables.ic_compound_reply)
    data object ReplyInThread : TimelineItemAction(CommonStrings.action_reply_in_thread, CompoundDrawables.ic_compound_reply)
    data object Edit : TimelineItemAction(CommonStrings.action_edit, CompoundDrawables.ic_compound_edit)
    data object ViewSource : TimelineItemAction(CommonStrings.action_view_source, CommonDrawables.ic_developer_options)
    data object ReportContent : TimelineItemAction(CommonStrings.action_report_content, CompoundDrawables.ic_compound_chat_problem, destructive = true)
    data object EndPoll : TimelineItemAction(CommonStrings.action_end_poll, CompoundDrawables.ic_compound_polls_end)
}
