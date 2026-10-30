package com.example.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonRose
import com.example.viewmodel.ConfirmDialogState

@Composable
fun ConfirmationDialog(
    state: ConfirmDialogState,
    onDismiss: () -> Unit
) {
    if (state.isOpen) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = "Advertencia de seguridad",
                    tint = NeonRose
                )
            },
            title = {
                Text(text = state.title)
            },
            text = {
                Text(text = state.message)
            },
            confirmButton = {
                Button(
                    onClick = state.onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRose),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_dialog_action_button")
                ) {
                    Text(state.confirmButtonText)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("confirm_dialog_cancel_button")
                ) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
