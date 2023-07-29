package com.axiel7.moelist.uicompose.ranking

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.RankingType
import com.axiel7.moelist.data.model.media.durationText
import com.axiel7.moelist.data.model.media.mediaFormatLocalized
import com.axiel7.moelist.data.model.media.totalDuration
import com.axiel7.moelist.data.model.media.userPreferredTitle
import com.axiel7.moelist.uicompose.composables.OnBottomReached
import com.axiel7.moelist.uicompose.composables.media.MediaItemDetailed
import com.axiel7.moelist.uicompose.composables.media.MediaItemDetailedPlaceholder
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.NumExtensions
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrNull
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrUnknown

@Composable
fun MediaRankingListView(
    mediaType: MediaType,
    rankingType: RankingType,
    navigateToMediaDetails: (MediaType, Int) -> Unit,
) {
    val context = LocalContext.current
    val viewModel: MediaRankingViewModel = viewModel(key = rankingType.value) {
        MediaRankingViewModel(mediaType, rankingType)
    }
    val listState = rememberLazyListState()

    listState.OnBottomReached(buffer = 3) {
        if (!viewModel.isLoading && viewModel.hasNextPage) {
            viewModel.getRanking(viewModel.nextPage)
        }
    }

    LaunchedEffect(viewModel.message) {
        if (viewModel.showMessage) {
            context.showToast(viewModel.message)
            viewModel.showMessage = false
        }
    }

    LaunchedEffect(Unit) {
        if (!viewModel.isLoading && viewModel.nextPage == null && !viewModel.loadedAllPages)
            viewModel.getRanking()
    }

    LazyColumn(
        contentPadding = PaddingValues(
            top = 8.dp,
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        ),
        state = listState
    ) {
        items(
            items = viewModel.mediaList,
            key = { it.node.id },
            contentType = { it.node }
        ) { item ->
            MediaItemDetailed(
                title = item.node.userPreferredTitle(),
                imageUrl = item.node.mainPicture?.large,
                badgeContent = {
                    Text(
                        text = "#${item.ranking?.rank}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                subtitle1 = {
                    Text(
                        text = buildString {
                            append(item.node.mediaType?.mediaFormatLocalized())
                            if (item.node.totalDuration().toStringPositiveValueOrNull() != null) {
                                append(" (${item.node.durationText()})")
                            }
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                subtitle2 = {
                    Icon(
                        painter = painterResource(R.drawable.ic_round_details_star_24),
                        contentDescription = "star",
                        modifier = Modifier.padding(end = 4.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.node.mean.toStringPositiveValueOrUnknown(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                subtitle3 = {
                    Icon(
                        painter = painterResource(R.drawable.ic_round_group_24),
                        contentDescription = "group",
                        modifier = Modifier.padding(end = 4.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = NumExtensions.numberFormat.format(item.node.numListUsers),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = {
                    navigateToMediaDetails(mediaType, item.node.id)
                }
            )
        }
        if (viewModel.mediaList.isEmpty() && viewModel.isLoading) {
            items(10) {
                MediaItemDetailedPlaceholder()
            }
        }
    }//:LazyColumn
}