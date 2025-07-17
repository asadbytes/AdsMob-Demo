package com.asadbyte.adsapp.ads

import android.graphics.Typeface
import android.text.TextUtils
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.asadbyte.adsapp.R
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
fun NativeAdComposable(
    nativeAd: NativeAd?,
    modifier: Modifier = Modifier,
    withMediaView: Boolean = true,
) {
    if (nativeAd == null) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ad Loading...",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                val nativeAdView = NativeAdView(context)

                // Create the layout programmatically
                val rootLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(16, 16, 16, 16)
                }

                // Header section
                val headerLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                }

                // Ad icon
                val iconImageView = ImageView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(60, 60).apply {
                        setMargins(0, 0, 16, 0)
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    background = ContextCompat.getDrawable(context, R.drawable.ic_launcher_background)
                }

                // Title and advertiser
                val titleLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val titleTextView = TextView(context).apply {
                    text = nativeAd.headline ?: "Ad Title"
                    textSize = 16f
                    setTypeface(null, Typeface.BOLD)
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                }

                val advertiserTextView = TextView(context).apply {
                    text = nativeAd.advertiser ?: "Advertiser"
                    textSize = 12f
                    setTextColor(android.graphics.Color.GRAY)
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                }

                titleLayout.addView(titleTextView)
                titleLayout.addView(advertiserTextView)

                headerLayout.addView(iconImageView)
                headerLayout.addView(titleLayout)

                rootLayout.addView(headerLayout)

                // Media view (if enabled)
                if (withMediaView && nativeAd.mediaContent != null) {
                    val mediaView = MediaView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            400
                        ).apply {
                            setMargins(0, 16, 0, 16)
                        }
                        mediaContent = nativeAd.mediaContent
                    }
                    rootLayout.addView(mediaView)
                    nativeAdView.mediaView = mediaView
                }

                // Body text
                val bodyTextView = TextView(context).apply {
                    text = nativeAd.body ?: "Ad description text goes here..."
                    textSize = 14f
                    maxLines = 3
                    ellipsize = TextUtils.TruncateAt.END
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 8, 0, 16)
                    }
                }

                rootLayout.addView(bodyTextView)

                // Call to action button
                val ctaButton = Button(context).apply {
                    text = nativeAd.callToAction ?: "Install"
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.END
                    }
                }

                rootLayout.addView(ctaButton)

                nativeAdView.addView(rootLayout)

                // Set the native ad view components
                nativeAdView.headlineView = titleTextView
                nativeAdView.bodyView = bodyTextView
                nativeAdView.callToActionView = ctaButton
                nativeAdView.iconView = iconImageView
                nativeAdView.advertiserView = advertiserTextView

                // Set the native ad
                nativeAdView.setNativeAd(nativeAd)

                nativeAdView
            }
        )
    }
}