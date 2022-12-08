package br.com.mangalovers.scrapper.datasource

import br.com.mangalovers.base.ResultStatus
import br.com.mangalovers.core.models.comic.*
import br.com.mangalovers.scrapper.datasource.errors.DataSourceErrors
import it.skrape.core.htmlDocument
import it.skrape.fetcher.AsyncFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.Doc
import it.skrape.selects.DocElement
import it.skrape.selects.and
import it.skrape.selects.eachText
import it.skrape.selects.html5.a
import it.skrape.selects.html5.div
import it.skrape.selects.html5.p
import it.skrape.selects.html5.span
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

private object AllComicsHelper {
    fun parseDocument(doc: Doc): List<DocElement> = doc.div {
        div {
            withClass = "seasonal-anime" and "js-seasonal-anime"
            findAll {
                this
            }
        }
    }
    fun getTitle(doc: DocElement): String = doc.a {
        withClass = "link-title"
        findFirst {
            text
        }
    }
    fun getInfo(doc: DocElement): Map<String, Any> {
        val mutableMap = mutableMapOf<String, Any>()
        doc.div {
            withClass = "info"
            // type + year
            span {
                withClass = "item"
                findFirst {
                    val (type, year) = text.split(", ")
                    mutableMap["type"] = type
                    mutableMap["year"] = year.toInt()
                }
                // isCompleted
                findSecond {
                    mutableMap["finished"] = text == "Finished"
                }
                // Volumes + Chapters
                findThird {
                    val volumesAndChapters = text.split(",").map {
                        val num = it.trim().split(" ")[0]
                        if (num == "?") {
                            0
                        } else {
                            num.toInt()
                        }
                    }
                    mutableMap["volumes"] = volumesAndChapters[0]
                    mutableMap["chapters"] = volumesAndChapters[1]
                }
            }
        }
        return mutableMap.toMap()
    }
    fun getGenres(doc: DocElement): List<ComicGenre> = doc.div {
        withClass = "genres-inner" and "js-genre-inner"
        span {
            withClass = "genre"
            findAll {
                eachText.map {
                    ComicGenre(genre = it)
                }
            }
        }
    }
    fun getDescription(doc: DocElement): String = doc.p {
        withClass = "preline"
        findFirst {
            text
        }
    }
    fun getProperties(doc: DocElement): Map<String, List<String>> {
        val map = mutableMapOf<String, List<String>>()
        doc.div {
            withClass = "properties"
            div {
                withClass = "property"
                findAll {
                    this.forEach { docElement ->
                        var propertyName = ""
                        docElement.span {
                            withClass = "caption"
                            findFirst {
                                propertyName = text.lowercase()
                            }
                        }
                        docElement.span {
                            withClass = "item"
                            findAll {
                                map[propertyName] = eachText
                            }
                        }
                    }
                }
            }
        }
        return map.toMap()
    }
    fun parseAllComicsFromPage(docs: List<DocElement>): Flow<ResultStatus<Comic, DataSourceErrors>> = flow {
        docs.forEach { it ->
            val info = getInfo(it)
            var authors: List<ComicAuthor> = emptyList()
            var serialization = ""
            var themes: List<ComicTheme> = emptyList()
            var demographics: List<ComicDemographic> = emptyList()
            getProperties(it).forEach { entry ->
                when (entry.key) {
                    "authors" -> {
                        authors = entry.value.map { ComicAuthor(name = it) }
                    }

                    "serialization" -> serialization = entry.value[0]
                    "themes" -> {
                        themes = entry.value.map { ComicTheme(theme = it) }
                    }

                    "demographics" -> {
                        demographics = entry.value.map { ComicDemographic(demographic = it) }
                    }

                    else -> Unit
                }
            }
            val comic = Comic(
                name = getTitle(it),
                type = ComicType(type = info["type"] as String),
                releaseYear = info["year"] as Int,
                isFinished = info["finished"] as Boolean,
                volumes = info["volumes"] as Int,
                chapters = info["chapters"] as Int,
                genres = getGenres(it),
                description = getDescription(it),
                authors = authors,
                serialization = serialization,
                themes = themes,
                demographics = demographics
            )
            emit(ResultStatus.Response(comic))
        }
    }
}

class MyAnimeListDataSource {
    fun getComicsFromRemotePage(page: String): Flow<ResultStatus<Comic, DataSourceErrors>> = flow {
        var doc: List<DocElement>? = null
        var error: ResultStatus.Error<DataSourceErrors>? = null
        skrape(AsyncFetcher) {
            request {
                url = page
            }
            response {
                val code = status {
                    code
                }
                when (code) {
                    200 -> htmlDocument {
                        doc = AllComicsHelper.parseDocument(this)
                    }
                    404 -> error = ResultStatus.Error(DataSourceErrors.PageNotFound)
                    403 -> error = ResultStatus.Error(DataSourceErrors.Forbidden)
                    408 -> error = ResultStatus.Error(DataSourceErrors.TimeOut)
                    500 -> error = ResultStatus.Error(DataSourceErrors.ServerError)
                    else -> error = ResultStatus.Error(DataSourceErrors.Unknown)
                }
                if (code == 200) {
                    htmlDocument {
                        doc = AllComicsHelper.parseDocument(this)
                    }
                }
            }
        }
        if (error != null) {
            emit(error!!)
        } else {
            if (doc != null) {
                emitAll(AllComicsHelper.parseAllComicsFromPage(doc!!))
            }
        }
    }
}