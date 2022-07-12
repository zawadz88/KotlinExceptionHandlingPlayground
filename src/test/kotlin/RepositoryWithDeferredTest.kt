@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("OPT_IN_IS_NOT_ENABLED")

import arrow.core.Either
import kotlinx.coroutines.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.coroutines.CoroutineContext

class RepositoryWithDeferredTest {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `methodThatWaitsForThrowingDeferred should fail when uncaught exception with custom test scope with test dispatcher`() {
        assertThrows<IllegalStateException>("throwingDeferred") {
            val testScope = TestScope(testDispatcher)
            testScope.runTest testScope@{
                val repo = Repository(this@testScope)
                repo.methodThatWaitsForThrowingDeferred()
            }
        }
    }

    @Test
    fun `methodThatWaitsForThrowingDeferred should fail when uncaught exception with default test scope from runTest`() {
        assertThrows<IllegalStateException>("throwingDeferred") {
            runTest(testDispatcher) testScope@{
                val repo = Repository(this@testScope)

                repo.methodThatWaitsForThrowingDeferred()
            }
        }
    }

    @Test
    fun `methodThatWaitsForThrowingDeferredWithEitherCatch should NOT fail when uncaught exception with default and deferred`() {
        assertThrows<IllegalStateException>("throwingDeferred") {
            runTest(testDispatcher) testScope@{
                val repo = Repository(this@testScope)

                // even though we wrap the exception in `runCatching` it still throws at the end!
                repo.methodThatWaitsForThrowingDeferredWithEitherCatch()
            }
        }
    }

    // This passes and just logs the Exception!
    @Test
    fun `methodThatWaitsForThrowingDeferred should fail when uncaught exception with Coroutine scope`() {
        runTest(testDispatcher) {
            val repo = Repository(CoroutineScope(testDispatcher))

            // the exception is logged in the original scope, but test runs in a different scope i.e. TestScope
            // that is not aware of the exception in the other scope!
            repo.methodThatWaitsForThrowingDeferred()
        }
    }

    // This passes and just logs the Exception!
    @Test
    fun `methodThatWaitsForThrowingDeferred should fail when uncaught exception with anonymous Coroutine scope`() {
        runTest(testDispatcher) {
            val repo = Repository(object : CoroutineScope {
                override val coroutineContext: CoroutineContext
                    get() = testDispatcher
            })

            // the exception is logged in the original scope, but test runs in a different scope i.e. TestScope
            // that is not aware of the exception in the other scope!
            repo.methodThatWaitsForThrowingDeferred()
        }
    }

    @Test
    fun `methodThatWaitsForThrowingDeferredWithEitherCatch should NOT fail when supervisorScope used`() {
        runTest(UnconfinedTestDispatcher()) {
            supervisorScope supervisorScope@{
                val repo = Repository(this@supervisorScope)

                repo.methodThatWaitsForThrowingDeferredWithEitherCatch()
            }
        }

    }

    @Test
    fun `methodThatWaitsForThrowingDeferred should fail when supervisorScope used`() {
        assertThrows<IllegalStateException>("throwingDeferred") {
            runTest(testDispatcher) {
                supervisorScope supervisorScope@{
                    val repo = Repository(this@supervisorScope)

                    repo.methodThatWaitsForThrowingDeferred()
                }
            }
        }
    }

    class Repository(private val coroutineScope: CoroutineScope) {

        private val throwingDeferred: Deferred<String> by lazy {
            coroutineScope.async {
                throw IllegalStateException("throwingDeferred")
            }
        }

        fun methodThatWaitsForThrowingDeferred() {
            coroutineScope.launch {
                throwingDeferred.await()
            }

        }

        fun methodThatWaitsForThrowingDeferredWithEitherCatch() {
            coroutineScope.launch {
                Either.catch {
                    throwingDeferred.await()
                }
            }
        }
    }
}
