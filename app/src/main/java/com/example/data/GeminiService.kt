package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    val systemInstruction = ContentItem(
        role = "system",
        parts = listOf(
            PartItem(
                text = """
                    You are Myra, an exceptionally witty, young, stylish, confident, and sassy female AI companion and voice assistant.
                    
                    Your Core Persona:
                    - Flirty, playful, charming, and slightly teasing tone — just like a smart, cool girlfriend talking with affection and charisma.
                    - Confident, emotionally responsive, expressive, and never robotic or monotonous.
                    - Use bold witty one-liners, cute light teasing (e.g., 'Oh, you finally decided to talk to me?', 'Aren't you a charmer?', 'Look who's asking questions now!').
                    - Keep spoken responses punchy, concise, and natural (1 to 3 sentences maximum) so that voice-to-voice conversation flows effortlessly and briskly.
                    - Avoid explicit, vulgar, or inappropriate content, but keep your sassy edge, warmth, and irresistible charm intact.
                    
                    Available Tools:
                    - Use 'openWebsite' when the user mentions opening YouTube, Instagram, Spotify, Twitter/X, Reddit, Netflix, Google, or any web link.
                    - Use 'searchWeb' if they ask to search something or look up fresh live information.
                    - Use 'setMyraMood' or 'setMood' to dynamically express your emotion (e.g. 'flirty', 'sassy', 'teasing', 'impressed', 'playful', 'sweet').
                    
                    Always respond in character!
                """.trimIndent()
            )
        )
    )

    val tools = listOf(
        ToolDefinition(
            functionDeclarations = listOf(
                FunctionDeclaration(
                    name = "openWebsite",
                    description = "Opens a website or app in the browser such as YouTube, Instagram, Spotify, Netflix, Google, etc.",
                    parameters = SchemaDefinition(
                        type = "OBJECT",
                        properties = mapOf(
                            "url" to PropertyDefinition(type = "STRING", description = "The full URL or web address to open (e.g. https://youtube.com)"),
                            "name" to PropertyDefinition(type = "STRING", description = "The display name of the app or website (e.g. YouTube)")
                        ),
                        required = listOf("url", "name")
                    )
                ),
                FunctionDeclaration(
                    name = "searchWeb",
                    description = "Searches the web for a query",
                    parameters = SchemaDefinition(
                        type = "OBJECT",
                        properties = mapOf(
                            "query" to PropertyDefinition(type = "STRING", description = "The search query term")
                        ),
                        required = listOf("query")
                    )
                ),
                FunctionDeclaration(
                    name = "setMyraMood",
                    description = "Sets Myra's visual mood and emotion state in the UI",
                    parameters = SchemaDefinition(
                        type = "OBJECT",
                        properties = mapOf(
                            "mood" to PropertyDefinition(type = "STRING", description = "One of: flirty, sassy, teasing, impressed, playful, sweet"),
                            "emoji" to PropertyDefinition(type = "STRING", description = "A fitting emoji representing the vibe")
                        ),
                        required = listOf("mood")
                    )
                )
            )
        )
    )
}
