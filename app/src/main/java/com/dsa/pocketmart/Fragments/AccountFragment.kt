package com.dsa.pocketmart.Fragments

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dsa.pocketmart.LoginActivity
import com.dsa.pocketmart.R
import com.dsa.pocketmart.databinding.FragmentAccountBinding
import com.dsa.pocketmart.models.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null

    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore

    private lateinit var auth: FirebaseAuth

    private var _userInfo: User? = null

    private val userInfo get() = _userInfo!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchUserData()
        binding.logOut.setOnClickListener {
            auth.signOut()
            signOutUser()
        }
        binding.edit.setOnClickListener {
            setEditingMode(true)
        }
        binding.saveChanges.setOnClickListener {
            saveChanges()
        }
    }

    private fun signOutUser() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }

    private fun saveChanges() {
        auth.currentUser?.let {
            val user = User(
                auth.currentUser?.email ?: "",
                binding.zipCode.text.toString(),
                binding.direction.text.toString(),
                binding.city.text.toString(),
                binding.state.text.toString()
            )
            db.collection("users").document(it.uid).set(user).addOnSuccessListener {
                Toast.makeText(
                    requireContext(), "Se guardaron los cambios con éxito", Toast.LENGTH_SHORT
                ).show()
                setEditingMode(false)
            }.addOnFailureListener {
                Toast.makeText(
                    requireContext(), "Ocurrió un error al guardar los cambios", Toast.LENGTH_SHORT
                ).show()
                Log.e("Saving user changes to firestore", "${it.cause}, ${it.message}")
            }
        }

    }

    private fun printData() {
        auth.currentUser?.let {
            binding.userName.text = it.email
        }
        val empty = "Campo vacío"
        binding.zipCode.setHint(if (userInfo.zipCode.isNotEmpty()) userInfo.zipCode else empty)
        binding.direction.setHint(if (userInfo.location.isNotEmpty()) userInfo.location else empty)
        binding.city.setHint(if (userInfo.city.isNotEmpty()) userInfo.city else empty)
        binding.state.setHint(if (userInfo.state.isNotEmpty()) userInfo.state else empty)
        setEditingMode(false)
    }

    private fun setEditingMode(editingMode: Boolean) {
        if (editingMode) {
            setEditable(binding.zipCode)
            setEditable(binding.direction)
            setEditable(binding.city)
            setEditable(binding.state)
            binding.saveChanges.visibility = View.VISIBLE
        } else {
            setNotEditable(binding.zipCode)
            setNotEditable(binding.direction)
            setNotEditable(binding.city)
            setNotEditable(binding.state)
            binding.saveChanges.visibility = View.GONE
        }
    }

    private fun setEditable(editText: EditText) {
        editText.isEnabled = true
    }

    private fun setNotEditable(editText: EditText) {
        editText.isEnabled = false
    }

    private fun fetchUserData() {
        auth.currentUser?.let {
            db.collection("users").document(it.uid).get().addOnSuccessListener {
                val email = it.getString("email") ?: ""
                val zipCode = it.getString("zipCode") ?: ""
                val location = it.getString("location") ?: ""
                val city = it.getString("city") ?: ""
                val state = it.getString("state") ?: ""

                _userInfo = User(email, zipCode, location, city, state)
                printData()
            }
        }
    }
}