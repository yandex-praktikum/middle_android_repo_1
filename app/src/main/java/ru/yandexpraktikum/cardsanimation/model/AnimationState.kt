package ru.yandexpraktikum.cardsanimation.model

/**
 * Состояние анимации
 *
 * @param isAnimating индикатор, запущена ли анимация
 * @param step 1 = сдвиг, 2 = перевод эл-та вперед, 3 = финальное позиционирование
 * @param currentAnimatingCard id текущей карты для анимации (нужно для анимации в Compose)
 */
data class AnimationState(
    val isAnimating: Boolean = false,
    val step: Int = 0,
    val currentAnimatingCard: Int? = null
)

fun AnimationState.isStep1() = isAnimating && step == 1
fun AnimationState.isStep2() = isAnimating && step == 2
fun AnimationState.isStep3() = isAnimating && step == 3