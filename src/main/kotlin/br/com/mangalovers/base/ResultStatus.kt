package br.com.mangalovers.base

sealed class ResultStatus<out T, out F> where F: Failure {
    data class Response<out T>(val result: T): ResultStatus<T, Nothing>()
    data class Error<out F : Failure>(val cause: F): ResultStatus<Nothing, F>()
}
