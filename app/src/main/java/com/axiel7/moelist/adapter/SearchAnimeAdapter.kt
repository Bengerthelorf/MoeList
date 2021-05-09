package com.axiel7.moelist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.axiel7.moelist.R
import com.axiel7.moelist.model.AnimeList
import com.axiel7.moelist.utils.StringFormat

class SearchAnimeAdapter(private val animes: MutableList<AnimeList>,
                         private val rowLayout: Int,
                         private val context: Context,
                         private val onClickListener: (View, AnimeList) -> Unit) :
    RecyclerView.Adapter<SearchAnimeAdapter.AnimeViewHolder>() {
    private var endListReachedListener: EndListReachedListener? = null

    class AnimeViewHolder internal constructor(v: View) : RecyclerView.ViewHolder(v) {
        val animeTitleView: TextView = v.findViewById(R.id.anime_title)
        val animePosterView: ImageView = v.findViewById(R.id.anime_poster)
        val mediaStatusView: TextView = v.findViewById(R.id.media_status)
        val yearView: TextView = v.findViewById(R.id.year_text)
        val scoreView: TextView = v.findViewById(R.id.score_text)

        init {
            animePosterView.clipToOutline = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(rowLayout, parent, false)
        return AnimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val posterUrl = animes[position].node.main_picture?.medium
        val animeTitle = animes[position].node.title
        val mediaType = animes[position].node.media_type?.let { StringFormat.formatMediaType(it, context) }
        val episodes = animes[position].node.num_episodes
        val year = animes[position].node.start_season?.year
        val score = animes[position].node.mean

        holder.animePosterView.load(posterUrl) {
            crossfade(true)
            crossfade(500)
            error(R.drawable.ic_launcher_foreground)
            allowHardware(false)
        }

        holder.animeTitleView.text = animeTitle

        val mediaStatus = if (episodes==0) { "$mediaType (?? ${context.getString(R.string.episodes)})" }
        else { "$mediaType ($episodes ${context.getString(R.string.episodes)})" }
        holder.mediaStatusView.text = mediaStatus

        if (year == null) {
            holder.yearView.text = context.getString(R.string.unknown)
        }
        else {
            holder.yearView.text = year.toString()
        }

        val scoreText = score?.toString() ?: "??"
        holder.scoreView.text = scoreText

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