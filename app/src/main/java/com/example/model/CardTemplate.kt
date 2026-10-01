package com.example.model

enum class CardOccasion(val label: String) {
    CHRISTMAS("Christmas"),
    NEW_YEAR("New Year"),
    HOLIDAY_SEASON("Holiday Season")
}

enum class CardDecorationStyle(val displayName: String) {
    CHRISTMAS_TREE("Christmas Tree"),
    SNOWFLAKES("Snowflakes"),
    STARS("Stars"),
    GIFT_BOXES("Gift Boxes"),
    ORNAMENTS("Ornaments"),
    BELLS("Holiday Bells"),
    SANTA_MAGIC("Santa's Cheer"),
    FIREWORKS("Fireworks"),
    CONFETTI("Festive Confetti"),
    YEAR_2027("2027 Celebration")
}

data class CardTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val defaultMessage: String,
    val occasion: CardOccasion,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val accentColorHex: Long,
    val badge: String = "Featured",
    val decorationStyle: CardDecorationStyle = CardDecorationStyle.STARS,
    val isPro: Boolean = false
)

data class CustomCardData(
    val templateId: String,
    val title: String,
    val recipientName: String,
    val message: String,
    val senderName: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val accentColorHex: Long,
    val decorationStyle: CardDecorationStyle = CardDecorationStyle.STARS,
    val textSizeSp: Float = 15f,
    val textAlign: String = "Center",
    val showRecipient: Boolean = true,
    val showSender: Boolean = true
)
