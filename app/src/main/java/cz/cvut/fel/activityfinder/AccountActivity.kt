package cz.cvut.fel.activityfinder


import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_account.*

class AccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
    }

    override fun onStart() {
        super.onStart()

        var user = FirebaseAuth.getInstance().currentUser
        profile_full_name.text.clear()
        profile_full_name.text.insert(0, user?.displayName)
        profile_email.text.clear()
        profile_email.text.insert(0, user?.email)

        profile_back.setOnClickListener{
            finish()
        }
        profile_save_button.setOnClickListener {
            if(profile_full_name.text.length > 4) {
                val userpd = UserProfileChangeRequest.Builder().setDisplayName(profile_full_name.text.toString()).build()
                FirebaseAuth.getInstance().currentUser!!.updateProfile(userpd).addOnCompleteListener {
                    if(it.isSuccessful) {
                        if(new_password.text.isNotEmpty()) {
                            FirebaseAuth.getInstance().currentUser!!.updatePassword(new_password.text.toString())
                                .addOnCompleteListener { tit ->
                                    if (tit.isSuccessful) {
                                        Toast.makeText(this, "Profil a heslo aktualizováno", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(this, "Profil aktualizován, heslo aktualizovat nejde, zkuste se znovu přihlásit", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this, "Profil aktualizován", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Profil nelze aktualizovat", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Jméno musí být delší jak 4 znaky", Toast.LENGTH_SHORT).show()
            }
        }
    }

}