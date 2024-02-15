package com.bright.sunriseset

class MainViewModel {

    fun loadLanguages(): ArrayList<Language> {
        return  arrayListOf(
            Language("English", "en"),
            Language("Chinese", "zh")
        )
    }
}