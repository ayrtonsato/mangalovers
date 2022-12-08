package br.com.mangalovers.base

sealed class Failure {
    object InternalServerError: Failure()

    abstract class FeatureFailure: Failure()
}
