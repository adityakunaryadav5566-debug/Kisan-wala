package com.example.ui.navigation

object NavRoutes {
    const val DASHBOARD = "dashboard"
    const val PLANT_DOCTOR = "plant_doctor"
    const val SOIL_SCANNER = "soil_scanner"
    const val ADVISORS = "advisors"
    const val ADVISORS_WITH_TAB = "advisors?tab={tab}"
    const val VOICE_CHAT = "voice_chat"
    const val SENSOR = "sensor"
    const val PROFILE = "profile"

    fun advisorsRoute(tabIndex: Int): String = "advisors?tab=$tabIndex"
}
