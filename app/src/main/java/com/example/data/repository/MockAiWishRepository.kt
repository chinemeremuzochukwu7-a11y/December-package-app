package com.example.data.repository

import com.example.model.AiLanguage
import com.example.model.AiOccasion
import com.example.model.AiRecipient
import com.example.model.AiTone
import com.example.model.AiWishRequest
import com.example.model.AiWishResponse
import kotlinx.coroutines.delay
import java.util.UUID

/**
 * High-fidelity offline mock implementation of [AiWishRepository].
 * Produces personalized, emotionally resonant wishes across languages, occasions, tones, and personal notes.
 * Includes simulated network latency to validate the UI loading and progress states.
 */
class MockAiWishRepository : AiWishRepository {

    override suspend fun generateWish(request: AiWishRequest): Result<AiWishResponse> {
        return try {
            // Realistic AI generation delay for smooth loading animation
            delay(750)

            val generatedText = buildWishMessage(request)

            val response = AiWishResponse(
                id = UUID.randomUUID().toString(),
                text = generatedText,
                occasion = request.occasion,
                recipient = request.recipient,
                recipientName = request.recipientName,
                tone = request.tone,
                language = request.language,
                createdAt = System.currentTimeMillis()
            )

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildWishMessage(request: AiWishRequest): String {
        val salutation = getSalutation(request.language, request.recipient, request.recipientName, request.tone)
        val body = getBodyText(request)
        val signoff = getSignoff(request.language, request.tone)

        val personalNoteSection = if (request.personalDetails.isNotBlank()) {
            when (request.language) {
                AiLanguage.FRENCH -> "\n\n\"${request.personalDetails.trim()}\""
                AiLanguage.SPANISH -> "\n\n\"${request.personalDetails.trim()}\""
                AiLanguage.PORTUGUESE -> "\n\n\"${request.personalDetails.trim()}\""
                else -> "\n\n\"${request.personalDetails.trim()}\""
            }
        } else ""

        return "$salutation\n\n$body$personalNoteSection\n\n$signoff"
    }

    private fun getSalutation(
        language: AiLanguage,
        recipient: AiRecipient,
        name: String,
        tone: AiTone
    ): String {
        val displayName = if (name.isNotBlank()) name.trim() else recipient.displayName
        return when (language) {
            AiLanguage.FRENCH -> {
                when (tone) {
                    AiTone.PROFESSIONAL -> "Cher/Chère $displayName,"
                    AiTone.LOVING, AiTone.EMOTIONAL -> "Mon très cher / Ma très chère $displayName,"
                    else -> "Cher(e) $displayName,"
                }
            }
            AiLanguage.SPANISH -> {
                when (tone) {
                    AiTone.PROFESSIONAL -> "Estimado/a $displayName,"
                    AiTone.LOVING, AiTone.EMOTIONAL -> "Queridísimo/a $displayName,"
                    else -> "Querido/a $displayName,"
                }
            }
            AiLanguage.PORTUGUESE -> {
                when (tone) {
                    AiTone.PROFESSIONAL -> "Prezado(a) $displayName,"
                    AiTone.LOVING, AiTone.EMOTIONAL -> "Queridíssimo(a) $displayName,"
                    else -> "Querido(a) $displayName,"
                }
            }
            AiLanguage.ENGLISH -> {
                when (tone) {
                    AiTone.PROFESSIONAL -> "Dear $displayName,"
                    AiTone.LOVING, AiTone.EMOTIONAL -> "Dearest $displayName,"
                    AiTone.FUNNY -> "Hey $displayName!"
                    AiTone.SHORT -> "To $displayName:"
                    else -> "Dear $displayName,"
                }
            }
        }
    }

    private fun getSignoff(language: AiLanguage, tone: AiTone): String {
        return when (language) {
            AiLanguage.FRENCH -> when (tone) {
                AiTone.PROFESSIONAL -> "Avec nos salutations les plus cordiales."
                AiTone.LOVING -> "Avec tout mon amour et ma tendresse,"
                AiTone.PRAYERFUL -> "Que la grâce et la paix vous accompagnent,"
                AiTone.FUNNY -> "Plein de sourires et de rires !"
                else -> "Chaleureusement,"
            }
            AiLanguage.SPANISH -> when (tone) {
                AiTone.PROFESSIONAL -> "Saludos cordiales y los mejores deseos."
                AiTone.LOVING -> "Con todo mi amor y cariño siempre,"
                AiTone.PRAYERFUL -> "Que Dios te bendiga abundantemente,"
                AiTone.FUNNY -> "¡Un abrazo gigante y muchas risas!"
                else -> "Con mucho cariño,"
            }
            AiLanguage.PORTUGUESE -> when (tone) {
                AiTone.PROFESSIONAL -> "Com os melhores cumprimentos e votos de sucesso."
                AiTone.LOVING -> "Com todo meu amor e carinho eterno,"
                AiTone.PRAYERFUL -> "Que Deus abençoe abundantemente seus caminhos,"
                AiTone.FUNNY -> "Com abraços bem apertados e alegria!"
                else -> "Com todo carinho,"
            }
            AiLanguage.ENGLISH -> when (tone) {
                AiTone.PROFESSIONAL -> "Warm regards and best wishes,"
                AiTone.LOVING -> "With all my love and heart,"
                AiTone.PRAYERFUL -> "May you be surrounded by heavenly grace and peace,"
                AiTone.FUNNY -> "Sending high fives, cookies, and lots of giggles!"
                AiTone.SHORT -> "Always,"
                else -> "Warmly and fondly,"
            }
        }
    }

    private fun getBodyText(request: AiWishRequest): String {
        val seed = (request.variationSeed % 3).toInt()

        return when (request.language) {
            AiLanguage.FRENCH -> getFrenchBody(request.occasion, request.tone, seed)
            AiLanguage.SPANISH -> getSpanishBody(request.occasion, request.tone, seed)
            AiLanguage.PORTUGUESE -> getPortugueseBody(request.occasion, request.tone, seed)
            AiLanguage.ENGLISH -> getEnglishBody(request.occasion, request.tone, seed)
        }
    }

    private fun getEnglishBody(occasion: AiOccasion, tone: AiTone, seed: Int): String {
        return when (occasion) {
            AiOccasion.CHRISTMAS -> when (tone) {
                AiTone.WARM -> listOf(
                    "May this Christmas wrap you in glowing warmth, comforting joy, and the sweet laughter of those you hold close. Thank you for being such an extraordinary blessing in my life.",
                    "Wishing you a cozy fireside Christmas surrounded by love, joyful carols, and cherished moments that linger long after the season ends.",
                    "May the gentle peace of Christmas softly fill your home with radiant happiness and lasting contentment."
                )[seed]
                AiTone.LOVING -> listOf(
                    "Having you in my life is the greatest Christmas gift I could ever hope for. May your holiday season sparkle with tender affection, sweet laughter, and everlasting comfort.",
                    "My heart is overflowed with gratitude for you this holiday season. You bring magic and sunshine into my world every single day.",
                    "No present under the tree could ever compare to your love. Wishing you a peaceful, joyful Christmas blessed with all your heart desires."
                )[seed]
                AiTone.FUNNY -> listOf(
                    "May your Christmas be merry, your eggnog be strong, and your holiday shopping receipts miraculously vanish into thin air!",
                    "Wishing you lots of sweet treats, zero awkward family questions, and a Santa who definitely didn't see what you did this year!",
                    "Here's to good food, holiday naps, stretchy pants, and all the festive cheer you can handle!"
                )[seed]
                AiTone.EMOTIONAL -> listOf(
                    "Christmas always makes me reflect on the people who truly matter, and you are forever at the top of that list. Thank you for your unwavering grace and presence.",
                    "Through every high and low of life, your warmth has been my guiding light. May this holiday bring you the same peace and reassurance you give to everyone else.",
                    "I am deeply thankful for the memories we've shared and the journey we are on. You make life so much more beautiful."
                )[seed]
                AiTone.INSPIRATIONAL -> listOf(
                    "May the wonder of Christmas renew your spirit, kindle fresh passions within you, and remind you of the incredible strength you carry inside.",
                    "As the stars shine brightly on this sacred night, may your own path be illuminated with bold courage, limitless hope, and boundless joy.",
                    "Every candle lit this holiday is a testament to hope over darkness. May this season inspire you to dream boldly and shine fearlessly."
                )[seed]
                AiTone.PROFESSIONAL -> listOf(
                    "Wishing you a relaxing and joyful holiday season. Thank you for your valued partnership and dedication throughout this year. Happy Holidays!",
                    "Warmest seasonal greetings to you and your family. May the holidays bring you well-deserved rest and rejuvenation.",
                    "We appreciate your trust and collaboration. Wishing you peace, prosperity, and joyous celebrations this Christmas."
                )[seed]
                AiTone.SHORT -> listOf(
                    "Merry Christmas! Wishing you peace, laughter, and endless holiday joy.",
                    "Warmest Christmas blessings to you and your loved ones!",
                    "May your holidays be merry, bright, and filled with love."
                )[seed]
                AiTone.PRAYERFUL -> listOf(
                    "May the birth of Christ bring deep peace to your soul, divine light to your home, and abundant grace to your family this Christmas.",
                    "Praying that God's gentle presence and boundless love enfold you this sacred season and throughout the coming year.",
                    "May the true meaning of Christmas fill your heart with holiness, reverence, and unconditional love from above."
                )[seed]
            }

            AiOccasion.NEW_YEAR -> when (tone) {
                AiTone.WARM -> listOf(
                    "As we step across the threshold into the New Year, may your days be painted with health, smiling faces, and quiet moments of sweet contentment.",
                    "Wishing you 365 fresh opportunities to celebrate life, nurture beautiful dreams, and enjoy every sunset.",
                    "May the coming year unfold like a wonderful story filled with laughter, kind friends, and abundant blessings."
                )[seed]
                AiTone.LOVING -> listOf(
                    "There is no one I'd rather journey through another year with than you. May 2027 be our sweetest, happiest chapter yet.",
                    "You are my favorite part of yesterday, my joy today, and my brightest hope for tomorrow. Happy New Year, my love!",
                    "Wishing you a year as beautiful, gentle, and radiant as the love you give so generously."
                )[seed]
                AiTone.FUNNY -> listOf(
                    "May all your troubles last as long as your New Year's resolutions! Cheers to another year of questionable life choices together!",
                    "Happy New Year! Let's pretend we're going to the gym and eating salad for at least three days!",
                    "365 new days, 365 new chances to lose your keys and blame it on the universe. Happy New Year!"
                )[seed]
                AiTone.INSPIRATIONAL -> listOf(
                    "May the dawn of this New Year awaken your boldest dreams. Step forward with unshakeable confidence, because greatness awaits you.",
                    "Write an extraordinary story on the blank pages ahead. You have the wisdom, strength, and heart to conquer new heights.",
                    "Leave yesterday's doubts behind and embrace the unlimited potential of the New Year with faith and fierce determination."
                )[seed]
                AiTone.PROFESSIONAL -> listOf(
                    "Wishing you a prosperous and successful New Year. We look forward to continuing our productive partnership in the year ahead.",
                    "May the New Year bring you and your organization continued growth, innovation, and outstanding achievements.",
                    "Warm greetings for a healthy and thriving New Year filled with rewarding endeavors and new milestones."
                )[seed]
                AiTone.PRAYERFUL -> listOf(
                    "May God bless your coming year with divine favor, good health, serene guidance, and prosperity in all your righteous pursuits.",
                    "Entrusting your footsteps to the Lord as a new calendar begins. May His peace guard your heart and mind continuously.",
                    "May the Almighty walk before you in 2027, clearing obstacles and showering you with mercy and blessing."
                )[seed]
                else -> listOf(
                    "Happy New Year! May each day bring vibrant health, prosperity, and memorable adventures.",
                    "Cheers to new beginnings, fresh chapters, and endless reasons to smile!",
                    "Wishing you a happy, peaceful, and breakthrough-filled New Year!"
                )[seed]
            }

            AiOccasion.BIRTHDAY -> listOf(
                "Wishing you the happiest of birthdays! May this milestone year bring you flourishing health, delightful surprises, and the realization of your deepest hopes.",
                "Happy Birthday! Celebrate today knowing how deeply appreciated, loved, and admired you are by everyone around you.",
                "May your special day be sprinkled with joy, sweet laughter, warm hugs, and all the cake your heart desires!"
            )[seed]

            AiOccasion.WEDDING -> listOf(
                "Congratulations on your wedding day! May the sacred promise you make today deepen with every passing year, weathering every storm and multiplying every joy.",
                "Wishing you a lifetime of holding hands, dancing in the kitchen, and loving each other more fiercely with every sunrise.",
                "May your marriage be blessed with deep friendship, honest communication, unwavering patience, and unconditional love."
            )[seed]

            AiOccasion.ANNIVERSARY -> listOf(
                "Happy Anniversary! Your enduring love and mutual devotion continue to inspire everyone lucky enough to witness your beautiful journey together.",
                "Celebrating the extraordinary love story that gets richer, sweeter, and stronger with each passing year. Happy Anniversary!",
                "May the memories of your yesterday and the shared hopes of your tomorrow make today an unforgettable celebration."
            )[seed]

            AiOccasion.FAMILY -> listOf(
                "Family is where love begins and never ends. Thank you for being the heartbeat of our home and the anchor of our family.",
                "Through every season of life, knowing I have your love and support means the world to me. I am so grateful for our family bond.",
                "No matter how far life takes us, the warmth of family brings us home. Sending you immense love and gratitude."
            )[seed]

            AiOccasion.FRIENDSHIP -> listOf(
                "A true friend is one of life's rarest and most precious gifts. Thank you for walking beside me through thick and thin.",
                "To the one who knows my stories, shares my laughter, and lifts my spirit: I am endlessly thankful for our friendship.",
                "Friendship like ours turns ordinary days into treasures. Here's to making countless more memories together!"
            )[seed]

            AiOccasion.LOVE -> listOf(
                "You are my shelter, my favorite song, and the greatest blessing my heart has ever known. Loving you is the easiest thing I've ever done.",
                "Every moment spent with you is a gift I cherish deeply. My love for you grows stronger and softer every day.",
                "In a busy world, your smile is my peace. Thank you for loving me exactly as I am."
            )[seed]

            AiOccasion.THANK_YOU -> listOf(
                "Words cannot fully express how thankful I am for your kindness, generosity, and thoughtful support. You made an immense difference.",
                "From the bottom of my heart, thank you for your compassion and presence. Your generosity will never be forgotten.",
                "Thank you for being someone I can always count on. Your thoughtfulness brought so much light and relief to my day."
            )[seed]

            AiOccasion.RELIGIOUS -> listOf(
                "May the peace of God which surpasses all human understanding guard your heart and mind in love, faith, and radiant joy.",
                "May you walk each day in the comforting assurance of God's unchanging promises, unfailing mercy, and divine protection.",
                "Praying that you experience God's boundless goodness, righteous direction, and healing presence in every area of your life."
            )[seed]

            AiOccasion.BUSINESS -> listOf(
                "Thank you for your valuable collaboration, reliability, and professionalism. We wish you continued prosperity and success in all your ventures.",
                "We appreciate our shared achievements and look forward to reaching even greater milestones together in the future.",
                "Wishing you and your team continued innovation, market success, and fruitful endeavors throughout the coming season."
            )[seed]

            AiOccasion.CONGRATULATIONS -> listOf(
                "Congratulations on this monumental achievement! Your hard work, persistence, and passion have deservedly paid off.",
                "Bravo! What you have accomplished is truly inspiring. Celebrate this triumphant victory with deep pride and joy!",
                "So thrilled to see your dedication rewarded in such grand fashion. Congratulations on reaching this wonderful milestone!"
            )[seed]

            AiOccasion.GENERAL -> listOf(
                "Sending you warm thoughts, sunny smiles, and positive energy for a truly wonderful and fulfilling day.",
                "May your day be brightened by sweet unexpected joys, kind encounters, and reasons to be grateful.",
                "Wishing you peace of mind, healthy vitality, and gentle contentment in everything you do."
            )[seed]
        }
    }

    private fun getFrenchBody(occasion: AiOccasion, tone: AiTone, seed: Int): String {
        return when (occasion) {
            AiOccasion.CHRISTMAS -> listOf(
                "Que la magie de Noël illumine votre foyer de paix, de tendresse et de précieux moments partagés avec ceux que vous aimez.",
                "En cette belle nuit de Noël, je vous souhaite chaleur, réconfort et d'innombrables bénédictions pour toute votre famille.",
                "Que la joie des fêtes résonne dans votre cœur et apporte sérénité et renouveau à chaque instant."
            )[seed]
            AiOccasion.NEW_YEAR -> listOf(
                "Que cette nouvelle année s'ouvre sur de magnifiques horizons, une santé florissante et la réalisation de vos vœux les plus chers.",
                "Belle et heureuse année 2027 ! Que chaque mois soit jalonné de succès, de rires et de belles opportunités.",
                "À l'aube de cette nouvelle année, recevez tous mes vœux d'amour, de paix intérieure et de prospérité."
            )[seed]
            AiOccasion.BIRTHDAY -> listOf(
                "Joyeux anniversaire ! Que cette nouvelle année de vie soit remplie de bonheur, d'éclats de rire et de merveilleuses surprises.",
                "Je te souhaite une journée mémorable, entourée de tout l'amour et de l'admiration que tu mérites tant.",
                "Que chaque bougie allumée aujourd'hui soit une promesse de joie et de succès pour ton avenir !"
            )[seed]
            AiOccasion.THANK_YOU -> listOf(
                "Du fond du cœur, je tiens à vous exprimer toute ma reconnaissance pour votre aide précieuse et votre grande gentillesse.",
                "Merci infiniment pour votre soutien constant et votre présence réconfortante à mes côtés.",
                "Votre générosité et votre attention m'ont profondément touché(e). Merci pour tout ce que vous êtes."
            )[seed]
            else -> listOf(
                "Je vous envoie mes pensées les plus chaleureuses et mes vœux les plus sincères pour cette belle occasion.",
                "Que la sérénité et le bonheur vous accompagnent aujourd'hui et toujours.",
                "Puisse cette période vous apporter tout le réconfort et l'harmonie dont vous avez besoin."
            )[seed]
        }
    }

    private fun getSpanishBody(occasion: AiOccasion, tone: AiTone, seed: Int): String {
        return when (occasion) {
            AiOccasion.CHRISTMAS -> listOf(
                "Que la dulce paz y la alegría de la Navidad llenen tu hogar de calidez, risas y hermosos recuerdos en familia.",
                "Deseándote una Navidad mágica, bendecida con mucho amor, salud inquebrantable y serenidad en el corazón.",
                "Que la luz de esta Navidad ilumine tu camino y te recuerde lo valioso/a y especial que eres para todos nosotros."
            )[seed]
            AiOccasion.NEW_YEAR -> listOf(
                "¡Feliz Año Nuevo! Que este año que comienza traiga 365 días colmados de salud, prosperidad y grandes triunfos.",
                "Brindo por un nuevo año lleno de proyectos ilusionantes, amor sincero y momentos inolvidables junto a los tuyos.",
                "Que el Año Nuevo te reciba con los brazos abiertos, colmando tus días de bendiciones y sueños cumplidos."
            )[seed]
            AiOccasion.BIRTHDAY -> listOf(
                "¡Muy feliz cumpleaños! Que la vida te regale hoy y siempre infinitas razones para sonreír y celebrar.",
                "Deseándote un día lleno de abrazos sinceros, mucho cariño y toda la felicidad que te mereces.",
                "¡Que comience tu mejor año! Gracias por iluminar nuestras vidas con tu hermosa presencia."
            )[seed]
            AiOccasion.THANK_YOU -> listOf(
                "Quiero agradecerte de todo corazón tu generosidad, tu tiempo y tu constante apoyo incondicional.",
                "Muchísimas gracias por estar siempre presente y por tu bondad que marca la diferencia en mi vida.",
                "Mi más sincero agradecimiento por tu valiosa ayuda. Valoro enormemente tu amistad y tu generoso corazón."
            )[seed]
            else -> listOf(
                "Te envío mis mejores deseos con todo mi aprecio y cariño en esta ocasión tan especial.",
                "Que la felicidad y el bienestar te acompañen en cada paso de tu camino hoy y siempre.",
                "Que la bendición de la vida y el cariño de tus seres queridos colmen tu corazón de profunda paz."
            )[seed]
        }
    }

    private fun getPortugueseBody(occasion: AiOccasion, tone: AiTone, seed: Int): String {
        return when (occasion) {
            AiOccasion.CHRISTMAS -> listOf(
                "Que a paz e a ternura do Natal envolvam seu lar de luz, amor, união e momentos inesquecíveis em família.",
                "Desejo a você um Natal abençoado, com a doçura da esperança e o aconchego de quem você mais ama ao seu lado.",
                "Que a magia deste Natal renove sua fé, cure seu coração e encha seus dias de profunda alegria."
            )[seed]
            AiOccasion.NEW_YEAR -> listOf(
                "Feliz Ano Novo! Que este novo ciclo traga saúde abundante, portas abertas, vitórias e felicidade sem medidas.",
                "Que 2027 seja o capítulo mais iluminado da sua vida, repleto de prosperidade, abraços apertados e paz.",
                "Brindamos a um Ano Novo repleto de bênçãos, recomeços frutíferos e sonhos tornados realidade!"
            )[seed]
            AiOccasion.BIRTHDAY -> listOf(
                "Feliz Aniversário! Que seu novo ano seja abençoado com saúde de ferro, grandes alegrias e realizações.",
                "Parabéns pelo seu dia especial! Que você continue sendo essa pessoa tão iluminada e admirável.",
                "Desejo um dia inesquecível, cheio de carinho, doces momentos e cercado das pessoas que você ama!"
            )[seed]
            AiOccasion.THANK_YOU -> listOf(
                "Agradeço do fundo do coração pela sua generosidade, gentileza e apoio indispensável neste momento.",
                "Muito obrigado(a) por sua presença amiga e por estender a mão com tanta sensibilidade e carinho.",
                "Sua ajuda fez toda a diferença. Guardarei para sempre no coração sua bondade sem igual."
            )[seed]
            else -> listOf(
                "Envio meus votos mais sinceros de muita paz, saúde e alegria nesta ocasião tão especial.",
                "Que a luz do bem e a serenidade acompanhem seus passos hoje e por todos os dias da sua vida.",
                "Que a esperança e o afeto sejam seus companheiros fiéis em cada instante desta jornada."
            )[seed]
        }
    }
}
