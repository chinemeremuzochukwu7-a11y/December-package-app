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

data class AiWishRequest(
    val occasion: AiOccasion,
    val recipient: AiRecipient,
    val recipientName: String = "",
    val tone: AiTone,
    val language: AiLanguage = AiLanguage.ENGLISH,
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
