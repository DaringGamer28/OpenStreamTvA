package com.ivor.openstream.presentation.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ivor.openstream.R
import com.ivor.openstream.domain.model.StreamQuality
import com.ivor.openstream.domain.model.VideoServer
import com.ivor.openstream.presentation.player.sourceSummary
import com.ivor.openstream.ui.theme.ExpressiveShapes

/**
 * Long-press download picker: every resolved source with its provider and quality, so the user
 * picks exactly where an episode/movie downloads from. Season downloads prefer the tapped row's
 * provider and quality for each episode.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DownloadOptionsDialog(
    state: DownloadOptionsUiState,
    onDismiss: () -> Unit,
    onDownload: (VideoServer) -> Unit
) {
    var qualityFilter by rememberSaveable { mutableStateOf<String?>(null) }
    val qualities = state.servers.map { it.quality.filterLabel() }.distinct()
        .sortedWith(compareByDescending<String> { qualityRank(it) }.thenBy { it })
    val visible = state.servers.filter {
        qualityFilter == null || it.quality.filterLabel() == qualityFilter
    }
    val subtitle = when {
        state.isBatch -> stringResource(R.string.dl_pick_batch_hint)
        else -> stringResource(R.string.dl_pick_source_hint)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(stringResource(R.string.dl_pick_source_title))
                Text(
                    text = if (state.episodeLabel != null && !state.isBatch) {
                        "${state.title} · ${state.episodeLabel}"
                    } else if (state.isBatch) {
                        "${state.title} · ${state.episodes.size}"
                    } else {
                        state.title
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text(
                        text = if (state.totalProviders > 0) {
                            stringResource(
                                R.string.src_progress,
                                state.servers.size,
                                state.completedProviders,
                                state.totalProviders
                            )
                        } else {
                            stringResource(R.string.player_checking_sources)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (qualities.size > 1) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = qualityFilter == null,
                                onClick = { qualityFilter = null },
                                label = { Text(stringResource(R.string.search_filter_all)) },
                                shape = ExpressiveShapes.small
                            )
                        }
                        items(qualities, key = { it }) { quality ->
                            FilterChip(
                                selected = qualityFilter == quality,
                                onClick = { qualityFilter = quality },
                                label = { Text(quality) },
                                shape = ExpressiveShapes.small
                            )
                        }
                    }
                }
                if (visible.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                    ) {
                        itemsIndexed(visible, key = { _, server -> server.id }) { index, server ->
                            SegmentedListItem(
                                onClick = { onDownload(server) },
                                shapes = ListItemDefaults.segmentedShapes(index = index, count = visible.size),
                                colors = ListItemDefaults.segmentedColors(),
                                supportingContent = {
                                    Text(
                                        "${server.providerName} · ${server.sourceSummary(LocalContext.current)}",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                trailingContent = {
                                    IconButton(onClick = { onDownload(server) }) {
                                        Icon(
                                            Icons.Default.Download,
                                            contentDescription = stringResource(
                                                R.string.cd_download_from,
                                                server.name
                                            )
                                        )
                                    }
                                }
                            ) {
                                Text(
                                    server.name,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                } else if (state.isComplete) {
                    Text(
                        text = stringResource(R.string.dl_no_source),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            Row {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
            }
        }
    )
}

private fun StreamQuality.filterLabel(): String = when (this) {
    StreamQuality.UNKNOWN -> "Adaptive"
    else -> label
}

private fun qualityRank(label: String): Int = when (label) {
    "4K" -> 6
    "1440p" -> 5
    "1080p" -> 4
    "HD" -> 3
    "720p" -> 2
    "480p" -> 1
    else -> 0
}
