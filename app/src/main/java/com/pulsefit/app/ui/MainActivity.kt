package com.pulsefit.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.pulsefit.app.databinding.ActivityMainBinding
import com.pulsefit.app.ui.auth.AuthActivity

/** Hosts the 5-tab bottom navigation shell: Dashboard, Map, Exercise, Diet, Settings. */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.mainNavHost.id) as NavHostFragment
        binding.bottomNav.setupWithNavController(navHostFragment.navController)
    }
}
