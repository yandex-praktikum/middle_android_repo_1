package ru.yandexpraktikum.cardsanimation

import kotlin.math.cos
import kotlin.math.sin

/**
 * Класс-хэлпер для схожих у двух разных визуальных компонент (Compose и View)
 */
object Helper {

    /**
     * Метод для вычисления поворота карты в конкретной позиции
     */
    fun calculateCardRotation(
        cardIndex: Int,
        cardCount: Int,
        isRotated: Boolean
    ): Float {
        if (cardCount <= 1) return 0f

        return if (isRotated) {
            // Расчёт финальной позиции (для эффекта раскрытой колоды карт)
            val angleStep = 180f / (cardCount - 1)
            90f - (cardIndex * angleStep)
        } else {
            // Расчёт расположения карт в исходной позиции
            val angleStep = 45f / (cardCount - 1)
            22.5f - (cardIndex * angleStep)
        }
    }

    /**
     * Для [Задание 5]. Подсчет смещения элемента.
     * Возвращает пару [вертикальное смещение, горизонтальное смещение]
     *
     * @return pair [deltaX, deltaY]
     */
    fun calculateMove(rotation: Float) =
        Pair(
            150 * cos(rotation),
            150 * sin(rotation)
        )

}
