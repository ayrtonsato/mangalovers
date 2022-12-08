package br.com.mangalovers.core.models.comic

import java.util.*

data class Comic(
    var id: UUID = UUID.randomUUID(),
    var name: String,
    var type: ComicType,
    var releaseYear: Int,
    var isFinished: Boolean,
    var volumes: Int,
    var chapters: Int,
    var genres: List<ComicGenre>,
    var description: String,
    var authors: List<ComicAuthor>,
    var serialization: String,
    var themes: List<ComicTheme>,
    var demographics: List<ComicDemographic>
)