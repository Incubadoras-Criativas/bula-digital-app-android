package com.example.app_buladigital

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BulaViewModel : ViewModel() {
    // LiveData que armazena o tamanho da fonte
    private val _fontSize = MutableLiveData<Float>(16f)
    val fontSize: LiveData<Float> get() = _fontSize

    fun atualizarFonte(novoTamanho: Float) {
        _fontSize.value = novoTamanho
    }
}