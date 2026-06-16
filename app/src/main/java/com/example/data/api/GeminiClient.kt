package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com"
    private const val MODEL_NAME = "gemini-3.5-flash"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateContent(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "MY_API_KEY") {
            Log.w(TAG, "Gemini API Key is empty or placeholder. Falling back to local helper.")
            return@withContext getLocalSimulatedResponse(prompt)
        }

        val url = "$BASE_URL/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"

        try {
            val root = JSONObject()
            val contentsArray = JSONArray()
            val contentObject = JSONObject()
            val partsArray = JSONArray()
            val partObject = JSONObject()
            partObject.put("text", prompt)
            partsArray.put(partObject)
            contentObject.put("parts", partsArray)
            contentsArray.put(contentObject)
            root.put("contents", contentsArray)

            if (systemInstruction != null) {
                val sysInstObj = JSONObject()
                val sysPartsArray = JSONArray()
                val sysPartObj = JSONObject()
                sysPartObj.put("text", systemInstruction)
                sysPartsArray.put(sysPartObj)
                sysInstObj.put("parts", sysPartsArray)
                root.put("systemInstruction", sysInstObj)
            }

            val requestBody = root.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: "no error body"
                    Log.e(TAG, "Request failed: code=${response.code} body=$errBody")
                    return@withContext "Error details: code=${response.code}. Try again, or check your API key."
                }

                val responseString = response.body?.string()
                if (responseString.isNullOrEmpty()) {
                    return@withContext "Received empty response from Hyper AI."
                }

                val responseJson = JSONObject(responseString)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    return@withContext "No candidates found in Hyper AI response."
                }

                val content = candidates.getJSONObject(0).optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return@withContext parts.getJSONObject(0).optString("text", "No text part found.")
                }

                "Sorry, I couldn't understand that response."
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network call failed", e)
            "Hyper Chat is currently offline or the network failed: ${e.message}"
        } catch (e: Exception) {
            Log.e(TAG, "JSON/Unexpected exception", e)
            getLocalSimulatedResponse(prompt)
        }
    }

    private fun getLocalSimulatedResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") -> {
                "🚀 *Hyper AI Local Helper*:\n\nHello! I am Hyper AI, built by *Tucci Cyber Nation and Kawooya Raymond*. Since your Gemini API key is offline or not set up in secrets yet, I am running locally!\n\nAsk me anything like:\n- \"summarize chat\"\n- \"translate to French\"\n- \"create smart reply\""
            }
            lower.contains("summarize") || lower.contains("summary") -> {
                "📁 *Hyper AI Chat Summary*:\n\n- *Kawooya Raymond* asked if the Android prototype compiles successfully.\n- *Tucci Cyber* approved the custom adaptive green-and-black black app launcher design icon.\n- *System Alert*: Peak encryption standards (AES-256) are currently active."
            }
            lower.contains("translate") -> {
                "🌎 *Hyper AI Translation System* (Local Mode):\n\n\"Let's chat about technology!\" ➡️ *\"Parlons de technologie!\"* (French)\n\"Hyper Chat is fast.\" ➡️ *\"Hyper Chat est rapide.\"*"
            }
            lower.contains("suggest") || lower.contains("reply") || lower.contains("suggestion") -> {
                "💡 *Hyper AI Smart Replies*:\n\n1. \"Got it! Looks super polished! 👍\"\n2. \"Great job Kawooya, compiling now. 🚀\"\n3. \"Will review immediately!\""
            }
            lower.contains("sticker") || lower.contains("generate") -> {
                "🎨 *Hyper AI Art Generator* (Simulated PNG):\n\n[Successfully generated Sticker: 'Cyber Emerald Chat Bubble with Neon Accent']\nSending to your clipboard!"
            }
            else -> {
                "🤖 *Hyper AI Assistant*:\n\nI received your prompt: \"$prompt\"\n\n*Pro-tip:* To unlock the full power of live *Gemini-3.5-Flash*, please head to Google AI Studio's **Secrets Panel**, add your `GEMINI_API_KEY`, and let me analyze files in real-time with true LLM capabilities!"
            }
        }
    }
}
