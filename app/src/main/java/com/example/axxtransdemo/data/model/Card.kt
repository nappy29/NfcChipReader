package com.example.axxtransdemo.data.model

data class Card (
    var pan: String? = null,
    var expireDate: String? = null,
    var cardType: CardType? = null,
    var track2: String? = null,
    var emvData: String? = null,
//    var logMessages: List<LogMessage>? = null
)