package ru.yandexpraktikum.cardsanimation.model

import androidx.compose.runtime.Immutable

@Immutable
enum class AnimationStep {
    MOVE_TO_RIGHT,
    MOVE_TO_CENTER,
    MOVE_TO_START_POSITION;

    fun shouldBringToFront(): Boolean = this == MOVE_TO_CENTER || this == MOVE_TO_START_POSITION
}