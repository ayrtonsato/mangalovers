package br.com.mangalovers.core.usecase

import br.com.mangalovers.core.models.comic.Comic

interface FindAndSaveComicsUseCase {
    fun scrapeComics(): List<Comic>
    fun saveComics(comics: List<Comic>)
}