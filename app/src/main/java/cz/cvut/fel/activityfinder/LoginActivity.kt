package cz.cvut.fel.activityfinder

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {

    lateinit var loginFragment: LoginFragment
    lateinit var registerFragment: RegisterFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mAuth = FirebaseAuth.getInstance();
        val currentUser = mAuth.currentUser;

        if(currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        registerFragment = RegisterFragment()
        loginFragment= LoginFragment()

        setContentView(R.layout.activity_login)
        supportFragmentManager.beginTransaction()
            .add(R.id.container, loginFragment)
            .add(R.id.container, registerFragment)
            .hide(registerFragment)
            .commit()
    }

    fun switchToRegister() {
        supportFragmentManager.beginTransaction()
            .hide(loginFragment).show(registerFragment).commit()
    }

    fun switchToLogin() {
        supportFragmentManager.beginTransaction()
            .hide(registerFragment).show(loginFragment).commit()
    }

    override fun onBackPressed() {
        if(registerFragment.isVisible) {
            this.switchToLogin()
        } else {
            finish()
        }
    }
}