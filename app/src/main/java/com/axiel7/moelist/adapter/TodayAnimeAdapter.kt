package com.axiel7.moelist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.axiel7.moelist.R
import com.axiel7.moelist.databinding.ListItemAnimeTodayExtendedBinding
import com.axiel7.moelist.model.SeasonalList
import com.axiel7.moelist.utils.SeasonCalendar
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue

class TodayAnimeAdapter(private val animes: MutableList<SeasonalList>,
                        private val context: Context,
                        private val onClickListener: (View, SeasonalList) -> Unit) :
    RecyclerView.Adapter<TodayAnimeAdapter.AnimeViewHolder>() {
    private var endListReachedListener: EndListReachedListener? = null

    class AnimeViewHolder(val binding: ListItemAnimeTodayExtendedBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val binding = ListItemAnimeTodayExtendedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return AnimeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val posterUrl = animes[position].node.main_picture?.medium
        val animeTitle = animes[position].node.title
        val score = animes[position].node.mean
        val startTime = animes[position].node.broadcast?.start_time
        val weekDay = animes[position].node.broadcast?.day_of_the_week
        holder.binding.animePoster.load(posterUrl) {
            crossfade(true)
            crossfade(500)
            error(R.drawable.ic_launcher_foreground)
            allowHardware(false)
        }
        holder.binding.animeTitle.text = animeTitle
        val scoreValue = "${context.getString(R.string.score_value)} $score"
        holder.binding.scoreText.text = scoreValue

        if (startTime!=null && weekDay!=null) {
            val jpTime = SeasonCalendar.getCurrentJapanHour()
            val startHour = LocalTime.parse(startTime, DateTimeFormatter.ISO_TIME).hour
            val currentWeekDay = SeasonCalendar.getCurrentJapanWeekday()
            val remaining = startHour - jpTime
            val airingValue = if (currentWeekDay==weekDay && remaining > 0) {
                context.getString(R.string.airing_in).format(remaining)
            }
            else {
                context.getString(R.string.aired_ago).format(remaining.absoluteValue)
            }
            holder.binding.airingTime.text = airingValue
        } else {
            val airingValue = "${context.getString(R.string.airing_in)} ??"
            holder.binding.airingTime.text = airingValue
        }

        val anime = animes[position]
        holder.itemView.setOnClickListener { view ->
            onClickListener(view, anime)
        }
        if (position == animes.size - 2) run {
            endListReachedListener?.onBottomReached(position, animes.size)
        }
    }

    override fun getItemCount(): Int {
        return animes.size
    }

    fun setEndListReachedListener(endListReachedListener: EndListReachedListener?) {
        this.endListReachedListener = endListReachedListener
    }
}