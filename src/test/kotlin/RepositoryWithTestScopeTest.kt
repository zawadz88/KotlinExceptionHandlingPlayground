@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("OPT_IN_IS_NOT_ENABLED")

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.coroutines.CoroutineContext

class RepositoryWithTestScopeTest {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `throwingMethod should fail when uncaught exception with default test scope from runTest`() {
        assertThrows<IllegalStateException>("throwingMethod") {
            runTest(testDispatcher) testScope@{
                val repo = Repository(this@testScope)

                repo.throwingMethod()
            }
        }
    }

    @Test
    fun `throwingMethodWithRunCatching should NOT fail when uncaught exception with default test scope from runTest`() {
        runTest(testDispatcher) testScope@{
            val repo = Repository(this@testScope)

            repo.throwingMethodWithRunCatching()
        }
    }

    @Test
    fun `throwingMethod should fail when uncaught exception with custom test scope`() {
        assertThrows<IllegalStateException>("throwingMethod") {
            TestScope(testDispatcher).runTest testScope@{
                val repo = Repository(this@testScope)
                repo.throwingMethod()
            }
        }
    }

    // This passes and just logs the Exception!
    @Test
    fun `throwingMethod should NOT fail when uncaught exception with Coroutine scope with test dispatcher`() {
        runTest(testDispatcher) {
            val repo = Repository(CoroutineScope(testDispatcher))

            // the exception is logged in the original scope, but test runs in a different scope i.e. TestScope
            // that is not aware of the exception in the other scope!
            repo.throwingMethod()
        }
    }

    // This passes and just logs the Exception!
    @Test
    fun `throwingMethod should fail when uncaught exception with anonymous Coroutine scope with test dispatcher`() {
        runTest(testDispatcher) {
            val repo = Repository(object : CoroutineScope {
                override val coroutineContext: CoroutineContext
                    get() = testDispatcher
            })

            // the exception is logged in the original scope, but test runs in a different scope i.e. TestScope
            // that is not aware of the exception in the other scope!
            repo.throwingMethod()
        }
    }

    class Repository(private val coroutineScope: CoroutineScope) {

        fun throwingMethod() {
            coroutineScope.launch {
                throw IllegalStateException("throwingMethod")
            }
        }

        fun throwingMethodWithRunCatching() {
            coroutineScope.launch {
                kotlin.runCatching {
                    throw IllegalStateException("throwingMethodWithRunCatching")
                }
            }
        }

    }
}

