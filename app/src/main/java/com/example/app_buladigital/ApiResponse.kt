package com.example.app_buladigital

import com.google.gson.annotations.SerializedName

// Data class que mapeia o JSON da API.
// A anotação @SerializedName é útil se o nome da chave na API for diferente do nome da variável em Kotlin.
data class ApiResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("user")
    val user: String?,

    @SerializedName("timer")
    val timer: Double?
)
