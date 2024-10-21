/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
    data object ViewInTimeline : TimelineItemAction(CommonStrings.action_view_in_timeline, CompoundDrawables.ic_compound_visibility_on)
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
    data object Pin : TimelineItemAction(CommonStrings.action_pin, CompoundDrawables.ic_compound_pin)
    data object Unpin : TimelineItemAction(CommonStrings.action_unpin, CompoundDrawables.ic_compound_unpin)
}
