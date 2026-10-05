package com.example.data.repository

import com.example.model.Wish
import com.example.model.WishCategory

object WishesRepository {

    private val wishesList: List<Wish> = listOf(
        // ==========================================
        // 1. CHRISTMAS WISHES (25 original wishes)
        // ==========================================
        Wish(
            id = "xm_01",
            text = "Merry Christmas! May your home be filled with love, peace, laughter, and beautiful memories that linger long after the decorations come down.",
            category = WishCategory.CHRISTMAS,
            occasion = "Christmas",
            isPopular = true
        ),
        Wish(
            id = "xm_02",
            text = "May this Christmas bring you and your family happiness, good health, peace, and countless reasons to smile. Have a joyful holiday season!",
            category = WishCategory.CHRISTMAS,
            occasion = "Christmas",
            isPopular = true
        ),
        Wish(
            id = "xm_03",
            text = "May the wonder and cozy warmth of Christmas illuminate your heart, bringing tranquility to your soul and cheer to your days.",
            category = WishCategory.CHRISTMAS,
            occasion = "Christmas",
            isPopular = true
        ),
        Wish(
            id = "xm_04",
            text = "Sending heartfelt holiday greetings your way! May the joyful songs of the season surround you with comfort, serenity, and hope.",
            category = WishCategory.CHRISTMAS,
            occasion = "Christmas",
            isPopular = false
        ),
        Wish(
            id = "xm_05",
            text = "May your festive table be surrounded by dear ones, sparkling laughter, delicious feasts, and the truest blessings of peace and goodwill.",
            category = WishCategory.CHRISTMAS,
            occasion = "Christmas",
            isPopular = true
        ),
        Wish(
            id = "xm_06",
            text = "Christmas waves a gentle wand over our world, reminding us of what matters most: kindness, compassion, and togetherness. Merry Christmas!",
            category = WishCategory.CHRISTMAS,
            occasion = "Christmas",
            isPopular = false
        ),
        Wish(
            id = "xm_07",
            text = "Wishing you a calm, restful, and joyous Christmas. May every twinkle of the tree lights bring a cheerful sparkle to your eyes.",
            category = WishCategory.CHRISTMAS,
            occasion = "Christmas",
            isPopular = false
        ),
        Wish(
            id = "xm_08",
            text = "May the true spirit of Christmas bestow upon you boundless inspiration, renewed strength, and everlasting delight with those you cherish.",
            category = WishCategory.CHRISTMAS,
            occasion = "Christmas",
            isPopular = true
        ),
        Wish(
            id = "xm_09",
            text = "Warmest Christmas greetings from our home to yours. May your holiday season be blessed with simple joys, sweet treats, and deep gratitude.",
            category = WishCategory.CHRISTMAS,
            occasion = "Christmas",
            isPopular = false
        ),
        Wish(
            id = "xm_10",
            text = "May the magic of Christmas Eve turn into sweet miracles on Christmas Day and carry harmonious blessings into all your tomorrows.",
            category = WishCategory.CHRISTMAS,
            occasion = "Christmas",
            isPopular = false
        ),
        Wish(
            id = "xm_11",
            text = "Here's to hot cocoa, crackling fires, gentle snowflakes, and the precious embrace of family. Wishing you a truly memorable Christmas!",
            category = WishCategory.CHRISTMAS,
            occasion = "Christmas",
            isPopular = true
        ),
        Wish(
            id = "xm_12",
            text = "May the peace that surpasses understanding fill your thoughts this holy night and stay with you through every season to come. Merry Christmas!",
            category = WishCategory.CHRISTMAS,
            occasion = "Christmas",
            isPopular = false
        ),
        Wish(
            id = "xm_13",
            text = "Wishing you a Christmas full of happy surprises, heartfelt conversations, and the warmth that only beloved friends and family can bring.",
            category = WishCategory.CHRISTMAS,
            occasion = "Christmas",
            isPopular = false
        ),
        Wish(
            id = "xm_14",
            text = "May your Christmas be bright with happiness and wrapped in the priceless gift of contentment. Have a safe and magical celebration!",
            category = WishCategory.CHRISTMAS,
            occasion = "Christmas",
            isPopular = false
        ),
        Wish(
            id = "xm_15",
            text = "May the joyous melodies of holiday carols lift your spirits and renew your faith in bright tomorrows. Merry Christmas to you and yours!",
            category = WishCategory.CHRISTMAS,
            occasion = "Christmas",
            isPopular = true
        ),
        Wish(
            id = "xm_16",
            text = "To a wonderful person who brings sunshine all year round: may Santa bring you an abundance of cheer, relaxation, and delight.",
            category = WishCategory.CHRISTMAS,
            occasion = "Christmas",
            isPopular = false
        ),
        Wish(
            id = "xm_17",
            text = "May the peace of Christmas enter your doorstep and make its everlasting home within your family circle. Wishing you boundless festive joy!",
            category = WishCategory.CHRISTMAS,
            occasion = "Christmas",
            isPopular = false
        ),
        Wish(
            id = "xm_18",
            text = "Celebrate the precious gift of love and togetherness this Christmas season. May every moment turn into a treasured keepsake in your heart.",
            category = WishCategory.CHRISTMAS,
            occasion = "Christmas",
            isPopular = false
        ),
        Wish(
            id = "xm_19",
            text = "May the gentle starlight of Christmas guide your path toward harmony, prosperity, and endless reasons for celebration. Merry Christmas!",
            category = WishCategory.CHRISTMAS,
            occasion = "Christmas",
            isPopular = true
        ),
        Wish(
            id = "xm_20",
            text = "Wishing you sweet gingerbread moments, delightful laughs under the mistletoe, and a heart full of wonder this magical Christmas day!",
            category = WishCategory.CHRISTMAS,
            occasion = "Christmas",
            isPopular = false
        ),
        Wish(
            id = "xm_21",
            text = "May the spirit of giving and gracious fellowship bloom brightly in your life this Christmas and throughout the coming holidays.",
            category = WishCategory.CHRISTMAS,
            occasion = "Christmas",
            isPopular = false
        ),
        Wish(
            id = "xm_22",
            text = "Merry Christmas! May your home be a sanctuary of comfort, delicious aromas, joyful stories, and unconditional affection.",
            category = WishCategory.CHRISTMAS,
            occasion = "Christmas",
            isPopular = false
        ),

        // ==========================================
        // 2. NEW YEAR WISHES (25 original wishes)
        // ==========================================
        Wish(
            id = "ny_01",
            text = "Happy New Year! May the coming twelve months be packed with bold adventures, fruitful achievements, and endless smiles.",
            category = WishCategory.NEW_YEAR,
            occasion = "New Year",
            isPopular = true
        ),
        Wish(
            id = "ny_02",
            text = "Cheers to 365 fresh blank pages! May you write an extraordinary chapter filled with courageous moves, authentic joy, and genuine triumph.",
            category = WishCategory.NEW_YEAR,
            occasion = "New Year",
            isPopular = true
        ),
        Wish(
            id = "ny_03",
            text = "Wishing you a bright New Year loaded with good health, unshakeable serenity, thriving projects, and plenty of quality time with loved ones.",
            category = WishCategory.NEW_YEAR,
            occasion = "New Year",
            isPopular = false
        ),
        Wish(
            id = "ny_04",
            text = "May each sunrise of the New Year bring you fresh hope, and each sunset leave you with deep peace and personal satisfaction. Happy New Year!",
            category = WishCategory.NEW_YEAR,
            occasion = "New Year",
            isPopular = true
        ),
        Wish(
            id = "ny_05",
            text = "To our wonderful family: may the New Year weave even stronger bonds between us and shower our household with harmony and abundant health.",
            category = WishCategory.NEW_YEAR,
            occasion = "New Year",
            isPopular = false
        ),
        Wish(
            id = "ny_06",
            text = "Happy New Year, my friend! Here's to making more late-night memories, cracking silly jokes, and lifting each other up through every high and low.",
            category = WishCategory.NEW_YEAR,
            occasion = "New Year",
            isPopular = true
        ),
        Wish(
            id = "ny_07",
            text = "May the year ahead bring you the bravery to chase your biggest ambitions and the wisdom to recognize every tiny daily blessing.",
            category = WishCategory.NEW_YEAR,
            occasion = "New Year",
            isPopular = false
        ),
        Wish(
            id = "ny_08",
            text = "Praying that the New Year blesses you with divine direction, inner tranquility, unwavering protection, and steady prosperity in all you undertake.",
            category = WishCategory.NEW_YEAR,
            occasion = "New Year",
            isPopular = false
        ),
        Wish(
            id = "ny_09",
            text = "Wishing you a year of monumental breakthroughs, expanding career horizons, and tremendous commercial success. Happy New Year!",
            category = WishCategory.NEW_YEAR,
            occasion = "New Year",
            isPopular = false
        ),
        Wish(
            id = "ny_10",
            text = "Out with past regrets, in with limitless opportunities! May the New Year treat you with kindness, generosity, and sweet surprises.",
            category = WishCategory.NEW_YEAR,
            occasion = "New Year",
            isPopular = true
        ),
        Wish(
            id = "ny_11",
            text = "Fresh year, fresh vision, fresh victories! Step into the coming days with unwavering confidence knowing your best days are unfolding now.",
            category = WishCategory.NEW_YEAR,
            occasion = "New Year",
            isPopular = false
        ),
        Wish(
            id = "ny_12",
            text = "May the countdown at midnight unlock doors of unprecedented favor, inspiring creativity, and flourishing relationships. Happy New Year!",
            category = WishCategory.NEW_YEAR,
            occasion = "New Year",
            isPopular = false
        ),
        Wish(
            id = "ny_13",
            text = "Sending heartfelt prayers that God enriches your spirit, heals every hidden sorrow, and crowns your upcoming year with bountiful goodness.",
            category = WishCategory.NEW_YEAR,
            occasion = "New Year",
            isPopular = false
        ),
        Wish(
            id = "ny_14",
            text = "Happy New Year! May your calendar be filled with warm celebrations, restful weekends, inspiring travels, and meaningful accomplishments.",
            category = WishCategory.NEW_YEAR,
            occasion = "New Year",
            isPopular = true
        ),
        Wish(
            id = "ny_15",
            text = "Here's to working smarter, laughing louder, and loving deeper in the year ahead. Happy New Year to you and those dearest to you!",
            category = WishCategory.NEW_YEAR,
            occasion = "New Year",
            isPopular = false
        ),
        Wish(
            id = "ny_16",
            text = "May the spark of fireworks remind you of the limitless potential waiting inside your heart this year. Aim high and soar gracefully!",
            category = WishCategory.NEW_YEAR,
            occasion = "New Year",
            isPopular = false
        ),
        Wish(
            id = "ny_17",
            text = "Wishing our colleagues and partners a prosperous New Year marked by pioneering milestones, shared wins, and seamless collaboration.",
            category = WishCategory.NEW_YEAR,
            occasion = "New Year",
            isPopular = false
        ),
        Wish(
            id = "ny_18",
            text = "May you find calm in every storm and reasons to dance in every sunshine. Happy New Year, filled with health and boundless grace!",
            category = WishCategory.NEW_YEAR,
            occasion = "New Year",
            isPopular = false
        ),
        Wish(
            id = "ny_19",
            text = "May your pocket be heavy with fortune and your heart light with genuine gratitude throughout all twelve months ahead.",
            category = WishCategory.NEW_YEAR,
            occasion = "New Year",
            isPopular = false
        ),
        Wish(
            id = "ny_20",
            text = "Happy New Year! Let go of yesterday's burdens and step boldly toward the brilliant future waiting for your unique footprint.",
            category = WishCategory.NEW_YEAR,
            occasion = "New Year",
            isPopular = true
        ),
        Wish(
            id = "ny_21",
            text = "Wishing you 52 weeks of vibrant health, 365 days of joyful moments, and 8,760 hours of unyielding peace. Have a stellar New Year!",
            category = WishCategory.NEW_YEAR,
            occasion = "New Year",
            isPopular = false
        ),

        // ==========================================
        // 3. FAMILY WISHES (20 original wishes)
        // ==========================================
        Wish(
            id = "fam_01",
            text = "To my dearest Mom: Your gentle love is the beating heart of our home. Wishing you comfort, joy, and all the pampering you so deeply deserve.",
            category = WishCategory.FAMILY,
            occasion = "Family",
            isPopular = true
        ),
        Wish(
            id = "fam_02",
            text = "To the best Father in the world: Thank you for being our steady anchor and wisest counselor. May your days be filled with pride and serene peace.",
            category = WishCategory.FAMILY,
            occasion = "Family",
            isPopular = true
        ),
        Wish(
            id = "fam_03",
            text = "To my brother: Having you by my side makes life’s journey so much more fun and resilient. Cheers to our unbreakable sibling bond!",
            category = WishCategory.FAMILY,
            occasion = "Family",
            isPopular = false
        ),
        Wish(
            id = "fam_04",
            text = "To my sweet sister: You are my confidante, my lifelong friend, and my favorite storyteller. Wishing you endless happiness, sisterly joy, and success!",
            category = WishCategory.FAMILY,
            occasion = "Family",
            isPopular = false
        ),
        Wish(
            id = "fam_05",
            text = "To my beloved Parents: Everything good in me started with your loving sacrifices and guidance. Wishing you health, longevity, and peaceful days together.",
            category = WishCategory.FAMILY,
            occasion = "Family",
            isPopular = true
        ),
        Wish(
            id = "fam_06",
            text = "To our wonderful Children: Watching you grow is our life’s sweetest privilege. May your paths be lit with curiosity, kindness, and big dreams.",
            category = WishCategory.FAMILY,
            occasion = "Family",
            isPopular = false
        ),
        Wish(
            id = "fam_07",
            text = "To my dearest Husband: Standing beside you is my greatest comfort and sweetest adventure. Thank you for your devotion, laughter, and strength.",
            category = WishCategory.FAMILY,
            occasion = "Family",
            isPopular = true
        ),
        Wish(
            id = "fam_08",
            text = "To my darling Wife: Your presence fills our lives with beauty, melody, and grace. Wishing you a season as warm and luminous as your smile.",
            category = WishCategory.FAMILY,
            occasion = "Family",
            isPopular = true
        ),
        Wish(
            id = "fam_09",
            text = "Family is the compass that guides us through life's storms and celebrations. So grateful to share our roots, our laughter, and our future together.",
            category = WishCategory.FAMILY,
            occasion = "Family",
            isPopular = true
        ),
        Wish(
            id = "fam_10",
            text = "No matter the distance across maps, our family ties remain tight and unbreakable. Sending all my love, hugs, and warm wishes to our family hearth.",
            category = WishCategory.FAMILY,
            occasion = "Family",
            isPopular = false
        ),
        Wish(
            id = "fam_11",
            text = "Mom, your comforting hug can heal anything. Thank you for teaching me unconditional compassion. Wishing you health and blissful tranquility!",
            category = WishCategory.FAMILY,
            occasion = "Family",
            isPopular = false
        ),
        Wish(
            id = "fam_12",
            text = "Dad, thank you for your quiet strength and warm encouragement. May your year be prosperous and filled with quiet pride in what you've built.",
            category = WishCategory.FAMILY,
            occasion = "Family",
            isPopular = false
        ),
        Wish(
            id = "fam_13",
            text = "Brother, from childhood squabbles to standing shoulder to shoulder as adults, I'm proud to call you my brother. Keep winning in life!",
            category = WishCategory.FAMILY,
            occasion = "Family",
            isPopular = false
        ),
        Wish(
            id = "fam_14",
            text = "Sister, thank you for sharing secrets, clothes, and endless heart-to-hearts. You deserve all the good things life can offer.",
            category = WishCategory.FAMILY,
            occasion = "Family",
            isPopular = false
        ),
        Wish(
            id = "fam_15",
            text = "To our children: May you always remember how deeply loved and cherished you are. Walk through this world with head held high and kindness in hand.",
            category = WishCategory.FAMILY,
            occasion = "Family",
            isPopular = false
        ),
        Wish(
            id = "fam_16",
            text = "May peace reign in our family circle, health dwell in our bodies, and love abound in our conversations every single day.",
            category = WishCategory.FAMILY,
            occasion = "Family",
            isPopular = false
        ),
        Wish(
            id = "fam_17",
            text = "To my partner in everything: Loving you is easy, raising our family with you is an honor. Here is to our continuous journey of shared joy.",
            category = WishCategory.FAMILY,
            occasion = "Family",
            isPopular = false
        ),
        Wish(
            id = "fam_18",
            text = "Having a loving family is like holding a warm candle in the coldest winter night. Grateful for each and every one of you!",
            category = WishCategory.FAMILY,
            occasion = "Family",
            isPopular = false
        ),

        // ==========================================
        // 4. FRIENDS WISHES (18 original wishes)
        // ==========================================
        Wish(
            id = "fr_01",
            text = "To my best friend: Life is a million times brighter and funnier with you in it. Thank you for always listening without judgment and laughing at my worst jokes!",
            category = WishCategory.FRIENDS,
            occasion = "Friendship",
            isPopular = true
        ),
        Wish(
            id = "fr_02",
            text = "True friends are rare treasures that never lose their luster. I am so lucky to count you as one of my closest companions in this life.",
            category = WishCategory.FRIENDS,
            occasion = "Friendship",
            isPopular = true
        ),
        Wish(
            id = "fr_03",
            text = "To my dear long-distance friend: Miles on a map cannot diminish the warmth in our hearts. Thinking of you today and sending massive hugs across the miles!",
            category = WishCategory.FRIENDS,
            occasion = "Friendship",
            isPopular = true
        ),
        Wish(
            id = "fr_04",
            text = "To an old friend: Though time and life keep moving forward, whenever we speak it feels like yesterday. Thank you for decades of treasured memories!",
            category = WishCategory.FRIENDS,
            occasion = "Friendship",
            isPopular = false
        ),
        Wish(
            id = "fr_05",
            text = "Friendship is the sweet glue that holds our spirit steady. Thank you for your steady encouragement, loyalty, and unmatched sense of humor.",
            category = WishCategory.FRIENDS,
            occasion = "Friendship",
            isPopular = false
        ),
        Wish(
            id = "fr_06",
            text = "Here's to the late-night talks, spontaneous road trips, and inside jokes that no one else will ever understand. Happy to walk through life with you!",
            category = WishCategory.FRIENDS,
            occasion = "Friendship",
            isPopular = true
        ),
        Wish(
            id = "fr_07",
            text = "May your days be filled with pleasant surprises, real satisfaction, and friends who appreciate your golden heart as much as I do.",
            category = WishCategory.FRIENDS,
            occasion = "Friendship",
            isPopular = false
        ),
        Wish(
            id = "fr_08",
            text = "To my ride-or-die best friend: Through thick and thin, stormy weather and brilliant sunny skies, you've stood beside me. I appreciate you endlessly!",
            category = WishCategory.FRIENDS,
            occasion = "Friendship",
            isPopular = false
        ),
        Wish(
            id = "fr_09",
            text = "Distance means so little when friendship means so much. Raising a toast to you from afar and wishing you boundless blessings today!",
            category = WishCategory.FRIENDS,
            occasion = "Friendship",
            isPopular = false
        ),
        Wish(
            id = "fr_10",
            text = "Thank you for being the kind of friend who celebrates my wins as if they were your own. May this season return all that generosity back to you tenfold.",
            category = WishCategory.FRIENDS,
            occasion = "Friendship",
            isPopular = false
        ),
        Wish(
            id = "fr_11",
            text = "Old friends are like vintage wine—they only grow sweeter and more precious with time. Here’s to many more years of our shared camaraderie!",
            category = WishCategory.FRIENDS,
            occasion = "Friendship",
            isPopular = false
        ),
        Wish(
            id = "fr_12",
            text = "Wishing my favorite friend boundless energy, fruitful opportunities, and the peaceful mind you so richly deserve. Keep shining bright!",
            category = WishCategory.FRIENDS,
            occasion = "Friendship",
            isPopular = false
        ),
        Wish(
            id = "fr_13",
            text = "Whenever I count my blessings, I count our friendship twice. Thank you for being a rock-solid confidant and an inspiration.",
            category = WishCategory.FRIENDS,
            occasion = "Friendship",
            isPopular = true
        ),
        Wish(
            id = "fr_14",
            text = "May your coffee be strong, your problems be tiny, and your days be surrounded by good company and hearty laughs. You're the best!",
            category = WishCategory.FRIENDS,
            occasion = "Friendship",
            isPopular = false
        ),
        Wish(
            id = "fr_15",
            text = "To my wonderful friend: Never forget how talented, kind, and capable you are. I am always cheering you on from the front row!",
            category = WishCategory.FRIENDS,
            occasion = "Friendship",
            isPopular = false
        ),
        Wish(
            id = "fr_16",
            text = "A loyal friend doubles your joys and divides your griefs. Thank you for being that irreplaceable presence in my life.",
            category = WishCategory.FRIENDS,
            occasion = "Friendship",
            isPopular = false
        ),

        // ==========================================
        // 5. LOVE WISHES (18 original wishes)
        // ==========================================
        Wish(
            id = "lov_01",
            text = "Every day by your side is a gift I cherish with all my heart. Thank you for filling my days with warmth, laughter, and true love.",
            category = WishCategory.LOVE,
            occasion = "Love",
            isPopular = true
        ),
        Wish(
            id = "lov_02",
            text = "In a world of constant noise and rush, your embrace is my peaceful haven. I love you more than words could ever convey.",
            category = WishCategory.LOVE,
            occasion = "Love",
            isPopular = true
        ),
        Wish(
            id = "lov_03",
            text = "You are the melody that makes my heart dance and the gentle anchor keeping me steady. Wishing us endless romantic memories together.",
            category = WishCategory.LOVE,
            occasion = "Love",
            isPopular = false
        ),
        Wish(
            id = "lov_04",
            text = "My favorite place in the entire world is simply beside you. May our bond grow sweeter and deeper with each passing sunrise.",
            category = WishCategory.LOVE,
            occasion = "Love",
            isPopular = true
        ),
        Wish(
            id = "lov_05",
            text = "Thank you for being my lover, my best friend, and my greatest supporter. Loving you is the easiest and most beautiful choice I make every day.",
            category = WishCategory.LOVE,
            occasion = "Love",
            isPopular = false
        ),
        Wish(
            id = "lov_06",
            text = "Your smile brightens my darkest days, and your laughter is my favorite song. Here is to our beautiful journey of shared dreams and devotion.",
            category = WishCategory.LOVE,
            occasion = "Love",
            isPopular = true
        ),
        Wish(
            id = "lov_07",
            text = "To the one who holds my heart: May our days ahead be painted with tender whispers, spontaneous hugs, and everlasting romance.",
            category = WishCategory.LOVE,
            occasion = "Love",
            isPopular = false
        ),
        Wish(
            id = "lov_08",
            text = "Whatever the future holds, holding your hand gives me courage. You are my true soulmate, today, tomorrow, and for all our years.",
            category = WishCategory.LOVE,
            occasion = "Love",
            isPopular = false
        ),
        Wish(
            id = "lov_09",
            text = "I didn’t know how sweet life could be until you walked into it. Thank you for loving me as I am. You have my heart completely.",
            category = WishCategory.LOVE,
            occasion = "Love",
            isPopular = false
        ),
        Wish(
            id = "lov_10",
            text = "Wrapped in your arms, the coldest seasons feel warm and inviting. Wishing my dearest love boundless happiness and tender moments.",
            category = WishCategory.LOVE,
            occasion = "Love",
            isPopular = false
        ),
        Wish(
            id = "lov_11",
            text = "Two hearts, one rhythm. Thank you for creating a life filled with kindness, understanding, and sweet romance by my side.",
            category = WishCategory.LOVE,
            occasion = "Love",
            isPopular = false
        ),
        Wish(
            id = "lov_12",
            text = "Every love story is special, but ours is my absolute favorite. Wishing you all the joy you so effortlessly bring into my life.",
            category = WishCategory.LOVE,
            occasion = "Love",
            isPopular = true
        ),
        Wish(
            id = "lov_13",
            text = "Looking into your eyes, I still see all the reasons I fell in love with you. Here’s to writing countless more romantic chapters together.",
            category = WishCategory.LOVE,
            occasion = "Love",
            isPopular = false
        ),
        Wish(
            id = "lov_14",
            text = "You turn ordinary moments into extraordinary memories. Thank you for your tenderness, devotion, and wonderful companionship.",
            category = WishCategory.LOVE,
            occasion = "Love",
            isPopular = false
        ),
        Wish(
            id = "lov_15",
            text = "May the love that binds our hearts keep blooming in harmony, trust, and everlasting affection through every season of life.",
            category = WishCategory.LOVE,
            occasion = "Love",
            isPopular = false
        ),
        Wish(
            id = "lov_16",
            text = "You are my favorite thought in the morning and my sweetest dream at night. Loving you is my greatest blessing.",
            category = WishCategory.LOVE,
            occasion = "Love",
            isPopular = false
        ),

        // ==========================================
        // 6. BIRTHDAY WISHES (18 original wishes)
        // ==========================================
        Wish(
            id = "bd_01",
            text = "Happy Birthday! May your day be loaded with sweet treats, great music, hearty laughs, and all your favorite people around you!",
            category = WishCategory.BIRTHDAY,
            occasion = "Birthday",
            isPopular = true
        ),
        Wish(
            id = "bd_02",
            text = "Another year older, wiser, and even more fabulous! Don't count the candles, count the priceless memories you've created. Happy Birthday!",
            category = WishCategory.BIRTHDAY,
            occasion = "Birthday",
            isPopular = true
        ),
        Wish(
            id = "bd_03",
            text = "Happy Birthday to the one who makes my heart flutter! Celebrating you today and thanking the universe for the day you were born.",
            category = WishCategory.BIRTHDAY,
            occasion = "Birthday",
            isPopular = false
        ),
        Wish(
            id = "bd_04",
            text = "Warmest birthday wishes to our cherished family member! May your new age bring you robust health, tranquil peace, and steady prosperity.",
            category = WishCategory.BIRTHDAY,
            occasion = "Birthday",
            isPopular = false
        ),
        Wish(
            id = "bd_05",
            text = "Happy Birthday, buddy! Remember, age is merely the number of years the world has been blessed with your entertaining antics!",
            category = WishCategory.BIRTHDAY,
            occasion = "Birthday",
            isPopular = true
        ),
        Wish(
            id = "bd_06",
            text = "Wishing you a very Happy Birthday! May your professional path flourish and your personal endeavors bring you immense satisfaction this year.",
            category = WishCategory.BIRTHDAY,
            occasion = "Birthday",
            isPopular = false
        ),
        Wish(
            id = "bd_07",
            text = "Wishing you 365 days of good health, bold breakthroughs, and spontaneous smiles. Have a splendid and unforgettable Birthday celebration!",
            category = WishCategory.BIRTHDAY,
            occasion = "Birthday",
            isPopular = false
        ),
        Wish(
            id = "bd_08",
            text = "May your birthday cake be extra sweet and your birthday presents be exactly what you wished for. Have the best celebration ever!",
            category = WishCategory.BIRTHDAY,
            occasion = "Birthday",
            isPopular = false
        ),
        Wish(
            id = "bd_09",
            text = "Happy Birthday to someone who never fails to light up any room they enter. May this upcoming year be your most rewarding yet!",
            category = WishCategory.BIRTHDAY,
            occasion = "Birthday",
            isPopular = true
        ),
        Wish(
            id = "bd_10",
            text = "You're not getting older, you're leveling up in wisdom, humor, and elegance! Wishing you a fantastic and joyous Birthday!",
            category = WishCategory.BIRTHDAY,
            occasion = "Birthday",
            isPopular = false
        ),
        Wish(
            id = "bd_11",
            text = "To my love on their Birthday: My life became so much richer the day you arrived. Here is to making your day as special as you make mine every single day.",
            category = WishCategory.BIRTHDAY,
            occasion = "Birthday",
            isPopular = false
        ),
        Wish(
            id = "bd_12",
            text = "Happy Birthday, colleague! It is a true pleasure collaborating with someone so dedicated, skilled, and encouraging. Enjoy your milestone!",
            category = WishCategory.BIRTHDAY,
            occasion = "Birthday",
            isPopular = false
        ),
        Wish(
            id = "bd_13",
            text = "May God bless this milestone birthday with longevity, abundant happiness, and the fulfillment of your heart's righteous desires.",
            category = WishCategory.BIRTHDAY,
            occasion = "Birthday",
            isPopular = false
        ),
        Wish(
            id = "bd_14",
            text = "Blow out your candles and make a giant wish! You deserve all the magnificent blessings that life has to give. Happy Birthday!",
            category = WishCategory.BIRTHDAY,
            occasion = "Birthday",
            isPopular = false
        ),
        Wish(
            id = "bd_15",
            text = "May your upcoming year be characterized by closed doors that didn't serve you and wide open doors leading to your true calling. Happy Birthday!",
            category = WishCategory.BIRTHDAY,
            occasion = "Birthday",
            isPopular = false
        ),
        Wish(
            id = "bd_16",
            text = "Happy Birthday! May your day be filled with warm calls from old friends, hugs from family, and reasons to dance with delight.",
            category = WishCategory.BIRTHDAY,
            occasion = "Birthday",
            isPopular = false
        ),

        // ==========================================
        // 7. RELIGIOUS WISHES (18 original wishes)
        // ==========================================
        Wish(
            id = "rel_01",
            text = "May the Lord bless you and keep you; may His countenance shine upon your home and grant you deep, abiding peace this season.",
            category = WishCategory.RELIGIOUS,
            occasion = "Faith",
            isPopular = true
        ),
        Wish(
            id = "rel_02",
            text = "Celebrating the miraculous gift of Christ’s birth! May the peace that surpasses human understanding fill your soul with reverence and joy.",
            category = WishCategory.RELIGIOUS,
            occasion = "Faith",
            isPopular = true
        ),
        Wish(
            id = "rel_03",
            text = "As we step into the New Year, may God's gracious hand guide your steps, shield your family, and open fountains of heavenly blessings in your life.",
            category = WishCategory.RELIGIOUS,
            occasion = "Faith",
            isPopular = true
        ),
        Wish(
            id = "rel_04",
            text = "Praying that the Holy Spirit fills your heart with steadfast faith, comforting hope, and limitless charity toward all humanity.",
            category = WishCategory.RELIGIOUS,
            occasion = "Faith",
            isPopular = false
        ),
        Wish(
            id = "rel_05",
            text = "May God’s boundless love illuminate every shadow along your path and remind you that you are never walking alone. Blessed holidays!",
            category = WishCategory.RELIGIOUS,
            occasion = "Faith",
            isPopular = false
        ),
        Wish(
            id = "rel_06",
            text = "In every season of life, God's grace remains sufficient. May you experience His divine comfort, spiritual renewal, and abundant favor today.",
            category = WishCategory.RELIGIOUS,
            occasion = "Faith",
            isPopular = false
        ),
        Wish(
            id = "rel_07",
            text = "Let us give thanks to the Almighty for another year of unmerited mercy, watchful protection, and countless daily provisions.",
            category = WishCategory.RELIGIOUS,
            occasion = "Faith",
            isPopular = true
        ),
        Wish(
            id = "rel_08",
            text = "May the true light of Christmas dispel all anxiety and kindle within your heart a steadfast flame of hope and holy peace.",
            category = WishCategory.RELIGIOUS,
            occasion = "Faith",
            isPopular = false
        ),
        Wish(
            id = "rel_09",
            text = "Praying for divine healing, fruitful harvests in your labor, and heavenly serenity across every room of your residence. God bless you!",
            category = WishCategory.RELIGIOUS,
            occasion = "Faith",
            isPopular = false
        ),
        Wish(
            id = "rel_10",
            text = "Trust in the Lord with all your heart as the calendar turns. He who started a noble work in you will surely carry it to triumphant completion.",
            category = WishCategory.RELIGIOUS,
            occasion = "Faith",
            isPopular = false
        ),
        Wish(
            id = "rel_11",
            text = "May your home be an altar of worship, gratitude, and hospitality. Wishing you a blessed, Christ-centered holiday season.",
            category = WishCategory.RELIGIOUS,
            occasion = "Faith",
            isPopular = false
        ),
        Wish(
            id = "rel_12",
            text = "May the divine warmth of God's presence banish every chill of doubt and fill your heart with quiet, joyful assurance. Stay blessed!",
            category = WishCategory.RELIGIOUS,
            occasion = "Faith",
            isPopular = false
        ),
        Wish(
            id = "rel_13",
            text = "Praise God for His unspeakable gift! May your family be encircled by guardian angels of health, unity, and spiritual fruitfulness.",
            category = WishCategory.RELIGIOUS,
            occasion = "Faith",
            isPopular = false
        ),
        Wish(
            id = "rel_14",
            text = "May the blessing of the Almighty rest upon your coming and going, from this day forward and for all generations to come. Amen.",
            category = WishCategory.RELIGIOUS,
            occasion = "Faith",
            isPopular = true
        ),
        Wish(
            id = "rel_15",
            text = "May your faith be deepened, your prayers answered, and your spirit refreshed by the fountain of living water this sacred season.",
            category = WishCategory.RELIGIOUS,
            occasion = "Faith",
            isPopular = false
        ),
        Wish(
            id = "rel_16",
            text = "Rejoice in the hope of glory! May God's loving kindness be your daily shield and your sweetest song throughout the coming year.",
            category = WishCategory.RELIGIOUS,
            occasion = "Faith",
            isPopular = false
        ),

        // ==========================================
        // 8. THANK YOU WISHES (14 original wishes)
        // ==========================================
        Wish(
            id = "ty_01",
            text = "Thank you from the bottom of my heart for your kindness, patience, and selfless support. Having you in my corner means the world to me!",
            category = WishCategory.THANK_YOU,
            occasion = "Gratitude",
            isPopular = true
        ),
        Wish(
            id = "ty_02",
            text = "The holidays are a time to count our greatest blessings, and knowing you is right at the top of my list. Thank you for your generosity!",
            category = WishCategory.THANK_YOU,
            occasion = "Gratitude",
            isPopular = true
        ),
        Wish(
            id = "ty_03",
            text = "Your thoughtful gesture brightened my entire week. Thank you for taking the time to care and for demonstrating what true thoughtfulness looks like.",
            category = WishCategory.THANK_YOU,
            occasion = "Gratitude",
            isPopular = false
        ),
        Wish(
            id = "ty_04",
            text = "I am deeply grateful for your continuous encouragement and wisdom. You have made a lasting, positive difference in my life. Thank you!",
            category = WishCategory.THANK_YOU,
            occasion = "Gratitude",
            isPopular = false
        ),
        Wish(
            id = "ty_05",
            text = "Words cannot fully convey how much your help meant when I needed it most. Thank you for your selfless heart and ready hands.",
            category = WishCategory.THANK_YOU,
            occasion = "Gratitude",
            isPopular = true
        ),
        Wish(
            id = "ty_06",
            text = "Thank you for the warm hospitality, the delicious food, and the wonderful company! Visiting your home was a true highlight of my season.",
            category = WishCategory.THANK_YOU,
            occasion = "Gratitude",
            isPopular = false
        ),
        Wish(
            id = "ty_07",
            text = "A sincere thank you for your steadfast friendship and loyalty through every up and down. I appreciate and honor our bond so much.",
            category = WishCategory.THANK_YOU,
            occasion = "Gratitude",
            isPopular = false
        ),
        Wish(
            id = "ty_08",
            text = "Thank you for believing in me even when self-doubt tried to creep in. Your encouragement has been my biggest motivator.",
            category = WishCategory.THANK_YOU,
            occasion = "Gratitude",
            isPopular = false
        ),
        Wish(
            id = "ty_09",
            text = "Your thoughtful gift brought the biggest smile to my face! Thank you for knowing me so well and sharing your festive warmth.",
            category = WishCategory.THANK_YOU,
            occasion = "Gratitude",
            isPopular = false
        ),
        Wish(
            id = "ty_10",
            text = "Thank you for being such an extraordinary role model and friend. May life reward you with the same unselfish love you shower upon everyone else.",
            category = WishCategory.THANK_YOU,
            occasion = "Gratitude",
            isPopular = true
        ),
        Wish(
            id = "ty_11",
            text = "Heartfelt thanks for your partnership, dedication, and high standards. Working alongside you makes every challenge enjoyable!",
            category = WishCategory.THANK_YOU,
            occasion = "Gratitude",
            isPopular = false
        ),
        Wish(
            id = "ty_12",
            text = "Thank you for always listening with patience and offering advice filled with kindness. You are an invaluable blessing to me.",
            category = WishCategory.THANK_YOU,
            occasion = "Gratitude",
            isPopular = false
        ),

        // ==========================================
        // 9. BUSINESS WISHES (14 original wishes)
        // ==========================================
        Wish(
            id = "biz_01",
            text = "Wishing our esteemed clients, partners, and friends a joyful holiday season and a prosperous New Year. Thank you for your trust and collaboration!",
            category = WishCategory.BUSINESS,
            occasion = "Business",
            isPopular = true
        ),
        Wish(
            id = "biz_02",
            text = "To our valued customers: It has been an absolute privilege serving you this year. May the coming year bring you immense success, health, and happiness.",
            category = WishCategory.BUSINESS,
            occasion = "Business",
            isPopular = true
        ),
        Wish(
            id = "biz_03",
            text = "Season’s greetings to our fantastic team! Your dedication, resilience, and teamwork made this year a triumph. Enjoy your well-earned holiday break!",
            category = WishCategory.BUSINESS,
            occasion = "Business",
            isPopular = true
        ),
        Wish(
            id = "biz_04",
            text = "To our business partners: Thank you for a year of shared milestones and strong teamwork. We look forward to reaching greater heights together in the New Year.",
            category = WishCategory.BUSINESS,
            occasion = "Business",
            isPopular = false
        ),
        Wish(
            id = "biz_05",
            text = "Warm holiday wishes to our esteemed colleagues! May the new calendar year open exciting doors, groundbreaking achievements, and shared victories.",
            category = WishCategory.BUSINESS,
            occasion = "Business",
            isPopular = false
        ),
        Wish(
            id = "biz_06",
            text = "As the fiscal year draws to a close, we extend our heartfelt gratitude for your loyalty. Wishing your enterprise sustained growth and prosperity.",
            category = WishCategory.BUSINESS,
            occasion = "Business",
            isPopular = false
        ),
        Wish(
            id = "biz_07",
            text = "Happy holidays! May the upcoming year bring strategic clarity, operational excellence, and rewarding outcomes to your organization.",
            category = WishCategory.BUSINESS,
            occasion = "Business",
            isPopular = false
        ),
        Wish(
            id = "biz_08",
            text = "To our dedicated employees: Your creative passion and tireless effort are the backbone of our success. Wishing you peaceful festivities with family.",
            category = WishCategory.BUSINESS,
            occasion = "Business",
            isPopular = false
        ),
        Wish(
            id = "biz_09",
            text = "May the holiday season offer you peaceful relaxation, and may the New Year usher in lucrative partnerships, high productivity, and team satisfaction.",
            category = WishCategory.BUSINESS,
            occasion = "Business",
            isPopular = false
        ),
        Wish(
            id = "biz_10",
            text = "Thank you for choosing to partner with us this year. We remain committed to your success and wish you a thriving, innovative year ahead!",
            category = WishCategory.BUSINESS,
            occasion = "Business",
            isPopular = true
        ),
        Wish(
            id = "biz_11",
            text = "Wishing you a peaceful holiday recharge and a New Year driven by ambition, continuous innovation, and market leadership.",
            category = WishCategory.BUSINESS,
            occasion = "Business",
            isPopular = false
        ),
        Wish(
            id = "biz_12",
            text = "To all our business associates: May your holiday celebrations be warm and the coming business cycles yield remarkable returns on your investments.",
            category = WishCategory.BUSINESS,
            occasion = "Business",
            isPopular = false
        ),

        // ==========================================
        // 10. GENERAL WISHES (14 original wishes)
        // ==========================================
        Wish(
            id = "gen_01",
            text = "May joy be your constant companion, peace your daily roof, and hope your guiding star through every season of celebration. Warmest wishes!",
            category = WishCategory.GENERAL,
            occasion = "Celebration",
            isPopular = true
        ),
        Wish(
            id = "gen_02",
            text = "Wishing you sunny mornings, fruitful afternoons, cozy evenings, and nights filled with peaceful rest. May good fortune walk beside you!",
            category = WishCategory.GENERAL,
            occasion = "Celebration",
            isPopular = true
        ),
        Wish(
            id = "gen_03",
            text = "May every step you take bring you closer to your cherished goals and surround you with genuine reasons to celebrate life’s beauty.",
            category = WishCategory.GENERAL,
            occasion = "Celebration",
            isPopular = false
        ),
        Wish(
            id = "gen_04",
            text = "Here's to celebrating the simple wonders of everyday life—good health, kind words, heartfelt smiles, and peaceful moments.",
            category = WishCategory.GENERAL,
            occasion = "Celebration",
            isPopular = false
        ),
        Wish(
            id = "gen_05",
            text = "May you find abundant reasons to smile today and every day. Wishing you endless cheer, robust health, and joyful serendipity!",
            category = WishCategory.GENERAL,
            occasion = "Celebration",
            isPopular = true
        ),
        Wish(
            id = "gen_06",
            text = "Sending positive vibrations and sincere goodwill your way! May the universe conspire to bring you prosperity, contentment, and triumph.",
            category = WishCategory.GENERAL,
            occasion = "Celebration",
            isPopular = false
        ),
        Wish(
            id = "gen_07",
            text = "May your heart remain open to new possibilities, your mind tranquil, and your spirit resilient against every challenge. Celebrate life!",
            category = WishCategory.GENERAL,
            occasion = "Celebration",
            isPopular = false
        ),
        Wish(
            id = "gen_08",
            text = "Life is a tapestry of moments made rich by loving gestures and shared celebrations. Wishing you a season of sweet contentment and laughter.",
            category = WishCategory.GENERAL,
            occasion = "Celebration",
            isPopular = false
        ),
        Wish(
            id = "gen_09",
            text = "Wishing you good news in your inbox, laughter around your table, and the peaceful satisfaction of a life well-lived. Cheers!",
            category = WishCategory.GENERAL,
            occasion = "Celebration",
            isPopular = true
        ),
        Wish(
            id = "gen_10",
            text = "May every celebration renew your strength, deepen your gratitude, and illuminate the wonderful potential waiting in your tomorrow.",
            category = WishCategory.GENERAL,
            occasion = "Celebration",
            isPopular = false
        ),
        Wish(
            id = "gen_11",
            text = "Wishing you quiet moments of reflection, joyful gatherings with loved ones, and the inner serenity that comes with counting your blessings.",
            category = WishCategory.GENERAL,
            occasion = "Celebration",
            isPopular = false
        ),
        Wish(
            id = "gen_12",
            text = "May happiness knock gently on your door every morning and stay as an honored guest throughout every chapter of your journey!",
            category = WishCategory.GENERAL,
            occasion = "Celebration",
            isPopular = false
        )
    ) + ProWishesData.proWishes

    fun getAllWishes(): List<Wish> = wishesList

    fun getWishesByCategory(category: WishCategory): List<Wish> {
        return when (category) {
            WishCategory.ALL -> wishesList
            WishCategory.PRO -> wishesList.filter { it.isPro }
            else -> wishesList.filter { it.category == category }
        }
    }

    fun getChristmasWishes(): List<Wish> = wishesList.filter { it.category == WishCategory.CHRISTMAS }

    fun getNewYearWishes(): List<Wish> = wishesList.filter { it.category == WishCategory.NEW_YEAR }

    fun getProWishes(): List<Wish> = wishesList.filter { it.isPro }

    fun getPopularWishes(): List<Wish> = wishesList.filter { it.isPopular }
}
