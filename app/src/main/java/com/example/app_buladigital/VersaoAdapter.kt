package com.example.app_buladigital

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class VersaoAdapter(
    private val versoes: List<BulaVersao>,
    private val onVersaoClick: (BulaVersao) -> Unit
) : RecyclerView.Adapter<VersaoAdapter.VersaoViewHolder>() {

    // Guarda a posição do item selecionado (começa na primeira versão)
    private var selectedPosition = 0

    inner class VersaoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById(R.id.cardLaboratorio)
        val txtNome: TextView = itemView.findViewById(R.id.txtNomeLaboratorioItem)

        fun bind(versao: BulaVersao, position: Int) {
            txtNome.text = versao.laboratorio

            // Lógica de destaque visual
            if (position == selectedPosition) {
                // Item Selecionado: Fundo Azul, Texto Branco
                card.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.brand_blue_primary))
                txtNome.setTextColor(Color.WHITE)
                card.strokeWidth = 0 // Remove a borda no selecionado
            } else {
                // Item Normal: Fundo Branco, Texto Azul, Borda Azul
                card.setCardBackgroundColor(Color.WHITE)
                txtNome.setTextColor(ContextCompat.getColor(itemView.context, R.color.brand_blue_primary))
                card.strokeWidth = 2
                card.strokeColor = ContextCompat.getColor(itemView.context, R.color.brand_blue_primary)
            }

            itemView.setOnClickListener {
                // Atualiza a posição selecionada e redesenha a lista
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

                // Dispara a ação de carregar a bula
                onVersaoClick(versao)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VersaoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_versao, parent, false)
        return VersaoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VersaoViewHolder, position: Int) {
        holder.bind(versoes[position], position)
    }

    override fun getItemCount(): Int = versoes.size
}