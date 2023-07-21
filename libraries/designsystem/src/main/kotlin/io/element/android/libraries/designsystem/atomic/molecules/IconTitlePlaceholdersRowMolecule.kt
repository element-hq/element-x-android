package io.element.android.libraries.designsystem.atomic.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.atomic.atoms.PlaceholderAtom
import io.element.android.libraries.designsystem.theme.placeholderBackground
import io.element.android.libraries.theme.ElementTheme

@Composable
fun IconTitlePlaceholdersRowMolecule(
    iconSize: Dp,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .align(Alignment.CenterVertically)
                .background(color = ElementTheme.colors.placeholderBackground, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        PlaceholderAtom(width = 20.dp, height = 7.dp)
        Spacer(modifier = Modifier.width(7.dp))
        PlaceholderAtom(width = 45.dp, height = 7.dp)
    }
}
