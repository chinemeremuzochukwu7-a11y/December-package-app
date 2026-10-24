package com.example.data.repository

import com.example.model.AiLanguage
import com.example.model.AiOccasion
import com.example.model.AiRecipient
import com.example.model.AiTone
import com.example.model.AiWishLength
import com.example.model.AiWishRequest
import com.example.model.AiWishResponse
import com.example.model.CardOccasion
import com.example.model.WishCategory
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.math.abs

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

        val tailoredBody = when (request.length) {
            com.example.model.AiWishLength.SHORT -> {
                body.split(".").filter { it.isNotBlank() }.take(1).joinToString(".") + "."
            }
            com.example.model.AiWishLength.MEDIUM -> body
            com.example.model.AiWishLength.LONG -> {
                body + "\n\nMay this season bring peace, abundance, and countless moments of gratitude to you and everyone you hold dearest. Cherish every memory and let joy illuminate your path forward."
            }
            com.example.model.AiWishLength.POETIC -> {
                body + "\n\nLike candles glowing against the winter frost, may your home radiate with enduring grace, gentle laughter, and the timeless gift of true companionship.\n\nMay the coming days unfold like a tapestry of golden wonders, blessed with triumphant joy and quiet serenity."
            }
        }

        return "$salutation\n\n$tailoredBody$personalNoteSection\n\n$signoff"
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
                AiTone.POETIC -> listOf(
                    "Beneath the starlit winter sky, where silent wonders gently lie, may quiet peace and starry light illuminate your holy night.",
                    "The snow falls softly like a prayer, whispers of hope throughout the air. May Christmas bring its tender grace to every corner of your place.",
                    "A winter rose upon the snow, a fireside with amber glow; wishing you wonder, love, and grace in this enchanting holiday space."
                )[seed]
                else -> listOf(
                    "Merry Christmas! May your home be filled with joy, peace, and warmth this holiday season.",
                    "Wishing you a season filled with peace, joy, and cherished moments with those you love.",
                    "May the spirit of Christmas bring you and your family happiness and harmony throughout the year."
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

    override suspend fun generateCardGreeting(
        occasion: CardOccasion,
        recipientName: String,
        senderName: String,
        tone: AiTone,
        length: AiWishLength,
        customPrompt: String,
        seed: Long
    ): String {
        delay(350)
        val name = if (recipientName.isNotBlank()) recipientName.trim() else "friend"
        val idx = (abs(seed) % 5).toInt()

        val coreGreetings = when (occasion) {
            CardOccasion.CHRISTMAS -> when (tone) {
                AiTone.WARM -> listOf(
                    "Wishing you a cozy fireside Christmas filled with peaceful light, sweet laughter, and the warmest memories with family, $name.",
                    "May the wonder and tranquil beauty of Christmas illuminate your home with joy and abundance, dearest $name.",
                    "May your Christmas tree sparkle with hope, and your days be filled with comfort, delight, and true serenity, $name.",
                    "Sending you heartfelt Christmas blessings wrapped in cozy warmth, serene joy, and good cheer, $name.",
                    "May peace, kindness, and boundless festive happiness surround your days this Christmas, $name!"
                )
                AiTone.LOVING -> listOf(
                    "Dearest $name, having you in my life is the greatest Christmas blessing. May your holiday season sparkle with tender affection and sweetest joy.",
                    "My heart is full of gratitude and love for you this Christmas, $name. May every moment bring warmth to your soul.",
                    "To my dearest $name, you make every season magical. Wishing you an enchanting Christmas surrounded by love.",
                    "Sending my endless love and warmest hugs across the miles this Christmas, $name. You mean the world to me.",
                    "May the quiet magic of Christmas remind you how deeply treasured and loved you are, dearest $name."
                )
                AiTone.POETIC -> listOf(
                    "Like snowflakes falling gently upon winter pine, may peace descend upon your heart this Christmas, $name, illuminating every day with golden wonder.",
                    "As constellations glow through sacred winter skies, may Christmas wrap your home in celestial grace, laughter, and timeless goodwill, $name.",
                    "May the symphony of carols and flickering holiday lights weave a tapestry of pure delight and serenity for you, $name.",
                    "Beneath the starlit canopy of this sacred night, may your days be blessed with harmony, radiant wonder, and eternal peace, $name.",
                    "May the quiet poetry of winter whisper gladness to your soul and fill your cup with celestial light, $name."
                )
                AiTone.FUNNY -> listOf(
                    "Merry Christmas, $name! May your eggnog be sweet, your treats be plentiful, and your holiday shopping bills magically disappear!",
                    "Happy Holidays, $name! Here is to good food, stretchy pants, long naps, and zero awkward family conversations!",
                    "Wishing you a Christmas so merry that Santa himself gets jealous of your festive snacks, $name!",
                    "May your Christmas be bright, and may none of your gifts require complicated assembly, $name!",
                    "Cheers to another year of surviving holiday chaos with style, cookies, and holiday movies, $name!"
                )
                AiTone.INSPIRATIONAL -> listOf(
                    "May the light of Christmas renew your spirit, kindle bold dreams in your heart, and guide your steps toward victory, $name.",
                    "Every candle lit this Christmas is a symbol of faith and triumphs ahead. Step fearlessly into your greatest chapter, $name.",
                    "May this season remind you of the extraordinary resilience, generosity, and goodness you bring into the world, $name.",
                    "Wishing you unshakeable hope, vibrant energy, and triumphant joy this Christmas and beyond, $name.",
                    "May the sacred spirit of this holiday inspire you to reach higher and shine brighter than ever before, $name."
                )
                AiTone.PRAYERFUL -> listOf(
                    "May the birth of our Savior fill your home with divine grace, heavenly peace, and everlasting salvation, $name.",
                    "Praying that God's gentle hand protects you and showers your family with abundant blessings this holy Christmas, $name.",
                    "May the true meaning of Christmas bring sacred joy, quiet reassurance, and holiness to your heart, $name.",
                    "May God's favor surround you like a shield, and His peace guard your footsteps through every festive day, $name.",
                    "Wishing you a Christmas sanctified by divine love, eternal hope, and spiritual renewal, $name."
                )
                AiTone.PROFESSIONAL -> listOf(
                    "Season's greetings, $name. Wishing you and your team a restful, joyous holiday season and continued success.",
                    "Warmest holiday wishes to you, $name. Thank you for your partnership and dedication throughout this memorable year.",
                    "May your holidays be relaxing and rewarding, $name. Looking forward to accomplishing great things together ahead.",
                    "Wishing you peace, prosperity, and joyous celebrations with family this Christmas, $name.",
                    "With appreciation for our collaboration, we wish you a delightful holiday season and a fruitful new chapter, $name."
                )
                AiTone.SHORT -> listOf(
                    "Merry Christmas, $name! Wishing you peace, laughter, and abundant festive cheer.",
                    "Warmest Christmas blessings to you and your loved ones, $name!",
                    "Wishing you a cozy, joyful Christmas filled with love and treats, $name!",
                    "Happy Holidays, $name! May your days be merry and bright.",
                    "Peace, joy, and sweetest holiday greetings to you, $name!"
                )
                AiTone.EMOTIONAL -> listOf(
                    "Christmas always makes me reflect on what truly matters, and having you in my life is at the top of that list, $name.",
                    "Through every chapter of life, your warmth and kindness have been my guiding comfort. Thank you, dearest $name.",
                    "May this Christmas bring you the same deep peace and gentleness that you so effortlessly give to others, $name.",
                    "I am holding you in my heart this holiday season with profound gratitude for our bond, $name.",
                    "You make the world softer and brighter just by being you. Merry Christmas with all my heart, $name."
                )
                else -> listOf(
                    "Wishing you a cozy fireside Christmas filled with peaceful light, sweet laughter, and the warmest memories, $name.",
                    "May peace, kindness, and boundless festive happiness surround your days this Christmas, $name!",
                    "Merry Christmas, $name! Wishing you peace, laughter, and abundant festive cheer."
                )
            }
            CardOccasion.NEW_YEAR -> when (tone) {
                AiTone.WARM -> listOf(
                    "Happy New Year, $name! May 2027 open brilliant doors of vibrant health, smiling days, and wonderful surprises for you.",
                    "Cheers to 365 fresh chances to celebrate life, pursue dreams, and cherish friendship, $name!",
                    "May the New Year unfold like a golden dawn, bringing you peace, prosperity, and abundant happiness, $name.",
                    "Wishing you laughter in every morning, triumph in every challenge, and contentment in every sunset, $name.",
                    "May your coming year be filled with exciting new adventures and cozy, treasured moments, $name!"
                )
                AiTone.LOVING -> listOf(
                    "Happy New Year, my sweetest $name. Loving you is my favorite joy, and I cannot wait to share 2027 with you.",
                    "To dearest $name: May the coming year be our brightest, tenderest, and happiest journey yet.",
                    "Wishing the one who holds my heart a magnificent New Year overflowing with affection and delight, $name.",
                    "Every new year with you is a treasure. May 2027 bring you every happiness your beautiful heart desires, $name.",
                    "Holding your hand as the calendar turns is the sweetest blessing of all. Happy New Year, $name!"
                )
                AiTone.POETIC -> listOf(
                    "As the old year turns its final page, may the dawn of 2027 arrive like sunlight over calm winter seas, $name.",
                    "May health, profound triumph, and boundless beauty rise up to greet you at every sunrise of the New Year, $name.",
                    "Step forward into the unfolding tapestry of the New Year with faith, grace, and an open heart, $name.",
                    "May the stars align in your favor, casting golden light on every path you tread in 2027, $name.",
                    "Let the whispers of the New Year carry promises of renewed peace, celestial wonder, and timeless joy, $name."
                )
                AiTone.FUNNY -> listOf(
                    "Happy New Year, $name! May your resolutions last at least longer than your leftover holiday pie!",
                    "Cheers to 2027, $name! 365 new opportunities to pretend we're fully functional adults!",
                    "May all your troubles last as long as my January diet motivation, $name! Happy New Year!",
                    "New Year, same glorious troublemaker! Excited to make questionable life choices together in 2027, $name!",
                    "Here is to another year of hilarious inside jokes, midnight snacks, and great memories, $name!"
                )
                AiTone.INSPIRATIONAL -> listOf(
                    "May 2027 awaken your boldest ambitions, $name. You carry the power, wisdom, and brilliance to conquer new heights.",
                    "Write an extraordinary masterpiece on the blank pages ahead. Step into your greatness, $name!",
                    "Leave yesterday's doubts behind. The New Year belongs to those who believe in the beauty of their dreams, $name.",
                    "May every challenge in 2027 become a stepping stone to your greatest triumphs and breakthroughs, $name.",
                    "Boldly embrace the limitless potential waiting for you in the New Year. The world is yours, $name!"
                )
                AiTone.PRAYERFUL -> listOf(
                    "May God bless your footsteps in 2027 with divine direction, robust health, and abundant favor, $name.",
                    "Entrusting your New Year to the Almighty. May His mercy guide your decisions and His peace guard your home, $name.",
                    "Praying that 2027 showers you with heavenly grace, spiritual elevation, and fruitful harvests in every endeavor, $name.",
                    "May the Lord walk ahead of you in the coming year, leveling mountains and opening doors of blessing, $name.",
                    "May you experience God's boundless goodness and unconditional love every single day of 2027, $name."
                )
                AiTone.PROFESSIONAL -> listOf(
                    "Happy New Year, $name. Wishing you and your organization continued innovation, prosperity, and standout achievements.",
                    "May the New Year bring rewarding endeavors, strategic growth, and exciting milestones to your team, $name.",
                    "With sincere appreciation for our professional collaboration, we wish you a prosperous and successful 2027, $name.",
                    "Wishing you a productive, energizing New Year filled with notable career triumphs and well-being, $name.",
                    "Here is to another year of fruitful partnership, shared victories, and breakthrough results, $name."
                )
                AiTone.SHORT -> listOf(
                    "Happy New Year, $name! Wishing you vibrant health, grand success, and endless joy in 2027.",
                    "Cheers to 2027, $name! May it be your finest and happiest year yet.",
                    "Wishing you 365 days of prosperity, good smiles, and great adventures, $name!",
                    "Happy 2027, $name! Step boldly into this exciting new chapter.",
                    "Brightest New Year greetings to you and your loved ones, $name!"
                )
                AiTone.EMOTIONAL -> listOf(
                    "As we step into 2027, I am so grateful for the moments we shared and the unconditional warmth you give, $name.",
                    "You have been an anchor of strength and kindness through every season. Wishing you the happiest New Year, $name.",
                    "May the coming year heal any past hurts and fill every day with the comforting gentleness you deserve, $name.",
                    "Thinking of you as the year turns, with immense love and boundless gratitude for our bond, $name.",
                    "May 2027 be kind, generous, and loving to you in every single way, dearest $name."
                )
                else -> listOf(
                    "Happy New Year, $name! May 2027 open brilliant doors of vibrant health, smiling days, and wonderful surprises for you.",
                    "Wishing you laughter in every morning, triumph in every challenge, and contentment in every sunset, $name.",
                    "Cheers to 2027, $name! May it be your finest and happiest year yet."
                )
            }
            CardOccasion.HOLIDAY_SEASON -> when (tone) {
                AiTone.POETIC -> listOf(
                    "As winter casts its quiet spell, may seasonal joy embrace your home with peaceful light and golden serenity, $name.",
                    "May the frosty winds outside carry only warmth, comfort, and harmonious grace into your hearth, $name.",
                    "Like stars reflecting upon silver ice, may this festive season sparkle with enchanting wonders for you, $name.",
                    "May the gentle cadence of winter melodies soothe your heart and inspire your festive days, $name.",
                    "Wishing you tranquil twilight hours, heartwarming conversations, and the timeless poetry of winter, $name."
                )
                AiTone.LOVING -> listOf(
                    "Sending you all my love and warmest seasonal embraces, dearest $name. You make every winter season special.",
                    "May this holiday season bring you as much joy, warmth, and comfort as you bring into my world, $name.",
                    "To dearest $name, thinking of you with pure affection and wishing you the sweetest holiday season.",
                    "You are the brightest light in my winter days, $name. Wishing you a season overflowing with love.",
                    "No matter how cold the winter gets, your friendship keeps my heart delightfully warm, $name!"
                )
                AiTone.FUNNY -> listOf(
                    "Happy Holiday Season, $name! May your coffee be hot, your blankets be fuzzy, and your holiday spirit be unstoppable!",
                    "Winter greetings, $name! Time to hibernate under warm blankets with good snacks until spring!",
                    "Sending you warm winter vibes and an abundance of festive sweet treats, $name!",
                    "May your holiday season be free of traffic, long lines, and cold toes, $name!",
                    "Cheers to cozy sweaters, holiday lights, and zero obligations to leave the couch, $name!"
                )
                else -> listOf(
                    "Sending warmest holiday season greetings to you and your family, $name! May your days be wrapped in peace.",
                    "May this festive season surround your home with sweet cheer, restorative rest, and happy laughter, $name.",
                    "Wishing you a serene and joyous winter season filled with cozy moments and dear companionship, $name.",
                    "May the beauty of the holidays illuminate your path with hope, vibrant health, and delightful memories, $name.",
                    "Warmest seasonal wishes, $name! May this festive period bring peace and renewed energy to your soul."
                )
            }
        }

        val baseMessage = coreGreetings[idx % coreGreetings.size]

        val bodyWithLength = when (length) {
            AiWishLength.SHORT -> baseMessage
            AiWishLength.MEDIUM -> baseMessage + " May each moment bring comfort to your soul and smiles to your face."
            AiWishLength.LONG -> baseMessage + " As the holidays unfold, may your home be a sanctuary of love, abundant health, and generous laughter. Cherish every memory and let joyful moments guide you forward."
            AiWishLength.POETIC -> baseMessage + "\n\nMay peace descend upon your family like pure white snow, filling every chamber with warmth, divine blessings, and enduring laughter throughout the festive days ahead."
        }

        val bodyWithPrompt = if (customPrompt.isNotBlank()) {
            "$bodyWithLength\n\n\"${customPrompt.trim()}\""
        } else {
            bodyWithLength
        }

        return if (senderName.isNotBlank()) {
            "$bodyWithPrompt\n\nWarmly,\n${senderName.trim()}"
        } else {
            bodyWithPrompt
        }
    }

    override suspend fun regenerateWishText(
        category: WishCategory,
        currentText: String,
        seed: Long
    ): String {
        delay(250)
        val pool = when (category) {
            WishCategory.CHRISTMAS -> listOf(
                "May the wonder and cozy warmth of Christmas illuminate your heart, bringing tranquil peace to your soul and boundless cheer to your home! 🎄✨",
                "Merry Christmas! Wishing you a season overflowing with fragrant pine, twinkling lights, delicious feasts, and the sweetest laughter with family.",
                "May the quiet miracle of Christmas wrap your days in celestial comfort, healing grace, and enduring joy that lasts all year long. 🎅🎁",
                "Sending you heartfelt holiday blessings! May your festive table be surrounded by love, true kindness, and cherished companionship.",
                "Like fresh snow glistening in the morning sun, may your Christmas shine with pure happiness, radiant health, and gentle contentment.",
                "Merry Christmas! May the timeless symphony of goodwill and peace on Earth echo through your home today and forever."
            )
            WishCategory.NEW_YEAR -> listOf(
                "Happy New Year! May 2027 open magnificent doors of vibrant health, joyful breakthroughs, and extraordinary new opportunities for you! 🎉🥂",
                "Cheers to 365 fresh pages waiting to be written with bold courage, laughter, true friendship, and abundant prosperity! 🎆",
                "May the dawn of the New Year bring serene peace to your spirit, prosperity to your hands, and sweet delight to every day.",
                "Happy 2027! Leave yesterday's worries behind and step fearlessly into your greatest, most rewarding chapter yet! ✨",
                "Wishing you 12 months of success, 52 weeks of laughter, and 365 days of good fortune! Happy New Year!",
                "May the coming year unfold like a wonderful story filled with kind friends, smiling faces, and unexpected triumphs."
            )
            WishCategory.FAMILY -> listOf(
                "Family is the truest gift of the holidays. Thank you for being the warm heartbeat of our home and the anchor of our lives! 🏡❤️",
                "No distance or time can ever dim the warmth of family. May this holiday season bless our home with peace, unity, and laughter.",
                "To our wonderful family: through every season of life, your love and support mean everything to me. Wishing you joyful holidays!",
                "May our festive dinner table be filled with sweet stories, fond memories, and the irreplaceable comfort of shared love.",
                "Home is where love lives and memories are made. Wishing our precious family boundless joy and cozy moments together."
            )
            WishCategory.FRIENDS -> listOf(
                "A true friend like you turns every season into a celebration. Thank you for walking beside me through thick and thin! 🤝✨",
                "To the one who shares my laughs, supports my dreams, and knows my stories: wishing you the happiest, most festive holidays!",
                "Cheers to friendship, holiday treats, late-night chats, and creating memories that will make us smile for decades to come!",
                "So grateful for the gift of your friendship this holiday season. May your days be filled with happiness and sweet adventures!",
                "Friendship is life's sweetest sparkle. Wishing you boundless joy, cozy evenings, and all the festive snacks you can handle!"
            )
            WishCategory.LOVE -> listOf(
                "You are my favorite holiday miracle, my cozy shelter, and my greatest blessing. Wishing you a season as beautiful as your heart! ❤️🎄",
                "Every holiday moment is sweeter and brighter because I get to share it with you. I love you more with every passing day.",
                "Having your hand to hold as the snow falls is all the gift I'll ever need. Happy holidays to the love of my life! 💖",
                "No present under the tree could ever compare to your love. May your holidays be wrapped in tender romance and pure bliss.",
                "You bring magic into my world every single day. Wishing you an enchanting holiday season filled with all my love."
            )
            WishCategory.BIRTHDAY -> listOf(
                "Happy Birthday! May this milestone year bring you flourishing health, radiant happiness, and the realization of your deepest dreams! 🎂🎉",
                "Celebrate today knowing how deeply appreciated and admired you are by everyone around you! May your day be sprinkled with joy and cake!",
                "Wishing you 365 new days filled with laughter, triumphant victories, and sweet surprises. Have the happiest birthday yet!",
                "May your special day be wrapped in warm hugs, genuine smiles, and all the love you so generously give to others. Happy Birthday!"
            )
            WishCategory.RELIGIOUS -> listOf(
                "May the sacred peace of God which surpasses all human understanding guard your heart and mind in divine joy and grace! ✝️🕊️",
                "Praying that God's loving presence enfolds your family with holy blessings, unwavering faith, and spiritual renewal this season.",
                "May the true light of Christ illuminate your path with righteousness, comforting hope, and everlasting salvation.",
                "May God's favor surround you like a shield, and may His mercies be fresh and abundant in your life each morning."
            )
            WishCategory.THANK_YOU -> listOf(
                "From the bottom of my heart, thank you for your immense kindness, generosity, and thoughtful support. You made a world of difference! 🙏✨",
                "Words cannot express how grateful I am for your presence and help. Your generosity will always be remembered and treasured.",
                "Thank you for being someone I can always rely upon. Your thoughtfulness brought so much light and relief to my life!"
            )
            WishCategory.BUSINESS -> listOf(
                "Season's greetings! Thank you for your valuable partnership, collaboration, and trust. Wishing you continued prosperity and success! 💼📈",
                "We appreciate our shared achievements this year and look forward to reaching even greater milestones together in the season ahead.",
                "Wishing you, your colleagues, and your organization peace, good health, and outstanding achievements throughout the coming year."
            )
            else -> listOf(
                "May the gentle spirit of the holiday season fill your home with radiant warmth, serene peace, and happy smiles! ✨🎄",
                "Sending you warm thoughts, sunny moments, and positive energy for a truly wonderful and fulfilling festive season.",
                "May your days be brightened by sweet unexpected joys, kind encounters, and countless reasons to celebrate life!",
                "Wishing you peace of mind, healthy vitality, and gentle contentment in everything you do this holiday season!"
            )
        }

        val available = pool.filter { it.trim() != currentText.trim() }
        val choices = if (available.isNotEmpty()) available else pool
        val selectedIndex = (abs(seed) % choices.size).toInt()
        return choices[selectedIndex]
    }
}
