package com.example.app_buladigital

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class BulaPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    // 1. Define a quantidade de abas (Resumo, Curiosidade, PDF)
    override fun getItemCount(): Int = 3

    // 2. Define qual fragmento será criado para cada posição
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ResumoFragment()
            1 -> CuriosidadeFragment()
            2 -> PdfFragment()
            else -> ResumoFragment() // Fallback de segurança
        }
    }
}