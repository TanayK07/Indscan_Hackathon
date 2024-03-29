package net.vishesh.scanner.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import net.vishesh.scanner.support.Either

abstract class UseCase<Type, in Params> where Type : Any? {

    abstract suspend fun run(params: Params): Either<Failure, Type>

    operator fun invoke(params: Params, onResult: (Either<Failure, Type>) -> Unit = {}) {
        val job = GlobalScope.async(Dispatchers.IO) { run(params) }
        GlobalScope.launch(Dispatchers.Main) { onResult(job.await()) }
    }
}

class Failure(val origin: Throwable)
