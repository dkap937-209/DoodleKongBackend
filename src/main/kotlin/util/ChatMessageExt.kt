package util

import data.models.ChatMessage
import java.util.*

fun ChatMessage.matchesWord(word: String): Boolean {
    return message.lowercase(Locale.getDefault()).trim() == word.lowercase(Locale.getDefault()).trim()
}