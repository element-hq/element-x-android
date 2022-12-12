package io.element.android.x.designsystem.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
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
        inlineContent = inlineContent
    )
}
