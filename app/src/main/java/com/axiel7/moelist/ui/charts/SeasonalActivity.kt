package com.axiel7.moelist.ui.charts

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import com.axiel7.moelist.MyApplication.Companion.animeDb
import com.axiel7.moelist.MyApplication.Companion.malApiService
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.EndListReachedListener
import com.axiel7.moelist.adapter.SeasonalAnimeAdapter
import com.axiel7.moelist.databinding.ActivitySeasonalBinding
import com.axiel7.moelist.model.SeasonalAnimeResponse
import com.axiel7.moelist.model.SeasonalList
import com.axiel7.moelist.model.StartSeason
import com.axiel7.moelist.ui.BaseActivity
import com.axiel7.moelist.ui.details.AnimeDetailsActivity
import com.axiel7.moelist.utils.InsetsHelper.addSystemWindowInsetToMargin
import com.axiel7.moelist.utils.SeasonCalendar
import com.axiel7.moelist.utils.SharedPrefsHelpers
import com.axiel7.moelist.utils.StringFormat
import com.axiel7.moelist.utils.Urls
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.transition.platform.MaterialSharedAxis
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.properties.Delegates

class SeasonalActivity : BaseActivity() {

    private lateinit var seasonalAdapter: SeasonalAnimeAdapter
    private lateinit var seasonalList: MutableList<SeasonalList>
    private lateinit var seasonLayout: TextInputLayout
    private lateinit var yearLayout: TextInputLayout
    private lateinit var season: String
    private lateinit var currentSeason: StartSeason
    private var year by Delegates.notNull<Int>()
    private var animeResponse: SeasonalAnimeResponse? = null
    private var showNsfw = 0
    private lateinit var binding: ActivitySeasonalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        window.enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        window.returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        window.allowEnterTransitionOverlap = true
        window.allowReturnTransitionOverlap = true
        super.onCreate(savedInstanceState)
        binding = ActivitySeasonalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = getColorFromAttr(R.attr.colorToolbar)

        setSupportActionBar(binding.seasonalToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        binding.seasonalToolbar.setNavigationOnClickListener { onBackPressed() }

        showNsfw = if (SharedPrefsHelpers.instance!!.getBoolean("nsfw", false)) { 1 } else { 0 }

        season = SeasonCalendar.getCurrentSeason()
        year = SeasonCalendar.getCurrentYear()
        currentSeason = StartSeason(year, season)
        binding.seasonalToolbar.title = "${StringFormat.formatSeason(season, this)} $year"

        seasonalList = mutableListOf()
        val savedResponse = animeDb?.seasonalResponseDao()?.getSeasonalResponse(season, year)
        if (savedResponse!=null) {
            animeResponse = savedResponse
            val savedList = animeResponse!!.data
            for (anime in savedList) {
                if (anime.node.start_season==currentSeason) {
                    seasonalList.add(anime)
                }
            }
            seasonalList.sortByDescending { it.node.mean }
        }
        seasonalAdapter = SeasonalAnimeAdapter(
            seasonalList,
            this,
            onClickListener = {itemView, animeList -> openDetails(animeList.node.id, itemView)})
        seasonalAdapter.setEndListReachedListener(object :EndListReachedListener {
            override fun onBottomReached(position: Int, lastPosition: Int) {
                if (animeResponse!=null) {
                    val nextPage = animeResponse?.paging?.next
                    if (nextPage!=null && nextPage.isNotEmpty()) {
                        val getMoreCall = malApiService.getNextSeasonalPage(nextPage)
                        initCalls(false, getMoreCall)
                    }
                }
            }
        })
        binding.seasonalRecycler.adapter = seasonalAdapter

        binding.filterFab.addSystemWindowInsetToMargin(bottom = true)
        setupBottomSheet()

        val seasonCall = malApiService.getSeasonalAnime(Urls.apiBaseUrl + "anime/season/$year/$season",
            "anime_score", "start_season,broadcast,num_episodes,media_type,mean", 300, showNsfw)
        initCalls(true, seasonCall)
    }
    private fun initCalls(shouldClear: Boolean, call: Call<SeasonalAnimeResponse>) {
        binding.seasonalLoading.show()
        call.enqueue(object :Callback<SeasonalAnimeResponse> {
            override fun onResponse(
                call: Call<SeasonalAnimeResponse>,
                response: Response<SeasonalAnimeResponse>) {
                if (response.isSuccessful && animeResponse!=response.body()) {
                    animeResponse = response.body()
                    val animeList = animeResponse!!.data
                    animeList.sortByDescending { it.node.mean }
                    if (shouldClear) {
                        seasonalList.clear()
                    }
                    seasonalList.addAll(animeList)
                    animeDb?.seasonalResponseDao()?.insertSeasonalResponse(animeResponse!!)
                    seasonalAdapter.notifyDataSetChanged()
                }
                else if (response.code()==401) {
                    Snackbar.make(binding.root, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                }
                else if (response.code()==404) {
                    Snackbar.make(binding.root, getString(R.string.error_not_found), Snackbar.LENGTH_SHORT).show()
                }
                binding.seasonalLoading.hide()
            }

            override fun onFailure(call: Call<SeasonalAnimeResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                binding.seasonalLoading.hide()
                Snackbar.make(binding.root, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    @SuppressLint("InflateParams")
    private fun setupBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_seasonal, null)
        bottomSheetDialog.setContentView(dialogView)
        binding.filterFab.setOnClickListener { bottomSheetDialog.show() }

        val applyButton = bottomSheetDialog.findViewById<Button>(R.id.apply_button)
        applyButton?.setOnClickListener {
            season = StringFormat.formatSeasonInverted(seasonLayout.editText?.text.toString(), this)
            year = yearLayout.editText?.text.toString().toInt()
            val seasonCall = malApiService.getSeasonalAnime(Urls.apiBaseUrl + "anime/season/$year/$season",
                "anime_score", "start_season,broadcast,num_episodes,media_type,mean", 300, showNsfw)
            initCalls(true, seasonCall)
            binding.seasonalToolbar.title = "${StringFormat.formatSeason(season, this)} $year"
            bottomSheetDialog.hide()
        }
        val cancelButton = bottomSheetDialog.findViewById<Button>(R.id.cancel_button)
        cancelButton?.setOnClickListener { 
            seasonLayout.editText?.setText(season)
            yearLayout.editText?.setText(year.toString())
            bottomSheetDialog.hide()
        }

        seasonLayout = bottomSheetDialog.findViewById(R.id.season_layout)!!
        val seasons = listOf(getString(R.string.winter), getString(R.string.spring),
            getString(R.string.summer), getString(R.string.fall))
        val seasonAdapter = ArrayAdapter(this, R.layout.list_status_item, seasons)
        (seasonLayout.editText as? AutoCompleteTextView)?.setAdapter(seasonAdapter)
        (seasonLayout.editText as? AutoCompleteTextView)
            ?.setText(StringFormat.formatSeason(season, this), false)

        yearLayout = bottomSheetDialog.findViewById(R.id.year_layout)!!
        val years = mutableListOf<Int>()
        val currentYear = SeasonCalendar.getCurrentYear()
        val baseYear = 1995
        for (x in baseYear..currentYear+1) { years.add(x) }
        years.sortDescending()
        val yearAdapter = ArrayAdapter(this, R.layout.list_status_item, years)
        (yearLayout.editText as? AutoCompleteTextView)?.setAdapter(yearAdapter)
        (yearLayout.editText as? AutoCompleteTextView)?.setText(year.toString(), false)
    }

    private fun openDetails(animeId: Int?, view: View) {
        val poster = view.findViewById<ImageView>(R.id.anime_poster)
        val intent = Intent(this, AnimeDetailsActivity::class.java)
        val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this, poster, poster.transitionName)
        intent.putExtra("animeId", animeId)
        startActivity(intent, bundle.toBundle())
    }

    override fun onResume() {
        super.onResume()
        if (seasonalList.isNotEmpty()) {
            binding.seasonalLoading.hide()
        }
    }
}