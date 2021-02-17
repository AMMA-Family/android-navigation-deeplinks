package family.amma.deep_link.generator.entity

import family.amma.deep_link.generator.ResReference
import com.squareup.kotlinpoet.ClassName
import family.amma.deep_link.generator.ext.filterUnique

/**
 * Destination model.
 * Same as [ParsedDestination], but without [ParsedDestination.nested].
 * [ParsedDestination] - Data model.
 * [DestinationModel] - Business model.
 */
internal data class DestinationModel(
    val id: ResReference?,
    val name: ClassName,
    val deepLinks: List<DeepLink>,
    val args: List<DestArgument>
) {
    val simpleName = name.simpleName
}

/** Convert data model to business model. */
internal fun ParsedDestination.toDestinationModel() =
    DestinationModel(id = id, name = name, deepLinks = deepLinks, args = args)

internal operator fun DestinationModel.plus(destination: DestinationModel): DestinationModel =
    copy(
        deepLinks = (deepLinks + destination.deepLinks).filterUnique(),
        args = (args + destination.args).filterUnique()
    )

/**
 * Parsed destination from xml.
 * ```xml
 * <fragment
 *   android:id="@+id/fooFragment"
 *   android:name="family.amma.FooFragment"
 *   android:label="FooFragment"
 *   tools:layout="@layout/fragment_foo" />
 * ```
 */
internal data class ParsedDestination(
    val id: ResReference?,
    val name: ClassName,
    val deepLinks: List<DeepLink>,
    val args: List<DestArgument>,
    val nested: List<ParsedDestination> = emptyList()
)
