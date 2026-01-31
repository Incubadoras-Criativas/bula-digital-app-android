package com.example.app_buladigital

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.converter.gson.GsonConverterFactory
import androidx.fragment.app.viewModels

class BulaDetalheFragment : Fragment(R.layout.fragment_bula_detalhe) {

    private lateinit var apiService: ApiService
    private var medicamentoId: Long = -1
    private var fontSizeAtual = 16f

    // ViewModel compartilhado para a fonte (conforme implementamos)
    private val sharedViewModel: BulaViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayoutBula)
        val viewPager = view.findViewById<ViewPager2>(R.id.viewPagerBula)

        // 1. Configura o nome do medicamento no NOVO ID do topo
        val nomeRaw = arguments?.getString("MEDICAMENTO_NOME") ?: "Medicamento"
        view.findViewById<TextView>(R.id.txtTituloTopo).text = nomeRaw.capitalizeWords()

        // 2. Configura o ViewPager
        val adapter = BulaPagerAdapter(this)
        viewPager.adapter = adapter

        // Recupera o ID do medicamento
        medicamentoId = arguments?.getLong("MEDICAMENTO_ID") ?: -1

        configurarApi()

        if (medicamentoId != -1L) {
            buscarListaDeVersoes(medicamentoId)
        }

        // 3. TabLayout + ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Resumo"
                1 -> "Curiosidade"
                2 -> "Bula Original"
                else -> null
            }
        }.attach()

        // 4. Botões de Fonte (agora no SharedViewModel)
        view.findViewById<Button>(R.id.btnAumentarFonte).setOnClickListener {
            if (fontSizeAtual < 30f) {
                fontSizeAtual += 2f
                notificarMudancaFonte()
            }
        }

        view.findViewById<Button>(R.id.btnDiminuirFonte).setOnClickListener {
            if (fontSizeAtual > 12f) {
                fontSizeAtual -= 2f
                notificarMudancaFonte()
            }
        }
    }

    // --- OTIMIZAÇÃO: Atualiza apenas o que mudou ---
    private fun atualizarCabecalho(versao: BulaVersao) {
        // Agora atualizamos apenas a data, pois o laboratório já está destacado no seletor (RecyclerView)
        view?.findViewById<TextView>(R.id.txtDataDetalhe)?.text = "Publicada em: ${versao.date}"
    }

    private fun buscarListaDeVersoes(id: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getVersoes(id)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val listaDeVersoes = response.body() ?: emptyList()

                        if (listaDeVersoes.isNotEmpty()) {
                            val rvVersoes = view?.findViewById<RecyclerView>(R.id.rvVersoesBula)

                            // Usando o VersaoAdapter com a lógica de seleção que criamos
                            val adapterVersoes = VersaoAdapter(listaDeVersoes) { versaoClicada ->
                                // Quando clica: atualiza a data e busca o novo conteúdo
                                atualizarCabecalho(versaoClicada)
                                carregarConteudoDaBula(versaoClicada.bula_id)
                            }
                            rvVersoes?.adapter = adapterVersoes

                            // Carregamento Inicial (Primeiro da lista)
                            val primeiraVersao = listaDeVersoes[0]
                            atualizarCabecalho(primeiraVersao)
                            carregarConteudoDaBula(primeiraVersao.bula_id)

                        } else {
                            exibirMensagemBulaFaltante()
                        }
                    } else {
                        if (response.code() == 404) {
                            exibirMensagemBulaFaltante()
                        } else {
                            // Outros erros (500, 503, etc) podem ser tratados com um Toast genérico
                            Toast.makeText(context, "Erro no servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tratarErroRede(e) {
                        buscarListaDeVersoes(id)
                    }
                }
            }
        }
    }

    private fun carregarConteudoDaBula(bulaId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getDetalhesBula(bulaId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val conteudo = response.body()
                        if (conteudo != null) {
                            // Envia os resultados para as abas (Fragments filhos)
                            val bundleResumo = Bundle().apply { putString("texto", conteudo.resumo) }
                            childFragmentManager.setFragmentResult("chave_resumo", bundleResumo)

                            val bundleCuriosidade = Bundle().apply { putString("texto", conteudo.curiosidades) }
                            childFragmentManager.setFragmentResult("chave_curiosidade", bundleCuriosidade)

                            val bundlePdf = Bundle().apply {
                                putString("url", conteudo.url_busca)
                                putString("pdf_url", conteudo.pdf_url)
                            }
                            childFragmentManager.setFragmentResult("chave_pdf", bundlePdf)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Agora funciona aqui também!
                    tratarErroRede(e) {
                        carregarConteudoDaBula(bulaId)
                    }
                }
                Log.e("API_CONTEUDO", "Erro: ${e.message}")
            }
        }
    }

    private fun configurarApi() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(ApiService::class.java)
    }

    private fun notificarMudancaFonte() {
        sharedViewModel.atualizarFonte(fontSizeAtual)
    }

    private fun exibirMensagemBulaFaltante() {
        if (!isAdded) return
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Bula não encontrada")
        builder.setMessage("Infelizmente, ainda não temos as versões digitais para este medicamento.")
        builder.setPositiveButton("Voltar") { _, _ -> parentFragmentManager.popBackStack() }
        builder.setCancelable(false)
        builder.show()
    }
}

fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }