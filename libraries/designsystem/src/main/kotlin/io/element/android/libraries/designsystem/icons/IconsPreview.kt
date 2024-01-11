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

package io.element.android.libraries.designsystem.icons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

internal class CompoundIconListPreviewProvider : PreviewParameterProvider<IconChunk> {
    override val values: Sequence<IconChunk>
        get() {
            val chunks = CompoundIcons.allResIds.chunked(36)
            return chunks.mapIndexed { index, chunk ->
                IconChunk(index = index + 1, total = chunks.size, icons = chunk.toPersistentList())
            }
                .asSequence()
        }
}

internal class OtherIconListPreviewProvider : PreviewParameterProvider<IconChunk> {
    override val values: Sequence<IconChunk>
        get() {
            val chunks = iconsOther.chunked(36)
            return chunks.mapIndexed { index, chunk ->
                IconChunk(index = index + 1, total = chunks.size, icons = chunk.toPersistentList())
            }
                .asSequence()
        }
}

internal data class IconChunk(
    val index: Int,
    val total: Int,
    val icons: ImmutableList<Int>,
)

@PreviewsDayNight
@Composable
internal fun IconsCompoundPreview(@PreviewParameter(CompoundIconListPreviewProvider::class) chunk: IconChunk) = ElementPreview {
    IconsPreview(
        title = "R.drawable.ic_compound_* ${chunk.index}/${chunk.total}",
        iconsList = chunk.icons,
        iconNameTransform = { name ->
            name.removePrefix("ic_compound_")
                .replace("_", " ")
        }
    )
}

@PreviewsDayNight
@Composable
internal fun IconsOtherPreview(@PreviewParameter(OtherIconListPreviewProvider::class) iconChunk: IconChunk) = ElementPreview {
    IconsPreview(
        title = "R.drawable.ic_*  ${iconChunk.index}/${iconChunk.total}",
        iconsList = iconChunk.icons,
        iconNameTransform = { name ->
            name.removePrefix("ic_")
                .replace("_", " ")
        }
    )
}

@Composable
private fun IconsPreview(
    title: String,
    iconsList: ImmutableList<Int>,
    iconNameTransform: (String) -> String,
    modifier: Modifier = Modifier,
) = ElementPreview {
    val context = LocalContext.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            style = ElementTheme.typography.fontHeadingSmMedium,
            text = title,
            textAlign = TextAlign.Center,
        )
        iconsList.chunked(6).forEach { iconsRow ->
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                iconsRow.forEach { icon ->
                    Column(
                        modifier = Modifier.width(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            modifier = Modifier.padding(2.dp),
                            resourceId = icon,
                            contentDescription = null,
                        )
                        Text(
                            text = iconNameTransform(
                                context.resources
                                    .getResourceEntryName(icon)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = ElementTheme.typography.fontBodyXsMedium,
                            color = ElementTheme.colors.textSecondary,
                        )
                    }
                }
            }
        }
    }
}
