package com.example.dadn_app.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.dadn_app.ui.theme.*

/**
 * Shared text field used across LoginScreen and RegisterScreen.
 * Renders an outlined field with a leading icon, optional trailing icon,
 * and an inline error message below when [errorMessage] is non-null.
 */
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value            = value,
        onValueChange    = onValueChange,
        label            = { Text(label) },
        leadingIcon      = { Icon(leadingIcon, contentDescription = null) },
        trailingIcon     = trailingIcon,
        isError          = errorMessage != null,
        supportingText   = errorMessage?.let { { Text(it) } },
        singleLine       = true,
        shape            = RoundedCornerShape(12.dp),
        visualTransformation = visualTransformation,
        keyboardOptions  = keyboardOptions,
        keyboardActions  = keyboardActions,
        modifier         = modifier.fillMaxWidth(),
        colors           = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Primary,
            focusedLabelColor    = Primary,
            focusedLeadingIconColor  = Primary,
            unfocusedBorderColor = OutlineVariant,
            unfocusedLabelColor  = OnSurfaceVariant,
            errorBorderColor     = Error,
            errorLabelColor      = Error,
            errorSupportingTextColor = Error,
            errorLeadingIconColor    = Error,
        ),
    )
}
