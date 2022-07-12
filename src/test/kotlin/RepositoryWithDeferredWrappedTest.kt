@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("OPT_IN_IS_NOT_ENABLED")

import arrow.core.Either
import kotlinx.coroutines.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RepositoryWithDeferredWrappedTest {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `methodThatWaitsForThrowingDeferred should NOT fail when uncaught exception with default test scope from runTest`() {
        runTest(testDispatcher) testScope@{
            val repo = Repository(this@testScope)

            repo.methodThatWaitsForThrowingDeferred()
        }
    }

    class Repository(private val coroutineScope: CoroutineScope) {

        private val throwingDeferred: Deferred<Either<Throwable, String>> by lazy {
            coroutineScope.async {
                Either.catch {
                    throw IllegalStateException("throwingDeferred")
                }
            }
        }

        fun methodThatWaitsForThrowingDeferred() {
            coroutineScope.launch {
                throwingDeferred.await()
                    .tapLeft { println("methodThatWaitsForThrowingDeferred tapLeft") }
                    .tap { println("methodThatWaitsForThrowingDeferred tap") }
            }
        }
    }
}
