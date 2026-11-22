package com.example.data.ai

import android.graphics.Bitmap
import android.graphics.Color
import com.example.data.model.AppLanguage
import com.example.data.model.FertilizerAdviceResult
import com.example.data.model.PlantDiagnosisResult
import com.example.data.model.SoilScanResult
import com.example.data.model.WaterAdviceResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.roundToInt

/**
 * Clearly labeled sample AI implementation used when Firebase AI Logic is not enabled.
 * Provides agriculturally responsible sample guidance without fabricating confirmed diagnoses.
 */
class PlaceholderAiRepository : AiRepository {
    override val isLiveAiConnected: Boolean = false

    override suspend fun analyzePlantLeaf(
        imageBitmap: Bitmap?,
        cropName: String,
        symptomsNote: String
    ): PlantDiagnosisResult {
        delay(900)
        val visualHint = inspectImageColorProfile(imageBitmap)
        val cropLabel = cropName.ifBlank { "आपकी फसल" }
        val lowerSymptoms = symptomsNote.lowercase()

        val suspectedCondition = when {
            lowerSymptoms.contains("पील") || lowerSymptoms.contains("yellow") || visualHint.yellowRatio > 0.28f ->
                "संभावित नाइट्रोजन/लौह तत्व की कमी या शुरुआती क्लोरोसिस (पत्ती का पीला पड़ना)"
            lowerSymptoms.contains("धब्ब") || lowerSymptoms.contains("spot") || lowerSymptoms.contains("दाग") ->
                "संभावित फफूंदजनित पत्ती धब्बा रोग (Leaf Spot / Blight के शुरुआती लक्षण)"
            lowerSymptoms.contains("कीट") || lowerSymptoms.contains("सुंडी") || lowerSymptoms.contains("मुड़") ->
                "संभावित रस चूसक कीट (माहू/थ्रिप्स/सफेद मक्खी) या पत्ती मोड़क का प्रकोप"
            else ->
                "पत्ती पर हल्का तनाव दिख रहा है — पोषण असंतुलन या शुरुआती रोग की संभावना"
        }

        val imageSummary = if (imageBitmap != null) {
            "अपलोड की गई फोटो (${imageBitmap.width}×${imageBitmap.height} px) के आधार पर सैंपल आकलन:"
        } else {
            "विवरण के आधार पर सैंपल आकलन:"
        }

        return PlantDiagnosisResult(
            titleHindi = "[सैंपल प्रीव्यू] $cropLabel — प्रारंभिक पत्ती जांच",
            possibleIssues = listOf(
                suspectedCondition,
                "जड़ क्षेत्र में अधिक या कम नमी के कारण पोषक तत्वों का अवशोषण धीमा होना",
                if (symptomsNote.isNotBlank()) "किसान द्वारा बताए लक्षण: $symptomsNote" else "सूक्ष्म कीट (जैसे माहू/माइट्स) के लिए पत्ती की निचली सतह जांचें"
            ),
            growthStuntExplanation = "$imageSummary जब पत्तियों में क्लोरोफिल कम होता है या जड़ों को हवा/नमी संतुलित नहीं मिलती, तो पौधा भोजन (प्रकाश संश्लेषण) कम बना पाता है और बढ़वार रुक जाती है।",
            treatmentStepsHindi = listOf(
                "1. सबसे पहले खेत से बहुत अधिक पीली या सूखी संक्रमित पत्तियों को सावधानी से हटाएं।",
                "2. सिंचाई संतुलित रखें — खेत में पानी जमा न होने दें ताकि जड़ों को ऑक्सीजन मिले।",
                "3. यदि छोटे रस चूसक कीट दिखें तो शुरुआती रोकथाम के लिए नीम तेल (Neem Oil 1500 ppm, 3–5 ml प्रति लीटर पानी) का शाम के समय छिड़काव करें।",
                "4. किसी भी रासायनिक दवा या खाद का छिड़काव करने से पहले नजदीकी कृषि विज्ञान केंद्र (KVK) विशेषज्ञ से पुष्टि अवश्य कराएं।"
            ),
            uncertaintyNote = "अनिश्चितता सूचना: केवल एक फोटो से किसी बीमारी, कीट या पोषक तत्व की कमी की 100% पक्की पुष्टि नहीं की जा सकती। समान लक्षण कई कारणों से दिख सकते हैं।",
            expertAdviceNote = "लाइव Gemini Vision विश्लेषण के लिए Firebase AI Logic सक्षम करें। गंभीर प्रकोप में नजदीकी KVK या कृषि अधिकारी को पौधा दिखाएं और रोग पूरी तरह ठीक होने की किसी गारंटी पर भरोसा न करें।",
            isSamplePreview = true
        )
    }

    override suspend fun analyzeSoilPhoto(
        imageBitmap: Bitmap?,
        cropName: String,
        locationNote: String
    ): SoilScanResult {
        delay(850)
        val visualHint = inspectImageColorProfile(imageBitmap)
        val soilGuess = when {
            visualHint.darkness > 0.58f -> "गहरे रंग की दोमट / जैविक कार्बन युक्त सतही मिट्टी (संभावित)"
            visualHint.warmth > 0.18f -> "हल्की लाल या बलुई-दोमट प्रकार की मिट्टी (संभावित)"
            else -> "मध्यम भूरी जलोढ़ / दोमट मिट्टी (संभावित)"
        }
        val cropLabel = cropName.ifBlank { "मुख्य फसल" }

        return SoilScanResult(
            preliminarySoilType = "[सैंपल प्रीव्यू] $soilGuess",
            surfaceConditionNote = buildString {
                append("सतही बनावट देखने में ")
                if (visualHint.darkness > 0.55f) {
                    append("नमीयुक्त व भुरभुरी प्रतीत होती है। ")
                } else {
                    append("हल्की सूखी या कणदार प्रतीत होती है; बुवाई/सिंचाई से पहले 6 इंच गहराई की नमी हाथ से जांचें। ")
                }
                if (locationNote.isNotBlank()) {
                    append("क्षेत्र ($locationNote) की स्थानीय मिट्टी के अनुसार $cropLabel के लिए जल निकासी आवश्यक है।")
                }
            },
            possibleNutrientProblems = listOf(
                "यदि मिट्टी बहुत हल्के रंग की है तो जैविक कार्बन (Organic Carbon) कम होने की संभावना हो सकती है।",
                "लगातार एक ही फसल लेने से नाइट्रोजन (N) और सूक्ष्म पोषक तत्व (जिंक/बोरॉन) असंतुलित हो सकते हैं।",
                "ऊपरी परत सख्त (पपड़ी) बनने पर जड़ों में हवा व पानी का संचार घट सकता है।"
            ),
            recommendedLabTests = listOf(
                "1. मिट्टी का pH और EC (लवणता) जांच — यह तय करने के लिए कि मिट्टी अम्लीय, क्षारीय या सामान्य है।",
                "2. जैविक कार्बन (OC) और मुख्य तत्व NPK (नाइट्रोजन, फास्फोरस, पोटाश) परीक्षण।",
                "3. सूक्ष्म पोषक तत्व (जिंक, सल्फर, आयरन, बोरॉन) — विशेषकर यदि फसल की बढ़वार कम हो।",
                "सैंपल लेने का तरीका: खेत में 5–6 जगह से 'V' आकार का 6 इंच गहरा गड्ढा खोदकर मिट्टी मिलाएं और सरकारी मृदा परीक्षण प्रयोगशाला (Soil Health Card Lab) में भेजें।"
            ),
            photoLimitationDisclaimer = "महत्वपूर्ण चेतावनी: केवल फोटो देखकर मिट्टी के वास्तविक pH, NPK या सूक्ष्म पोषक तत्वों को सटीक रूप से नहीं मापा जा सकता। सटीक खाद मात्रा के लिए प्रयोगशाला मृदा परीक्षण (Lab Soil Test) अनिवार्य है।",
            isSamplePreview = true
        )
    }

    override suspend fun getFertilizerAdvice(
        cropName: String,
        growthStage: String,
        soilTestSummary: String,
        symptoms: String
    ): FertilizerAdviceResult {
        delay(650)
        val crop = cropName.ifBlank { "गेहूँ / सामान्य फसल" }
        val stage = growthStage.ifBlank { "वानस्पतिक वृद्धि (Vegetative)" }
        val hasSoilTest = soilTestSummary.isNotBlank() && !soilTestSummary.contains("नहीं")

        val fertilizers = when {
            stage.contains("बुवाई") || stage.contains("अंकुरण") -> listOf(
                "सड़ी हुई गोबर की खाद / वर्मीकम्पोस्ट (जैविक आधार)",
                "डीएपी (DAP) या सिंगल सुपर फॉस्फेट (SSP) — मिट्टी जांच के अनुसार संतुलित मात्रा",
                "म्यूरेट ऑफ पोटाश (MOP) — जड़ मजबूती के लिए"
            )
            stage.contains("फूल") || stage.contains("फल") || stage.contains("दाना") -> listOf(
                "पोटाश युक्त जल-घुलनशील खाद (जैसे NPK 0:0:50 या 13:0:45)",
                "सूक्ष्म पोषक तत्व मिश्रण (जिंक / बोरॉन — कमी दिखने पर ही)"
            )
            else -> listOf(
                "नीम कोटेड यूरिया (सिर्फ हल्की टॉप-ड्रेसिंग, छोटे हिस्सों में)",
                "सल्फर या जिंक सल्फेट (यदि पत्तियों में पीलापन और मिट्टी जांच में कमी हो)",
                "जीवामृत या तरल जैविक खाद"
            )
        }

        return FertilizerAdviceResult(
            recommendedFertilizers = fertilizers,
            purposeHindi = "[सैंपल प्रीव्यू] $crop ($stage अवस्था): जड़ों का विकास मजबूत करने, पत्तियों में हरापन बढ़ाने और दाने/फल बनने की क्षमता सुधारने के लिए संतुलित पोषण जरूरी है। " +
                if (hasSoilTest) "आपके मिट्टी परीक्षण विवरण ($soilTestSummary) को ध्यान में रखकर संतुलित विकल्प दिए गए हैं।"
                else "चूंकि मिट्टी परीक्षण रिपोर्ट उपलब्ध नहीं है, इसलिए केवल सुरक्षित न्यूनतम आधार सुझाव दिए जा रहे हैं।",
            safeDosageHindi = "सुरक्षित सैंपल मात्रा: एक बार में अधिक खाद न डालें। यूरिया को हमेशा 2–3 बार में बांटकर दें (उदाहरण के लिए प्रति एकड़ 20–25 किग्रा प्रति किस्त से अधिक एक साथ न डालें) और अपने जिले के कृषि विभाग / मृदा स्वास्थ्य कार्ड की सटीक सिफारिश का पालन करें।",
            applicationTimingHindi = "खाद हमेशा हल्की सिंचाई के बाद या नमी युक्त मिट्टी में शाम के समय दें। कड़ी धूप या सूखी मिट्टी में यूरिया न डालें, और भारी बारिश से ठीक पहले छिड़काव से बचें।",
            excessiveUseWarning = "अतिरिक्त खाद से सावधान: आवश्यकता से अधिक यूरिया या रासायनिक खाद डालने से पौधे कमजोर होकर गिर सकते हैं, कीट व फफूंद का हमला बढ़ता है, मिट्टी की उर्वरता खराब होती है और खर्च बेवजह बढ़ता है।",
            isSamplePreview = true
        )
    }

    override suspend fun getWaterAdvice(
        cropName: String,
        growthStage: String,
        soilMoisturePercent: Float?,
        weatherCondition: String
    ): WaterAdviceResult {
        delay(600)
        val crop = cropName.ifBlank { "आपकी फसल" }
        val moisture = soilMoisturePercent
        val hotWeather = weatherCondition.contains("गर्म") || weatherCondition.contains("तेज धूप")
        val rainyWeather = weatherCondition.contains("बारिश") || weatherCondition.contains("बादल")

        val shouldWater = when {
            rainyWeather -> false
            moisture != null && moisture < 32f -> true
            moisture != null && moisture > 65f -> false
            hotWeather -> true
            else -> true
        }

        val headline = when {
            rainyWeather -> "[सैंपल प्रीव्यू] अभी सिंचाई टालें — बारिश/नमी की संभावना"
            moisture != null && moisture < 32f -> "[सैंपल प्रीव्यू] आज हल्की सिंचाई आवश्यक है (नमी ${moisture.roundToInt()}%)"
            moisture != null && moisture > 65f -> "[सैंपल प्रीव्यू] मिट्टी में पर्याप्त नमी है (${moisture.roundToInt()}%) — अभी पानी न दें"
            else -> "[सैंपल प्रीव्यू] $crop ($growthStage) के लिए सुबह/शाम संतुलित सिंचाई करें"
        }

        return WaterAdviceResult(
            shouldWaterNow = shouldWater,
            recommendationHeadline = headline,
            timingHindi = "सिंचाई का सबसे अच्छा समय सुबह 6–9 बजे या शाम 4:30 बजे के बाद है। दोपहर की तेज धूप में पानी देने से वाष्पीकरण अधिक होता है और जड़ों को झटका लग सकता है।",
            weatherAndMoistureNote = buildString {
                if (moisture != null) {
                    append("सेंसर/इनपुट नमी: ${moisture.roundToInt()}%। ")
                }
                append("मौसम स्थिति: ${weatherCondition.ifBlank { "सामान्य" }}। ")
                append("अवस्था ($growthStage) में जड़ क्षेत्र को न तो सूखने दें और न ही जलभराव होने दें।")
            },
            waterSavingTip = "जल बचत सुझाव: संभव हो तो ड्रिप/स्प्रिंकलर या मेड़-नाली विधि अपनाएं और मिट्टी की नमी बनाए रखने के लिए सूखी घास/पुआल से मल्चिंग करें।",
            isSamplePreview = true
        )
    }

    override fun streamChatResponse(
        query: String,
        language: AppLanguage,
        farmContext: String
    ): Flow<String> = flow {
        val cleanQuery = query.trim()
        val responseText = when (language) {
            AppLanguage.HINDI -> buildString {
                append("[सैंपल AI उत्तर]\n")
                append("नमस्ते किसान भाई! आपका सवाल: \"$cleanQuery\"\n\n")
                if (farmContext.isNotBlank()) {
                    append("खेत संदर्भ: $farmContext\n\n")
                }
                append("• प्राथमिक सलाह: फसल की पत्तियों, तने और जड़ के पास की मिट्टी की नमी को ध्यान से देखें। यदि पत्तियाँ पीली पड़ रही हैं या दाग हैं, तो तुरंत 'पत्ती डॉक्टर' में साफ फोटो अपलोड करें।\n")
                append("• सुरक्षित कदम: संतुलित सिंचाई सुबह या शाम को करें और बिना मिट्टी जांच के अतिरिक्त यूरिया न डालें।\n")
                append("• ध्यान दें: लाइव Gemini AI बातचीत के लिए Firebase AI Logic सक्षम करना आवश्यक है। अभी यह एक सैंपल उत्तर है।")
            }
            AppLanguage.BHOJPURI -> buildString {
                append("[सैंपल AI जवाब]\n")
                append("प्रणाम किसान भाई! राउर सवाल बा: \"$cleanQuery\"\n\n")
                if (farmContext.isNotBlank()) {
                    append("राउर खेत के जानकारी: $farmContext\n\n")
                }
                append("• जरूरी सलाह: फसल के पत्ता आ जड़ के लगे के माटी ध्यान से देखीं। अगर पत्ता पीयर हो रहल बा या दाग लउकता, त 'पत्ती डॉक्टर' में फोटो खींच के जांच करीं।\n")
                append("• पानी आ खाद: सिंचाई भोर में या सांझ के करीं आ बिना माटी जांच करवले बेसी यूरिया मत छींटीं।\n")
                append("• सूचना: लाइव Gemini AI से बात करे खातिर Firebase चालू करे के पड़ी। ई अभी सैंपल जवाब ह।")
            }
            AppLanguage.ENGLISH -> buildString {
                append("[Sample AI Response]\n")
                append("Hello Farmer! Regarding your question: \"$cleanQuery\"\n\n")
                if (farmContext.isNotBlank()) {
                    append("Farm context: $farmContext\n\n")
                }
                append("• Quick Guidance: Inspect leaf undersides and root-zone soil moisture before applying any input. For leaf spots or yellowing, upload a clear close-up in Plant Doctor.\n")
                append("• Safe Practice: Water during cool morning/evening hours and avoid excess nitrogen without a soil lab test.\n")
                append("• Notice: Live Gemini AI chat requires enabling Firebase AI Logic. This is currently a labeled sample preview.")
            }
        }

        val words = responseText.split(" ")
        val builder = StringBuilder()
        for ((index, word) in words.withIndex()) {
            if (index > 0) builder.append(' ')
            builder.append(word)
            emit(builder.toString())
            delay(28)
        }
    }

    private data class ColorProfile(val darkness: Float, val yellowRatio: Float, val warmth: Float)

    private fun inspectImageColorProfile(bitmap: Bitmap?): ColorProfile {
        if (bitmap == null) return ColorProfile(0.5f, 0.1f, 0.1f)
        val stepX = (bitmap.width / 16).coerceAtLeast(1)
        val stepY = (bitmap.height / 16).coerceAtLeast(1)
        var count = 0
        var darkCount = 0
        var yellowCount = 0
        var warmSum = 0f

        var y = 0
        while (y < bitmap.height) {
            var x = 0
            while (x < bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                val luma = (0.299f * r + 0.587f * g + 0.114f * b) / 255f
                if (luma < 0.35f) darkCount++
                if (r > 130 && g > 125 && b < 95) yellowCount++
                warmSum += (r - b) / 255f
                count++
                x += stepX
            }
            y += stepY
        }
        val safeCount = count.coerceAtLeast(1)
        return ColorProfile(
            darkness = darkCount.toFloat() / safeCount,
            yellowRatio = yellowCount.toFloat() / safeCount,
            warmth = warmSum / safeCount
        )
    }
}
