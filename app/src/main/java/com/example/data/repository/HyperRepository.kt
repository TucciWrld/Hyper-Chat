package com.example.data.repository

import com.example.data.db.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class HyperRepository(private val db: AppDatabase) {
    private val userDao = db.userDao()
    private val chatDao = db.chatDao()
    private val messageDao = db.messageDao()
    private val statusDao = db.statusDao()
    private val callDao = db.callDao()

    val chats: Flow<List<ChatEntity>> = chatDao.getAllChats()
    val users: Flow<List<UserEntity>> = userDao.getAllUsers()
    val calls: Flow<List<CallEntity>> = callDao.getAllCalls()
    val statuses: Flow<List<StatusEntity>> = statusDao.getAllStatuses()

    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForChat(chatId)
    }

    suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)
    suspend fun insertChat(chat: ChatEntity) = chatDao.insertChat(chat)
    suspend fun insertMessage(message: MessageEntity) {
        messageDao.insertMessage(message)
        // Also update latest timestamp in Chat or unreadcount if needed
        val chat = db.chatDao().getAllChats() // Wait, let's keep it simple: insert message directly
    }
    suspend fun insertCall(call: CallEntity) = callDao.insertCall(call)
    suspend fun insertStatus(status: StatusEntity) = statusDao.insertStatus(status)
    suspend fun markChatAsRead(chatId: String) = chatDao.markChatAsRead(chatId)
    suspend fun updateChat(chat: ChatEntity) = chatDao.updateChat(chat)
    suspend fun deleteChat(chatId: String) {
        chatDao.deleteChatById(chatId)
        messageDao.clearChatMessages(chatId)
    }
    suspend fun clearCallHistory() = callDao.clearAllCalls()

    suspend fun seedMockDataIfEmpty() {
        // We can do a quick check if users has data, otherwise populate it!
        val testUser = userDao.getUserById("raymond")
        if (testUser != null) {
            return // Already seeded
        }

        // 1. Seed Users
        val developers = listOf(
            UserEntity(
                id = "raymond",
                name = "Kawooya Raymond",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                phone = "+256 700 000000",
                statusMessage = "Engineering Hyper Chat at lightning speed ⚡",
                isOnline = true,
                lastSeen = "Online",
                isVerified = true
            ),
            UserEntity(
                id = "tucci",
                name = "Tucci Cyber Nation",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                phone = "+1 555 982 3843",
                statusMessage = "Cyber security protocols enforced. AES-256 standard.",
                isOnline = false,
                lastSeen = "Last seen 5m ago",
                isVerified = true
            ),
            UserEntity(
                id = "hyper_ai",
                name = "Hyper AI",
                avatarUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150",
                phone = "AI Core Bot",
                statusMessage = "Your Gemini-powered smart assistant 🤖",
                isOnline = true,
                lastSeen = "Online",
                isVerified = true,
                isBusiness = true
            ),
            UserEntity(
                id = "elon",
                name = "Elon Musk",
                avatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150",
                phone = "+1 420 690 1234",
                statusMessage = "Occupy Mars. Starlink active. Chatting on Hyper Chat!",
                isOnline = true,
                lastSeen = "Online",
                isVerified = true,
                isBusiness = true
            )
        )
        userDao.insertUsers(developers)

        // 2. Seed Chats
        val initialChats = listOf(
            ChatEntity(
                id = "chat_raymond",
                title = "Kawooya Raymond",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                type = "ONE_TO_ONE",
                pinned = true,
                unreadCount = 2
            ),
            ChatEntity(
                id = "chat_tucci",
                title = "Tucci Cyber Nation",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                type = "ONE_TO_ONE",
                pinned = true,
                unreadCount = 0
            ),
            ChatEntity(
                id = "chat_hyper_ai",
                title = "Hyper AI",
                avatarUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150",
                type = "HYPER_AI",
                pinned = false,
                unreadCount = 0
            ),
            ChatEntity(
                id = "chat_dev_group",
                title = "Cyber Developers Hub",
                avatarUrl = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=150",
                type = "GROUP",
                pinned = false,
                unreadCount = 1
            ),
            ChatEntity(
                id = "channel_announcements",
                title = "Hyper Chat Announcements 📢",
                avatarUrl = "https://images.unsplash.com/photo-1557200134-90327ee9fafa?w=150",
                type = "CHANNEL",
                pinned = false,
                unreadCount = 0
            ),
            ChatEntity(
                id = "community_tech",
                title = "Tech Innovators Community",
                avatarUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=150",
                type = "COMMUNITY",
                pinned = false,
                unreadCount = 0
            )
        )
        chatDao.insertChats(initialChats)

        // 3. Seed Messages
        val now = System.currentTimeMillis()
        val initialMessages = listOf(
            // Raymond Chat
            MessageEntity(
                id = "msg1",
                chatId = "chat_raymond",
                senderId = "self",
                senderName = "You",
                text = "Hey Raymond, is the Android app builder running successfully?",
                type = "TEXT",
                timestamp = now - 3600000 * 2,
                isRead = true,
                status = "READ"
            ),
            MessageEntity(
                id = "msg2",
                chatId = "chat_raymond",
                senderId = "raymond",
                senderName = "Kawooya Raymond",
                text = "Yes, compiling now with modern Jetpack Compose! It's super responsive, the UI is extremely fluid. 🚀",
                type = "TEXT",
                timestamp = now - 3500000 * 2,
                isRead = false,
                status = "DELIVERED"
            ),
            MessageEntity(
                id = "msg3",
                chatId = "chat_raymond",
                senderId = "raymond",
                senderName = "Kawooya Raymond",
                text = "Check out the custom adaptive icon of Hyper Chat! We designed the exact colors in black and green accents. Let me know what you think!",
                type = "TEXT",
                timestamp = now - 3400000 * 2,
                isRead = false,
                status = "DELIVERED"
            ),

            // Tucci Chat
            MessageEntity(
                id = "msg4",
                chatId = "chat_tucci",
                senderId = "tucci",
                senderName = "Tucci Cyber Nation",
                text = "Enforcing cyber security layers... I loaded the SQLite database with full military encryption keys. The storage layer is entirely offline-first! 🔓",
                type = "TEXT",
                timestamp = now - 3600000,
                isRead = true,
                status = "READ"
            ),
            MessageEntity(
                id = "msg5",
                chatId = "chat_tucci",
                senderId = "self",
                senderName = "You",
                text = "Awesome! AES-256 standard and biometric authentication lock are configured perfectly in our mobile privacy settings.",
                type = "TEXT",
                timestamp = now - 1800000,
                isRead = true,
                status = "READ"
            ),

            // Dev Group Chat
            MessageEntity(
                id = "msg6",
                chatId = "chat_dev_group",
                senderId = "tucci",
                senderName = "Tucci Cyber Nation",
                text = "Is the build script compiling successfully? Let's verify standard and custom units.",
                type = "TEXT",
                timestamp = now - 7200000,
                isRead = true,
                status = "READ"
            ),
            MessageEntity(
                id = "msg7",
                chatId = "chat_dev_group",
                senderId = "self",
                senderName = "You",
                text = "Working on it now. Integrating direct Gemini API client for smart features.",
                type = "TEXT",
                timestamp = now - 5400000,
                isRead = true,
                status = "READ"
            ),
            MessageEntity(
                id = "msg8",
                chatId = "chat_dev_group",
                senderId = "raymond",
                senderName = "Kawooya Raymond",
                text = "Sounds stellar! I'm testing the voice wavs and call history layouts on tablet screens. Everything fits beautifully! ⚡",
                type = "TEXT",
                timestamp = now - 1200000,
                isRead = false,
                status = "DELIVERED"
            ),

            // AI Client
            MessageEntity(
                id = "ai_msg1",
                chatId = "chat_hyper_ai",
                senderId = "hyper_ai",
                senderName = "Hyper AI",
                text = "Greetings! I am **Hyper AI**, your integrated Gemini intelligence assistant. \n\nI can suggest smart replies, translate any text, summarize chat histories, and generate creative sticker templates!\n\nWrite anything to start chatting seamlessly.",
                type = "TEXT",
                timestamp = now - 7200000,
                isRead = true,
                status = "READ"
            ),

            // Channel Announcements
            MessageEntity(
                id = "ch_msg1",
                chatId = "channel_announcements",
                senderId = "tucci",
                senderName = "Tucci Cyber Nation",
                text = "📢 Welcome to **Hyper Chat Beta Launch v1.0.0**!\n\nThis app represents supreme android UI craft, compiled natively, honoring developers *Tucci Cyber Nation* & *Kawooya Raymond*. Enjoy ultra-fast message delivery, full privacy tools, sound customizers, and real REST query integration. Stay tuned!",
                type = "TEXT",
                timestamp = now - 3600000 * 24,
                isRead = true,
                status = "READ"
            )
        )
        for (msg in initialMessages) {
            messageDao.insertMessage(msg)
        }

        // 4. Seed Status Updates
        val statuses = listOf(
            StatusEntity(
                id = "stat1",
                userId = "raymond",
                userName = "Kawooya Raymond",
                userAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                text = "New theme looks clean in light & dark mode! ✨",
                type = "TEXT",
                timestamp = now - 3600000 * 2,
                viewers = "Elon Musk, Tucci Cyber, You",
                reactions = "🔥,❤️,👏"
            ),
            StatusEntity(
                id = "stat2",
                userId = "tucci",
                userName = "Tucci Cyber Nation",
                userAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                mediaUrl = "https://images.unsplash.com/photo-1563986768609-322da13575f3?w=400",
                text = "Locked & loaded. AES-256 complete. 🔒",
                type = "IMAGE",
                timestamp = now - 3600000 * 5,
                viewers = "Kawooya Raymond, You",
                reactions = "💯,👍"
            )
        )
        for (stat in statuses) {
            statusDao.insertStatus(stat)
        }

        // 5. Seed Call Logs
        val callsList = listOf(
            CallEntity(
                id = "call1",
                userId = "raymond",
                userName = "Kawooya Raymond",
                userAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                type = "VIDEO",
                direction = "INCOMING",
                timestamp = now - 3600000 * 3,
                duration = 452
            ),
            CallEntity(
                id = "call2",
                userId = "tucci",
                userName = "Tucci Cyber Nation",
                userAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                type = "VOICE",
                direction = "MISSED",
                timestamp = now - 3600000 * 8,
                duration = 0
            ),
            CallEntity(
                id = "call3",
                userId = "raymond",
                userName = "Kawooya Raymond",
                userAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                type = "VOICE",
                direction = "OUTGOING",
                timestamp = now - 3600000 * 12,
                duration = 124
            )
        )
        for (call in callsList) {
            callDao.insertCall(call)
        }
    }
}
