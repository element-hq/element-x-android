package io.element.android.x.avatar

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

/**
 * TODO fallback Avatar
 */
@Composable
fun Avatar(avatarData: AvatarData) {
    Image(
        painter = rememberAsyncImagePainter(
            model = avatarData.url,
            onError = {
                Log.e("TAG", "Error $it\n${it.result}", it.result.throwable)
            }),
        contentDescription = null,
        modifier = Modifier
            .size(avatarData.size)
            .clip(CircleShape)
            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
    )
}

