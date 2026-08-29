/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.eventformatter.api

import io.element.android.libraries.matrix.api.roomlist.LatestEventValue

/**
 * Formats the one-line preview of a room's latest event, as shown in the room list.
 */
interface RoomLatestEventFormatter {
    /**
     * Formats an event that is still local, i.e. queued or being sent.
     *
     * @param latestEvent the local event to describe.
     * @param isDmRoom whether the room is a direct message, which lets the sender name be left out.
     * @return the preview text, or `null` when this kind of event should not be previewed.
     */
    fun format(latestEvent: LatestEventValue.Local, isDmRoom: Boolean): CharSequence?

    /**
     * Formats an event that has been received from the server.
     *
     * @param latestEvent the remote event to describe.
     * @param isDmRoom whether the room is a direct message, which lets the sender name be left out.
     * @return the preview text, or `null` when this kind of event should not be previewed.
     */
    fun format(latestEvent: LatestEventValue.Remote, isDmRoom: Boolean): CharSequence?
}
