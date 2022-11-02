import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * TODO fallback Avatar
 */
@Composable
fun Avatar(
    data: ByteArray?,
    size: Dp = 48.dp,
) {
    AsyncImage(
        model = data,
        onError = {
            Log.e("TAG", "Error $it\n${it.result}", it.result.throwable)
        },
        contentDescription = null,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
    )
}

