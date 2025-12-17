#!/usr/bin/env python3

# Copyright (c) 2025 Element Creations Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

files = [
    "ic_compound_arrow_left.xml",
    "ic_compound_arrow_right.xml",
    "ic_compound_arrow_up_right.xml",
    "ic_compound_attachment.xml",
    "ic_compound_backspace.xml",
    "ic_compound_backspace_solid.xml",
    "ic_compound_block.xml",
    "ic_compound_chart.xml",
    "ic_compound_chat.xml",
    "ic_compound_chat_new.xml",
    "ic_compound_chat_problem.xml",
    "ic_compound_chat_solid.xml",
    "ic_compound_chevron_left.xml",
    "ic_compound_chevron_right.xml",
    "ic_compound_cloud.xml",
    "ic_compound_cloud_solid.xml",
    "ic_compound_collapse.xml",
    "ic_compound_company.xml",
    "ic_compound_compose.xml",
    "ic_compound_copy.xml",
    "ic_compound_dark_mode.xml",
    "ic_compound_devices.xml",
    "ic_compound_document.xml",
    "ic_compound_earpiece.xml",
    "ic_compound_edit.xml",
    "ic_compound_edit_solid.xml",
    "ic_compound_expand.xml",
    "ic_compound_extensions.xml",
    "ic_compound_extensions_solid.xml",
    "ic_compound_file_error.xml",
    "ic_compound_files.xml",
    "ic_compound_forward.xml",
    "ic_compound_guest.xml",
    "ic_compound_history.xml",
    "ic_compound_host.xml",
    "ic_compound_image.xml",
    "ic_compound_image_error.xml",
    "ic_compound_indent_decrease.xml",
    "ic_compound_indent_increase.xml",
    "ic_compound_italic.xml",
    "ic_compound_key.xml",
    "ic_compound_key_off.xml",
    "ic_compound_key_off_solid.xml",
    "ic_compound_key_solid.xml",
    "ic_compound_leave.xml",
    "ic_compound_link.xml",
    "ic_compound_list_bulleted.xml",
    "ic_compound_lock_off.xml",
    "ic_compound_mark_as_unread.xml",
    "ic_compound_mark_threads_as_read.xml",
    "ic_compound_marker_read_receipts.xml",
    "ic_compound_mic_off.xml",
    "ic_compound_mic_off_solid.xml",
    "ic_compound_notifications_off.xml",
    "ic_compound_notifications_off_solid.xml",
    "ic_compound_offline.xml",
    "ic_compound_play.xml",
    "ic_compound_play_solid.xml",
    "ic_compound_polls.xml",
    "ic_compound_polls_end.xml",
    "ic_compound_pop_out.xml",
    "ic_compound_qr_code.xml",
    "ic_compound_quote.xml",
    "ic_compound_reaction_add.xml",
    "ic_compound_reply.xml",
    "ic_compound_restart.xml",
    "ic_compound_room.xml",
    "ic_compound_search.xml",
    "ic_compound_send.xml",
    "ic_compound_send_solid.xml",
    "ic_compound_share_android.xml",
    "ic_compound_sidebar.xml",
    "ic_compound_sign_out.xml",
    "ic_compound_spinner.xml",
    "ic_compound_spotlight.xml",
    "ic_compound_switch_camera_solid.xml",
    "ic_compound_threads.xml",
    "ic_compound_threads_solid.xml",
    "ic_compound_unknown.xml",
    "ic_compound_unknown_solid.xml",
    "ic_compound_unpin.xml",
    "ic_compound_user_add.xml",
    "ic_compound_user_add_solid.xml",
    "ic_compound_video_call.xml",
    "ic_compound_video_call_declined_solid.xml",
    "ic_compound_video_call_missed_solid.xml",
    "ic_compound_video_call_off.xml",
    "ic_compound_video_call_off_solid.xml",
    "ic_compound_video_call_solid.xml",
    "ic_compound_visibility_off.xml",
    "ic_compound_voice_call.xml",
    "ic_compound_voice_call_solid.xml",
    "ic_compound_volume_off.xml",
    "ic_compound_volume_off_solid.xml",
    "ic_compound_volume_on.xml",
    "ic_compound_volume_on_solid.xml",
]


def main():
    for file in files:
        # Open file for read
        with open("./libraries/compound/src/main/res/drawable/" + file, 'r') as f:
            data = f.read().split("\n")
        # Open file to write
        with open("./libraries/compound/src/main/res/drawable/" + file, 'w') as f:
            # Write new data
            # write the 3 first lines in data
            for i in range(3):
                f.write(data[i] + "\n")
            f.write("    android:autoMirrored=\"true\"\n")
            # write the rest of the data
            for i in range(3, len(data) - 1):
                f.write(data[i] + "\n")
    print("Added autoMirrored to " + str(len(files)) + " files.")


if __name__ == "__main__":
    main()
