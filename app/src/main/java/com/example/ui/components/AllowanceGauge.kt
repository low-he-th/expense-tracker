package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.BudgetEngine
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonOverspent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.math.BigDecimal

@Composable
fun AllowanceGauge(
    dailyAllowance: BigDecimal,
    spentToday: BigDecimal,
    remainingAllowance: BigDecimal,
    overspent: BigDecimal,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    val isOver = overspent > BigDecimal.ZERO
    val ratio = BudgetEngine.calculateUtilizationPercentage(dailyAllowance, spentToday)
    val sweepFraction by animateFloatAsState(
        targetValue = ratio.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "sweepFraction"
    )

    val gaugeColor by animateColorAsState(
        targetValue = when {
            isOver -> CrimsonOverspent
            ratio >= 0.75f -> AmberWarning
            else -> EmeraldPrimary
        },
        animationSpec = tween(durationMillis = 400),
        label = "gaugeColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("allowance_gauge_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(gaugeColor.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = if (isOver) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = gaugeColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = when {
                        isOver -> "Daily Limit Exceeded"
                        ratio >= 0.75f -> "75% Allowance Used"
                        spentToday == BigDecimal.ZERO -> "Fresh Day Allowance"
                        else -> "Safely Within Budget"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = gaugeColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Gauge Center Display
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                // Background and Progress Arcs
                Canvas(modifier = Modifier.size(190.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    // Track
                    drawArc(
                        color = Color.White.copy(alpha = 0.08f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    // Filled Progress
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(gaugeColor.copy(alpha = 0.6f), gaugeColor)
                        ),
                        startAngle = 135f,
                        sweepAngle = 270f * sweepFraction,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Inner Text Details
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (isOver) "OVERSPENT" else "REMAINING TODAY",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOver) CrimsonOverspent else TextSecondary,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isOver) {
                            BudgetEngine.formatCurrency(overspent, currencyCode)
                        } else {
                            BudgetEngine.formatCurrency(remainingAllowance, currencyCode)
                        },
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = if (isOver) CrimsonOverspent else TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "of ${BudgetEngine.formatCurrency(dailyAllowance, currencyCode)} daily",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Lower metrics bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Daily Allowance",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = BudgetEngine.formatCurrency(dailyAllowance, currencyCode),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(width = 1.dp, height = 28.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Spent Today",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = BudgetEngine.formatCurrency(spentToday, currencyCode),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isOver) CrimsonOverspent else gaugeColor
                    )
                }
            }
        }
    }
}
