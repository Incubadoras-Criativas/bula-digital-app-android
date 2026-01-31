package com.example.app_buladigital

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

class ResumoFragment : Fragment(R.layout.fragment_resumo) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Referências dos novos componentes do XML
        val txtResumo = view.findViewById<TextView>(R.id.txtResumoBula)
        val loading = view.findViewById<ProgressBar>(R.id.loadingResumo)
        val txtStatus = view.findViewById<TextView>(R.id.txtStatusResumo)

        val sharedViewModel: BulaViewModel by viewModels({ requireParentFragment() })

        // OUVINTE 1: Conteúdo da Bula (Texto HTML)
        parentFragmentManager.setFragmentResultListener("chave_resumo", viewLifecycleOwner) { _, bundle ->
            val texto = bundle.getString("texto")
            Log.d("DEBUG_ABA", "Texto recebido! Tamanho: ${texto?.length}")

            // 2. Injeta o texto
            txtResumo.text = if (!texto.isNullOrBlank()) {
                HtmlCompat.fromHtml(texto, HtmlCompat.FROM_HTML_MODE_LEGACY)
            } else {
                "Conteúdo não disponível para este medicamento."
            }

            // 3. A MÁGICA: Esconde o loading e mostra o texto
            loading.visibility = View.GONE
            txtStatus.visibility = View.GONE
            txtResumo.visibility = View.VISIBLE
        }

        // OUVINTE 2: Tamanho da Fonte
        sharedViewModel.fontSize.observe(viewLifecycleOwner) { novoTamanho ->
            txtResumo.textSize = novoTamanho
            Log.d("DEBUG_ABA", "Fonte atualizada via ViewModel: $novoTamanho")
        }
    }
}