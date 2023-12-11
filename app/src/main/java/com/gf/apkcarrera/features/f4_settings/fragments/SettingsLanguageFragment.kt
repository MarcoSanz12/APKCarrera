package com.gf.apkcarrera.features.f4_settings.fragments

import android.app.LocaleManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.gf.apkcarrera.MainActivity
import com.gf.apkcarrera.databinding.Frg04SettingsLanguageBinding
import com.gf.common.extensions.putAny
import com.gf.common.platform.BaseFragment
import com.gf.common.utils.Constants
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class SettingsLanguageFragment : BaseFragment<Frg04SettingsLanguageBinding>() {

    @Inject
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var lanPair : Pair<List<String>,List<MaterialButton>>
    private lateinit var bt : MaterialButton
    override fun initializeView() {
        val language : String =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                with(requireContext().getSystemService(LocaleManager::class.java).applicationLocales){
                    if (this.isEmpty)
                        Constants.LANGUAGE_ES
                    else
                        this[0].language
                }
            }else{
                with(AppCompatDelegate.getApplicationLocales()){
                    if (this.isEmpty)
                        Constants.LANGUAGE_ES
                    else
                        this[0]!!.language
                }
            }
        with(binding){
            bt = when(language){
                Constants.LANGUAGE_ES-> btCastellano
                Constants.LANGUAGE_EN-> btIngles
                Constants.LANGUAGE_FR-> btFrances
                Constants.LANGUAGE_CA-> btValenciano
                else -> {btCastellano}
            }
            lanPair = Pair(listOf(Constants.LANGUAGE_ES,Constants.LANGUAGE_FR,Constants.LANGUAGE_EN,Constants.LANGUAGE_CA),
                listOf(btCastellano,btFrances,btIngles,btValenciano))
        }

        assignLanguageButtons()

        bt.apply{
            isChecked = true
            isClickable = false
        }
    }

    private fun assignLanguageButtons() {
        for ((index, b) in lanPair.second.withIndex()) {
            b.setOnClickListener {
                changeLanguage(lanPair.first[index])
                it.isClickable = false
                lanPair.second
                    .filter { materialButton ->
                        materialButton.id != b.id
                    }
                    .forEach { materialButton ->
                        materialButton.apply {
                            isChecked = false
                            isClickable = true
                        }
                    }

                val refresh = Intent(requireContext(), MainActivity::class.java)
                requireActivity().startActivity(refresh)
                requireActivity().finish()
            }
        }
    }

    private fun changeLanguage(idioma : String) {
        sharedPreferences.putAny(Constants.LANGUAGE,idioma)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().getSystemService(LocaleManager::class.java).applicationLocales = LocaleList(
                Locale.forLanguageTag(idioma))
        }else{
            val appLocale : LocaleListCompat = LocaleListCompat.forLanguageTags(idioma)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }
}