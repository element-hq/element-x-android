package io.element.android.x.designsystem.components.avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.element.android.x.designsystem.AvatarGradientEnd
import io.element.android.x.designsystem.AvatarGradientStart
import timber.log.Timber

@Composable
fun Avatar(avatarData: AvatarData, modifier: Modifier = Modifier) {
    val commonModifier = modifier
        .size(avatarData.size.dp)
        .clip(CircleShape)
    if (avatarData.model == null) {
        InitialsAvatar(
            modifier = commonModifier,
            initials = avatarData.name.first().uppercase()
        )
    } else {
        ImageAvatar(
            modifier = commonModifier,
            avatarData = avatarData
        )
    }
}

@Composable
private fun ImageAvatar(
    avatarData: AvatarData,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = avatarData.model,
        onError = {
            Timber.e("TAG", "Error $it\n${it.result}", it.result.throwable)
        },
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

@Composable
private fun InitialsAvatar(
    initials: String,
    modifier: Modifier = Modifier,
) {
    val initialsGradient = Brush.linearGradient(
        listOf(
            AvatarGradientStart,
            AvatarGradientEnd,
        ),
        start = Offset(0.0f, 100f),
        end = Offset(100f, 0f)
    )
    Box(
        modifier.background(brush = initialsGradient)
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = initials,
            fontSize = 24.sp,
            color = Color.White,
        )
    }
}
