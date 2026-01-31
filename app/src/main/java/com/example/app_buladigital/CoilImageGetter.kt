package com.example.app_buladigital

import android.graphics.Canvas
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.widget.TextView
import coil.imageLoader
import coil.request.ImageRequest
import android.graphics.Rect

class CoilImageGetter(
    private val textView: TextView
) : Html.ImageGetter {

    override fun getDrawable(source: String): Drawable {
        val drawablePlaceholder = BitmapDrawablePlaceholder()

        val imageLoader = textView.context.imageLoader
        val request = ImageRequest.Builder(textView.context)
            .data(source)
            .target { drawable ->
                val bitmap = (drawable as BitmapDrawable).bitmap
                val width = textView.width - textView.paddingLeft - textView.paddingRight
                val aspectRatio = bitmap.height.toDouble() / bitmap.width.toDouble()
                val height = (width * aspectRatio).toInt()

                val bounds = android.graphics.Rect(0, 0, width, height)
                drawable.bounds = bounds

                // Atualiza o placeholder E define os limites dele
                drawablePlaceholder.setActualDrawable(drawable)
                drawablePlaceholder.bounds = bounds

                // O "Pulo do Gato" para o Cache:
                textView.post {
                    // Forçamos o texto a ser reatribuído.
                    // Isso faz o TextView reprocessar todos os Spans de imagem.
                    val textoAtual = textView.text
                    textView.text = textoAtual

                    textView.requestLayout()
                    textView.invalidate()
                }
            }
            .build()

        imageLoader.enqueue(request)
        return drawablePlaceholder
    }

    // Classe de suporte para o container da imagem
    private class BitmapDrawablePlaceholder : BitmapDrawable() {
        private var actualDrawable: Drawable? = null

        fun setActualDrawable(drawable: Drawable) {
            actualDrawable = drawable
            invalidateSelf()
        }

        override fun draw(canvas: Canvas) {
            actualDrawable?.draw(canvas)
        }

        override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
            super.setBounds(left, top, right, bottom)
            actualDrawable?.setBounds(left, top, right, bottom)
        }
    }
}