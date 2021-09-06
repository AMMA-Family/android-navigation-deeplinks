package deep_link

import kotlin.String
import kotlin.collections.List

public interface DeepLinkAdditionalInfo {
    public val protocol: String?

    public val host: String?

    public val pathSegments: List<String>
}
