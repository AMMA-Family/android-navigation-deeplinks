package family.amma.module_a

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
import deep_link.GeneratedDeepLink
import family.amma.ModuleB
import family.amma.module_b.SecondFragmentDeepLink
import kotlin.random.Random

class FirstFragment : Fragment(R.layout.fragment_first) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button).setOnClickListener {
            // deep links args
            val id = Random.nextInt()
            val isEditMode = Random.nextBoolean()

            // the same deep links - by name and hierarchical
            val deepLink = if (Random.nextBoolean()) {
                SecondFragmentDeepLink.BarFoo(id, isEditMode)
            } else {
                ModuleB.Http.WwwExampleCom.UsersDeepLink(id, isEditMode)
            }
            findNavController().navigate(deepLink.toNavDeepLinkRequest())
        }
    }
}

private inline fun GeneratedDeepLink.toNavDeepLinkRequest(block: NavDeepLinkRequest.Builder.() -> Unit = {}) =
    NavDeepLinkRequest.Builder.fromUri(Uri.parse(this.uri)).also(block).build()
