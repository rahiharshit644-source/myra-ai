package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @param:Json(name = "contents") val contents: List<ContentItem>,
    @param:Json(name = "systemInstruction") val systemInstruction: ContentItem? = null,
    @param:Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @param:Json(name = "tools") val tools: List<ToolDefinition>? = null
)

@JsonClass(generateAdapter = true)
data class ContentItem(
    @param:Json(name = "role") val role: String? = null,
    @param:Json(name = "parts") val parts: List<PartItem>
)

@JsonClass(generateAdapter = true)
data class PartItem(
    @param:Json(name = "text") val text: String? = null,
    @param:Json(name = "functionCall") val functionCall: FunctionCallItem? = null,
    @param:Json(name = "functionResponse") val functionResponse: FunctionResponseItem? = null
)

@JsonClass(generateAdapter = true)
data class FunctionCallItem(
    @param:Json(name = "name") val name: String,
    @param:Json(name = "args") val args: Map<String, Any>? = null
)

@JsonClass(generateAdapter = true)
data class FunctionResponseItem(
    @param:Json(name = "name") val name: String,
    @param:Json(name = "response") val response: Map<String, Any>
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @param:Json(name = "temperature") val temperature: Float? = 0.85f,
    @param:Json(name = "topP") val topP: Float? = 0.95f,
    @param:Json(name = "topK") val topK: Int? = 40,
    @param:Json(name = "maxOutputTokens") val maxOutputTokens: Int? = 300
)

@JsonClass(generateAdapter = true)
data class ToolDefinition(
    @param:Json(name = "functionDeclarations") val functionDeclarations: List<FunctionDeclaration>
)

@JsonClass(generateAdapter = true)
data class FunctionDeclaration(
    @param:Json(name = "name") val name: String,
    @param:Json(name = "description") val description: String,
    @param:Json(name = "parameters") val parameters: SchemaDefinition? = null
)

@JsonClass(generateAdapter = true)
data class SchemaDefinition(
    @param:Json(name = "type") val type: String = "OBJECT",
    @param:Json(name = "properties") val properties: Map<String, PropertyDefinition>? = null,
    @param:Json(name = "required") val required: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class PropertyDefinition(
    @param:Json(name = "type") val type: String,
    @param:Json(name = "description") val description: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @param:Json(name = "candidates") val candidates: List<CandidateItem>? = null
)

@JsonClass(generateAdapter = true)
data class CandidateItem(
    @param:Json(name = "content") val content: ContentItem? = null,
    @param:Json(name = "finishReason") val finishReason: String? = null
)

