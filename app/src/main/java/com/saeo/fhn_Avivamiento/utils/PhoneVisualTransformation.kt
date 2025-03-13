package com.saeo.fhn_Avivamiento.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text

        // Insertar un guion después del cuarto dígito, si el usuario ya escribió 4 o más.
        val formatted = if (digits.length > 4) {
            digits.substring(0, 4) + "-" + digits.substring(4)
        } else {
            digits
        }

        // OffsetMapping para que el cursor se comporte correctamente
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // Si el cursor está antes o en el cuarto dígito, no hay desplazamiento
                // Si ya pasamos el 4º dígito, desplazamos 1 posición más por el guion
                return if (offset <= 4) offset else offset + 1
            }

            override fun transformedToOriginal(offset: Int): Int {
                // Inverso de lo anterior
                return if (offset <= 4) offset else offset - 1
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}