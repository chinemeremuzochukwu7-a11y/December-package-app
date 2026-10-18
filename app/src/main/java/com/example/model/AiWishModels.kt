package com.example.model

enum class AiOccasion(val displayName: String, val iconEmoji: String) {
    CHRISTMAS("Christmas", "🎄"),
    NEW_YEAR("New Year", "🎉"),
    BIRTHDAY("Birthday", "🎂"),
    WEDDING("Wedding", "💍"),
    ANNIVERSARY("Anniversary", "💖"),
    FAMILY("Family", "🏡"),
    FRIENDSHIP("Friendship", "🤝"),
    LOVE("Love", "❤️"),
    THANK_YOU("Thank You", "🙏"),
    RELIGIOUS("Religious", "✝️"),
    BUSINESS("Business", "💼"),
    CONGRATULATIONS("Congratulations", "🎊"),
    GENERAL("General", "✨")
}

enum class AiRecipient(val displayName: String) {
    MOTHER("Mother"),
    FATHER("Father"),
    WIFE("Wife"),
    HUSBAND("Husband"),
    SON("Son"),
    DAUGHTER("Daughter"),
    BROTHER("Brother"),
    SISTER("Sister"),
    FRIEND("Friend"),
    PARTNER("Partner"),
    CUSTOMER("Customer"),
    CLIENT("Client"),
    COLLEAGUE("Colleague"),
    FAMILY("Family"),
    OTHER("Other")
}

enum class AiTone(val displayName: String) {
    WARM("Warm"),
    LOVING("Loving"),
    FUNNY("Funny"),
    EMOTIONAL("Emotional"),
    INSPIRATIONAL("Inspirational"),
    PROFESSIONAL("Professional"),
    SHORT("Short"),
    PRAYERFUL("Prayerful")
}

enum class AiLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    FRENCH("fr", "French", "Français"),
    SPANISH("es", "Spanish", "Español"),
    PORTUGUESE("pt", "Portuguese", "Português")
}

enum class AiWishLength(
    val displayName: String,
    val wordRange: String,
    val creditCost: Int,
    val description: String
) {
    SHORT("Short & Sweet", "15-30 words", 5, "Quick status, SMS, or card greeting"),
    MEDIUM("Standard Warm", "40-75 words", 10, "Balanced, heartfelt paragraphs"),
    LONG("Deep & Detailed", "90-150 words", 15, "Rich, expressive storytelling"),
    POETIC("Epic & Poetic", "160-250 words", 20, "Grand celebration with poetic blessings")
}

data class AiWishRequest(
    val occasion: AiOccasion,
    val recipient: AiRecipient,
    val recipientName: String = "",
    val tone: AiTone,
    val language: AiLanguage = AiLanguage.ENGLISH,
    val length: AiWishLength = AiWishLength.MEDIUM,
    val personalDetails: String = "",
    val variationSeed: Long = System.currentTimeMillis()
)

data class AiWishResponse(
    val id: String,
    val text: String,
    val occasion: AiOccasion,
    val recipient: AiRecipient,
    val recipientName: String,
    val tone: AiTone,
    val language: AiLanguage,
    val createdAt: Long = System.currentTimeMillis()
)
