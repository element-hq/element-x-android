/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.libraries.designsystem.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Text
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

@Composable
fun ClickableLinkText(
    text: AnnotatedString,
    linkAnnotationTag: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    inlineContent: ImmutableMap<String, InlineTextContent> = persistentMapOf(),
) {
    val uriHandler = LocalUriHandler.current
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val pressIndicator = Modifier.pointerInput(onClick) {
        detectTapGestures(
            onPress = { offset: Offset ->
                val pressInteraction = PressInteraction.Press(offset)
                interactionSource.emit(pressInteraction)
                val isReleased = tryAwaitRelease()
                if (isReleased) {
                    interactionSource.emit(PressInteraction.Release(pressInteraction))
                } else {
                    interactionSource.emit(PressInteraction.Cancel(pressInteraction))
                }
            },
            onLongPress = {
                onLongClick()
            }
        ) { offset ->
            layoutResult.value?.let { layoutResult ->
                val position = layoutResult.getOffsetForPosition(offset)
                val linkAnnotations =
                    text.getStringAnnotations(linkAnnotationTag, position, position)
                if (linkAnnotations.isEmpty()) {
                    onClick()
                } else {
                    uriHandler.openUri(linkAnnotations.first().item)
                }
            }
        }
    }
    Text(
        text = text,
        modifier = modifier.then(pressIndicator),
        style = style,
        onTextLayout = {
            layoutResult.value = it
        },
        inlineContent = inlineContent,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Preview
@Composable
internal fun ClickableLinkTextLightPreview() =
    ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
internal fun ClickableLinkTextDarkPreview() =
    ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    ClickableLinkText(
        text = AnnotatedString("Hello", ParagraphStyle()),
        linkAnnotationTag = "",
        onClick = {},
        onLongClick = {},
        interactionSource = MutableInteractionSource(),
    )
}
