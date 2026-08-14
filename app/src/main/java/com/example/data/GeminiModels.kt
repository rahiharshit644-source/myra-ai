package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<ContentItem>,
    @Json(name = "systemInstruction") val systemInstruction: ContentItem? = null,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "tools") val tools: List<ToolDefinition>? = null
)

@JsonClass(generateAdapter = true)
data class ContentItem(
    @Json(name = "role") val role: String? = null,
    @Json(name = "parts") val parts: List<PartItem>
)

@JsonClass(generateAdapter = true)
data class PartItem(
    @Json(name = "text") val text: String? = null,
    @Json(name = "functionCall") val functionCall: FunctionCallItem? = null,
    @Json(name = "functionResponse") val functionResponse: FunctionResponseItem? = null
)

@JsonClass(generateAdapter = true)
data class FunctionCallItem(
    @Json(name = "name") val name: String,
    @Json(name = "args") val args: Map<String, Any>? = null
)

@JsonClass(generateAdapter = true)
data class FunctionResponseItem(
    @Json(name = "name") val name: String,
    @Json(name = "response") val response: Map<String, Any>
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "temperature") val temperature: Float? = 0.85f,
    @Json(name = "topP") val topP: Float? = 0.95f,
    @Json(name = "topK") val topK: Int? = 40,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = 300
)

@JsonClass(generateAdapter = true)
data class ToolDefinition(
    @Json(name = "functionDeclarations") val functionDeclarations: List<FunctionDeclaration>
)

@JsonClass(generateAdapter = true)
data class FunctionDeclaration(
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String,
    @Json(name = "parameters") val parameters: SchemaDefinition? = null
)

@JsonClass(generateAdapter = true)
data class SchemaDefinition(
    @Json(name = "type") val type: String = "OBJECT",
    @Json(name = "properties") val properties: Map<String, PropertyDefinition>? = null,
    @Json(name = "required") val required: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class PropertyDefinition(
    @Json(name = "type") val type: String,
    @Json(name = "description") val description: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<CandidateItem>? = null
)

@JsonClass(generateAdapter = true)
data class CandidateItem(
    @Json(name = "content") val content: ContentItem? = null,
    @Json(name = "finishReason") val finishReason: String? = null
)
