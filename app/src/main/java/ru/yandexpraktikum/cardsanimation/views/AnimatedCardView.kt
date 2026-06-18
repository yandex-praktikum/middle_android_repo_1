package ru.yandexpraktikum.cardsanimation.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.cardview.widget.CardView
import ru.yandexpraktikum.cardsanimation.Helper
import ru.yandexpraktikum.cardsanimation.R
import ru.yandexpraktikum.cardsanimation.model.CardData

class AnimatedCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val cardView: CardView
    private val cardImageView: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.card_view, this, true)

        cardView = this.getChildAt(0) as CardView
        cardImageView = findViewById(R.id.cardImage)

        pivotX = width / 2f
        pivotY = height.toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        pivotX = w / 2f
        pivotY = h.toFloat()
    }

    fun setCardData(cardData: CardData) {
        cardImageView.setImageResource(cardData.imageResId)
    }

    fun setStackPosition(index: Int) {
        cardView.cardElevation = (4 + index * 1).toFloat() * resources.displayMetrics.density
    }

     /**
      * [Задание 1] Анимация поворота карты (для раскрытия/закрытия колоды)
      */
     fun animateToRotation(targetRotation: Float, duration: Long = 300) {
        ObjectAnimator
            .ofFloat(this, View.ROTATION, this.rotation, targetRotation)
            .apply {
                this.duration = duration
            }.start()
     }

     /**
      * [Задание 5] шаг 1: анимация перетасовки карт (нижняя карта двигается вправо)
      */
     fun moveCardRight(onComplete: (() -> Unit)? = null) {
         val (moveX, moveY) = Helper.calculateMove(this.rotation)

         val animatorX = ObjectAnimator.ofFloat(this, "x", x, x - moveX)
         val animatorY = ObjectAnimator.ofFloat(this, "y", y, y + moveY)

         AnimatorSet().apply {
             playTogether(animatorX, animatorY)
             duration = 1_000
             addListener(object : AnimatorListenerAdapter() {
                 override fun onAnimationEnd(animation: Animator) {
                     onComplete?.invoke()
                 }
             })
         }.start()
     }

     /**
      * [Задание 5] шаг 2: анимация выдвижения нижней карты наверх
      */
     fun moveCardToTop(targetElevation: Float, onComplete: (() -> Unit)? = null) {
         val parent = parent as? FrameLayout ?: return
         val (cardWidth, cardHeight) = this.getCardSize()
         val centerX = parent.width / 2f - cardWidth / 2f
         val centerY = parent.height / 2f - cardHeight / 2f

         val animatorX = ObjectAnimator.ofFloat(this, "x", x, centerX)
         val animatorY = ObjectAnimator.ofFloat(this, "y", y, centerY)
         val animatorZ = ObjectAnimator.ofFloat(this, "translationZ", targetElevation - cardView.cardElevation)

         AnimatorSet().apply {
             playTogether(animatorX, animatorY, animatorZ)
             duration = 1_000
             addListener(object : AnimatorListenerAdapter() {
                 override fun onAnimationEnd(animation: Animator) {
                     onComplete?.invoke()
                 }
             })
         }.start()
     }

    /**
     * [Задание 5] шаг 3: Добавьте анимацию перемещения всей колоды карты в желаемую позицию
     */
    fun adjustToFinalPosition(finalRotation: Float, finalStackPos: Int, onComplete: (() -> Unit)? = null) {
        ObjectAnimator.ofFloat(this, "rotation", rotation, finalRotation).apply {
            duration = 1_000
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    setStackPosition(finalStackPos)
                    onComplete?.invoke()
                }
            })
        }.start()
    }
}

/**
 * Вычисление размеров карты с фиксированными параметрами ширины и высоты
 *
 * @return (width, height)
 */
fun View.getCardSize(): Pair<Float, Float> =
    Pair(100f * resources.displayMetrics.density, 160f * resources.displayMetrics.density)
