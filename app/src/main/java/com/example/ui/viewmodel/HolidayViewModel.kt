package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AiGeneratedWishEntity
import com.example.data.local.AppDatabase
import com.example.data.local.CardRepository
import com.example.data.local.FavoriteRepository
import com.example.data.local.FavoriteWishEntity
import com.example.data.local.SavedCardEntity
import com.example.data.repository.AiWishRepository
import com.example.data.repository.MockAiWishRepository
import com.example.data.repository.CardTemplatesRepository
import com.example.data.repository.MonetizationManager
import com.example.data.repository.PricingTiers
import com.example.data.repository.WishGenerator
import com.example.data.repository.WishesRepository
import com.example.model.AiLanguage
import com.example.model.AiOccasion
import com.example.model.AiRecipient
import com.example.model.AiTone
import com.example.model.AiWishRequest
import com.example.model.AiWishResponse
import com.example.model.CardDecorationStyle
import com.example.model.CardTemplate
import com.example.model.GeneratorOccasion
import com.example.model.GeneratorRecipient
import com.example.model.GeneratorRelationship
import com.example.model.GeneratorTone
import com.example.model.Wish
import com.example.model.WishCategory
import com.example.model.WishGenerationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class SettingsDialogType {
    ABOUT, LANGUAGE, NOTIFICATIONS, PRIVACY_POLICY, TERMS_OF_SERVICE
}

class HolidayViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val favoriteRepository = FavoriteRepository(db.favoriteWishDao())
    private val cardRepository = CardRepository(db.savedCardDao())
    private val monetizationManager = MonetizationManager.getInstance(application)

    val isProUser: StateFlow<Boolean> = monetizationManager.isProUser
    val isVipUser: StateFlow<Boolean> = monetizationManager.isVipUser
    val unlockedCardIds: StateFlow<Set<String>> = monetizationManager.unlockedCardIds

    // All favorites from Room DB
    val favorites: StateFlow<List<FavoriteWishEntity>> = favoriteRepository.allFavorites
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteIds: StateFlow<Set<String>> = favorites.map { list ->
        list.map { it.id }.toSet()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    // All saved cards from Room DB
    val savedCards: StateFlow<List<SavedCardEntity>> = cardRepository.allSavedCards
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // AI Wish Generator Architecture & State
    private val aiWishRepository: AiWishRepository = MockAiWishRepository()
    private val aiGeneratedWishDao = db.aiGeneratedWishDao()

    val aiWishHistory: StateFlow<List<AiGeneratedWishEntity>> = aiGeneratedWishDao.getAllHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _aiOccasion = MutableStateFlow(AiOccasion.CHRISTMAS)
    val aiOccasion: StateFlow<AiOccasion> = _aiOccasion.asStateFlow()

    private val _aiRecipient = MutableStateFlow(AiRecipient.MOTHER)
    val aiRecipient: StateFlow<AiRecipient> = _aiRecipient.asStateFlow()

    private val _aiRecipientName = MutableStateFlow("")
    val aiRecipientName: StateFlow<String> = _aiRecipientName.asStateFlow()

    private val _aiTone = MutableStateFlow(AiTone.WARM)
    val aiTone: StateFlow<AiTone> = _aiTone.asStateFlow()

    private val _aiLanguage = MutableStateFlow(AiLanguage.ENGLISH)
    val aiLanguage: StateFlow<AiLanguage> = _aiLanguage.asStateFlow()

    private val _aiPersonalDetails = MutableStateFlow("")
    val aiPersonalDetails: StateFlow<String> = _aiPersonalDetails.asStateFlow()

    private val _aiIsGenerating = MutableStateFlow(false)
    val aiIsGenerating: StateFlow<Boolean> = _aiIsGenerating.asStateFlow()

    private val _aiGeneratedResult = MutableStateFlow<AiWishResponse?>(null)
    val aiGeneratedResult: StateFlow<AiWishResponse?> = _aiGeneratedResult.asStateFlow()

    private val _aiErrorMessage = MutableStateFlow<String?>(null)
    val aiErrorMessage: StateFlow<String?> = _aiErrorMessage.asStateFlow()

    // Wishes Screen State
    private val _selectedCategory = MutableStateFlow(WishCategory.ALL)
    val selectedCategory: StateFlow<WishCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredWishes: StateFlow<List<Wish>> = combine(
        _selectedCategory,
        _searchQuery
    ) { category, query ->
        val list = WishesRepository.getWishesByCategory(category)
        if (query.isBlank()) {
            list
        } else {
            list.filter { it.text.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WishesRepository.getAllWishes()
    )

    // Generator State
    private val _genOccasion = MutableStateFlow(GeneratorOccasion.CHRISTMAS)
    val genOccasion: StateFlow<GeneratorOccasion> = _genOccasion.asStateFlow()

    private val _genRecipient = MutableStateFlow(GeneratorRecipient.FAMILY)
    val genRecipient: StateFlow<GeneratorRecipient> = _genRecipient.asStateFlow()

    private val _genRelationship = MutableStateFlow(GeneratorRelationship.CLOSE)
    val genRelationship: StateFlow<GeneratorRelationship> = _genRelationship.asStateFlow()

    private val _genTone = MutableStateFlow(GeneratorTone.HEARTFELT)
    val genTone: StateFlow<GeneratorTone> = _genTone.asStateFlow()

    private val _personalMessage = MutableStateFlow("")
    val personalMessage: StateFlow<String> = _personalMessage.asStateFlow()

    private val _generatedWishes = MutableStateFlow<List<WishGenerationResult>>(emptyList())
    val generatedWishes: StateFlow<List<WishGenerationResult>> = _generatedWishes.asStateFlow()

    // Cards State
    val cardTemplates: List<CardTemplate> = CardTemplatesRepository.getAllTemplates()

    // Customization Screen active editing state
    private val _selectedCardTemplate = MutableStateFlow(cardTemplates.first())
    val selectedCardTemplate: StateFlow<CardTemplate> = _selectedCardTemplate.asStateFlow()

    private val _cardRecipientName = MutableStateFlow("")
    val cardRecipientName: StateFlow<String> = _cardRecipientName.asStateFlow()

    private val _cardCustomMessage = MutableStateFlow(cardTemplates.first().defaultMessage)
    val cardCustomMessage: StateFlow<String> = _cardCustomMessage.asStateFlow()

    private val _cardSenderName = MutableStateFlow("")
    val cardSenderName: StateFlow<String> = _cardSenderName.asStateFlow()

    private val _cardTextSizeSp = MutableStateFlow(15f)
    val cardTextSizeSp: StateFlow<Float> = _cardTextSizeSp.asStateFlow()

    private val _cardTextAlign = MutableStateFlow("Center") // "Left", "Center", "Right"
    val cardTextAlign: StateFlow<String> = _cardTextAlign.asStateFlow()

    private val _showRecipient = MutableStateFlow(true)
    val showRecipient: StateFlow<Boolean> = _showRecipient.asStateFlow()

    private val _showSender = MutableStateFlow(true)
    val showSender: StateFlow<Boolean> = _showSender.asStateFlow()

    private val _cardFontStyle = MutableStateFlow("Classic")
    val cardFontStyle: StateFlow<String> = _cardFontStyle.asStateFlow()

    private val _cardAspectRatio = MutableStateFlow("Standard")
    val cardAspectRatio: StateFlow<String> = _cardAspectRatio.asStateFlow()

    private val _cardStickers = MutableStateFlow<List<String>>(emptyList())
    val cardStickers: StateFlow<List<String>> = _cardStickers.asStateFlow()

    private val _cardPhotoUri = MutableStateFlow<String?>(null)
    val cardPhotoUri: StateFlow<String?> = _cardPhotoUri.asStateFlow()

    private val _snowEffectsEnabled = MutableStateFlow(true)
    val snowEffectsEnabled: StateFlow<Boolean> = _snowEffectsEnabled.asStateFlow()

    // Settings State
    private val _activeDialog = MutableStateFlow<SettingsDialogType?>(null)
    val activeDialog: StateFlow<SettingsDialogType?> = _activeDialog.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _countdownEnabled = MutableStateFlow(true)
    val countdownEnabled: StateFlow<Boolean> = _countdownEnabled.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    init {
        // Initial generation so the generator screen shows ready suggestions immediately
        generateWish()
    }

    // Actions
    fun selectCategory(category: WishCategory) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(wish: Wish) {
        viewModelScope.launch {
            if (favoriteIds.value.contains(wish.id)) {
                favoriteRepository.removeFavorite(wish.id)
            } else {
                favoriteRepository.addFavorite(wish)
            }
        }
    }

    fun toggleFavoriteGenerated(result: WishGenerationResult) {
        viewModelScope.launch {
            if (favoriteIds.value.contains(result.id)) {
                favoriteRepository.removeFavorite(result.id)
            } else {
                favoriteRepository.addCustomFavorite(
                    id = result.id,
                    text = result.wishText,
                    category = result.occasion.label,
                    occasion = result.occasion.label
                )
            }
        }
    }

    fun removeFavoriteById(id: String) {
        viewModelScope.launch {
            favoriteRepository.removeFavorite(id)
        }
    }

    // Generator setters
    fun setGenOccasion(occasion: GeneratorOccasion) {
        _genOccasion.value = occasion
    }

    fun setGenRecipient(recipient: GeneratorRecipient) {
        _genRecipient.value = recipient
    }

    fun setGenRelationship(relationship: GeneratorRelationship) {
        _genRelationship.value = relationship
    }

    fun setGenTone(tone: GeneratorTone) {
        _genTone.value = tone
    }

    fun setPersonalMessage(msg: String) {
        _personalMessage.value = msg
    }

    fun generateWish() {
        val results = WishGenerator.generateWishes(
            occasion = _genOccasion.value,
            recipient = _genRecipient.value,
            relationship = _genRelationship.value,
            tone = _genTone.value,
            personalNote = _personalMessage.value
        )
        _generatedWishes.value = results
    }

    // Cards actions
    fun prepareCardForCustomization(template: CardTemplate) {
        _selectedCardTemplate.value = template
        _cardRecipientName.value = ""
        _cardCustomMessage.value = template.defaultMessage
        _cardSenderName.value = ""
        _cardTextSizeSp.value = 15f
        _cardTextAlign.value = "Center"
        _showRecipient.value = true
        _showSender.value = true
    }

    fun updateCardRecipient(name: String) {
        _cardRecipientName.value = name
    }

    fun updateCardMessage(message: String) {
        _cardCustomMessage.value = message
    }

    fun updateCardSender(name: String) {
        _cardSenderName.value = name
    }

    fun updateCardTextSize(sizeSp: Float) {
        _cardTextSizeSp.value = sizeSp
    }

    fun updateCardTextAlign(alignment: String) {
        _cardTextAlign.value = alignment
    }

    fun toggleShowRecipient(show: Boolean) {
        _showRecipient.value = show
    }

    fun toggleShowSender(show: Boolean) {
        _showSender.value = show
    }

    // Save Card to Room DB
    fun saveCurrentCard(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val template = _selectedCardTemplate.value
            val newCard = SavedCardEntity(
                id = UUID.randomUUID().toString(),
                templateId = template.id,
                title = template.title,
                subtitle = template.subtitle,
                occasion = template.occasion.name,
                recipientName = _cardRecipientName.value.trim(),
                message = _cardCustomMessage.value.trim(),
                senderName = _cardSenderName.value.trim(),
                primaryColorHex = template.primaryColorHex,
                secondaryColorHex = template.secondaryColorHex,
                accentColorHex = template.accentColorHex,
                decorationStyle = template.decorationStyle.name,
                textSizeSp = _cardTextSizeSp.value,
                textAlign = _cardTextAlign.value,
                showRecipient = _showRecipient.value,
                showSender = _showSender.value,
                createdAt = System.currentTimeMillis()
            )
            cardRepository.saveCard(newCard)
            onSuccess()
        }
    }

    fun deleteSavedCard(id: String) {
        viewModelScope.launch {
            cardRepository.deleteCard(id)
        }
    }

    fun setCardFontStyle(style: String) {
        _cardFontStyle.value = style
    }

    fun setCardAspectRatio(ratio: String) {
        _cardAspectRatio.value = ratio
    }

    fun toggleCardSticker(sticker: String) {
        val current = _cardStickers.value.toMutableList()
        if (current.contains(sticker)) {
            current.remove(sticker)
        } else {
            if (current.size < 4) {
                current.add(sticker)
            }
        }
        _cardStickers.value = current
    }

    fun setCardPhotoUri(uri: String?) {
        _cardPhotoUri.value = uri
    }

    fun toggleSnowEffects() {
        _snowEffectsEnabled.value = !_snowEffectsEnabled.value
    }

    // Settings actions
    fun openDialog(type: SettingsDialogType) {
        _activeDialog.value = type
    }

    fun closeDialog() {
        _activeDialog.value = null
    }

    fun toggleNotifications() {
        _notificationsEnabled.value = !_notificationsEnabled.value
    }

    fun toggleCountdown() {
        _countdownEnabled.value = !_countdownEnabled.value
    }

    fun setLanguage(language: String) {
        _selectedLanguage.value = language
        closeDialog()
    }

    // AI Wish Generator Actions
    fun setAiOccasion(occasion: AiOccasion) {
        _aiOccasion.value = occasion
    }

    fun setAiRecipient(recipient: AiRecipient) {
        _aiRecipient.value = recipient
    }

    fun setAiRecipientName(name: String) {
        _aiRecipientName.value = name
    }

    fun setAiTone(tone: AiTone) {
        _aiTone.value = tone
    }

    fun setAiLanguage(language: AiLanguage) {
        _aiLanguage.value = language
    }

    fun setAiPersonalDetails(details: String) {
        _aiPersonalDetails.value = details
    }

    fun clearAiErrorMessage() {
        _aiErrorMessage.value = null
    }

    fun generateAiWish() {
        viewModelScope.launch {
            _aiIsGenerating.value = true
            _aiErrorMessage.value = null

            val request = AiWishRequest(
                occasion = _aiOccasion.value,
                recipient = _aiRecipient.value,
                recipientName = _aiRecipientName.value.trim(),
                tone = _aiTone.value,
                language = _aiLanguage.value,
                personalDetails = _aiPersonalDetails.value.trim(),
                variationSeed = System.currentTimeMillis()
            )

            val result = aiWishRepository.generateWish(request)
            result.onSuccess { response ->
                _aiGeneratedResult.value = response
                val entity = AiGeneratedWishEntity(
                    id = response.id,
                    text = response.text,
                    occasion = response.occasion.displayName,
                    recipient = response.recipient.displayName,
                    recipientName = response.recipientName,
                    tone = response.tone.displayName,
                    language = response.language.displayName,
                    createdAt = response.createdAt
                )
                aiGeneratedWishDao.insertWish(entity)
            }.onFailure {
                _aiErrorMessage.value = "Something went wrong. Please try again."
            }

            _aiIsGenerating.value = false
        }
    }

    fun generateAgainAiWish() {
        generateAiWish()
    }

    fun saveAiWishToFavorites(wishText: String, occasionName: String) {
        viewModelScope.launch {
            favoriteRepository.addCustomFavorite(
                id = UUID.randomUUID().toString(),
                text = wishText,
                category = occasionName,
                occasion = occasionName
            )
        }
    }

    fun deleteAiWishHistory(id: String) {
        viewModelScope.launch {
            aiGeneratedWishDao.deleteWishById(id)
        }
    }

    fun prepareCardFromAiWish(wishText: String, recipientName: String = "") {
        val isNewYear = _aiOccasion.value == AiOccasion.NEW_YEAR
        val template = if (isNewYear) {
            CardTemplatesRepository.getNewYearTemplates().first()
        } else {
            CardTemplatesRepository.getChristmasTemplates().first()
        }

        _selectedCardTemplate.value = template
        _cardRecipientName.value = recipientName
        _cardCustomMessage.value = wishText
        _cardSenderName.value = ""
        _cardTextSizeSp.value = 14f
        _cardTextAlign.value = "Center"
        _showRecipient.value = recipientName.isNotBlank()
        _showSender.value = true
    }

    val unlockedWishIds: StateFlow<Set<String>> = monetizationManager.unlockedWishIds

    fun isWishUnlocked(wish: com.example.model.Wish): Boolean {
        return monetizationManager.isWishUnlocked(wish.id, wish.isPro)
    }

    fun unlockWishViaRewardedAd(wishId: String) {
        monetizationManager.unlockWishViaRewardedAd(wishId)
    }

    fun isCardUnlocked(template: CardTemplate): Boolean {
        return monetizationManager.isCardUnlocked(template.id, template.isPro)
    }

    fun purchaseProTier(tierId: String): Boolean {
        return monetizationManager.completePurchase(tierId)
    }

    fun unlockCardViaRewardedAd(cardId: String) {
        monetizationManager.unlockCardViaRewardedAd(cardId)
    }

    fun restorePurchases(): Boolean {
        return monetizationManager.restorePurchases()
    }
}
