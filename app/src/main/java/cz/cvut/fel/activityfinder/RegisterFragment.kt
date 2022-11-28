package cz.cvut.fel.activityfinder

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.fragment_register.*

class RegisterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        register_back.setOnClickListener{
            (it.context as LoginActivity).switchToLogin()
        }
        register_create_button.setOnClickListener {
            register_full_name.error = null
            register_email.error = null
            register_password.error = null
            register_password_again.error = null
            var isvalid = true
            if(register_full_name.text == null || register_full_name.text.length <= 2) {
                register_full_name.error = getString(R.string.validation_error_invalid_full_name)
                isvalid = false
            }
            if(register_email.text == null || !register_email.text.matches(Regex("^(.+)@(.+)\$"))) {
                register_email.error = getString(R.string.validation_error_invalid_email)
                isvalid = false
            }
            if(register_password.text == null || register_password.text.length < 8) {
                register_password.error = getString(R.string.validation_error_invalid_password)
                isvalid = false
            }
            if(register_password_again.text==null || register_password_again.text.toString() != register_password.text.toString()) {
                register_password_again.error = getString(R.string.validation_error_invalid_password_compare)
                isvalid = false
            }


            if(isvalid) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(register_email.text.toString(), register_password.text.toString())
                    .addOnCompleteListener{ task ->
                        if(task.isSuccessful) {
                            val userpd = UserProfileChangeRequest.Builder().setDisplayName(register_full_name.text.toString()).build()
                            task.result.user?.updateProfile(userpd)?.addOnCompleteListener{
                                if(!it.isSuccessful) {
                                    Toast.makeText(requireContext(), getString(R.string.validation_error_name_dont_changed), Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(requireContext(), getString(R.string.success_created_account), Toast.LENGTH_SHORT).show()
                                }
                                val intent = Intent(context, LoginActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                            }
                        } else {
                            val exc = task.exception as FirebaseAuthUserCollisionException
                            if(exc.errorCode == "ERROR_EMAIL_ALREADY_IN_USE") {
                                Toast.makeText(requireContext(), getString(R.string.validation_error_invalid_used_email), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), exc.localizedMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }
    }
}