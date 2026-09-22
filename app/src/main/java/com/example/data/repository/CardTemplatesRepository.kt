package com.example.data.repository

import com.example.model.CardDecorationStyle
import com.example.model.CardOccasion
import com.example.model.CardTemplate

object CardTemplatesRepository {

    private val templates: List<CardTemplate> = listOf(
        // =========================================================
        // 10 CHRISTMAS CARD TEMPLATES
        // =========================================================
        CardTemplate(
            id = "xm_card_01_crimson_tree",
            title = "Merry Christmas",
            subtitle = "Evergreen Joy & Peace",
            defaultMessage = "May the wonder, love, and sweet comfort of Christmas wrap your heart and home in endless warmth and boundless cheer.",
            occasion = CardOccasion.CHRISTMAS,
            primaryColorHex = 0xFF8A0014,
            secondaryColorHex = 0xFFC62828,
            accentColorHex = 0xFFFFD700,
            badge = "Classic Tree",
            decorationStyle = CardDecorationStyle.CHRISTMAS_TREE
        ),
        CardTemplate(
            id = "xm_card_02_silent_snow",
            title = "Silent & Holy Night",
            subtitle = "Winter Snowflakes",
            defaultMessage = "As snow gently covers the world outside, may your home be filled with quiet peace, soft candlelight, and heartfelt gratitude.",
            occasion = CardOccasion.CHRISTMAS,
            primaryColorHex = 0xFF14243B,
            secondaryColorHex = 0xFF283F63,
            accentColorHex = 0xFF90E0EF,
            badge = "Snowfall",
            decorationStyle = CardDecorationStyle.SNOWFLAKES
        ),
        CardTemplate(
            id = "xm_card_03_golden_bells",
            title = "Jingle Holiday Bells",
            subtitle = "Festive Melodies",
            defaultMessage = "Listen to the merry chime of holiday bells ringing out peace on Earth and joyful blessings for you and your beloved family.",
            occasion = CardOccasion.CHRISTMAS,
            primaryColorHex = 0xFF3D1308,
            secondaryColorHex = 0xFF702812,
            accentColorHex = 0xFFFFC72C,
            badge = "Chiming Bells",
            decorationStyle = CardDecorationStyle.BELLS
        ),
        CardTemplate(
            id = "xm_card_04_forest_pine",
            title = "Evergreen Blessings",
            subtitle = "Pine Grove & Berries",
            defaultMessage = "May the steadfast hope of the season flourish in your life like an evergreen tree, standing strong and vibrant through every winter.",
            occasion = CardOccasion.CHRISTMAS,
            primaryColorHex = 0xFF0A331E,
            secondaryColorHex = 0xFF1B5937,
            accentColorHex = 0xFFD4AF37,
            badge = "Evergreen",
            decorationStyle = CardDecorationStyle.CHRISTMAS_TREE
        ),
        CardTemplate(
            id = "xm_card_05_shining_star",
            title = "Star of Wonder",
            subtitle = "Guiding Light",
            defaultMessage = "May the brilliant star that guided kings lead you toward love, unyielding faith, and peaceful joy all through the holidays.",
            occasion = CardOccasion.CHRISTMAS,
            primaryColorHex = 0xFF160F30,
            secondaryColorHex = 0xFF322359,
            accentColorHex = 0xFFFFDF00,
            badge = "Guiding Star",
            decorationStyle = CardDecorationStyle.STARS
        ),
        CardTemplate(
            id = "xm_card_06_santa_spirit",
            title = "Santa's Jolly Delivery",
            subtitle = "Holiday Cheer",
            defaultMessage = "Wishing you sweet surprises, stocking treats, merry laughter by the fireside, and all the whimsical magic Santa brings!",
            occasion = CardOccasion.CHRISTMAS,
            primaryColorHex = 0xFF9B111E,
            secondaryColorHex = 0xFFD32F2F,
            accentColorHex = 0xFFFFFFFF,
            badge = "Santa Cheer",
            decorationStyle = CardDecorationStyle.SANTA_MAGIC
        ),
        CardTemplate(
            id = "xm_card_07_festive_gifts",
            title = "Wrapped with Love",
            subtitle = "Holiday Gift Boxes",
            defaultMessage = "The greatest holiday gifts aren't found under the tree, but in the cherished memories and warm smiles we share together.",
            occasion = CardOccasion.CHRISTMAS,
            primaryColorHex = 0xFF194D33,
            secondaryColorHex = 0xFF2A724E,
            accentColorHex = 0xFFFF7043,
            badge = "Gift Boxes",
            decorationStyle = CardDecorationStyle.GIFT_BOXES
        ),
        CardTemplate(
            id = "xm_card_08_ruby_baubles",
            title = "Radiant Ornaments",
            subtitle = "Sparkling Decor",
            defaultMessage = "May your festive days sparkle as brightly as glass baubles, illuminating your home with laughter, fellowship, and deep contentment.",
            occasion = CardOccasion.CHRISTMAS,
            primaryColorHex = 0xFF580C1F,
            secondaryColorHex = 0xFF8A1C36,
            accentColorHex = 0xFFFFD700,
            badge = "Ornaments",
            decorationStyle = CardDecorationStyle.ORNAMENTS
        ),
        CardTemplate(
            id = "xm_card_09_royal_cozy",
            title = "Warmest Christmas Hugs",
            subtitle = "Fireside Harmony",
            defaultMessage = "Sending mug-fulls of hot cocoa, cozy blanket snuggles, and the warmest Christmas wishes across the miles straight to your doorstep.",
            occasion = CardOccasion.CHRISTMAS,
            primaryColorHex = 0xFF4A192C,
            secondaryColorHex = 0xFF732E48,
            accentColorHex = 0xFFF5B7B1,
            badge = "Cozy Glow",
            decorationStyle = CardDecorationStyle.ORNAMENTS
        ),
        CardTemplate(
            id = "xm_card_10_twilight_carol",
            title = "Christmas Melodies",
            subtitle = "Harmonious Peace",
            defaultMessage = "May the angelic melodies of Christmas carols lift your spirits, soothe your mind, and bring serenity to your holiday season.",
            occasion = CardOccasion.CHRISTMAS,
            primaryColorHex = 0xFF0D2818,
            secondaryColorHex = 0xFF1E4D2B,
            accentColorHex = 0xFF80ED99,
            badge = "Carols",
            decorationStyle = CardDecorationStyle.STARS
        ),

        // =========================================================
        // 10 NEW YEAR CARD TEMPLATES
        // =========================================================
        CardTemplate(
            id = "ny_card_01_midnight_fireworks",
            title = "Happy New Year",
            subtitle = "Midnight Fireworks",
            defaultMessage = "As the midnight countdown chimes, may fireworks illuminate your path to bold adventures, fresh beginnings, and magnificent victories!",
            occasion = CardOccasion.NEW_YEAR,
            primaryColorHex = 0xFF0B0C10,
            secondaryColorHex = 0xFF1F2833,
            accentColorHex = 0xFFFFD700,
            badge = "Fireworks",
            decorationStyle = CardDecorationStyle.FIREWORKS
        ),
        CardTemplate(
            id = "ny_card_02_golden_glamour",
            title = "Cheers to New Beginnings",
            subtitle = "Golden Confetti",
            defaultMessage = "Raise a shimmering glass to the past year's lessons and step forward with confidence into a sparkling year of prosperity and joy!",
            occasion = CardOccasion.NEW_YEAR,
            primaryColorHex = 0xFF1A1A1D,
            secondaryColorHex = 0xFF4E4E50,
            accentColorHex = 0xFFF5B700,
            badge = "Confetti",
            decorationStyle = CardDecorationStyle.CONFETTI
        ),
        CardTemplate(
            id = "ny_card_03_welcome_2027",
            title = "Welcome 2027",
            subtitle = "Year of Triumph",
            defaultMessage = "Step boldly into 2027! May this monumental new year bring you unparalleled breakthroughs, flourishing health, and genuine fulfillment.",
            occasion = CardOccasion.NEW_YEAR,
            primaryColorHex = 0xFF0D1B2A,
            secondaryColorHex = 0xFF1B263B,
            accentColorHex = 0xFF48CAE4,
            badge = "2027 Special",
            decorationStyle = CardDecorationStyle.YEAR_2027
        ),
        CardTemplate(
            id = "ny_card_04_champagne_spark",
            title = "Sparkle & Shine",
            subtitle = "Champagne Bubbles",
            defaultMessage = "Pop the bubbly and toss the confetti! May every single month of the coming year bring you laughter that echoes and love that stays.",
            occasion = CardOccasion.NEW_YEAR,
            primaryColorHex = 0xFF350036,
            secondaryColorHex = 0xFF5D0060,
            accentColorHex = 0xFFFFD166,
            badge = "Celebration",
            decorationStyle = CardDecorationStyle.FIREWORKS
        ),
        CardTemplate(
            id = "ny_card_05_starlight_dreams",
            title = "Reach for the Stars",
            subtitle = "Boundless Ambitions",
            defaultMessage = "May the coming year grant you wings to chase your loftiest dreams, courage to break new frontiers, and the calm to savor every sunrise.",
            occasion = CardOccasion.NEW_YEAR,
            primaryColorHex = 0xFF03071E,
            secondaryColorHex = 0xFF370617,
            accentColorHex = 0xFFFFBA08,
            badge = "Ambition",
            decorationStyle = CardDecorationStyle.STARS
        ),
        CardTemplate(
            id = "ny_card_06_aurora_dawn",
            title = "A Radiant Dawn",
            subtitle = "Fresh Horizons",
            defaultMessage = "Greeting the first sunrise of the new year with gratitude, hope, and an open heart ready to welcome life’s sweetest surprises.",
            occasion = CardOccasion.NEW_YEAR,
            primaryColorHex = 0xFF03254C,
            secondaryColorHex = 0xFF1167B1,
            accentColorHex = 0xFFD0E1FD,
            badge = "Horizon",
            decorationStyle = CardDecorationStyle.CONFETTI
        ),
        CardTemplate(
            id = "ny_card_07_emerald_prosperity",
            title = "Prosperous New Year",
            subtitle = "Health & Wealth",
            defaultMessage = "Wishing you a year blessed with thriving ventures, deep inner harmony, lasting vitality, and joyful fellowship with loved ones.",
            occasion = CardOccasion.NEW_YEAR,
            primaryColorHex = 0xFF08201D,
            secondaryColorHex = 0xFF14453D,
            accentColorHex = 0xFF52B788,
            badge = "Prosperity",
            decorationStyle = CardDecorationStyle.YEAR_2027
        ),
        CardTemplate(
            id = "ny_card_08_midnight_crystal",
            title = "Count Down to Joy",
            subtitle = "Midnight Magic",
            defaultMessage = "When the clock strikes twelve, leave every burden behind and embrace 365 fresh chances to live, love, and inspire. Happy New Year!",
            occasion = CardOccasion.NEW_YEAR,
            primaryColorHex = 0xFF1C1124,
            secondaryColorHex = 0xFF372549,
            accentColorHex = 0xFFF9C784,
            badge = "Countdown",
            decorationStyle = CardDecorationStyle.FIREWORKS
        ),
        CardTemplate(
            id = "ny_card_09_vibrant_carnival",
            title = "Dance into the New Year",
            subtitle = "Joyous Rhythm",
            defaultMessage = "May your upcoming year be as colorful, lively, and exuberant as a grand holiday parade. Sing loud, dance freely, and celebrate every day!",
            occasion = CardOccasion.NEW_YEAR,
            primaryColorHex = 0xFF4A0E17,
            secondaryColorHex = 0xFF7A1726,
            accentColorHex = 0xFFFFE066,
            badge = "Carnival",
            decorationStyle = CardDecorationStyle.CONFETTI
        ),
        CardTemplate(
            id = "ny_card_10_serene_renewal",
            title = "Peace & Renewal",
            subtitle = "Quiet Clarity",
            defaultMessage = "May the new year bestow upon you quiet mornings, clear decisions, serene family ties, and restful contentment from January through December.",
            occasion = CardOccasion.NEW_YEAR,
            primaryColorHex = 0xFF1B263B,
            secondaryColorHex = 0xFF415A77,
            accentColorHex = 0xFFE0E1DD,
            badge = "Serenity",
            decorationStyle = CardDecorationStyle.STARS
        )
    )

    fun getAllTemplates(): List<CardTemplate> = templates

    fun getChristmasTemplates(): List<CardTemplate> = templates.filter { it.occasion == CardOccasion.CHRISTMAS }

    fun getNewYearTemplates(): List<CardTemplate> = templates.filter { it.occasion == CardOccasion.NEW_YEAR }

    fun getTemplateById(id: String): CardTemplate? = templates.find { it.id == id }
}
