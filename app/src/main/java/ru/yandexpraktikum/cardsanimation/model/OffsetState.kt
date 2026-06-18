package ru.yandexpraktikum.cardsanimation.model

import kotlin.math.abs


class OffsetState {

    var vDragOffset = 0f
        private set
    var hDragOffset = 0f
        private set

    // минимальный порог для обработки свайпа, чтобы не дергалось на каждый чих
    private val threshold = 100f


    fun changeOffset(offsetX: Float, offsetY: Float) {
        if (abs(offsetX) > abs(offsetY)) {
            hDragOffset += offsetX
        } else if (abs(offsetX) < abs(offsetY)) {
            vDragOffset += offsetY
        }
    }

    /**
     * @return `-1` if vertical direction, `1` if horizontal, `0` otherwise
     */
    fun handleDragEnd() =
        when {
            abs(vDragOffset) > abs(hDragOffset) && abs(vDragOffset) > threshold -> -1
            abs(hDragOffset) > abs(vDragOffset) && abs(hDragOffset) > threshold -> 1
            else -> 0
        }

    fun clearState() {
        vDragOffset = 0f
        hDragOffset = 0f
    }
}