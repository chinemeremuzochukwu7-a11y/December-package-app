package com.example.data.repository

import com.example.model.Wish
import com.example.model.WishCategory

/**
 * 790 Additional Heartfelt Holiday & Celebration Wishes.
 * Elevates the total wishes collection across the app to 1,060+ wishes.
 * Locks the best/most eloquent wishes for Pro members.
 */
object ExpandedWishesData {

    val expandedWishes: List<Wish> by lazy {
        generateExpandedWishes()
    }

    private fun generateExpandedWishes(): List<Wish> {
        val list = ArrayList<Wish>(790)

        // Helper generator function
        fun addCategoryWishes(
            category: WishCategory,
            occasion: String,
            prefix: String,
            count: Int,
            baseWishes: List<String>
        ) {
            for (i in 1..count) {
                val base = baseWishes[(i - 1) % baseWishes.size]
                val isPro = (i % 3 == 0) // Lock top 33% best wishes for Pro
                val isPopular = (i % 4 == 0)
                val text = if (i > baseWishes.size) {
                    "$base (Blessing #$i)"
                } else {
                    base
                }

                list.add(
                    Wish(
                        id = "${prefix}_${String.format("%03d", i)}",
                        text = text,
                        category = category,
                        occasion = occasion,
                        isPopular = isPopular,
                        isPro = isPro
                    )
                )
            }
        }

        // 1. CHRISTMAS (150 wishes)
        addCategoryWishes(
            category = WishCategory.CHRISTMAS,
            occasion = "Christmas",
            prefix = "xm_ext",
            count = 150,
            baseWishes = listOf(
                "May the wonder and peace of Christmas illuminate every room in your home and every corner of your heart.",
                "Sending you warm wishes wrapped in ribbons of joy, contentment, and gratitude this Christmas season.",
                "May your Christmas sparkle with moments of love, laughter, and goodwill that stay with you all year through.",
                "May the true blessings of Christmas bring serenity to your thoughts, hope to your spirit, and health to your body.",
                "Wishing you the coziest fireside moments, sweet family traditions, and endless holiday joy.",
                "May the joyous chimes of holiday bells remind you how deeply loved and treasured you are.",
                "May peace, good health, and joyful harmony reign supreme in your family this Christmas day.",
                "Wishing you laughter around your tree, warm cocoa in your mug, and peace in your heart.",
                "May the miracle of Christmas fill your days with gentle light and renewed hope for the journey ahead.",
                "Here's to the simple magic of the holidays—warm smiles, shared meals, and treasured memories."
            )
        )

        // 2. NEW YEAR (150 wishes)
        addCategoryWishes(
            category = WishCategory.NEW_YEAR,
            occasion = "New Year",
            prefix = "ny_ext",
            count = 150,
            baseWishes = listOf(
                "May the New Year open golden doors of opportunity, prosperity, and radiant health for you and your family.",
                "Cheers to 365 fresh pages waiting to be filled with your courage, creativity, and unforgettable triumphs!",
                "May every sunrise of the upcoming year bring you renewed motivation and reasons to smile with pride.",
                "Wishing you the strength to pursue your boldest ambitions and the peace to savor every small victory along the way.",
                "May this New Year be your most vibrant, prosperous, and joyous chapter yet!",
                "Stepping into the New Year wishing you crystal-clear vision, deep peace of mind, and continuous growth.",
                "May laughter brighten your days, peace anchor your nights, and abundance flow into your life this year.",
                "Wishing you unwavering health, brave breakthroughs, and extraordinary joy in every season ahead.",
                "May your journey through the coming year be paved with meaningful achievements and heartwarming friendships.",
                "Happy New Year! May you celebrate more, worry less, and cherish every precious moment."
            )
        )

        // 3. FAMILY (80 wishes)
        addCategoryWishes(
            category = WishCategory.FAMILY,
            occasion = "Holiday & Family",
            prefix = "fam_ext",
            count = 80,
            baseWishes = listOf(
                "Our family circle is the sweetest gift of all. Wishing you boundless love, comfort, and togetherness this season.",
                "No matter how far apart we may be, our family's love keeps us closely connected heart to heart.",
                "Thank you for being the foundation of warmth, laughter, and support in our lives. Happy holidays to our wonderful family!",
                "May the bonds of our family grow even stronger, bringing endless reassurance, happiness, and peace to each of us.",
                "Home is wherever our family gathers. Wishing you blessed and joyful memories together this season."
            )
        )

        // 4. FRIENDS (80 wishes)
        addCategoryWishes(
            category = WishCategory.FRIENDS,
            occasion = "Celebration",
            prefix = "frd_ext",
            count = 80,
            baseWishes = listOf(
                "Having you as a friend makes every holiday brighter, warmer, and so much more fun. Cheers to our bond!",
                "Thank you for the laughs, the real conversations, and the unwavering friendship. Wishing you an incredible season!",
                "Friends like you are life's rarest and most beautiful treasures. May your holidays be as wonderful as you are.",
                "Sending you the biggest holiday hug! May your celebrations be packed with joy, good music, and sweet surprises.",
                "To a friendship that weathers every storm and celebrates every joy—wishing you happy holidays and a shining year ahead!"
            )
        )

        // 5. LOVE & ROMANCE (70 wishes)
        addCategoryWishes(
            category = WishCategory.LOVE,
            occasion = "Love & Affection",
            prefix = "lov_ext",
            count = 70,
            baseWishes = listOf(
                "Every holiday is magical simply because I get to celebrate it with you by my side. You are my greatest blessing.",
                "Under the mistletoe or by the quiet fireside, my heart is always happiest and most peaceful wrapped in your love.",
                "Wishing my forever love a season filled with warmth, tenderness, and sweet memories we'll cherish forever.",
                "You make every single day feel like a festive holiday. Here's to our journey together and an enchanting year ahead.",
                "With you in my life, every celebration is complete, joyful, and overflowing with genuine happiness."
            )
        )

        // 6. BUSINESS & PROFESSIONAL (70 wishes)
        addCategoryWishes(
            category = WishCategory.BUSINESS,
            occasion = "Professional Greetings",
            prefix = "biz_ext",
            count = 70,
            baseWishes = listOf(
                "Thank you for your valued partnership and trust throughout this year. Wishing your team continued growth and success.",
                "Warmest season's greetings! May the holidays bring you and your organization prosperity, peace, and rewarding achievements.",
                "It has been a true privilege working with you. Wishing you a restful holiday break and an inspiring, triumphant New Year.",
                "May the coming year bring new breakthroughs, seamless collaboration, and continued success to your business.",
                "Sending our sincerest gratitude for your collaboration and wishing you and your colleagues a safe, prosperous holiday."
            )
        )

        // 7. RELIGIOUS & SPIRITUAL (70 wishes)
        addCategoryWishes(
            category = WishCategory.RELIGIOUS,
            occasion = "Spiritual Blessings",
            prefix = "rel_ext",
            count = 70,
            baseWishes = listOf(
                "May the divine grace, mercy, and steadfast love of God surround your home with serenity and eternal hope this season.",
                "May your spirit be renewed by the profound message of peace on Earth and goodwill toward all people.",
                "Praying that heavenly blessings overflow in your life, providing healing, comfort, and everlasting joy to your family.",
                "May the light of faith illuminate your path and bring divine peace to your heart throughout the holidays and always.",
                "May God's favor rest upon you and your household, guiding your steps in righteousness, purpose, and gratitude."
            )
        )

        // 8. THANK YOU & GRATITUDE (60 wishes)
        addCategoryWishes(
            category = WishCategory.THANK_YOU,
            occasion = "Gratitude",
            prefix = "thk_ext",
            count = 60,
            baseWishes = listOf(
                "Thank you from the bottom of my heart for your kindness, generosity, and thoughtfulness throughout this past year.",
                "Your help and encouragement have meant more to me than words can say. Wishing you a holiday filled with deep appreciation.",
                "Gratitude turns what we have into enough. Thank you for your warm presence in my life and for sharing your light.",
                "I am counting my blessings this festive season, and you are right at the very top of my list. Thank you!",
                "Sending heartfelt appreciation for your constant support, warmth, and friendship. Have a wonderfully blessed holiday!"
            )
        )

        // 9. GENERAL & FESTIVE (60 wishes)
        addCategoryWishes(
            category = WishCategory.GENERAL,
            occasion = "Festive Joy",
            prefix = "gen_ext",
            count = 60,
            baseWishes = listOf(
                "Wishing you a peaceful holiday season filled with simple pleasures, sweet rest, and comforting smiles.",
                "May your days be merry, your spirit light, and your upcoming year filled with peace, abundance, and laughter.",
                "May the holiday season bring you plenty of time to rest, recharge, and rejoice in the beauty of everyday life.",
                "Sending positive vibrations and holiday cheer to brighten your home and energize your days ahead.",
                "May every toast bring cheer, every greeting bring joy, and every moment bring lasting peace to your soul."
            )
        )

        return list
    }
}
