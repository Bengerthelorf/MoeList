package com.axiel7.moelist.ui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.axiel7.moelist.BuildConfig
import com.axiel7.moelist.R
import com.axiel7.moelist.UseCases
import com.axiel7.moelist.databinding.ActivitySettingsBinding
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

@Suppress("unused")
class SettingsActivity : BaseActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var sharedPreferences: SharedPreferences
    private var themeChanged = false
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val settingsFragment = SettingsFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, settingsFragment)
            .commit()

        setSupportActionBar(binding.settingToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.settingToolbar.setNavigationOnClickListener { onBackPressed() }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        when(sharedPreferences.getString("theme", "follow_system")) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "follow_system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "amoled" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key=="theme") {
            this.recreate()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.main_preferences, rootKey)

            val logout = findPreference<Preference>("logout")
            logout?.setOnPreferenceClickListener {
                UseCases.logOut(requireContext())
                true
            }

            val version = findPreference<Preference>("version")
            version?.summary = BuildConfig.VERSION_NAME
            var clicks = 0
            version?.setOnPreferenceClickListener {
                if (clicks==7) {
                    Toast.makeText(context, "✧◝(⁰▿⁰)◜✧", Toast.LENGTH_LONG).show()
                    clicks = 0
                } else { clicks++ }
                true
            }

            val discord = findPreference<Preference>("discord")
            discord?.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("https://discord.gg/CTv3WdfxHh")
                    startActivity(intent)
                    true
                }

            val github = findPreference<Preference>("github")
            github?.setOnPreferenceClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://github.com/axiel7/MoeList")
                startActivity(intent)
                true
            }

            val feedback = findPreference<Preference>("feedback")
            feedback?.setOnPreferenceClickListener {
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("mailto:")
                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("contacto.axiel7@gmail.com"))
                    startActivity(intent)
                    true
                }

            val licenses = findPreference<Preference>("licenses")
            licenses?.setOnPreferenceClickListener {
                startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
                true
            }
        }
    }

    class CreditsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.credits_preferences, rootKey)

            val dany = findPreference<Preference>("dany")
            dany?.setOnPreferenceClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://instagram.com/danielvd_art")
                startActivity(intent)
                true
            }

            val jelu = findPreference<Preference>("jelu")
            jelu?.setOnPreferenceClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://github.com/Jeluchu")
                startActivity(intent)
                true
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }
    override fun finish() {
        val returnIntent = Intent()
        setResult(Activity.RESULT_OK, returnIntent)
        super.finish()
    }
}