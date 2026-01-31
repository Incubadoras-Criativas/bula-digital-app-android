package com.example.app_buladigital

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MedicamentoAdapter(
    private var listaMedicamentos: MutableList<Medicamento>,
    private val onItemClick: (Medicamento) -> Unit // <--- A "ponte" de comunicação
) : RecyclerView.Adapter<MedicamentoAdapter.MedicamentoViewHolder>() {

    // 1. ViewHolder: Referências dos elementos da tela
    class MedicamentoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNome: TextView = view.findViewById(R.id.txtNomeBula)
        val txtVersoes: TextView = view.findViewById(R.id.txtVersoesBula)
        val imgStatus: ImageView = view.findViewById(R.id.imgStatusBula)
    }

    // 2. Cria a linha (infla o XML)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicamentoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicamento, parent, false)
        return MedicamentoViewHolder(view)
    }

    // 3. Alimenta a linha com os dados
    override fun onBindViewHolder(holder: MedicamentoViewHolder, position: Int) {
        val med = listaMedicamentos[position]

        holder.txtNome.text = med.nome
        holder.txtVersoes.text = "Versões disponíveis: ${med.versions}"

        // Lógica visual do ícone
        if (med.present) {
            holder.imgStatus.setImageResource(android.R.drawable.presence_online)
        } else {
            holder.imgStatus.setImageResource(android.R.drawable.presence_invisible)
        }

        //clique na raiz da linha (itemView)
        holder.itemView.setOnClickListener {
            onItemClick(med)
        }
    }

    override fun getItemCount() = listaMedicamentos.size

    // --- FUNÇÕES DE GESTÃO DA LISTA ---

    // Substitui a lista inteira (usado na busca nova)
    fun atualizarDados(novaLista: List<Medicamento>) {
        this.listaMedicamentos.clear()
        this.listaMedicamentos.addAll(novaLista)
        notifyDataSetChanged()
    }

    // Adiciona itens ao final (usado na paginação/scroll infinito)
    fun adicionarDados(maisMedicamentos: List<Medicamento>) {
        val posicaoInicial = listaMedicamentos.size
        this.listaMedicamentos.addAll(maisMedicamentos)
        notifyItemRangeInserted(posicaoInicial, maisMedicamentos.size)
    }

    // Limpa tudo
    fun limparDados() {
        this.listaMedicamentos.clear()
        notifyDataSetChanged()
    }
}