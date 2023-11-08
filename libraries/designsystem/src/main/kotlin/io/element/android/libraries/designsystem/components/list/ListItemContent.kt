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

package io.element.android.libraries.designsystem.components.list

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
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
     * @param onChange Callback when the switch is toggled: it should only be set to override the default click behaviour in the [ListItem].
     * @param enabled Whether the switch is enabled or not.
     */
    data class Switch(
        val checked: Boolean,
        val onChange: ((Boolean) -> Unit)? = null,
        val enabled: Boolean = true
    ) : ListItemContent

    /**
     * Default Checkbox content for [ListItem].
     * @param checked The current state of the checkbox.
     * @param onChange Callback when the checkbox is toggled: it should only be set to override the default click behaviour in the [ListItem].
     * @param enabled Whether the checkbox is enabled or not.
     * @param compact Reduces the size of the component to make the wrapping [ListItem] smaller.
     * This is especially useful when the [ListItem] is used inside a Dialog. `false` by default.
     */
    data class Checkbox(
        val checked: Boolean,
        val onChange: ((Boolean) -> Unit)? = null,
        val enabled: Boolean = true,
        val compact: Boolean = false
    ) : ListItemContent

    /**
     * Default RadioButton content for [ListItem].
     * @param selected The current state of the radio button.
     * @param onClick Callback when the radio button is toggled: it should only be set to override the default click behaviour in the [ListItem].
     * @param enabled Whether the radio button is enabled or not.
     * @param compact Reduces the size of the component to make the wrapping [ListItem] smaller.
     * This is especially useful when the [ListItem] is used inside a Dialog. `false` by default.
     */
    data class RadioButton(
        val selected: Boolean,
        val onClick: (() -> Unit)? = null,
        val enabled: Boolean = true,
        val compact: Boolean = false
    ) : ListItemContent

    /**
     * Default Icon content for [ListItem]. Sets the Icon component to a predefined size.
     * @param iconSource The icon to display, using [IconSource.getPainter].
     */
    data class Icon(val iconSource: IconSource) : ListItemContent

    /**
     * Default Text content for [ListItem]. Sets the Text component to a max size and clips overflow.
     * @param text The text to display.
     */
    data class Text(val text: String) : ListItemContent

    /** Displays any custom content. */
    data class Custom(val content: @Composable () -> Unit) : ListItemContent

    /** Displays a badge. */
    data object Badge : ListItemContent

    @Composable
    fun View() {
        when (this) {
            is Switch -> SwitchComponent(
                checked = checked,
                onCheckedChange = onChange,
                enabled = enabled
            )
            is Checkbox -> CheckboxComponent(
                modifier = if (compact) Modifier.size(maxCompactSize) else Modifier,
                checked = checked,
                onCheckedChange = onChange,
                enabled = enabled
            )
            is RadioButton -> RadioButtonComponent(
                modifier = if (compact) Modifier.size(maxCompactSize) else Modifier,
                selected = selected,
                onClick = onClick,
                enabled = enabled
            )
            is Icon -> {
                IconComponent(
                    modifier = Modifier.size(maxCompactSize),
                    painter = iconSource.getPainter(),
                    contentDescription = iconSource.contentDescription
                )
            }
            is Text -> TextComponent(modifier = Modifier.widthIn(max = 128.dp), text = text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            is Badge -> RedIndicatorAtom()
            is Custom -> content()
        }
    }
}

private val maxCompactSize = DpSize(24.dp, 24.dp)
