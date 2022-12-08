package br.com.mangalovers.scrapper.datasource.errors

import br.com.mangalovers.base.Failure

sealed class DataSourceErrors: Failure.FeatureFailure() {
    object PageNotFound: DataSourceErrors()
    object Forbidden: DataSourceErrors()
    object ServerError: DataSourceErrors()
    object TimeOut: DataSourceErrors()
    object Unknown: DataSourceErrors()
}
