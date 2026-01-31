package com.example.app_buladigital

import android.content.Context
import androidx.activity.enableEdgeToEdge
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
//import retrofit2.http.Body
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson
import java.util.UUID

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment

import coil.load
import androidx.core.view.updatePadding




class MainActivity : AppCompatActivity() {

    private lateinit var userNameTextView: TextView
    private lateinit var mainContentLayout: View
    private lateinit var dimOverlay: View
    private lateinit var errorCard: View // Usamos View aqui de forma genérica para o CardView
    private lateinit var retryButton: Button
    private lateinit var loadingSpinner: ProgressBar
    private lateinit var apiService: ApiService
    private lateinit var bottomNavigation: com.google.android.material.bottomnavigation.BottomNavigationView

    private var cacheWelcome: PingData? = null
    private var cacheAppInfo: AppInfo? = null
    private var currentTextSize = 17f
    private lateinit var contentCard: androidx.cardview.widget.CardView
    private lateinit var dynamicContentContainer: LinearLayout
    private lateinit var mainScrollView: androidx.core.widget.NestedScrollView



    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        userNameTextView = findViewById(R.id.userNameTextView)
        mainContentLayout = findViewById(R.id.mainContentLayout)
        dimOverlay = findViewById(R.id.dimOverlay)
        errorCard = findViewById(R.id.errorCard)
        retryButton = findViewById(R.id.btnTentarNovamente)
        loadingSpinner = findViewById(R.id.loadingSpinner)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        contentCard = findViewById(R.id.contentCard)
        dynamicContentContainer = findViewById(R.id.dynamicContentContainer)
        mainScrollView = findViewById(R.id.contentScrollView)


        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // 1. Aplicamos o padding no TOPO do container (para a barra de notificações)
            v.updatePadding(top = systemBars.top)

            // 2. Aplicamos o padding no FUNDO apenas do Menu
            // Assim, o fundo branco do menu "sangra" até o fim da tela (Edge-to-Edge)
            // mas os ícones ficam protegidos acima dos botões do sistema.
            val navBar = findViewById<View>(R.id.bottomNavigation)
            navBar.setPadding(0, 0, 0, systemBars.bottom)

            insets
        }

        // botões de zoom no texto
        findViewById<TextView>(R.id.btnZoomIn).setOnClickListener {
            if (currentTextSize < 30f) {
                currentTextSize += 2f
                atualizarTamanhoTexto()
            }
        }

        findViewById<TextView>(R.id.btnZoomOut).setOnClickListener {
            if (currentTextSize > 12f) {
                currentTextSize -= 2f
                atualizarTamanhoTexto()
            }
        }

        // Configura o clique no TextView para abrir o formulário

        userNameTextView.setOnClickListener {
            exibirFormularioCadastro()
        }

        // Configura o botão de recarga
        retryButton.setOnClickListener {
            fetchApiData() // Chama a função novamente
        }

        val baseUrl = BuildConfig.BASE_URL
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        ativarFase2(false)
        fetchApiData()

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    ativarFase2(false)
                    fetchApiData()
                    true
                }
                R.id.nav_search -> {
                    // AÇÃO DO BOTÃO BUSCA (Vamos preparar a função a seguir)
                    executarTrocaParaBusca()
                    true
                }
                R.id.nav_about -> {
                    ativarFase2(false)
                    carregarInfoRemota("about") // Simples e direto!
                    true
                }
                R.id.nav_terms -> {
                    ativarFase2(false)
                    carregarInfoRemota("terms") // Reutilizando a mesma lógica!
                    true
                }
                else -> false
            }
        }

        findViewById<Button>(R.id.btnIrParaBusca).setOnClickListener {
            if (bottomNavigation.selectedItemId != R.id.nav_search) {
                bottomNavigation.selectedItemId = R.id.nav_search
            }
        }
    }

    private fun fetchApiData() {

        val uniqueId = getUniqueId(this)
        val deviceData = DeviceData(
            uniqueId = uniqueId,
            manufacturer = android.os.Build.MANUFACTURER,
            model = android.os.Build.MODEL,
            osVersion = android.os.Build.VERSION.SDK_INT
        )

        cacheWelcome?.let {
            exibirDadosHome(it)
            return // Sai da função sem chamar a rede
        }

        // UI: ESTADO CARREGANDO
        loadingSpinner.visibility = View.VISIBLE
        userNameTextView.visibility = View.INVISIBLE
        errorCard.visibility = View.GONE
        dimOverlay.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.pingApiWithData(deviceData)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        cacheWelcome = response.body() // SALVA NO CACHE
                        mainContentLayout.alpha = 1.0f
                        exibirDadosHome(cacheWelcome!!, deveResetarScroll = true)
                    } else {
                        // Se a API responder erro (ex: 404), não deixe a tela em branco!
                        showError("Erro ao carregar dados iniciais (${response.code()})") {
                            fetchApiData()
                        }
                        loadingSpinner.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tratarErroRede(e) {
                        fetchApiData()
                    }
                }
            }
        }
    }

    private fun exibirDadosHome(dados: PingData, deveResetarScroll: Boolean = false) {
        val nome = dados.user ?: "Visitante"
        loadingSpinner.visibility = View.GONE
        userNameTextView.text = "Olá, $nome"
        userNameTextView.visibility = View.VISIBLE
        exibirTextoInformativo("Boas-vindas", dados.welcome_message ?: "", deveResetarScroll)
        findViewById<Button>(R.id.btnIrParaBusca).visibility = View.VISIBLE
    }

    // Função auxiliar para mostrar o erro na tela
    fun showError(mensagem: String, acaoTentarNovamente: (() -> Unit)? = null) {
        // 1. Buscamos os componentes diretamente no layout da Activity
        val cardErro = findViewById<androidx.cardview.widget.CardView>(R.id.errorCard)
        val txtDescricao = findViewById<TextView>(R.id.errorDescriptionTextView)
        val btnTentar = findViewById<Button>(R.id.btnTentarNovamente)

        // 2. Atualizamos a mensagem de erro
        txtDescricao.text = mensagem

        // 3. Tornamos o card visível
        cardErro.visibility = View.VISIBLE

        // 4. Configuramos o clique do botão
        btnTentar.setOnClickListener {
            // Primeiro escondemos o aviso (equivalente ao dismiss)
            cardErro.visibility = View.GONE

            // Depois executamos a ação de tentar novamente, se ela existir
            acaoTentarNovamente?.invoke()
        }
    }

    /**
     * Obtém um ID único para a instalação do aplicativo.
     * Se o ID já existir em SharedPreferences, o retorna.
     * Caso contrário, gera um novo UUID, salva-o e o retorna.
     */
    private fun getUniqueId(context: Context): String {
        // Usamos SharedPreferences para armazenar o ID de forma persistente
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        var uniqueId = prefs.getString("unique_id", null)

        // Se o ID não existe, gera um novo e salva
        if (uniqueId == null) {
            uniqueId = UUID.randomUUID().toString()
            prefs.edit().putString("unique_id", uniqueId).apply()
        }
        return uniqueId
    }

    private fun exibirFormularioCadastro() {
        val uuid = getUniqueId(this)
        loadingSpinner.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getUserData(uuid) // Nova chamada GET

                withContext(Dispatchers.Main) {
                    loadingSpinner.visibility = View.GONE
                    if (response.isSuccessful) {
                        val dadosAtuais = response.body()
                        if ( dadosAtuais?.message == "success") {
                            desenharFormulario(dadosAtuais)
                        } else {
                            showError("Cadastro não localizado!")
                        }
                    } else {
                        showError("Não foi possível recuperar seus dados.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tratarErroRede(e) {
                        exibirFormularioCadastro()
                    }
                }
            }
        }
    }

    private fun desenharFormulario(dados: UserUpdateData?) {


        val btnBusca = findViewById<Button>(R.id.btnIrParaBusca)
        val zoomControls = findViewById<LinearLayout>(R.id.zoomControls)

        dynamicContentContainer.removeAllViews()

        contentCard.visibility = View.VISIBLE
        btnBusca.visibility = View.GONE
        zoomControls.visibility = View.GONE

        // 2. Infla o formulário XML dentro do container principal
        val viewForm = layoutInflater.inflate(R.layout.form_cadastro, dynamicContentContainer, false)
        dynamicContentContainer.addView(viewForm)

        // 3. Referências dos campos de texto (dentro da viewForm)
        val editNome = viewForm.findViewById<EditText>(R.id.editNome)
        val editEmail = viewForm.findViewById<EditText>(R.id.editEmail)
        val editNickname = viewForm.findViewById<EditText>(R.id.editNickname)
        val editPassword = viewForm.findViewById<EditText>(R.id.editPassword)
        val editPasswordConfirm = viewForm.findViewById<EditText>(R.id.editPasswordConfirm)

        // 4. Referências dos controles de senha
        val btnAlterarSenha = viewForm.findViewById<Button>(R.id.btnAlterarSenha)
        val containerSenha = viewForm.findViewById<LinearLayout>(R.id.containerSenha)
        val btnSalvar = viewForm.findViewById<Button>(R.id.btnSalvar)

        // 5. Lógica de Preenchimento (Dados que vieram do Rails)
        // Se o nickname NÃO começar com "tmp-", preenchemos os campos
        val isTemporario: Boolean = dados?.is_temporary ?: true

        if (isTemporario) {
            editNome.setText("")
            editEmail.setText("")
            editNickname.setText("")

            // Em reedição, os campos de senha começam escondidos
            containerSenha.visibility = View.VISIBLE
            btnAlterarSenha.visibility = View.GONE
        } else {
            editNome.setText(dados?.nome)
            editEmail.setText(dados?.email)
            editNickname.setText(dados?.nickname)
            containerSenha.visibility = View.GONE
            btnAlterarSenha.visibility = View.VISIBLE
        }

        // 6. Listener para o botão de expansão da senha
        btnAlterarSenha.setOnClickListener {
            containerSenha.visibility = View.VISIBLE
            btnAlterarSenha.visibility = View.GONE
        }

        // 7. Ação do botão Salvar (coleta os dados e envia para a API)
        btnSalvar.setOnClickListener {
            val nomeStr = editNome.text.toString()
            val emailStr = editEmail.text.toString()
            val nickStr = editNickname.text.toString()

            //Campo data de nascimento removido
            //val nascBr = viewForm.findViewById<EditText>(R.id.editNascimento).text.toString()

            /*
            // converte a string data do padrão PT-BR para o padrão ISO
            val nascIso = if (nascBr.contains("/")) {
                val partes = nascBr.split("/")
                if (partes.size == 3) {
                    // Inverte: Dia/Mes/Ano -> Ano-Mes-Dia
                    "${partes[2]}-${partes[1]}-${partes[0]}"
                } else nascBr
            } else nascBr
             */

            // 2. Lógica condicional para as senhas
            var senha: String? = null
            var senhaConfirm: String? = null

            // Só coletamos a senha se o container estiver visível para o usuário
            if (containerSenha.visibility == View.VISIBLE) {
                senha = editPassword.text.toString()
                senhaConfirm = editPasswordConfirm.text.toString()
            }

            val userData = UserUpdateData(
                message = null,
                is_temporary = false,
                nome = nomeStr,
                email = emailStr,
                nickname = nickStr,
                //nascimento = nascIso, // removido
                password = if (senha.isNullOrBlank()) null else senha,
                password_confirmation = if (senhaConfirm.isNullOrBlank()) null else senhaConfirm
            )

            // Chamamos a função que faz o POST para o Rails
            enviarDadosParaApi(userData)
        }
    }

    private fun enviarDadosParaApi(userData: UserUpdateData) {
        val uuid = getUniqueId(this)

        // LOG DE SEGURANÇA: Se o UUID estiver vazio, o 404 é certo.
        if (uuid.isNullOrBlank()) {
            showError("Erro interno: Identificador do dispositivo não encontrado.")
            return
        }

        loadingSpinner.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {

                val response = apiService.updateUser(uuid, userData)

                withContext(Dispatchers.Main) {
                    loadingSpinner.visibility = View.GONE
                    if (response.isSuccessful) {
                        val apiResponse = response.body()

                        // Verificamos se a mensagem de sucesso do seu Rails está lá
                        if (apiResponse?.message == "success") {
                            // Usamos o nickname confirmado pelo Rails.
                            // Se por algum motivo o Rails não mandar o nickname,
                            // mantemos o que já estava no TextView (sem atualizar precocemente).
                            val nomeConfirmado = apiResponse.nickname

                            if (nomeConfirmado != null) {
                                userNameTextView.text = "Olá, $nomeConfirmado"
                            }

                            dynamicContentContainer.removeAllViews()
                            contentCard.visibility = View.GONE

                            // 3. Feedback visual não persistente (Toast)
                            Toast.makeText(
                                this@MainActivity,
                                "Cadastro atualizado com sucesso!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // Caso o Rails mande um 200 OK mas com message: "fail"
                            showError("Erro ao salvar: ${apiResponse?.message}")
                        }
                    } else {
                        showError("Erro na validação: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Agora funciona aqui também!
                    tratarErroRede(e) {
                        enviarDadosParaApi(userData) // Ou qualquer outra função que falhou
                    }
                }
            }
        }
    }

    private fun showDefaultWelcome() {
        dynamicContentContainer.removeAllViews()

        val tv = TextView(this).apply {
            text = "Bem-vindo ao Bula Digital!"
            textSize = 16f
            setTextColor(android.graphics.Color.parseColor("#333333"))
            gravity = android.view.Gravity.CENTER
            setLineSpacing(0f, 1.3f)
        }

        dynamicContentContainer.addView(tv)
    }

    private fun atualizarTamanhoTexto() {
        //val container = findViewById<LinearLayout>(R.id.dynamicContentContainer)
        for (i in 0 until dynamicContentContainer.childCount) {
            val view = dynamicContentContainer.getChildAt(i)
            if (view is TextView) {
                view.textSize = currentTextSize
            }
        }
    }

    private fun exibirTextoInformativo(titulo: String, htmlConteudo: String, deveResetarScroll: Boolean = false) {

        ativarFase2(false)
        contentCard.visibility = View.VISIBLE
        mainContentLayout.alpha = 1.0f

        dynamicContentContainer.removeAllViews()

        val fatias = fatiarHtml(htmlConteudo)

        fatias.forEach { fatia ->
            when (fatia) {
                is HtmlToText.Text -> {
                    val tv = TextView(this).apply {
                        text = androidx.core.text.HtmlCompat.fromHtml(
                            fatia.html,
                            androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                        textSize = currentTextSize
                        setTextColor(Color.DKGRAY)
                        setLineSpacing(0f, 1.3f)
                        setPadding(0, 10, 0, 10)
                    }
                    dynamicContentContainer.addView(tv)
                }
                is HtmlToText.Image -> {
                    // 1. A Imagem
                    val iv = android.widget.ImageView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { setMargins(0, 30, 0, 0) }
                        adjustViewBounds = true
                    }
                    dynamicContentContainer.addView(iv)

                    // Carrega com Coil
                    iv.load(fatia.url)

                    // 2. A Legenda (se existir)
                    fatia.legenda?.let { textoLegenda ->
                        val tvLegenda = TextView(this).apply {
                            text = textoLegenda
                            textSize = currentTextSize * 0.8f // 20% menor que o texto principal
                            setTextColor(Color.GRAY)
                            gravity = android.view.Gravity.CENTER
                            setTypeface(null, android.graphics.Typeface.ITALIC)
                            setPadding(0, 10, 0, 40)
                        }
                        dynamicContentContainer.addView(tvLegenda)
                    }
                }
            }
        }
        if (deveResetarScroll) {
            resetarScrollParaOTopo()
        }
    }

    private fun executarTrocaParaBusca() {
        // 1. Alterna a visibilidade dos containers (Fase 1 vs Fase 2)
        ativarFase2(true)

        // 2. Transação de Fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_container, SearchFragment())
            .commit()
    }

    private fun carregarInfoRemota(tipo: String) {
        cacheAppInfo?.let {
            processarExibicaoInfo(tipo, it, false)
            return
        }

        //Se não tem cache, busca na rede
        loadingSpinner.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val response = apiService.getAppInfo()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    cacheAppInfo = response.body() // SALVA NO CACHE
                    processarExibicaoInfo(tipo, cacheAppInfo!!, true)
                }
                loadingSpinner.visibility = View.GONE
            }
        }
    }

    private fun processarExibicaoInfo(tipo: String, info: AppInfo, scrollPos: Boolean = false) {
        when (tipo) {
            "about" -> exibirTextoInformativo("Sobre o App", info.about_text ?: "", scrollPos)
            "terms" -> exibirTextoInformativo("Termos de Uso", info.terms_text ?: "", scrollPos)
        }
    }

    private fun resetarScrollParaOTopo() {
        // O post garante que o scroll ocorra após o layout ser desenhado
        mainScrollView.post {
            mainScrollView.fullScroll(View.FOCUS_UP)
        }
    }

    private fun ativarFase2(ativar: Boolean) {
        val fragmentContainer = findViewById<View>(R.id.main_fragment_container)
        val zoomControls = findViewById<View>(R.id.zoomControls)

        if (ativar) {
            // FASE 2: Mostra Fragment, esconde o ScrollView linear
            mainScrollView.visibility = View.GONE
            fragmentContainer.visibility = View.VISIBLE
            zoomControls.visibility = View.GONE // Geralmente busca não precisa de zoom de texto
            findViewById<Button>(R.id.btnIrParaBusca).visibility = View.GONE
        } else {
            // FASE 1: Mostra ScrollView linear, esconde o Fragment
            mainScrollView.visibility = View.VISIBLE
            fragmentContainer.visibility = View.GONE
            zoomControls.visibility = View.VISIBLE

            // Remove o fragment atual para liberar memória ao voltar para a Fase 1
            supportFragmentManager.findFragmentById(R.id.main_fragment_container)?.let {
                supportFragmentManager.beginTransaction().remove(it).commit()
            }
        }
    }


}
