package com.pulsefit.app.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.pulsefit.app.data.repository.AuthRepository
import com.pulsefit.app.databinding.FragmentSettingsBinding
import com.pulsefit.app.ui.auth.AuthActivity
import com.pulsefit.app.util.SyncWorker
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val authRepo = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadProfile()

        binding.saveProfileButton.setOnClickListener { saveProfile() }
        binding.forceSyncButton.setOnClickListener { triggerManualSync() }
        binding.logoutButton.setOnClickListener { logOut() }
    }

    private fun loadProfile() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            authRepo.fetchUserProfile(uid).onSuccess { user ->
                binding.nameInput.setText(user.name)
                binding.unitSystemSwitch.isChecked = user.unitSystem == "Metric"
                binding.unitSystemSwitch.text = user.unitSystem
            }
        }
    }

    private fun saveProfile() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            authRepo.fetchUserProfile(uid).onSuccess { user ->
                val updated = user.copy(
                    name = binding.nameInput.text.toString(),
                    unitSystem = if (binding.unitSystemSwitch.isChecked) "Metric" else "Imperial"
                )
                authRepo.updateUserProfile(updated)
                Toast.makeText(requireContext(), "Profile updated.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun triggerManualSync() {
        binding.syncStatusText.text = "Syncing…"
        val request = OneTimeWorkRequestBuilder<SyncWorker>().build()
        val workManager = WorkManager.getInstance(requireContext())
        workManager.enqueue(request)

        workManager.getWorkInfoByIdLiveData(request.id).observe(viewLifecycleOwner) { info ->
            if (info?.state == WorkInfo.State.SUCCEEDED) {
                binding.syncStatusText.text = "Last synced just now"
            } else if (info?.state == WorkInfo.State.FAILED) {
                binding.syncStatusText.text = "Sync failed — will retry automatically"
            }
        }
    }

    private fun logOut() {
        authRepo.signOut()
        startActivity(Intent(requireContext(), AuthActivity::class.java))
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
