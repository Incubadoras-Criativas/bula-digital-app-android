package com.example.app_buladigital

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.widget.TextView


class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var apiService: ApiService
    private lateinit var adapter: MedicamentoAdapter
    private lateinit var rvResultados: RecyclerView

    //controle de paginação
    private var paginaAtual = 0
    private var carregando = false
    private var termoBuscaAtual = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Inicializa a API
        configurarApi()

        // Configura o RecyclerView
        rvResultados = view.findViewById(R.id.rvBulasResult)
        val layoutManager = LinearLayoutManager(context)
        rvResultados.layoutManager = layoutManager


        buscarNaApi("", 0)

        // Detecta o fim do scroll
        rvResultados.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // dy > 0 significa que o usuário está scrollando para baixo
                if (dy > 0) {
                    val itensVisiveis = layoutManager.childCount
                    val totalItens = layoutManager.itemCount
                    val primeiroItemVisivel = layoutManager.findFirstVisibleItemPosition()

                    // Se não estiver carregando e chegamos perto do fim (ex: faltam 2 itens)
                    if (!carregando) {
                        if ((itensVisiveis + primeiroItemVisivel) >= totalItens - 2) {
                            carregarProximaPagina()
                        }
                    }
                }
            }
        })

        val editSearch = view.findViewById<EditText>(R.id.editSearchBula)

        editSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val texto = s.toString()
                if (texto != termoBuscaAtual) {
                    termoBuscaAtual = texto
                    resetarEBuscar(texto) // Se mudou o termo, volta para página 0
                }
                if (texto.isEmpty()) {
                    // Se o usuário apagar tudo, voltamos para a lista inicial
                    buscarNaApi("", 0)
                } else if (texto.length >= 3) {
                    buscarNaApi(texto, 0)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        adapter = MedicamentoAdapter(mutableListOf()) { medicamento ->
            // Passamos o ID e o Nome para a função que navega
            abrirDetalhesDaBula(medicamento.id, medicamento.nome)
        }
        rvResultados.adapter = adapter
    }

    private fun buscarNaApi(query: String, pagina: Int) {
        if (carregando) return
        carregando = true

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.buscarMedicamentos(query, pagina)
                withContext(Dispatchers.Main) {
                    carregando = false
                    if (response.isSuccessful) {
                        val medicamentos = response.body() ?: emptyList()
                        if (medicamentos.isNotEmpty()) {
                            // IMPORTANTE: Adiciona à lista existente
                            adapter.adicionarDados(medicamentos)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Agora funciona aqui também!
                    tratarErroRede(e) {
                        buscarNaApi(query, pagina)
                    }
                }
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

    private fun carregarProximaPagina() {
        paginaAtual++
        buscarNaApi(termoBuscaAtual, paginaAtual)
    }

    private fun resetarEBuscar(query: String) {
        paginaAtual = 0
        // No reset, precisamos limpar a lista atual do adapter antes de buscar
        adapter.limparDados()
        buscarNaApi(query, 0)
    }

    // Função para navegar
    private fun abrirDetalhes(medicamentoId: Long) {
        val fragment = BulaDetalheFragment()

        // Passamos o ID como "argumento" para o novo fragmento
        val bundle = Bundle()
        bundle.putLong("MEDICAMENTO_ID", medicamentoId)
        fragment.arguments = bundle

        // Transação de Fragmento para trocar a tela
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_container, fragment)
            .addToBackStack(null) // Permite que o botão "voltar" funcione
            .commit()
    }

    private fun abrirDetalhesDaBula(idMedicamento: Long, nomeMedicamento: String) {
        // 1. Criamos a instância do novo fragmento de abas que fizemos
        val fragmentDetalhe = BulaDetalheFragment()

        // 2. Criamos um "pacote" (Bundle) para enviar o ID
        val args = Bundle()
        args.putLong("MEDICAMENTO_ID", idMedicamento)
        args.putString("MEDICAMENTO_NOME", nomeMedicamento)
        fragmentDetalhe.arguments = args

        // 3. Realizamos a transação de fragmento
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_container, fragmentDetalhe) // Troca a busca pelo detalhe
            .addToBackStack(null) // PERMITE VOLTAR: Se o usuário apertar 'voltar', ele retorna à busca
            .commit()
    }
}