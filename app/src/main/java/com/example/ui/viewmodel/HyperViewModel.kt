package com.example.ui.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.db.AppDatabase
import com.example.data.db.CallEntity
import com.example.data.db.ChatEntity
import com.example.data.db.MessageEntity
import com.example.data.db.StatusEntity
import com.example.data.db.UserEntity
import com.example.data.repository.HyperRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class HyperViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = HyperRepository(database)

    // UI Navigation State
    private val _currentTab = MutableStateFlow(0) // 0: Chats, 1: Updates, 2: Communities, 3: Calls
    val currentTab = _currentTab.asStateFlow()

    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId = _activeChatId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Immersive Dialog States
    private val _activeCall = MutableStateFlow<CallEntity?>(null)
    val activeCall = _activeCall.asStateFlow()

    private val _activeStatus = MutableStateFlow<StatusEntity?>(null)
    val activeStatus = _activeStatus.asStateFlow()

    private val _showNewChatDialog = MutableStateFlow(false)
    val showNewChatDialog = _showNewChatDialog.asStateFlow()

    // Settings & Privacy Settings (Stored in memory/mock for prototype persistence)
    private val _userName = MutableStateFlow("John Doe")
    val userName = _userName.asStateFlow()

    private val _userAvatar = MutableStateFlow("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150")
    val userAvatar = _userAvatar.asStateFlow()

    private val _isFingerprintEnabled = MutableStateFlow(false)
    val isFingerprintEnabled = _isFingerprintEnabled.asStateFlow()

    private val _isPasscodeEnabled = MutableStateFlow(false)
    val isPasscodeEnabled = _isPasscodeEnabled.asStateFlow()

    private val _disappearingMessagesDuration = MutableStateFlow(0) // minutes
    val disappearingMessagesDuration = _disappearingMessagesDuration.asStateFlow()

    private val _isCustomWallpaperActive = MutableStateFlow(false)
    val isCustomWallpaperActive = _isCustomWallpaperActive.asStateFlow()

    private val _accentColorName = MutableStateFlow("Emerald Green") // "Emerald Green", "Midnight Obsidian", "Mint Blue"
    val accentColorName = _accentColorName.asStateFlow()

    private val _storageSizeMb = MutableStateFlow(342.1)
    val storageSizeMb = _storageSizeMb.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping = _isTyping.asStateFlow()

    private val _smartReplies = MutableStateFlow<List<String>>(emptyList())
    val smartReplies = _smartReplies.asStateFlow()

    // Room Database Flows
    val chats = repository.chats
    val users = repository.users
    val calls = repository.calls
    val statuses = repository.statuses

    val activeChatMessages: StateFlow<List<MessageEntity>> = _activeChatId
        .flatMapLatest { chatId ->
            if (chatId != null) {
                repository.getMessagesForChat(chatId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            // Seed the mock SQLite Room DB with Developers and default conversations
            repository.seedMockDataIfEmpty()
        }
    }

    fun setTab(tabIndex: Int) {
        _currentTab.value = tabIndex
    }

    fun selectChat(chatId: String?) {
        _activeChatId.value = chatId
        if (chatId != null) {
            viewModelScope.launch {
                repository.markChatAsRead(chatId)
                generateSmartRepliesForChat(chatId)
            }
        } else {
            _smartReplies.value = emptyList()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleNewChatDialog(show: Boolean) {
        _showNewChatDialog.value = show
    }

    fun updateProfile(name: String, avatar: String) {
        _userName.value = name
        _userAvatar.value = avatar
        Toast.makeText(getApplication(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
    }

    fun toggleFingerprint(enabled: Boolean) {
        _isFingerprintEnabled.value = enabled
    }

    fun togglePasscode(enabled: Boolean) {
        _isPasscodeEnabled.value = enabled
    }

    fun setDisappearingMessages(minutes: Int) {
        _disappearingMessagesDuration.value = minutes
        _activeChatId.value?.let { currentChatId ->
            viewModelScope.launch {
                // Find chat in current list and update it
                chats.first().find { it.id == currentChatId }?.let { chat ->
                    repository.updateChat(chat.copy(disappearingMessages = minutes))
                }
            }
        }
    }

    fun setWallpaperActive(active: Boolean) {
        _isCustomWallpaperActive.value = active
    }

    fun setAccentColor(color: String) {
        _accentColorName.value = color
    }

    fun clearCache() {
        viewModelScope.launch {
            _storageSizeMb.value = 4.2 // cleared, keeping basic application size
            Toast.makeText(getApplication(), "Cache cleared successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    fun createChatWithUser(user: UserEntity) {
        viewModelScope.launch {
            val chatId = "chat_${user.id}"
            val newChat = ChatEntity(
                id = chatId,
                title = user.name,
                avatarUrl = user.avatarUrl,
                type = "ONE_TO_ONE",
                unreadCount = 0
            )
            repository.insertChat(newChat)
            _showNewChatDialog.value = false
            selectChat(chatId)
        }
    }

    fun createGroupChat(title: String, userIds: List<String>) {
        viewModelScope.launch {
            val chatId = "chat_group_${UUID.randomUUID().toString().take(6)}"
            val groupChat = ChatEntity(
                id = chatId,
                title = title,
                avatarUrl = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=150",
                type = "GROUP"
            )
            repository.insertChat(groupChat)
            
            // Insert initial welcome message
            val welcome = MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                senderId = "system",
                senderName = "System",
                text = "Welcome to '$title' group created by Tucci Cyber Nation & Kawooya Raymond.",
                type = "TEXT",
                timestamp = System.currentTimeMillis(),
                status = "SENT"
            )
            repository.insertMessage(welcome)
            _showNewChatDialog.value = false
            selectChat(chatId)
        }
    }

    fun createChannel(title: String) {
        viewModelScope.launch {
            val chatId = "chat_chan_${UUID.randomUUID().toString().take(6)}"
            val channel = ChatEntity(
                id = chatId,
                title = title,
                avatarUrl = "https://images.unsplash.com/photo-1557200134-90327ee9fafa?w=150",
                type = "CHANNEL"
            )
            repository.insertChat(channel)
            _showNewChatDialog.value = false
            selectChat(chatId)
        }
    }

    fun sendMessage(
        text: String,
        type: String = "TEXT",
        replyToId: String? = null,
        replyToText: String? = null,
        attachments: String? = null,
        voiceDuration: Int = 0,
        isViewOnce: Boolean = false,
        pollOptions: String? = null
    ) {
        val chatId = _activeChatId.value ?: return
        viewModelScope.launch {
            val messageId = UUID.randomUUID().toString()
            val senderName = _userName.value
            val senderId = "self"

            val message = MessageEntity(
                id = messageId,
                chatId = chatId,
                senderId = senderId,
                senderName = senderName,
                text = text,
                type = type,
                timestamp = System.currentTimeMillis(),
                isRead = true,
                status = "SENT",
                replyToId = replyToId,
                replyToText = replyToText,
                attachments = attachments,
                voiceDuration = voiceDuration,
                isViewOnce = isViewOnce,
                pollOptions = pollOptions
            )
            repository.insertMessage(message)

            // Trigger AI Smart feedback if the chat is Hyper AI
            val chat = chats.first().find { it.id == chatId }
            if (chat != null && (chat.type == "HYPER_AI" || chat.id == "chat_hyper_ai")) {
                handleHyperAiResponse(text)
            } else {
                // Occasional automatic reply simulation from Raymond or Tucci for a dynamic feel:
                if (chat != null && chat.type == "ONE_TO_ONE") {
                    simulateUserReply(chat)
                }
            }
        }
    }

    private fun handleHyperAiResponse(userText: String) {
        viewModelScope.launch {
            _isTyping.value = true
            delay(1500) // organic typing speed simulation

            val sysInst = "You are 'Hyper AI', a custom smart assistant integrated inside the premium 'Hyper Chat' app, " +
                    "developed with supreme engineering by Tucci Cyber Nation and Kawooya Raymond. " +
                    "Respond with professional intelligence, clarity, and bold style. Encourage the user to explore technical efficiency."

            val rawResponse = GeminiClient.generateContent(userText, sysInst)

            val aiMessage = MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "chat_hyper_ai",
                senderId = "hyper_ai",
                senderName = "Hyper AI",
                text = rawResponse,
                type = "TEXT",
                timestamp = System.currentTimeMillis(),
                isRead = false,
                status = "READ"
            )
            repository.insertMessage(aiMessage)
            _isTyping.value = false
        }
    }

    private suspend fun simulateUserReply(chat: ChatEntity) {
        delay(2000)
        _isTyping.value = true
        delay(1500)

        val text = when (chat.id) {
            "chat_raymond" -> "Awesome message! I am reviewing the biometric security standards and dynamic Material 3 overlays with Kawooya Raymond now. Stay tuned!"
            "chat_tucci" -> "Affirmative. Encryption protocols active. Tucci cyber scanners indicate a 100% secure build pipeline!"
            "elon" -> "SpaceX launch on schedule. Starlink hyper-speeds fully sync with Hyper Chat!"
            else -> "Superb! Received your message. Let's build something grand!"
        }

        val partnerMessage = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chat.id,
            senderId = when (chat.id) {
                "chat_raymond" -> "raymond"
                "chat_tucci" -> "tucci"
                "elon" -> "elon"
                else -> "sender_${chat.id}"
            },
            senderName = chat.title,
            text = text,
            type = "TEXT",
            timestamp = System.currentTimeMillis(),
            isRead = false,
            status = "DELIVERED"
        )
        repository.insertMessage(partnerMessage)
        repository.updateChat(chat.copy(unreadCount = chat.unreadCount + 1))
        _isTyping.value = false
    }

    private fun generateSmartRepliesForChat(chatId: String) {
        val replies = when (chatId) {
            "chat_raymond" -> listOf("Perfect layout!", "Looks fast Kawooya!", "Compiles great 🚀")
            "chat_tucci" -> listOf("Enforce standard keys 🔑", "Scanning now", "Verified ok")
            "chat_hyper_ai" -> listOf("Summarize this chat", "Translate to Spanish", "Help me code")
            else -> listOf("Sounds good!", "Got it", "Talk soon")
        }
        _smartReplies.value = replies
    }

    // Call Actions
    fun startCall(userId: String, isVideo: Boolean) {
        viewModelScope.launch {
            val user = users.first().find { it.id == userId }
            if (user != null) {
                val call = CallEntity(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    userName = user.name,
                    userAvatar = user.avatarUrl,
                    type = if (isVideo) "VIDEO" else "VOICE",
                    direction = "OUTGOING",
                    timestamp = System.currentTimeMillis(),
                    duration = 0 // ongoing
                )
                _activeCall.value = call
            }
        }
    }

    fun receiveIncomingCall(userId: String, isVideo: Boolean) {
        viewModelScope.launch {
            val user = users.first().find { it.id == userId }
            if (user != null) {
                val call = CallEntity(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    userName = user.name,
                    userAvatar = user.avatarUrl,
                    type = if (isVideo) "VIDEO" else "VOICE",
                    direction = "INCOMING",
                    timestamp = System.currentTimeMillis(),
                    duration = 0
                )
                _activeCall.value = call
            }
        }
    }

    fun acceptCall() {
        val currentCall = _activeCall.value ?: return
        viewModelScope.launch {
            // Simulated answered state
            Toast.makeText(getApplication(), "Call Connected", Toast.LENGTH_SHORT).show()
        }
    }

    fun endCall(durationSec: Int = 12) {
        val currentCall = _activeCall.value ?: return
        viewModelScope.launch {
            val finishedCall = currentCall.copy(duration = durationSec)
            repository.insertCall(finishedCall)
            _activeCall.value = null
            Toast.makeText(getApplication(), "Call Ended. Duration: ${durationSec}s", Toast.LENGTH_SHORT).show()
        }
    }

    // Status Actions
    fun postStatus(text: String?, imageUrl: String?, type: String) {
        viewModelScope.launch {
            val status = StatusEntity(
                id = UUID.randomUUID().toString(),
                userId = "self",
                userName = _userName.value,
                userAvatar = _userAvatar.value,
                mediaUrl = imageUrl,
                text = text,
                type = type,
                timestamp = System.currentTimeMillis()
            )
            repository.insertStatus(status)
            Toast.makeText(getApplication(), "Status posted successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    fun viewStatusItem(status: StatusEntity?) {
        _activeStatus.value = status
    }

    fun addStatusReaction(statusId: String, emoji: String) {
        viewModelScope.launch {
            statuses.first().find { it.id == statusId }?.let { status ->
                val updatedReactions = if (status.reactions.isEmpty()) emoji else "${status.reactions},$emoji"
                repository.insertStatus(status.copy(reactions = updatedReactions))
                Toast.makeText(getApplication(), "Reacted with $emoji", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteStatus(statusId: String) {
        viewModelScope.launch {
            database.statusDao().deleteStatus(statusId)
            Toast.makeText(getApplication(), "Status deleted", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteChatById(chatId: String) {
        viewModelScope.launch {
            repository.deleteChat(chatId)
            if (_activeChatId.value == chatId) {
                _activeChatId.value = null
            }
            Toast.makeText(getApplication(), "Chat deleted", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearCallHistory() {
        viewModelScope.launch {
            database.callDao().clearAllCalls()
            Toast.makeText(getApplication(), "Call history cleared", Toast.LENGTH_SHORT).show()
        }
    }
}
