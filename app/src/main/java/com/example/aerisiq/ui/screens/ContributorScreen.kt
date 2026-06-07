package com.example.aerisiq.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aerisiq.R
import com.example.aerisiq.ui.theme.*

data class Contributor(
    val name: String,
    val role: String,
    val subtitle: String,
    val bio: String,
    val quote: String,
    val githubUrl: String?,
    val instagramUrl: String?,
    val imageResId: Int
)

@Composable
fun ContributorScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    
    val contributorsList = listOf(
        Contributor(
            name = "Agnel Francis Olakkengil",
            role = "Lead Architect & Developer",
            subtitle = "Cybersecurity Aspirant, Thrissur",
            bio = "Agnel is the sole creator of the AerisIQ platform, responsible for designing the local LLM parsing logic, public CAP RSS alerts syncing mechanism, regional Kerala Elite features, and the custom Jetpack Compose interface framework.",
            quote = "the more you think the more you become stupid :)",
            githubUrl = "https://github.com/agnelfranciso",
            instagramUrl = "https://www.instagram.com/oslohaz_e/",
            imageResId = R.drawable.contributor_agnel
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(DarkBackground, Color(0xFF0F121C))))
            .statusBarsPadding()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Contributors",
                color = Color.White,
                fontSize = 24.sp,
                fontFamily = GoogleSansFlex,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Contributor Cards List
        contributorsList.forEach { contributor ->
            ContributorCard(contributor = contributor)
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ContributorCard(contributor: Contributor) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceTranslucent),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Header Row: Avatar + Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = contributor.imageResId),
                    contentDescription = contributor.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(2.dp, PrimaryBlue, CircleShape)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = contributor.name,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.ExtraBold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = contributor.role,
                        color = PrimaryBlue,
                        fontSize = 14.sp,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = contributor.subtitle,
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontFamily = GoogleSansFlex,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bio
            Text(
                text = contributor.bio,
                color = Color.LightGray,
                fontSize = 13.sp,
                fontFamily = GoogleSansFlex,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quote / Philosophy
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FormatQuote,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "“${contributor.quote}”",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Social Buttons Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (contributor.githubUrl != null) {
                    Button(
                        onClick = { uriHandler.openUri(contributor.githubUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.07f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "GitHub",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("GitHub", color = Color.White, fontFamily = GoogleSansFlex, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                if (contributor.instagramUrl != null) {
                    Button(
                        onClick = { uriHandler.openUri(contributor.instagramUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C).copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        modifier = Modifier.border(1.dp, Color(0xFFE1306C).copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Instagram",
                            tint = Color(0xFFE1306C),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Instagram", color = Color(0xFFE1306C), fontFamily = GoogleSansFlex, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
