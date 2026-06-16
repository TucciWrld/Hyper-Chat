package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val avatarUrl: String,
    val phone: String,
    val statusMessage: String,
    val isOnline: Boolean,
    val lastSeen: String,
    val isVerified: Boolean = false,
    val isBusiness: Boolean = false
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val title: String,
    val avatarUrl: String,
    val type: String, // "ONE_TO_ONE", "GROUP", "CHANNEL", "COMMUNITY", "HYPER_AI"
    val unreadCount: Int = 0,
    val pinned: Boolean = false,
    val archived: Boolean = false,
    val wallpaper: String = "",
    val disappearingMessages: Int = 0 // Duration in minutes, 0 means off
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val type: String, // "TEXT", "IMAGE", "VIDEO", "VOICE", "DOCUMENT", "POLL", "LOCATION"
    val timestamp: Long,
    val isRead: Boolean = false,
    val status: String, // "SENT", "DELIVERED", "READ"
    val replyToId: String? = null,
    val replyToText: String? = null,
    val reactions: String = "", // Comma-separated reactions list, e.g. "👍,❤️"
    val attachments: String? = null, // URI or description of attachment
    val voiceDuration: Int = 0, // Duration in seconds
    val pollOptions: String? = null, // Comma-separated options
    val pollVotes: String? = null, // JSON/comma format for votes
    val isViewOnce: Boolean = false
)

@Entity(tableName = "status_updates")
data class StatusEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val mediaUrl: String? = null,
    val text: String? = null,
    val type: String, // "TEXT", "IMAGE", "VIDEO", "VOICE"
    val timestamp: Long,
    val viewers: String = "", // Comma-separated viewer names
    val reactions: String = "" // List of emojis
)

@Entity(tableName = "calls")
data class CallEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val type: String, // "VOICE", "VIDEO"
    val direction: String, // "INCOMING", "OUTGOING", "MISSED"
    val timestamp: Long,
    val duration: Int // Duration in seconds (0 for missed/unanswered)
)
