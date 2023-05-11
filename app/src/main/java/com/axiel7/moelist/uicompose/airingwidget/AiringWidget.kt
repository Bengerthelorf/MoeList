package com.axiel7.moelist.uicompose.airingwidget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.nextAiringDayFormatted
import com.axiel7.moelist.uicompose.MainActivity
import com.axiel7.moelist.uicompose.theme.AppWidgetBox
import com.axiel7.moelist.uicompose.theme.AppWidgetColumn
import com.axiel7.moelist.uicompose.theme.stringResource

class AiringWidget : GlanceAppWidget() {

    override val stateDefinition = AiringInfoStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        AiringWidgetWorker.enqueue(context)

        provideContent {
            val airingInfo = currentState<AiringInfo>()

            GlanceTheme {
                when (airingInfo) {
                    is AiringInfo.Loading -> {
                        AppWidgetBox(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = GlanceTheme.colors.primary)
                        }
                    }
                    is AiringInfo.Available -> {
                        if (airingInfo.animeList.isEmpty()) {
                            AppWidgetBox(contentAlignment = Alignment.Center) {
                                Text(text = stringResource(R.string.nothing_today))
                            }
                        }
                        else AppWidgetColumn {
                            LazyColumn {
                                items(airingInfo.animeList) { item ->
                                    Column(
                                        modifier = GlanceModifier
                                            .padding(bottom = 8.dp)
                                            .fillMaxWidth()
                                            .clickable(actionStartActivity(
                                                Intent(LocalContext.current, MainActivity::class.java).apply {
                                                    action = "details"
                                                    putExtra("media_id", item.id)
                                                    putExtra("media_type", "anime")
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                    addCategory(item.id.toString())
                                                }
                                            ))
                                    ) {
                                        Text(
                                            text = item.title,
                                            style = TextStyle(
                                                color = GlanceTheme.colors.onSurfaceVariant
                                            ),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = item.broadcast!!.nextAiringDayFormatted()
                                                ?: stringResource(R.string.unknown),
                                            style = TextStyle(
                                                color = GlanceTheme.colors.onPrimaryContainer
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is AiringInfo.Unavailable -> {
                        AppWidgetColumn(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = airingInfo.message,
                                modifier = GlanceModifier.padding(bottom = 8.dp)
                            )
                            Button(
                                text = stringResource(R.string.refresh),
                                onClick = actionRunCallback<UpdateAiringInfoAction>()
                            )
                        }
                    }
                }
            }
        }
    }

    override suspend fun onDelete(context: Context, glanceId: GlanceId) {
        super.onDelete(context, glanceId)
        AiringWidgetWorker.cancel(context)
    }
}

class UpdateAiringInfoAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        AiringWidgetWorker.enqueue(context = context, force = true)
    }
}

class AiringWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AiringWidget()
}