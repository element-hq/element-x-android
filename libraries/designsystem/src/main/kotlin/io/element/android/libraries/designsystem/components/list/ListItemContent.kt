/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.atomic.atoms.CounterAtom
import io.element.android.libraries.designsystem.atomic.atoms.RedIndicatorAtom
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Checkbox as CheckboxComponent
import io.element.android.libraries.designsystem.theme.components.Icon as IconComponent
import io.element.android.libraries.designsystem.theme.components.RadioButton as RadioButtonComponent
import io.element.android.libraries.designsystem.theme.components.Switch as SwitchComponent
import io.element.android.libraries.designsystem.theme.components.Text as TextComponent

/**
 * This is a helper to set default leading and trailing content for [ListItem]s.
 */
@Immutable
sealed interface ListItemContent {
    /**
     * Default Switch content for [ListItem].
     * @param checked The current state of the switch.
     * @param enabled Whether the switch is enabled or not.
     */
    data class Switch(
        val checked: Boolean,
        val enabled: Boolean = true
    ) : ListItemContent

    /**
     * Default Checkbox content for [ListItem].
     * @param checked The current state of the checkbox.
     * @param enabled Whether the checkbox is enabled or not.
     * @param compact Reduces the size of the component to make the wrapping [ListItem] smaller.
     * This is especially useful when the [ListItem] is used inside a Dialog. `false` by default.
     */
    data class Checkbox(
        val checked: Boolean,
        val enabled: Boolean = true,
        val compact: Boolean = false
    ) : ListItemContent

    /**
     * Default RadioButton content for [ListItem].
     * @param selected The current state of the radio button.
     * @param enabled Whether the radio button is enabled or not.
     * @param compact Reduces the size of the component to make the wrapping [ListItem] smaller.
     * This is especially useful when the [ListItem] is used inside a Dialog. `false` by default.
     */
    data class RadioButton(
        val selected: Boolean,
        val enabled: Boolean = true,
        val compact: Boolean = false
    ) : ListItemContent

    /**
     * Default Icon content for [ListItem]. Sets the Icon component to a predefined size.
     * @param iconSource The icon to display, using [IconSource.getPainter].
     * @param tintColor The tint color for the icon, if any. Defaults to `null`.
     */
    data class Icon(val iconSource: IconSource, val tintColor: Color? = null) : ListItemContent

    /**
     * Default Text content for [ListItem]. Sets the Text component to a max size and clips overflow.
     * @param text The text to display.
     */
    data class Text(val text: String) : ListItemContent

    /** Displays any custom content. */
    data class Custom(val content: @Composable () -> Unit) : ListItemContent

    /** Displays a badge. */
    data object Badge : ListItemContent

    /** Displays a counter. */
    data class Counter(val count: Int) : ListItemContent

    @Composable
    fun View(isItemEnabled: Boolean) {
        when (this) {
            is Switch -> SwitchComponent(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled && isItemEnabled,
            )
            is Checkbox -> CheckboxComponent(
                modifier = if (compact) Modifier.size(maxCompactSize) else Modifier,
                checked = checked,
                onCheckedChange = null,
                enabled = enabled && isItemEnabled,
            )
            is RadioButton -> RadioButtonComponent(
                modifier = if (compact) Modifier.size(maxCompactSize) else Modifier,
                selected = selected,
                onClick = null,
                enabled = enabled && isItemEnabled,
            )
            is Icon -> {
                IconComponent(
                    modifier = Modifier.size(maxCompactSize),
                    painter = iconSource.getPainter(),
                    contentDescription = iconSource.contentDescription,
                    tint = tintColor ?: LocalContentColor.current,
                )
            }
            is Text -> TextComponent(modifier = Modifier.widthIn(max = 128.dp), text = text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            is Badge -> Box(
                modifier = Modifier.size(maxCompactSize),
                contentAlignment = Alignment.Center,
            ) {
                RedIndicatorAtom()
            }
            is Counter -> {
                CounterAtom(count = count)
            }
            is Custom -> content()
        }
    }
}

private val maxCompactSize = DpSize(24.dp, 24.dp)
