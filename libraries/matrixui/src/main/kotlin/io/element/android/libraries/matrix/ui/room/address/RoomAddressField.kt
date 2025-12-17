/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.room.address

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.designsystem.theme.components.TextFieldValidity
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun RoomAddressField(
    address: String,
    homeserverName: String,
    addressValidity: RoomAddressValidity,
    onAddressChange: (String) -> Unit,
    label: String,
    supportingText: String,
    modifier: Modifier = Modifier,
) {
    TextField(
        modifier = modifier.testTag(TestTags.roomAddressField),
        value = address,
        label = label,
        leadingIcon = {
            Text(
                text = "#",
                style = ElementTheme.typography.fontBodyLgMedium,
                color = ElementTheme.colors.textSecondary,
            )
        },
        trailingIcon = {
            Text(
                text = homeserverName,
                style = ElementTheme.typography.fontBodyLgMedium,
                color = ElementTheme.colors.textSecondary,
            )
        },
        supportingText = when (addressValidity) {
            RoomAddressValidity.InvalidSymbols -> {
                stringResource(CommonStrings.error_room_address_invalid_symbols)
            }
            RoomAddressValidity.NotAvailable -> {
                stringResource(CommonStrings.error_room_address_already_exists)
            }
            else -> supportingText
        },
        validity = when (addressValidity) {
            RoomAddressValidity.InvalidSymbols, RoomAddressValidity.NotAvailable -> TextFieldValidity.Invalid
            else -> TextFieldValidity.None
        },
        onValueChange = onAddressChange,
        singleLine = true,
    )
}

@PreviewsDayNight
@Composable
internal fun RoomAddressFieldPreview() = ElementPreview {
    RoomAddressField(
        address = "room",
        homeserverName = "element.io",
        addressValidity = RoomAddressValidity.Valid,
        onAddressChange = {},
        label = "Room address",
        supportingText = "This is the address that people will use to join your room",
    )
}
