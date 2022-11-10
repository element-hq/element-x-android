package io.element.android.x.features.messages.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowRow
import io.element.android.x.features.messages.model.AggregatedReaction
import io.element.android.x.features.messages.model.MessagesItemReactionState

@Composable
fun MessagesReactionsView(
    reactionsState: MessagesItemReactionState,
    modifier: Modifier = Modifier,
) {
    if(reactionsState.reactions.isEmpty()) return
    FlowRow(
        modifier = modifier,
        mainAxisSpacing = 2.dp,
        crossAxisSpacing = 8.dp,
    ) {
        reactionsState.reactions.forEach { reaction ->
            MessagesReactionButton(reaction = reaction)
        }
    }
}

@Composable
fun MessagesReactionButton(reaction: AggregatedReaction, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.background),
        shape = RoundedCornerShape(corner = CornerSize(12.dp)),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 5.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = reaction.key, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = reaction.count, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
        }
    }
}