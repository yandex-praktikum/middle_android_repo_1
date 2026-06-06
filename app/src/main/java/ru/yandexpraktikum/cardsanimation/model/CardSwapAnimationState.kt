package ru.yandexpraktikum.cardsanimation.model

import androidx.compose.runtime.Immutable

@Immutable
data class CardSwapAnimationState(
    val isAnimating: Boolean = false,
    val animationStep: AnimationStep = AnimationStep.INITIAL
)