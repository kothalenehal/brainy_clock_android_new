import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.Window
import com.brainyclockuser.databinding.PopupLoginBinding

class SimplePopupDialog(context: Context) {
    private val dialog = Dialog(context)
    val binding: PopupLoginBinding

    init {
        binding = PopupLoginBinding.inflate(LayoutInflater.from(context))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    fun show(message: String) {
        binding.popupMessage.text = message
        binding.closeButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

}