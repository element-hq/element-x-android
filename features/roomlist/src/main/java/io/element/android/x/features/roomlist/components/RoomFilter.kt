package io.element.android.x.features.roomlist.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomFilter(
    modifier: Modifier = Modifier,
    filter: String,
    onFilterChanged: (String) -> Unit
) {
    TextField(
        modifier = modifier,
        value = filter,
        onValueChange = onFilterChanged,
        //label = {
        //    Text(text = "Search")
        //},
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null
            )
        },
        trailingIcon = if (filter.isNotEmpty()) {
            {
                IconButton(onClick = { onFilterChanged("") }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = null
                    )
                }
            }
        } else null
    )
}

@Composable
@Preview
private fun RoomFilterPreview() {
    RoomFilter(
        filter = "",
        onFilterChanged = {}
    )
}