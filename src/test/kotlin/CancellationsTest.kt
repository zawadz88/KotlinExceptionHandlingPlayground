@file:Suppress("OPT_IN_IS_NOT_ENABLED")

import arrow.core.Either
import kotlinx.coroutines.*
import org.junit.jupiter.api.Test

class CancellationsTest {

    @Test
    fun `cancellation example with runCatching`() = runBlocking {
        val testScope = CoroutineScope(Dispatchers.Unconfined)

        val deferred = testScope.async {
            val nestedDeferred = async {
                println("Nested async 1")
                delay(2000L)
                // this won't be printed'
                println("Nested async 2")
                "XXX"
            }
            kotlin.runCatching {
                println("async 1")
                delay(1000L)
                // this won't be printed'
                println("async 2")
                throw IllegalStateException("throwingDeferred")
            }
            // this is still printed even though it should not be!
            println("still doing something")
            // this won't be printed
            println("nested deferred: ${nestedDeferred.await()}")
        }

        delay(500L)

        testScope.cancel()
        println("Job was cancelled: ${testScope.coroutineContext[Job]?.isCancelled}")
        println("deferred was cancelled: ${deferred.isCancelled}, completed: ${deferred.isCompleted}")
    }

    @Test
    fun `cancellation example with Either#catch`() = runBlocking {
        val testScope = CoroutineScope(Dispatchers.Unconfined)

        val deferred = testScope.async {
            val nestedDeferred = async {
                println("Nested async 1")
                delay(2000L)
                // this won't be printed'
                println("Nested async 2")
                "XXX"
            }
            Either.catch {
                println("async 1")
                delay(1000L)
                // this won't be printed'
                println("async 2")
                throw IllegalStateException("throwingDeferred")
            }
            // this won't be printed'
            println("still doing something")
            // this won't be printed either
            println("nested deferred: ${nestedDeferred.await()}")
        }

        delay(500L)

        testScope.cancel()
        println("Job was cancelled: ${testScope.coroutineContext[Job]?.isCancelled}")
        println("deferred was cancelled: ${deferred.isCancelled}, completed: ${deferred.isCompleted}")
    }

}
