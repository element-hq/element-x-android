import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.element.android.x.designsystem.AvatarGradientEnd
import io.element.android.x.designsystem.AvatarGradientStart
import io.element.android.x.designsystem.components.avatar.AvatarData

@Composable
fun Avatar(avatarData: AvatarData, modifier: Modifier = Modifier) {
    if (avatarData.model == null) {
        InitialsAvatar(
            modifier = modifier
                .size(avatarData.size.dp)
                .clip(CircleShape),
            initials = avatarData.initials
        )
    } else {
        ImageAvatar(
            modifier = modifier
                .size(avatarData.size.dp)
                .clip(CircleShape),
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
            Log.e("TAG", "Error $it\n${it.result}", it.result.throwable)
        },
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(avatarData.size.dp)
            .clip(CircleShape)
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
        )
    )
    Box(
        modifier
            .background(brush = initialsGradient)
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.Center),
            text = initials,
            fontSize = 24.sp,
            color = Color.White,
        )
    }
}


