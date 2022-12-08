package br.com.mangalovers.scrapper.datasource

import br.com.mangalovers.base.ResultStatus
import br.com.mangalovers.scrapper.datasource.errors.DataSourceErrors
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import javax.xml.transform.Result

@OptIn(ExperimentalCoroutinesApi::class)
internal class MyAnimeListDataSourceTest {

    private lateinit var server: MockWebServer
    private lateinit var dataSource: MyAnimeListDataSource

    @Before
    fun setUp() {
        dataSource = MyAnimeListDataSource()
        server = MockWebServer().apply {
            start(8081)
        }
    }

    @Test
    fun `getComicsFromRemotePage() should return a collection of Comic`() = runTest {
        // arrange
        val expected = 76
        val page = "http://localhost:8081/comics?page=1"
        with(server) {
            enqueue(
                MockResponse()
                    .setBody(
                        File("src/test/assets/myanimelist_collection.html").readText(Charsets.UTF_8)
                    )
            )
        }
        // action
        val result = dataSource.getComicsFromRemotePage(page).count()
        // assert
        assertEquals(expected, result)
    }

    @Test
    fun `getComicsFromRemotePage() should return ResultStatus Error when webserver returns 404`() = runTest {
        // arrange
        val page = "http://localhost:8081/notfoundpage?page=233"
        with(server) {
            enqueue(
                MockResponse().setResponseCode(404)
            )
        }
        dataSource.getComicsFromRemotePage(page).collectLatest { result ->
            assert(result is ResultStatus.Error<DataSourceErrors>)
        }
    }

}