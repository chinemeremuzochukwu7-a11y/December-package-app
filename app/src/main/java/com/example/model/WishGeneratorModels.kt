package com.example.model

enum class GeneratorOccasion(val label: String) {
    CHRISTMAS("Christmas"),
    NEW_YEAR("New Year"),
    HOLIDAY_SEASON("Holiday Season"),
    BIRTHDAY("Holiday Birthday"),
    THANK_YOU("Gratitude & Thanks")
}

enum class GeneratorRecipient(val label: String) {
    FAMILY("Family"),
    PARENTS("Parents"),
    PARTNER("Partner / Spouse"),
    FRIEND("Friend"),
    COLLEAGUE("Colleague"),
    BOSS("Client / Boss"),
    TEACHER("Teacher"),
    EVERYONE("Everyone")
}

enum class GeneratorRelationship(val label: String) {
    CLOSE("Close & Loving"),
    WARM("Warm & Caring"),
    PROFESSIONAL("Professional"),
    CASUAL("Casual"),
    RELIGIOUS("Faith-Filled")
}

enum class GeneratorTone(val label: String) {
    HEARTFELT("Heartfelt"),
    JOYFUL("Joyful & Festive"),
    FUNNY("Lighthearted & Fun"),
    INSPIRATIONAL("Inspirational"),
    SHORT_SWEET("Short & Sweet"),
    POETIC("Poetic & Elegant")
}

data class WishGenerationResult(
    val id: String,
    val wishText: String,
    val occasion: GeneratorOccasion,
    val recipient: GeneratorRecipient,
    val tone: GeneratorTone
)
