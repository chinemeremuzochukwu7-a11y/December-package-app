package com.example.model

enum class WishCategory(val displayName: String) {
    ALL("All"),
    CHRISTMAS("Christmas"),
    NEW_YEAR("New Year"),
    FAMILY("Family"),
    FRIENDS("Friends"),
    LOVE("Love"),
    BIRTHDAY("Birthday"),
    RELIGIOUS("Religious"),
    THANK_YOU("Thank You"),
    BUSINESS("Business"),
    GENERAL("General")
}

data class Wish(
    val id: String,
    val text: String,
    val category: WishCategory,
    val occasion: String = "Holiday",
    val isFavorite: Boolean = false,
    val isPopular: Boolean = false
)
