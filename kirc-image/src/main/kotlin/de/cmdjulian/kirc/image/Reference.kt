package de.cmdjulian.kirc.image

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

@JvmDefaultWithoutCompatibility
sealed interface Reference {
    val separator: Char
    fun asImagePart() = "$separator${toString()}"
}

class Tag @JsonCreator constructor(@JsonValue private val value: String) : Reference {
    init {
        require(value.matches(Regex("\\w[\\w.\\-]{0,127}"))) { "invalid tag" }
    }

    override val separator: Char get() = Companion.separator

    override fun equals(other: Any?): Boolean = other is Tag && other.value == value
    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = value

    companion object {
        val LATEST = Tag("latest")
        const val separator: Char = ':'
    }
}

class Digest @JsonCreator constructor(@JsonValue private val value: String) : Reference, Comparable<Digest> {
    init {
        require(value.matches(Regex("sha256:[\\da-fA-F]{32,}"))) { "invalid digest" }
    }

    override val separator: Char get() = Companion.separator

    override fun compareTo(other: Digest): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean = other is Digest && other.value == value
    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = value

    companion object {
        const val separator: Char = '@'
    }
}
