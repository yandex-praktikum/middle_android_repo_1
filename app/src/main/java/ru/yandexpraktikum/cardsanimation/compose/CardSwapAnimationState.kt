package ru.yandexpraktikum.cardsanimation.compose

data class CardSwapAnimationState(
    val isAnimating: Boolean = false,
    val animationStep: Int = 0
)