package com.example.data.gemini

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Moshi Data Classes for Gemini REST API ---

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class InlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = null,
    @Json(name = "topP") val topP: Float? = null,
    @Json(name = "topK") val topK: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content? = null
)

// --- Retrofit Endpoints ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

// --- Retrofit Client ---

object RetrofitGeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

// --- Helper Functions in repository/service layer ---

object GeminiService {
    suspend fun generateMarketingTemplate(
        itemName: String,
        itemSpecs: String,
        discount: Double,
        shopName: String,
        customerName: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key Configuration Missing! Please set your GEMINI_API_KEY in the Secrets panel in AI Studio UI to generate real messages."
        }

        val prompt = if (customerName != null) {
            "Draft a highly compelling, elegant, and personalized promotional WhatsApp message for our valued jewelry customer, $customerName, from \"$shopName\". " +
            "The message is for a premium jewelry item \"$itemName\" ($itemSpecs). " +
            (if (discount > 0) "Provide a special celebratory discount of ${discount}% off. " else "") +
            "Make it luxurious, appetizing for exquisite gems, concise, include classy jewelry emojis, and end with a courteous call-to-action to visit our showroom. Do not contain brackets like [Your Name] - make it immediately copy-pastable."
        } else {
            "Draft a luxurious, highly persuasive, and catchy marketing SMS/WhatsApp status broadcast message for our jewelry store \"$shopName\", promoting a stunning item: \"$itemName\" ($itemSpecs). " +
            (if (discount > 0) "Highlight a special discount of ${discount}% for a limited time! " else "") +
            "Include brilliant jewelry emojis, keep it relatively short (under 130 words), elegant, and extremely appealing to luxury collectors. Ensure it is ready to copy and send."
        }

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            generationConfig = GenerationConfig(
                temperature = 0.7f,
                maxOutputTokens = 1000
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are a professional luxury brand copywriter specializing in precious metals, diamond jewelry, design artistry, and customer delight."))
            )
        )

        try {
            val response = RetrofitGeminiClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No response generated. Please double check if your inputs are detailed enough."
        } catch (e: Exception) {
            "Failed to connect to Gemini API: ${e.localizedMessage}. Check internet connectivity or API token validity."
        }
    }

    suspend fun getBusinessInsights(
        salesCount: Int,
        totalRevenue: Double,
        topItem: String,
        shopName: String,
        currentRates: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Configure your GEMINI_API_KEY in the Secrets panel in AI Studio to unlock automated AI Business Coaching insights."
        }

        val prompt = "Analyze the daily sales summary for premium jewelry showroom \"$shopName\":\n" +
            "- Invoiced Orders: $salesCount\n" +
            "- Total Revenue Sales: $$totalRevenue\n" +
            "- Peak Performer Item: $topItem\n" +
            "- Ongoing Metal Rates: $currentRates\n\n" +
            "Based on these numbers, output an executive briefing (maximum 4 short bullet points) detailing: " +
            "1. An encouraging, professional business assessment.\n" +
            "2. Fast actionable inventory/stock advice (such as restocks or security reminders).\n" +
            "3. A clever gold rate pricing strategy (if rates are high or volatile).\n" +
            "Keep the feedback luxurious, focused on cash flow, high-end retail craftsmanship, and highly actionable. No markdown tables."

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            generationConfig = GenerationConfig(
                temperature = 0.6f,
                maxOutputTokens = 800
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are a veteran high-fashion jewelry retail consultant and chief executive business coach."))
            )
        )

        try {
            val response = RetrofitGeminiClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Review successfully initiated. Add data points to refresh insights."
        } catch (e: Exception) {
            "Insights unavailable: ${e.localizedMessage}. Please check internet or API Key."
        }
    }

    suspend fun analyzeJewelImage(
        base64Data: String,
        mimeType: String = "image/jpeg"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key Configuration Missing! Please set your GEMINI_API_KEY."
        }

        val prompt = "Analyze this fine jewelry ornament image in detail. " +
            "1. Identify the ornament category (e.g., Ring, Necklace, Earrings, Bracelet, Bangle, Chain, Pendant).\n" +
            "2. Guess metal type (Gold, Silver, Platinum) and estimated purity status (22K, 18K, 950 Pt, etc.).\n" +
            "3. Describe key aesthetic specifications (patterns, stone inserts, handcraft detail style).\n" +
            "4. Provide a luxurious marketing heading and body for social status showcase.\n\n" +
            "Be extremely descriptive and highlight its premium, high-fashion artistry!"

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt),
                        Part(inlineData = InlineData(mimeType = mimeType, data = base64Data))
                    )
                )
            ),
            generationConfig = GenerationConfig(
                temperature = 0.5f,
                maxOutputTokens = 1200
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are a master jeweler, senior metal appraiser, and high-fashion luxury marketing director."))
            )
        )

        try {
            val response = RetrofitGeminiClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "AI analysis could not recognize the jewelry components of the uploaded image. Try a clearer close-up."
        } catch (e: Exception) {
            "AI Image Analysis failed: ${e.localizedMessage}. Please verify internet or API spec."
        }
    }

    suspend fun generateSegmentationMarketingCampaigns(
        customersJsonStr: String,
        recentInvoicesSummary: String,
        shopName: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key Configuration Missing! Please set your GEMINI_API_KEY in the Secrets panel in AI Studio UI to generate real marketing strategies."
        }

        val prompt = "Our premium jewelry shop is \"$shopName\".\n" +
            "Here is our customer database:\n$customersJsonStr\n\n" +
            "Here are our recent sales transactions and invoice logs:\n$recentInvoicesSummary\n\n" +
            "Please analyze these clients and their purchase histories to generate segment-specific marketing campaigns.\n" +
            "Provide exactly 3 custom marketing campaigns. Structure your output clearly and attractively as follows:\n" +
            "1. Segment Name (e.g. VIP Royal Gold Enthusiasts, Bridal & Heavy Jewelry Seekers, Minimalist Silver & Diamond Shoppers)\n" +
            "2. Segment Persona & Buying Trend analysis\n" +
            "3. WhatsApp / SMS Marketing Copy (In natural Bengali with jewelry emojis) ready to capture and send\n" +
            "4. Highly targeted inventory pitches\n\n" +
            "Make all instructions extremely clear and use brilliant, professional, high-fashion branding tones. Format gracefully in clean Markdown!"

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            generationConfig = GenerationConfig(
                temperature = 0.7f,
                maxOutputTokens = 1500
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are a senior jewelry retail growth consultant, professional CRM campaigns coordinator, and master copywriter fluent in high-society Bengali and modern English."))
            )
        )

        try {
            val response = RetrofitGeminiClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No response generated. Please complete more client invoices to compile analysis."
        } catch (e: Exception) {
            "Segmentation Campaign Analysis unavailable: ${e.localizedMessage}."
        }
    }

    suspend fun askBusinessAssistant(
        currentHistorySummary: String,
        currentInventorySummary: String,
        query: String,
        shopName: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key Configuration Missing! Please set your GEMINI_API_KEY in the Secrets panel in AI Studio UI to generate real marketing strategies or answer questions."
        }

        val prompt = "Our premium jewelry shop name is \"$shopName\".\n" +
            "Here is the database summary:\n" +
            "--- RECENT SALES & TRANSACTION RECORDS ---\n$currentHistorySummary\n\n" +
            "--- LIVE STOCK INVENTORY REGISTER ---\n$currentInventorySummary\n\n" +
            "User's business inquiry or copy-writing request:\n\"$query\"\n\n" +
            "Please analyze the store statistics and provide a highly useful, accurate, and professional response. If they ask about sales or highest category or stock performance, calculate/derive it from the live records provided above. If they ask to draft elegant sms or messages (e.g. Diwali promotional messages, Eid wishes, or engagement offers), write beautiful, ready-to-copy Bengali and English promotional copy with luxury jewelry emojis. Format neatly in clean Markdown!"

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            generationConfig = GenerationConfig(
                temperature = 0.7f,
                maxOutputTokens = 1500
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are Swarnali Shilpaloy's elite AI Business Strategist, Jewelry Consultant, and Financial Retail Analyst. You write professional, accurate reports and exquisite copy in beautiful Bengali and English."))
            )
        )

        try {
            val response = RetrofitGeminiClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "AI Assistant couldn't process this request. Feel free to rephrase."
        } catch (e: Exception) {
            "Assistant offline: ${e.localizedMessage}."
        }
    }
}
