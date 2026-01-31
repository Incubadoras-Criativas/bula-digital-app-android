package com.example.app_buladigital

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

class CuriosidadeFragment : Fragment(R.layout.fragment_curiosidade) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Referências dos componentes do XML atualizado
        val txtCuriosidade = view.findViewById<TextView>(R.id.txtCuriosidadeBula)
        val loading = view.findViewById<ProgressBar>(R.id.loadingCuriosidade)
        val txtStatus = view.findViewById<TextView>(R.id.txtStatusCuriosidade)

        // Acessa o ViewModel do "Pai" (BulaDetalheFragment)
        val sharedViewModel: BulaViewModel by viewModels({ requireParentFragment() })

        // OUVINTE 1: Conteúdo da Bula (Texto HTML)
        parentFragmentManager.setFragmentResultListener("chave_curiosidade", viewLifecycleOwner) { _, bundle ->
            val texto = bundle.getString("texto")
            Log.d("DEBUG_ABA", "Texto recebido (Curiosidade)! Tamanho: ${texto?.length}")

            // 2. Preenchimento do texto com suporte a HTML
            txtCuriosidade.text = if (!texto.isNullOrBlank()) {
                HtmlCompat.fromHtml(texto, HtmlCompat.FROM_HTML_MODE_LEGACY)
            } else {
                "Nenhuma curiosidade adicional encontrada para este medicamento."
            }

            // 3. Troca de visibilidade: sai o loading, entra o conteúdo
            loading.visibility = View.GONE
            txtStatus.visibility = View.GONE
            txtCuriosidade.visibility = View.VISIBLE
        }

        // OUVINTE 2: Tamanho da Fonte (Sincronizado via ViewModel)
        sharedViewModel.fontSize.observe(viewLifecycleOwner) { novoTamanho ->
            txtCuriosidade.textSize = novoTamanho
            Log.d("DEBUG_ABA", "Fonte curiosidade atualizada: $novoTamanho")
        }
    }
}