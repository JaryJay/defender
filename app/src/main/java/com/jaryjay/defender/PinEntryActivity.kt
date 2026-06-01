package com.jaryjay.defender

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PinEntryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_entry)

        val title = findViewById<TextView>(R.id.pin_title)
        val pinInput = findViewById<EditText>(R.id.pin_input)
        val pinConfirm = findViewById<EditText>(R.id.pin_confirm)
        val action = findViewById<Button>(R.id.pin_action)
        val error = findViewById<TextView>(R.id.pin_error)

        val isPinSet = PinManager.isPinSet(this)
        val resetMode = intent.getBooleanExtra(EXTRA_RESET_PIN, false)
        val isSetMode = !isPinSet || resetMode

        if (isSetMode) {
            title.text = getString(R.string.pin_set_title)
            pinConfirm.visibility = View.VISIBLE
            action.text = getString(R.string.pin_action_save)
        } else {
            title.text = getString(R.string.pin_title)
            action.text = getString(R.string.pin_action_unlock)
        }

        action.setOnClickListener {
            error.visibility = View.GONE
            val pin = pinInput.text.toString().filter { it.isDigit() }
            if (pin.length < 4) {
                showError(error, getString(R.string.pin_error_short))
                return@setOnClickListener
            }
            if (isSetMode) {
                val confirm = pinConfirm.text.toString().filter { it.isDigit() }
                if (confirm != pin) {
                    showError(error, getString(R.string.pin_error_mismatch))
                    return@setOnClickListener
                }
                PinManager.setPin(this, pin)
                PinManager.recordAuthorized(this)
                finish()
            } else {
                if (PinManager.verifyPin(this, pin)) {
                    PinManager.recordAuthorized(this)
                    finish()
                } else {
                    showError(error, getString(R.string.pin_error_incorrect))
                }
            }
        }
    }

    private fun showError(view: TextView, message: String) {
        view.text = message
        view.visibility = View.VISIBLE
    }

    companion object {
        const val EXTRA_RESET_PIN = "reset_pin"
    }
}
