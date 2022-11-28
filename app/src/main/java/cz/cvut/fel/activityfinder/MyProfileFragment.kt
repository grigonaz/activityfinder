package cz.cvut.fel.activityfinder

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_my_profile.*

/**
 * A simple [Fragment] subclass.
 */
class MyProfileFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_my_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logout_button.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        account_profile.setOnClickListener {
            val intent = Intent(context, AccountActivity::class.java)
            startActivity(intent)
        }
        account_full_name.text = FirebaseAuth.getInstance().currentUser?.displayName

    }
}