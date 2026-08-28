package ru.netology.nmedia.entity

import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.enumeration.AttachmentType


data class AttachmentEmbeddable(
    val type: String,
    val url: String?,
) {
    fun toDto(): Attachment = Attachment(
        url = url ?: "",
        type = AttachmentType.valueOf(type),
    )

    companion object {
        fun fromDto(dto: Attachment?): AttachmentEmbeddable? = dto?.let {
            AttachmentEmbeddable(
                type = it.type.name,
                url = it.url,
            )
        }
    }
}


