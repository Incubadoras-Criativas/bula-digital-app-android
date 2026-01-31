package com.example.app_buladigital

import com.google.gson.annotations.SerializedName

data class DeviceData (
    @SerializedName("unique_id")
    val uniqueId: String,
    @SerializedName("manufacturer")
    val manufacturer: String,
    @SerializedName("model")
    val model: String,
    @SerializedName("os_version")
    val osVersion: Int
)