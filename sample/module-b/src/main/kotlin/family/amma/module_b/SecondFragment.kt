package family.amma.module_b

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs

class SecondFragment : Fragment(R.layout.fragment_second) {
    private val args by navArgs<SecondFragmentArgs>()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.argsTextView).text = "Args: id = ${args.id}, isEditMode = ${args.isEditMode}"
    }
}
