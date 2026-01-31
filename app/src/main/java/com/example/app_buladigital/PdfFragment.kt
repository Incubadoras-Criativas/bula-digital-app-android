package com.example.app_buladigital

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class PdfFragment : Fragment(R.layout.fragment_pdf) {

    private var urlRecebida: String? = null
    private var urlBackup: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnAbrir = view.findViewById<Button>(R.id.btnAbrirAnvisa) // Nome corrigido aqui
        val txtUrl = view.findViewById<TextView>(R.id.txtUrlAnvisa)
        val btnBackup = view.findViewById<Button>(R.id.btnAbrirBackup)

        view.findViewById<Button>(R.id.btnAbrirAnvisa).setOnClickListener {
            // No futuro, pegaremos o nome do medicamento dinamicamente
            val url = "https://consultas.anvisa.gov.br/#/bula/"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        parentFragmentManager.setFragmentResultListener("chave_pdf", viewLifecycleOwner) { _, bundle ->
            urlRecebida = bundle.getString("url")
            val url = bundle.getString("url")
            urlBackup = bundle.getString("pdf_url")
            urlRecebida = url

            // LOG PARA VER NO LOGCAT:
            android.util.Log.d("API_REDE", "URL Anvisa: $urlRecebida")
            android.util.Log.d("API_REDE", "URL Backup (Rails): $urlBackup")

            // Agora você verá exatamente o que o Rails enviou na tela do celular
            txtUrl.text = url ?: "Link não recebido da API"

            if (!urlBackup.isNullOrEmpty()) {
                btnBackup.visibility = View.VISIBLE
            }
        }

        btnAbrir.setOnClickListener {
            urlRecebida?.trim()?.let { urlLimpa ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(urlLimpa))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Não foi possível abrir este link", Toast.LENGTH_SHORT).show()
                }
            }
        }
        btnBackup.setOnClickListener {
            android.util.Log.d("CLIQUE", "Botão Backup clicado! Abrindo: $urlBackup")
            abrirLink(urlBackup)
        }
    }

    private fun abrirLink(url: String?) {
        url?.let {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            startActivity(intent)
        } ?: Toast.makeText(context, "Link não disponível", Toast.LENGTH_SHORT).show()
    }
}


