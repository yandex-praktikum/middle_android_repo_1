package ru.yandexpraktikum.cardsanimation

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.views.AnimatedCardStackView

class XmlViewActivity : AppCompatActivity() {
    
    private lateinit var cardStackView: AnimatedCardStackView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_xml)
        enableEdgeToEdge()
        cardStackView = findViewById(R.id.cardStackView)
        setupCustomCards()
    }
    
    private fun setupCustomCards() {
        val customCards = listOf(
            CardData(R.drawable.card_clover),
            CardData(R.drawable.card_hearts), 
            CardData(R.drawable.card_spades),
            CardData(R.drawable.card_diamond)
        )
        cardStackView.setCardsData(customCards)
    }
}

fun Float.dp(context: Context) = this * context.resources.displayMetrics.density
