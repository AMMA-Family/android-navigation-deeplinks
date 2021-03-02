package family.amma.deep_link.generator.ext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend inline fun <T> io(noinline block: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.IO, block = block)
