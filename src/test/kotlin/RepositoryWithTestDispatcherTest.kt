@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("OPT_IN_IS_NOT_ENABLED")

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RepositoryWithTestDispatcherTest {

    private val testDispatcher = StandardTestDispatcher()

    private val unconfinedTestDispatcher = UnconfinedTestDispatcher()

    @Test
    fun `throwingMethod should fail when uncaught exception with standard test dispatcher`() {
        assertThrows<IllegalStateException>("throwingMethod") {
            runTest(testDispatcher) {
                val repo = Repository(testDispatcher)

                repo.throwingMethod()
            }
        }
    }

    @Test
    fun `throwingMethodWithDispatcher should fail when uncaught exception with standard test dispatcher`() {
        assertThrows<IllegalStateException>("throwingMethodWithDispatcher") {
            runTest(testDispatcher) {
                val repo = Repository(testDispatcher)

                repo.throwingMethodWithDispatcher()
            }
        }
    }

    @Test
    fun `throwingMethod should fail when uncaught exception with unconfined test dispatcher`() {
        assertThrows<IllegalStateException>("throwingMethod") {
            runTest(unconfinedTestDispatcher) {
                val repo = Repository(unconfinedTestDispatcher)

                repo.throwingMethod()
            }
        }
    }

    @Test
    fun `throwingMethodWithDispatcher should fail when uncaught exception with unconfined test dispatcher`() {
        assertThrows<IllegalStateException>("throwingMethodWithDispatcher") {
            runTest(unconfinedTestDispatcher) {
                val repo = Repository(unconfinedTestDispatcher)

                repo.throwingMethodWithDispatcher()
            }
        }
    }

    class Repository(private val dispatcher: CoroutineDispatcher) {

        @Suppress("RedundantSuspendModifier")
        suspend fun throwingMethod() {
            throw IllegalStateException("throwingMethod")
        }

        suspend fun throwingMethodWithDispatcher(): Unit = withContext(dispatcher) {
            throw IllegalStateException("throwingMethodWithDispatcher")
        }
    }
}


