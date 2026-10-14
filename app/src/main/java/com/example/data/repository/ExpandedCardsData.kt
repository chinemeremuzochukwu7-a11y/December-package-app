package com.example.data.repository

import com.example.model.CardDecorationStyle
import com.example.model.CardOccasion
import com.example.model.CardTemplate

/**
 * 410 Additional Christmas & New Year Card Templates.
 * Combined with existing templates and Season templates, this elevates the catalog to 1,050+ cards.
 * Locks the best/premium designs for Pro users.
 */
object ExpandedCardsData {

    val expandedCards: List<CardTemplate> by lazy {
        generateExpandedCards()
    }

    private fun generateExpandedCards(): List<CardTemplate> {
        val list = ArrayList<CardTemplate>(410)

        // ----------------------------------------------------
        // 205 CHRISTMAS EXPANDED TEMPLATES
        // ----------------------------------------------------
        val christmasThemes = listOf(
            "Yuletide Splendor", "Noel Angel Song", "Crimson Mistletoe Glow", "Bethlehem Radiant Light",
            "Nutcracker March", "Holly Berry Garland", "Gingerbread Joy", "St. Nicholas Warmth",
            "Evergreen Carol", "Silver Bells Ringing", "Pine & Poinsettia", "Golden Christmas Star",
            "Merry Chimney Magic", "Fireside Stocking Cheer", "Snowy Sleigh Ride", "Christmas Eve Glow",
            "Angel Wings Blessing", "Midnight Mass Serenity", "Holiday Rejoice", "Vintage Christmas Joy"
        )

        val christmasMessages = listOf(
            "May the wonder and cozy warmth of Christmas illuminate your heart, bringing tranquil peace to your home and cheer to your days.",
            "Sending heartfelt Christmas blessings! May your festive table be surrounded by dear ones, sparkling laughter, and true joy.",
            "May the radiant light of the Christmas star guide you toward love, unyielding faith, and peaceful joy all through the holidays.",
            "Warmest Christmas greetings from our home to yours. May your holiday season be blessed with simple joys, sweet treats, and deep gratitude.",
            "Wishing you a season of boundless happiness, comforting embraces, and magical memories that shine for a lifetime."
        )

        val christmasPalettes = listOf(
            Triple(0xFF8A0014L, 0xFFC62828L, 0xFFFFD700L), // Crimson Gold
            Triple(0xFF0A331EL, 0xFF1B5937L, 0xFFD4AF37L), // Forest Pine
            Triple(0xFF14243BL, 0xFF283F63L, 0xFF90E0EFL), // Winter Blue
            Triple(0xFF580C1FL, 0xFF851934L, 0xFFFFDF00L), // Velvet Ruby
            Triple(0xFF3D1308L, 0xFF702812L, 0xFFFFC72CL)  // Cinnamon Hearth
        )

        val christmasStyles = listOf(
            CardDecorationStyle.CHRISTMAS_TREE,
            CardDecorationStyle.BELLS,
            CardDecorationStyle.STARS,
            CardDecorationStyle.SNOWFLAKES,
            CardDecorationStyle.SANTA_MAGIC,
            CardDecorationStyle.ORNAMENTS,
            CardDecorationStyle.GIFT_BOXES
        )

        for (i in 1..205) {
            val titleIdx = (i - 1) % christmasThemes.size
            val msgIdx = (i - 1) % christmasMessages.size
            val palIdx = (i - 1) % christmasPalettes.size
            val styleIdx = (i - 1) % christmasStyles.size

            val isPro = (i % 3 == 0)
            val badge = if (isPro) "👑 PRO Xmas Best" else "Christmas Classic"
            val title = if (i > christmasThemes.size) "${christmasThemes[titleIdx]} ${i}" else christmasThemes[titleIdx]
            val (p, s, a) = christmasPalettes[palIdx]

            list.add(
                CardTemplate(
                    id = "xm_exp_${String.format("%03d", i)}",
                    title = title,
                    subtitle = "Christmas Joy & Blessings",
                    defaultMessage = christmasMessages[msgIdx],
                    occasion = CardOccasion.CHRISTMAS,
                    primaryColorHex = p,
                    secondaryColorHex = s,
                    accentColorHex = a,
                    badge = badge,
                    decorationStyle = christmasStyles[styleIdx],
                    isPro = isPro
                )
            )
        }

        // ----------------------------------------------------
        // 205 NEW YEAR EXPANDED TEMPLATES
        // ----------------------------------------------------
        val newYearThemes = listOf(
            "Midnight Countdown Spark", "Golden Horizon 2027", "Champagne Bubbles Joy", "Neon New Year Glow",
            "Glittering Confetti Skies", "Prosperous New Dawn", "Bold New Beginnings", "Radiant Future Hope",
            "Starlight Resolutions", "Gala Midnight Toast", "Crystal Ball Drop", "Luminous Tomorrow",
            "Fireworks Grand Finale", "Triumphant New Year", "Sparkling Ambition", "Emerald Fresh Start",
            "Midnight Clock Strikes 12", "Joyous Jubilee", "Dazzling Fireworks", "Golden Century Dreams"
        )

        val newYearMessages = listOf(
            "Cheers to a brand new chapter! May the coming year bring you unmatched success, vibrant good health, and thrilling new adventures.",
            "May every second of the New Year bring you renewed purpose, immense breakthroughs, and reasons to celebrate with those you love.",
            "Step into the New Year with courage in your soul, joy in your eyes, and confidence that your brightest days are just beginning.",
            "Wishing you 365 days of radiant health, abundant prosperity, and sweet peace of mind. Happy New Year!",
            "May the sparks of the New Year ignite your passions, fulfill your dreams, and bring boundless laughter to your journey."
        )

        val newYearPalettes = listOf(
            Triple(0xFF0F172AL, 0xFF1E293BL, 0xFFFFD700L), // Midnight Gold
            Triple(0xFF160F30L, 0xFF322359L, 0xFFFFE066L), // Gala Purple Gold
            Triple(0xFF0B192CL, 0xFF1E3E62L, 0xFF00D2FCL), // Sapphire Electric
            Triple(0xFF2B0938L, 0xFF4A0E4EL, 0xFFFFB703L), // Champagne Velvet
            Triple(0xFF1F1D36L, 0xFF3F3351L, 0xFFE9A6A6L)  // Rose Gold Midnight
        )

        val newYearStyles = listOf(
            CardDecorationStyle.FIREWORKS,
            CardDecorationStyle.CONFETTI,
            CardDecorationStyle.STARS,
            CardDecorationStyle.YEAR_2027,
            CardDecorationStyle.BELLS
        )

        for (i in 1..205) {
            val titleIdx = (i - 1) % newYearThemes.size
            val msgIdx = (i - 1) % newYearMessages.size
            val palIdx = (i - 1) % newYearPalettes.size
            val styleIdx = (i - 1) % newYearStyles.size

            val isPro = (i % 3 == 0)
            val badge = if (isPro) "👑 PRO NY VIP" else "New Year Spark"
            val title = if (i > newYearThemes.size) "${newYearThemes[titleIdx]} ${i}" else newYearThemes[titleIdx]
            val (p, s, a) = newYearPalettes[palIdx]

            list.add(
                CardTemplate(
                    id = "ny_exp_${String.format("%03d", i)}",
                    title = title,
                    subtitle = "New Year Celebrations & Hope",
                    defaultMessage = newYearMessages[msgIdx],
                    occasion = CardOccasion.NEW_YEAR,
                    primaryColorHex = p,
                    secondaryColorHex = s,
                    accentColorHex = a,
                    badge = badge,
                    decorationStyle = newYearStyles[styleIdx],
                    isPro = isPro
                )
            )
        }

        return list
    }
}
