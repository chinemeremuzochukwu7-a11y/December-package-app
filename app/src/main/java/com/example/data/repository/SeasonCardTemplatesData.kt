package com.example.data.repository

import com.example.model.CardDecorationStyle
import com.example.model.CardOccasion
import com.example.model.CardTemplate

/**
 * 520 Exclusive Holiday Season Card Templates.
 * Provides a dedicated "Season" section with 500+ templates, locking premium ones for Pro users.
 */
object SeasonCardTemplatesData {

    val seasonTemplates: List<CardTemplate> by lazy {
        generateSeasonTemplates()
    }

    private fun generateSeasonTemplates(): List<CardTemplate> {
        val titles = listOf(
            "Winter Solstice Serenity", "Cozy Fireside Hearth", "Nordic Frost & Pine", "Aurora Borealis Wonder",
            "Peaceful Snowfall Haven", "Golden Hearth Memories", "Candlelit Winter Night", "Silver Cedar Whispers",
            "Alpine Chalet Glow", "Midnight Starlight Dream", "Winter Forest Melody", "Frosted Evergreen Glow",
            "Glimmering Icicle Crown", "Warm Wool & Cocoa", "Fireside Cocoa Joy", "Silent Winter Glade",
            "Twilight Snowfall Blessing", "Crystal Mountain Dawn", "Cozy Cabin Retreat", "Winter Solstice Light",
            "Radiant Silver Frost", "Emerald Winter Grove", "Enchanted Forest Glow", "Holiday Spirit Harmony",
            "Winter Wonderland Song", "Golden Hearth Radiance", "Serene Snowflake Dance", "Peace On Earth Harmony",
            "Winter Blessings Grace", "Snowy Pine Grove", "Cottage Warmth & Cheer", "Starry Winter Sky",
            "Velvet Winter Evening", "Winter Solstice Peace", "Frost & Starlight", "Alpine Valley Magic",
            "Warm Winter Hugs", "Winter Reverie & Light", "Cozy Winter Twilight", "Holiday Haven Blessings"
        )

        val subtitles = listOf(
            "Hearthside Warmth & Peace", "Winter Whispers & Soft Light", "Snowy Pine & Alpine Frost",
            "Candlelight Glow & Serenity", "Fireside Cheer & Gratitude", "Evergreen Grace & Love",
            "Starry Skies & Winter Magic", "Warm Cocoa & Cozy Wool", "Pure White Winter Solitude",
            "Joyful Season Celebrations", "Silver Ice & Midnight Frost", "Holiday Fellowship & Light",
            "Peaceful Hearts & Cozy Homes", "Winter Forest Echoes", "Northern Lights & Serenity"
        )

        val messages = listOf(
            "Wishing you a cozy winter season wrapped in the comforting warmth of family, hot cocoa by the hearth, and quiet peace.",
            "May the serene beauty of fresh snowfall bring tranquil thoughts, restful evenings, and sweet laughter to your home.",
            "Sending you warmest winter greetings! May your days be illuminated by cherished friendships and gentle joyful blessings.",
            "May the peace of the winter season linger gently in your heart and bring boundless renewed hope to your journey.",
            "As the snow falls softly across the hills, may your hearth glow bright with love, hospitality, and joyful togetherness.",
            "Wishing you quiet winter mornings, heartwarming fireside chats, and an abundance of sweet moments throughout this festive season.",
            "May the splendor of the holiday season envelop you and your family in everlasting harmony, health, and bright wonder.",
            "Warm winter blessings to you and yours! May every snowflake outside remind you of life's delicate and precious miracles.",
            "May this season of rest and reflection bring deep contentment to your spirit and brighten every corner of your days.",
            "Here's to warm mittens, crackling wood fires, fragrant spiced cider, and memories that will keep you smiling all season long.",
            "May the quiet stillness of winter fill your soul with peace, resilience, and boundless gratitude for life's simple gifts.",
            "Sending you a warm winter embrace! May your home be a sanctuary of comfort, joy, and deep festive contentment.",
            "May the winter stars guide your steps toward peace, hope, and new opportunities as we celebrate this wonder-filled season."
        )

        val palettes = listOf(
            Triple(0xFF14243BL, 0xFF283F63L, 0xFF90E0EFL), // Midnight Frost
            Triple(0xFF0A331EL, 0xFF1B5937L, 0xFFD4AF37L), // Evergreen Pine & Gold
            Triple(0xFF3D1308L, 0xFF702812L, 0xFFFFC72CL), // Amber Hearth
            Triple(0xFF160F30L, 0xFF322359L, 0xFFFFDF00L), // Royal Velvet Indigo
            Triple(0xFF0B192CL, 0xFF1E3E62L, 0xFF00D2FCL), // Sapphire Glacier
            Triple(0xFF580C1FL, 0xFF851934L, 0xFFFFD700L), // Imperial Crimson
            Triple(0xFF1E293BL, 0xFF334155L, 0xFFE2E8F0L), // Slate Silver
            Triple(0xFF1A1A24L, 0xFF2D2D44L, 0xFFFFB703L), // Starry Twilight
            Triple(0xFF0F3B2EL, 0xFF195342L, 0xFF80ED99L), // Mint Alpine Pine
            Triple(0xFF3C096CL, 0xFF5A189AL, 0xFFFFE169L), // Amethyst Glow
            Triple(0xFF2B2D42L, 0xFF8D99AEL, 0xFFEF233CL), // Nordic Snowflake
            Triple(0xFF1B263BL, 0xFF415A77L, 0xFFE0E1DDL)  // Platinum Ice
        )

        val decorationStyles = listOf(
            CardDecorationStyle.SNOWFLAKES,
            CardDecorationStyle.STARS,
            CardDecorationStyle.CHRISTMAS_TREE,
            CardDecorationStyle.BELLS,
            CardDecorationStyle.ORNAMENTS,
            CardDecorationStyle.GIFT_BOXES,
            CardDecorationStyle.CONFETTI
        )

        val proBadges = listOf(
            "👑 PRO Luxury", "👑 PRO Platinum", "👑 PRO Gold", "👑 PRO Velvet",
            "👑 PRO Solstice", "👑 PRO Aurora", "👑 PRO VIP Season", "👑 PRO Diamond Frost"
        )

        val freeBadges = listOf(
            "Winter Cozy", "Season Joy", "Snow Magic", "Fireside",
            "Holiday Peace", "Frost Light", "Alpine Star", "Nordic Haven"
        )

        val list = ArrayList<CardTemplate>(520)

        for (i in 1..520) {
            val titleIndex = (i - 1) % titles.size
            val subIndex = (i - 1) % subtitles.size
            val msgIndex = (i - 1) % messages.size
            val palIndex = (i - 1) % palettes.size
            val styleIndex = (i - 1) % decorationStyles.size

            // Lock best cards for Pro: roughly 35% of season cards are locked (every 3rd template)
            val isProCard = (i % 3 == 0)
            val badge = if (isProCard) {
                proBadges[((i / 3) - 1) % proBadges.size]
            } else {
                freeBadges[i % freeBadges.size]
            }

            val titleVariation = if (i > titles.size) {
                "${titles[titleIndex]} #${(i / titles.size) + 1}"
            } else {
                titles[titleIndex]
            }

            val (pCol, sCol, aCol) = palettes[palIndex]

            list.add(
                CardTemplate(
                    id = "season_card_${String.format("%03d", i)}",
                    title = titleVariation,
                    subtitle = subtitles[subIndex],
                    defaultMessage = messages[msgIndex],
                    occasion = CardOccasion.HOLIDAY_SEASON,
                    primaryColorHex = pCol,
                    secondaryColorHex = sCol,
                    accentColorHex = aCol,
                    badge = badge,
                    decorationStyle = decorationStyles[styleIndex],
                    isPro = isProCard
                )
            )
        }

        return list
    }
}
