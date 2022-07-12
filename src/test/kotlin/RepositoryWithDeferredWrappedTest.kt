@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("OPT_IN_IS_NOT_ENABLED")

import kotlinx.coroutines.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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

        private val throwingDeferred: Deferred<Result<String>> by lazy {
            coroutineScope.async {
                kotlin.runCatching {
                    throw IllegalStateException("throwingDeferred")
                }
            }
        }

        fun methodThatWaitsForThrowingDeferred() {
            coroutineScope.launch {
                throwingDeferred.await()
                    .onFailure { println("methodThatWaitsForThrowingDeferred onFailure") }
                    .onSuccess { println("methodThatWaitsForThrowingDeferred onFailure") }
            }
        }
    }
}
