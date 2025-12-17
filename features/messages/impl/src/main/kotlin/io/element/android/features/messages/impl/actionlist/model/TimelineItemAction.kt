/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.actionlist.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.ui.strings.CommonStrings

enum class TimelineItemAction(
    @StringRes val titleRes: Int,
    @DrawableRes val icon: Int,
    val destructive: Boolean = false
) {
    ViewInTimeline(CommonStrings.action_view_in_timeline, CompoundDrawables.ic_compound_visibility_on),
    Forward(CommonStrings.action_forward, CompoundDrawables.ic_compound_forward),
    CopyText(CommonStrings.action_copy_text, CompoundDrawables.ic_compound_copy),
    CopyCaption(CommonStrings.action_copy_caption, CompoundDrawables.ic_compound_copy),
    CopyLink(CommonStrings.action_copy_link_to_message, CompoundDrawables.ic_compound_link),
    Redact(CommonStrings.action_remove, CompoundDrawables.ic_compound_delete, destructive = true),
    Reply(CommonStrings.action_reply, CompoundDrawables.ic_compound_reply),
    ReplyInThread(CommonStrings.action_reply_in_thread, CompoundDrawables.ic_compound_reply),
    Edit(CommonStrings.action_edit, CompoundDrawables.ic_compound_edit),
    EditPoll(CommonStrings.action_edit_poll, CompoundDrawables.ic_compound_edit),
    EditCaption(CommonStrings.action_edit_caption, CompoundDrawables.ic_compound_edit),
    AddCaption(CommonStrings.action_add_caption, CompoundDrawables.ic_compound_edit),
    RemoveCaption(CommonStrings.action_remove_caption, CompoundDrawables.ic_compound_close, destructive = true),
    ViewSource(CommonStrings.action_view_source, CompoundDrawables.ic_compound_code),
    ReportContent(CommonStrings.action_report_content, CompoundDrawables.ic_compound_chat_problem, destructive = true),
    EndPoll(CommonStrings.action_end_poll, CompoundDrawables.ic_compound_polls_end),
    Pin(CommonStrings.action_pin, CompoundDrawables.ic_compound_pin),
    Unpin(CommonStrings.action_unpin, CompoundDrawables.ic_compound_unpin),
}
