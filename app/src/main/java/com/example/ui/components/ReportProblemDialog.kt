package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HolidayCrimson
import com.example.ui.theme.HolidayPineGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportProblemDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var issueCategory by remember { mutableStateOf("AI Wish Generator") }
    var issueDescription by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf(
        "AI Wish Generator",
        "Card Creator",
        "Schedule & Alarms",
        "Credits & Purchases",
        "Trackers",
        "Other"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("report_problem_dialog"),
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null,
                    tint = HolidayCrimson
                )
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(
                    text = "Report a Problem",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Let us know what went wrong so we can fix it for you right away:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat == issueCategory
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) HolidayCrimson else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { issueCategory = cat }
                                .testTag("cat_chip_${cat.replace(" ", "_")}")
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = issueDescription,
                    onValueChange = {
                        issueDescription = it
                        errorMessage = null
                    },
                    label = { Text("Describe what happened *") },
                    placeholder = { Text("e.g. Card font didn't update, or AI generation was slow...") },
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp),
                    isError = errorMessage != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_description_input")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = userEmail,
                    onValueChange = { userEmail = it },
                    label = { Text("Your Email (optional, for follow-up)") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = HolidayPineGreen)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_email_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (issueDescription.isBlank()) {
                        errorMessage = "Please enter a brief description of the issue."
                        return@Button
                    }

                    // Save report locally or send via email intent
                    submitProblemReport(
                        context = context,
                        category = issueCategory,
                        description = issueDescription.trim(),
                        email = userEmail.trim()
                    )
                    Toast.makeText(context, "Thank you! Your problem report was received 💌", Toast.LENGTH_LONG).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = HolidayCrimson),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("submit_report_button")
            ) {
                Text("Submit Report", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_report_button")
            ) {
                Text("Cancel")
            }
        }
    )
}

private fun submitProblemReport(
    context: Context,
    category: String,
    description: String,
    email: String
) {
    // Save report timestamp and info to SharedPreferences
    val prefs = context.getSharedPreferences("problem_reports_prefs", Context.MODE_PRIVATE)
    val existingCount = prefs.getInt("report_count", 0)
    prefs.edit()
        .putInt("report_count", existingCount + 1)
        .putString("last_report_category", category)
        .putString("last_report_description", description)
        .putLong("last_report_timestamp", System.currentTimeMillis())
        .apply()

    // Also attempt to draft an email intent to support if an email app is present
    try {
        val subject = "[Holiday Wishes Bug Report] $category"
        val body = """
            Category: $category
            User Email: ${if (email.isNotBlank()) email else "Not provided"}
            Device: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT})
            
            Description:
            $description
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:support@geegroup.org")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    } catch (e: Exception) {
        // SharedPreferences record was already saved
    }
}
