package com.example.app_buladigital

// Em um novo arquivo chamado UiUtils.kt
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity


fun Fragment.tratarErroRede(
    e: Exception,
    loadingView: View? = null,
    acaoTentarNovamente: (() -> Unit)? = null // Novo parâmetro opcional
) {
    val spinner = loadingView ?: requireActivity().findViewById(R.id.loadingSpinner)
    spinner?.visibility = View.GONE
    android.util.Log.e("API_ERROR", "Erro: ${e.message}")

    // Passamos a ação adiante para a MainActivity
    (requireActivity() as? MainActivity)?.showError(
        "Falha na conexão ou erro interno.",
        acaoTentarNovamente
    )
}

fun AppCompatActivity.tratarErroRede(e: Exception, loadingView: View? = null, acaoTentarNovamente: (() -> Unit)? = null) {
    // Na Activity, usamos o findViewById diretamente
    val spinner = loadingView ?: findViewById(R.id.loadingSpinner)
    spinner?.visibility = View.GONE

    android.util.Log.e("API_ERROR", "Erro na Activity: ${e.message}")

    // Se for a MainActivity, chamamos o showError dela
    if (this is MainActivity) {
        this.showError("Falha na conexão ou erro interno.", acaoTentarNovamente)
    }
}