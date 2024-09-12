/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl.file

sealed interface ViewFileEvents {
    data object SaveOnDisk : ViewFileEvents
    data object Share : ViewFileEvents
}
