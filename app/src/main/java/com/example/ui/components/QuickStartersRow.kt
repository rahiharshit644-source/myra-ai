package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkTertiary
import com.example.ui.theme.TextPrimary

private data class StarterPrompt(
    val title: String,
    val emoji: String,
    val prompt: String
)

private val STARTERS = listOf(
    StarterPrompt("Roast me", "😏", "Roast my day, Myra! Give me your best witty burn."),
    StarterPrompt("Are you flirting?", "💖", "Are you flirting with me, Myra?"),
    StarterPrompt("Open YouTube", "🎵", "Open YouTube for me!"),
    StarterPrompt("Open Instagram", "📸", "Open Instagram, Myra!"),
    StarterPrompt("Tell a spicy joke", "🌶️", "Tell me your funniest and spiciest witty joke!"),
    StarterPrompt("Pep Talk", "🔥", "Give me a confident, sassy pep talk right now."),
    StarterPrompt("Your secret", "🤫", "What is your biggest secret, Myra?")
)

@Composable
fun QuickStartersRow(
    onSelectStarter: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .testTag("quick_starters_carousel"),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        STARTERS.forEachIndexed { index, starter ->
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color(0x1AEC4899),
                        spotColor = Color(0x26DB2777)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xEBFFFFFF))
                    .border(
                        width = 1.dp,
                        color = Color(0x40F472B6),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelectStarter(starter.prompt) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("starter_chip_$index")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = starter.emoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = starter.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

