package cz.cvut.fel.activityfinder

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_login.*


/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_login, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.auth = FirebaseAuth.getInstance()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        login_button.setOnClickListener {
            val email: String = inputEmail.text.toString()
            val password: String = inputPassword.text.toString()
            if(email == "") {this.info(getString(R.string.empty_email_warning)); return@setOnClickListener}
            if(password == "") {this.info(getString(R.string.empty_password_warning)); return@setOnClickListener}
            if(!email.matches(Regex("^(.+)@(.+)\$"))) {this.info(getString(R.string.wrong_email_warning)); return@setOnClickListener}
            if(password.length < 7) {this.info(getString(R.string.wrong_password_warning)); return@setOnClickListener}
            this.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show()
                    }
                }
        }
        register_button.setOnClickListener {
            (it.context as LoginActivity).switchToRegister()
        }
    }

    private fun info(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

}
