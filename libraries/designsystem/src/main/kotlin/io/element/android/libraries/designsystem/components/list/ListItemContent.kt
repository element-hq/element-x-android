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

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.Checkbox as CheckboxComponent
import io.element.android.libraries.designsystem.theme.components.Icon as IconComponent
import io.element.android.libraries.designsystem.theme.components.RadioButton as RadioButtonComponent
import io.element.android.libraries.designsystem.theme.components.Switch as SwitchComponent
import io.element.android.libraries.designsystem.theme.components.Text as TextComponent

sealed interface ListItemContent {
    data class Switch(val checked: Boolean, val onChange: (Boolean) -> Unit, val enabled: Boolean = true) : ListItemContent
    data class Checkbox(val checked: Boolean, val onChange: (Boolean) -> Unit, val enabled: Boolean = true, val compact: Boolean = false) : ListItemContent
    data class RadioButton(val selected: Boolean, val onClick: () -> Unit, val enabled: Boolean = true, val compact: Boolean = false) : ListItemContent
    data class Icon(val iconSource: IconSource) : ListItemContent
    data class Text(val text: String) : ListItemContent
    data class Custom(val content: @Composable () -> Unit) : ListItemContent

    @Composable
    fun View() {
        when (this) {
            is Switch -> SwitchComponent(
                checked = checked,
                onCheckedChange = onChange,
                enabled = enabled
            )
            is Checkbox -> CheckboxComponent(
                modifier = if (compact) Modifier.height(maxCompactSize.height) else Modifier,
                checked = checked,
                onCheckedChange = onChange,
                enabled = enabled
            )
            is RadioButton -> RadioButtonComponent(
                modifier = if (compact) Modifier.height(maxCompactSize.height) else Modifier,
                selected = selected,
                onClick = onClick,
                enabled = enabled
            )
            is Icon -> IconComponent(
                modifier = Modifier.size(maxCompactSize),
                painter = iconSource.getPainter(),
                contentDescription = iconSource.contentDescription
            )
            is Text -> TextComponent(text = text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            is Custom -> content()
        }
    }
}

private val maxCompactSize = DpSize(24.dp, 24.dp)
