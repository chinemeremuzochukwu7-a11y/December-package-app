package com.example.data.repository

import com.example.model.GeneratorOccasion
import com.example.model.GeneratorRecipient
import com.example.model.GeneratorRelationship
import com.example.model.GeneratorTone
import com.example.model.WishGenerationResult
import java.util.UUID

object WishGenerator {

    fun generateWishes(
        occasion: GeneratorOccasion,
        recipient: GeneratorRecipient,
        relationship: GeneratorRelationship,
        tone: GeneratorTone,
        personalNote: String = ""
    ): List<WishGenerationResult> {
        val noteSuffix = if (personalNote.isNotBlank()) {
            " Also thinking of: ${personalNote.trim()}."
        } else ""

        val opening = when (recipient) {
            GeneratorRecipient.PARENTS -> "Dearest Mom & Dad,"
            GeneratorRecipient.FAMILY -> "To my cherished family,"
            GeneratorRecipient.PARTNER -> "To my beloved,"
            GeneratorRecipient.FRIEND -> "To my dearest friend,"
            GeneratorRecipient.COLLEAGUE -> "Dear colleague,"
            GeneratorRecipient.BOSS -> "Dear valued partner & mentor,"
            GeneratorRecipient.TEACHER -> "Dear Teacher,"
            GeneratorRecipient.EVERYONE -> "To everyone near and dear,"
        }

        val greetings = when (occasion) {
            GeneratorOccasion.CHRISTMAS -> when (tone) {
                GeneratorTone.HEARTFELT -> listOf(
                    "$opening Christmas is so much richer with you in my life. May the quiet peace of holy nights and the loving warmth of shared traditions surround you with boundless comfort.$noteSuffix Merry Christmas with all my heart!",
                    "$opening There is no greater holiday gift than having your love and guidance in my journey. May your Christmas be gentle, peaceful, and filled with deep joy.$noteSuffix",
                    "$opening Wishing you a Christmas season filled with warmth by the hearth, sincere smiles, and sweet moments that linger forever in memory.$noteSuffix Merry Christmas!"
                )
                GeneratorTone.JOYFUL -> listOf(
                    "$opening Deck the halls and let the good times roll! Wishing you a holly, jolly, festive Christmas overflowing with laughter, festive treats, and sparkling celebrations!$noteSuffix",
                    "$opening May your holiday bells chime with joy and your stockings burst with cheer! Wishing you a vibrant, merry, and unforgettable Christmas!$noteSuffix",
                    "$opening Here is to singing carols loud, wrapping gifts with excitement, and celebrating the magic of Christmas together!$noteSuffix"
                )
                GeneratorTone.FUNNY -> listOf(
                    "$opening May your holidays be as cheerful as Santa after a batch of cookies, and your holiday cooking survive without setting off alarms!$noteSuffix Merry Christmas!",
                    "$opening Christmas tip: Don't check your bank account until January, and enjoy every single slice of pie without remorse!$noteSuffix Merry Christmas!",
                    "$opening Hoping Santa brings you everything on your wishlist, and none of the fruitcakes you secretly dread! Cheers and Merry Christmas!$noteSuffix"
                )
                GeneratorTone.INSPIRATIONAL -> listOf(
                    "$opening May the enduring light of Christmas ignite renewed hope, deep peace, and visionary dreams in your soul for the journey ahead.$noteSuffix Merry Christmas!",
                    "$opening In the stillness of the season, may you discover fresh strength, deeper gratitude, and the courage to illuminate the world around you.$noteSuffix Blessed Christmas!",
                    "$opening Christmas reminds us that light always conquers darkness. May your holiday be a luminous beacon of goodness and love.$noteSuffix"
                )
                GeneratorTone.SHORT_SWEET -> listOf(
                    "$opening Warmest wishes for a bright, peaceful, and joyful Christmas.$noteSuffix Merry Christmas!",
                    "$opening May the beauty of Christmas brighten your days.$noteSuffix Merry Christmas with love!",
                    "$opening Wishing you peace, love, and sweet holiday happiness today and always.$noteSuffix"
                )
                GeneratorTone.POETIC -> listOf(
                    "$opening Like quiet snowfall beneath starlit skies, may peace gently settle upon your home this sacred Christmas night.$noteSuffix",
                    "$opening Across the winter hush, may the songs of hope and the fragrance of pine bring timeless enchantment to your hearth.$noteSuffix Merry Christmas.",
                    "$opening May celestial starlight guide your path and crown your holiday season with quiet grace and sacred wonder.$noteSuffix"
                )
            }
            GeneratorOccasion.NEW_YEAR -> when (tone) {
                GeneratorTone.HEARTFELT -> listOf(
                    "$opening As we step across the threshold of a brand new year, I want to thank you for every shared moment and unwavering kindness.$noteSuffix May the coming year shower you with health, harmony, and joy!",
                    "$opening Grateful for your presence in my story. Wishing you a year where every sunrise brings renewed hope and every sunset brings peaceful satisfaction.$noteSuffix Happy New Year!",
                    "$opening May 365 fresh days unfold before you with grace, tenderness, and cherished breakthroughs.$noteSuffix Happy New Year!"
                )
                GeneratorTone.JOYFUL -> listOf(
                    "$opening 3... 2... 1... Happy New Year! Pop the champagne, crank up the music, and step into this dazzling chapter with courage and wide smiles!$noteSuffix",
                    "$opening Out with the old, in with the magnificent! Wishing you a high-energy, confetti-filled, wildly fun New Year!$noteSuffix",
                    "$opening Cheers to new adventures, spontaneous celebrations, and living every day to its absolute fullest! Happy New Year!$noteSuffix"
                )
                GeneratorTone.FUNNY -> listOf(
                    "$opening May your New Year resolutions last longer than your holiday leftover cookies! Happy New Year!$noteSuffix",
                    "$opening Here is to a year of making great decisions... or at least making hilarious new mistakes together! Happy New Year!$noteSuffix",
                    "$opening May your coffee be strong, your year be fruitful, and your gym membership not go completely unused by February! Happy New Year!$noteSuffix"
                )
                GeneratorTone.INSPIRATIONAL -> listOf(
                    "$opening Stand tall before the blank pages of this new year. Write an epic tale of courage, passion, kindness, and relentless growth.$noteSuffix Happy New Year!",
                    "$opening May the new year expand your horizons, sharpen your vision, and grant you the resilience to turn every challenge into victory.$noteSuffix",
                    "$opening Tomorrow is the first page of a 365-page book. Write a masterpiece of courage and love.$noteSuffix Happy New Year!"
                )
                GeneratorTone.SHORT_SWEET -> listOf(
                    "$opening Wishing you health, happiness, and prosperity in the New Year.$noteSuffix Cheers!",
                    "$opening Happy New Year! May every day be filled with blessings.$noteSuffix",
                    "$opening Here is to a bright, healthy, and successful New Year!$noteSuffix"
                )
                GeneratorTone.POETIC -> listOf(
                    "$opening As midnight's bell tolls into the velvet dark, may your hopes take flight on golden wings into the dawn of a sparkling new year.$noteSuffix",
                    "$opening A fresh year dawns like pristine snow, waiting for the footprints of your dreams to illuminate the trail.$noteSuffix Happy New Year.",
                    "$opening May time weave threads of peace, fortitude, and starlight through every day of your coming year.$noteSuffix"
                )
            }
            GeneratorOccasion.HOLIDAY_SEASON -> when (tone) {
                GeneratorTone.HEARTFELT -> listOf(
                    "$opening May the holiday season surround you with deep gratitude, warm embraces, and comforting peace.$noteSuffix Happy Holidays!",
                    "$opening Thinking of you with warmth and affection this festive season. May your holidays be as wonderful as your generous spirit.$noteSuffix",
                    "$opening Sending my warmest wishes for a tranquil holiday season and a year bright with promise and love.$noteSuffix"
                )
                GeneratorTone.JOYFUL -> listOf(
                    "$opening Happy Holidays! May your days be merry, your nights cozy, and your festive spirit shining brightly!$noteSuffix",
                    "$opening Wishing you boundless holiday cheer, delicious feasts, and fabulous celebrations with everyone you adore!$noteSuffix",
                    "$opening Celebrate the season with wholehearted laughter, bright lights, and festive surprises! Happy Holidays!$noteSuffix"
                )
                GeneratorTone.FUNNY -> listOf(
                    "$opening Wishing you peace, love, and uninterrupted nap times after holiday dinners! Happy Holidays!$noteSuffix",
                    "$opening May your holiday sweater be wonderfully ugly and your holiday punch pleasantly strong! Happy Holidays!$noteSuffix",
                    "$opening Survive the holiday shopping crowds and enjoy every single holiday treat with zero guilt! Happy Holidays!$noteSuffix"
                )
                GeneratorTone.INSPIRATIONAL -> listOf(
                    "$opening May this holiday season ignite fresh inspiration, rekindle beloved dreams, and guide you toward fruitful horizons.$noteSuffix",
                    "$opening In this season of reflection and giving, may you find renewed purpose and profound inner peace.$noteSuffix Happy Holidays!",
                    "$opening Let the light of the season inspire kindness in your steps and strength in your heart.$noteSuffix"
                )
                GeneratorTone.SHORT_SWEET -> listOf(
                    "$opening Wishing you a peaceful and joyful holiday season.$noteSuffix Happy Holidays!",
                    "$opening Warm holiday greetings to you and yours!$noteSuffix",
                    "$opening Season's greetings with lots of love and cheer!$noteSuffix"
                )
                GeneratorTone.POETIC -> listOf(
                    "$opening Where winter winds whisper songs of peace, may your sanctuary be warmed with hearthfire and love.$noteSuffix Happy Holidays.",
                    "$opening May the quiet glow of winter dusk wrap your days in serenity and timeless comfort.$noteSuffix",
                    "$opening Silver nights and golden days—may this season be an ode to peace and goodwill in your life.$noteSuffix"
                )
            }
            GeneratorOccasion.BIRTHDAY -> listOf(
                "$opening What a blessed day to celebrate you! May your holiday birthday sparkle with double the joy, double the love, and double the blessings.$noteSuffix Happy Birthday!",
                "$opening A holiday season birthday is the sweetest gift of all. Wishing you a year ahead filled with good health, laughter, and realized dreams.$noteSuffix Happy Birthday!",
                "$opening Celebrate your special day surrounded by twinkling lights and the warmest hugs! Wishing you the happiest of birthdays!$noteSuffix"
            )
            GeneratorOccasion.THANK_YOU -> listOf(
                "$opening As this year draws to a close, my heart overflows with gratitude for your presence, support, and kindness.$noteSuffix Thank you and Happy Holidays!",
                "$opening Thank you for being such an incredible source of strength, warmth, and inspiration. Wishing you abundant blessings this holiday season!$noteSuffix",
                "$opening Your thoughtfulness has made this season truly meaningful. Thank you from the bottom of my heart, and may the New Year bring you peace!$noteSuffix"
            )
        }

        return greetings.mapIndexed { index, text ->
            WishGenerationResult(
                id = "gen_${System.currentTimeMillis()}_$index",
                wishText = text,
                occasion = occasion,
                recipient = recipient,
                tone = tone
            )
        }
    }
}
