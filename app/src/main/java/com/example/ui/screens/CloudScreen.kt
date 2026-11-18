package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.data.torrent.TorrentDownloadProgress
import com.example.data.torrent.TorrentMetadata
import com.example.data.torrent.TorrentParser
import com.example.data.torrent.TorrentThumbnailExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.data.util.GooglePhotosHelper
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.jellyfin.JellyfinLibraryItem
import com.example.data.jellyfin.JellyfinMediaItem
import com.example.data.model.MediaFormat
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.ui.components.MediaCard
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.viewmodel.CloudOption
import com.example.ui.viewmodel.JellyfinUiState

@Composable
fun CloudScreen(
    cloudOption: CloudOption,
    onSelectCloudOption: (CloudOption) -> Unit,
    // Jellyfin handlers
    jellyfinState: JellyfinUiState,
    onConnectJellyfinClick: () -> Unit,
    onSelectJellyfinLibrary: (JellyfinLibraryItem) -> Unit,
    onOpenJellyfinItem: (JellyfinMediaItem) -> Unit,
    // Stream URL handlers
    cloudStreams: List<MediaItem>,
    onAddCloudMedia: (title: String, url: String, type: MediaType, format: MediaFormat, thumbnailUrl: String?) -> Unit,
    onStreamTorrent: ((rawMagnetOrUri: String, customTitle: String?, fileIndex: Int, thumbUrl: String?) -> Unit)? = null,
    onStartTorrentDownload: ((metadata: TorrentMetadata, fileIndex: Int) -> Unit)? = null,
    downloadStates: Map<String, TorrentDownloadProgress> = emptyMap(),
    onOpenMedia: (MediaItem) -> Unit,
    onInfoClick: (MediaItem) -> Unit,
    onFavoriteToggle: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .testTag("cloud_screen")
    ) {
        // Sticky Top Selector: Jellyfin vs Stream URL vs Torrent
        Surface(
            color = SurfaceDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Segmented Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceElevated)
                        .padding(4.dp)
                ) {
                    // Jellyfin Option
                    val isJellyfin = cloudOption == CloudOption.JELLYFIN
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isJellyfin) PrimaryViolet else Color.Transparent)
                            .clickable { onSelectCloudOption(CloudOption.JELLYFIN) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = if (isJellyfin) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Jellyfin",
                                fontWeight = if (isJellyfin) FontWeight.Bold else FontWeight.Medium,
                                color = if (isJellyfin) Color.White else Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Stream URL Option
                    val isStreamUrl = cloudOption == CloudOption.STREAM_URL
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isStreamUrl) PrimaryViolet else Color.Transparent)
                            .clickable { onSelectCloudOption(CloudOption.STREAM_URL) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = if (isStreamUrl) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Stream URL",
                                fontWeight = if (isStreamUrl) FontWeight.Bold else FontWeight.Medium,
                                color = if (isStreamUrl) Color.White else Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Magnet / Torrent Option
                    val isTorrent = cloudOption == CloudOption.TORRENT
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isTorrent) PrimaryViolet else Color.Transparent)
                            .clickable { onSelectCloudOption(CloudOption.TORRENT) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = if (isTorrent) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Torrent",
                                fontWeight = if (isTorrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isTorrent) Color.White else Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Body Content based on active Cloud Option
        when (cloudOption) {
            CloudOption.JELLYFIN -> {
                JellyfinBrowserScreen(
                    state = jellyfinState,
                    onConnectClick = onConnectJellyfinClick,
                    onSelectLibrary = onSelectJellyfinLibrary,
                    onOpenItem = onOpenJellyfinItem
                )
            }
            CloudOption.STREAM_URL -> {
                StreamUrlPortal(
                    cloudStreams = cloudStreams.filter { !it.uriString.startsWith("magnet:?", ignoreCase = true) && !it.uriString.lowercase().contains(".torrent") },
                    onAddCloudMedia = onAddCloudMedia,
                    onOpenMedia = onOpenMedia,
                    onInfoClick = onInfoClick,
                    onFavoriteToggle = onFavoriteToggle
                )
            }
            CloudOption.TORRENT -> {
                TorrentStreamPortal(
                    torrentStreams = cloudStreams.filter { it.uriString.startsWith("magnet:?", ignoreCase = true) || it.uriString.lowercase().contains(".torrent") },
                    onStreamTorrent = onStreamTorrent,
                    onStartTorrentDownload = onStartTorrentDownload,
                    downloadStates = downloadStates,
                    onOpenMedia = onOpenMedia,
                    onInfoClick = onInfoClick,
                    onFavoriteToggle = onFavoriteToggle
                )
            }
        }
    }
}

@Composable
private fun StreamUrlPortal(
    cloudStreams: List<MediaItem>,
    onAddCloudMedia: (title: String, url: String, type: MediaType, format: MediaFormat, thumbnailUrl: String?) -> Unit,
    onOpenMedia: (MediaItem) -> Unit,
    onInfoClick: (MediaItem) -> Unit,
    onFavoriteToggle: (MediaItem) -> Unit
) {
    var urlInput by remember { mutableStateOf("") }
    var titleInput by remember { mutableStateOf("") }
    var thumbnailUrlInput by remember { mutableStateOf("") }
    var selectedFormat by remember { mutableStateOf(MediaFormat.MP4) }
    val coroutineScope = rememberCoroutineScope()
    var isResolvingAlbum by remember { mutableStateOf(false) }

    val selectedType = when (selectedFormat) {
        MediaFormat.MP4 -> MediaType.VIDEO
        MediaFormat.PDF -> MediaType.PDF
        MediaFormat.JPG, MediaFormat.PNG -> MediaType.PHOTO
        MediaFormat.UNKNOWN -> MediaType.VIDEO
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().testTag("stream_url_grid")
    ) {
        // Direct URL Input Card
        item(span = { GridItemSpan(maxLineSpan) }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth().testTag("card_stream_url_input")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AccentCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddLink,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Stream Direct Media URL",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Stream MP4, HLS (.m3u8), PDF, or images from any web URL",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { 
                            urlInput = it
                            val trimmed = it.trim()
                            val lower = trimmed.lowercase()
                            if (GooglePhotosHelper.isGooglePhotosUrl(trimmed)) {
                                selectedFormat = MediaFormat.JPG
                                coroutineScope.launch {
                                    isResolvingAlbum = true
                                    try {
                                        val info = GooglePhotosHelper.extractAlbumInfo(trimmed)
                                        info?.let { album ->
                                            if (titleInput.isBlank() && album.title.isNotBlank()) {
                                                titleInput = album.title
                                            }
                                            if (thumbnailUrlInput.isBlank() && album.coverUrl.isNotBlank()) {
                                                thumbnailUrlInput = album.coverUrl
                                            }
                                        }
                                    } catch (_: Exception) {
                                    } finally {
                                        isResolvingAlbum = false
                                    }
                                }
                            } else if (lower.contains("mycloudz") || lower.contains("/v/") || lower.contains("embed") || lower.contains("streamtape") || lower.contains("vidcloud") || lower.contains(".mp4") || lower.contains(".m3u8")) {
                                selectedFormat = MediaFormat.MP4
                            } else if (lower.contains(".pdf")) {
                                selectedFormat = MediaFormat.PDF
                            } else if (lower.contains(".jpg") || lower.contains(".jpeg") || lower.contains(".png") || lower.contains(".webp")) {
                                selectedFormat = MediaFormat.JPG
                            } else {
                                selectedFormat = MediaFormat.MP4
                            }
                        },
                        placeholder = { Text("https://mycloudz.cc/v/... or photos.app.goo.gl/...", fontSize = 13.sp) },
                        label = { Text("Stream Media / Google Photos / Web URL") },
                        supportingText = {
                            if (isResolvingAlbum) {
                                Text(
                                    "Detecting Google Photos album cover & title...",
                                    color = AccentCyan,
                                    fontSize = 11.sp
                                )
                            } else if (GooglePhotosHelper.isGooglePhotosUrl(urlInput.trim())) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "Google Photos album detected",
                                        color = Color(0xFF4285F4),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Fetch Cover",
                                        color = AccentCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable {
                                            coroutineScope.launch {
                                                isResolvingAlbum = true
                                                try {
                                                    val info = GooglePhotosHelper.extractAlbumInfo(urlInput.trim())
                                                    info?.let { album ->
                                                        if (album.title.isNotBlank()) titleInput = album.title
                                                        if (album.coverUrl.isNotBlank()) thumbnailUrlInput = album.coverUrl
                                                    }
                                                } catch (_: Exception) {
                                                } finally {
                                                    isResolvingAlbum = false
                                                }
                                            }
                                        }
                                    )
                                }
                            } else {
                                Text(
                                    "Supports direct video (.mp4, .m3u8), Google Photos albums, documents (.pdf), and Web Streams",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryViolet,
                            unfocusedBorderColor = SurfaceElevated,
                            focusedContainerColor = SurfaceElevated.copy(alpha = 0.5f),
                            unfocusedContainerColor = SurfaceElevated.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_stream_url")
                    )

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        placeholder = { Text("Optional title (e.g., Live Cyber Stream)", fontSize = 13.sp) },
                        label = { Text("Stream Title") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryViolet,
                            unfocusedBorderColor = SurfaceElevated,
                            focusedContainerColor = SurfaceElevated.copy(alpha = 0.5f),
                            unfocusedContainerColor = SurfaceElevated.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_stream_title")
                    )

                    OutlinedTextField(
                        value = thumbnailUrlInput,
                        onValueChange = { thumbnailUrlInput = it },
                        placeholder = { Text("https://example.com/poster.jpg (webURL)", fontSize = 13.sp) },
                        label = { Text("Thumbnail Link (webURL - Optional)") },
                        supportingText = {
                            Text(
                                "Web image URL for card thumbnail. Essential for web view streams and saved to JSON config.",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Image, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            if (thumbnailUrlInput.isNotBlank()) {
                                IconButton(onClick = { thumbnailUrlInput = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryViolet,
                            unfocusedBorderColor = SurfaceElevated,
                            focusedContainerColor = SurfaceElevated.copy(alpha = 0.5f),
                            unfocusedContainerColor = SurfaceElevated.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_stream_thumbnail_url")
                    )

                    // Live Thumbnail Preview if URL is entered
                    if (thumbnailUrlInput.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceElevated.copy(alpha = 0.6f))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp, 40.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DeepObsidian),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = thumbnailUrlInput.trim(),
                                    contentDescription = "Thumbnail Preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (GooglePhotosHelper.isGooglePhotosUrl(urlInput.trim())) "Google Photos Album Cover" else "Thumbnail Preview",
                                    color = if (GooglePhotosHelper.isGooglePhotosUrl(urlInput.trim())) Color(0xFF4285F4) else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (GooglePhotosHelper.isGooglePhotosUrl(urlInput.trim())) "Saved as folder cover and backed up in configuration JSON" else "Will be saved in configuration JSON and displayed on cards",
                                    color = AccentCyan,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (urlInput.isNotBlank()) {
                                    val defaultTitle = when {
                                        GooglePhotosHelper.isGooglePhotosUrl(urlInput) -> "Google Photos Album"
                                        selectedFormat == MediaFormat.MP4 -> "Web Video Stream"
                                        selectedFormat == MediaFormat.PDF -> "Online PDF"
                                        else -> "Online Photo"
                                    }
                                    val finalTitle = titleInput.ifBlank { defaultTitle }
                                    val cleanThumb = thumbnailUrlInput.trim().ifBlank { null }
                                    val streamType = when (selectedFormat) {
                                        MediaFormat.MP4 -> MediaType.VIDEO
                                        MediaFormat.PDF -> MediaType.PDF
                                        else -> MediaType.PHOTO
                                    }
                                    onAddCloudMedia(finalTitle, urlInput.trim(), streamType, selectedFormat, cleanThumb)
                                    urlInput = ""
                                    titleInput = ""
                                    thumbnailUrlInput = ""
                                }
                            },
                            enabled = urlInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("btn_play_stream_url")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Launch Stream", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Saved Cloud Streams Header
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "Saved Cloud Streams (${cloudStreams.size})",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Saved Streams Grid
        if (cloudStreams.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No saved stream URLs yet",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Enter any stream URL above to stream",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        } else {
            items(cloudStreams, key = { it.id }) { streamItem ->
                MediaCard(
                    item = streamItem,
                    onClick = { onOpenMedia(streamItem) },
                    onInfoClick = { onInfoClick(streamItem) },
                    onFavoriteToggle = { onFavoriteToggle(streamItem) }
                )
            }
        }
    }
}

@Composable
private fun TorrentStreamPortal(
    torrentStreams: List<MediaItem>,
    onStreamTorrent: ((rawMagnetOrUri: String, customTitle: String?, fileIndex: Int, thumbUrl: String?) -> Unit)?,
    onStartTorrentDownload: ((metadata: TorrentMetadata, fileIndex: Int) -> Unit)?,
    downloadStates: Map<String, TorrentDownloadProgress>,
    onOpenMedia: (MediaItem) -> Unit,
    onInfoClick: (MediaItem) -> Unit,
    onFavoriteToggle: (MediaItem) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var magnetInput by remember { mutableStateOf("") }
    var customTitleInput by remember { mutableStateOf("") }
    var thumbnailUrlInput by remember { mutableStateOf("") }
    var parsedMetadata by remember { mutableStateOf<TorrentMetadata?>(null) }
    var selectedFileIndex by remember { mutableIntStateOf(0) }
    var isParsingTorrent by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                isParsingTorrent = true
                try {
                    val stream = context.contentResolver.openInputStream(uri)
                    val bytes = stream?.readBytes()
                    stream?.close()
                    if (bytes != null && bytes.isNotEmpty()) {
                        val meta = TorrentParser.parseTorrentFile(bytes, uri.toString())
                        val thumb = TorrentThumbnailExtractor.extractThumbnail(context, uri.toString(), meta.displayName)
                        withContext(Dispatchers.Main) {
                            parsedMetadata = meta
                            magnetInput = uri.toString()
                            if (customTitleInput.isBlank()) {
                                customTitleInput = meta.displayName
                            }
                            if (thumbnailUrlInput.isBlank() && thumb != null) {
                                thumbnailUrlInput = thumb
                            }
                        }
                    }
                } catch (_: Exception) {
                } finally {
                    withContext(Dispatchers.Main) {
                        isParsingTorrent = false
                    }
                }
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().testTag("torrent_stream_grid")
    ) {
        // Torrent Input Card
        item(span = { GridItemSpan(maxLineSpan) }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth().testTag("card_torrent_stream_input")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryViolet.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Magnet & Torrent Video Stream",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Direct stream & download torrents with multiple video support",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Quick Sample Torrents for Instant Testing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sample:",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        val sampleTorrents = listOf(
                            Triple("Sintel", "magnet:?xt=urn:btih:08ada5a7a6183aae1e09d831df6748d566095a10&dn=Sintel&tr=udp%3A%2F%2Fexplodie.org%3A6969&ws=https%3A%2F%2Fcommondatastorage.googleapis.com%2Fgtv-videos-bucket%2Fsample%2FSintel.mp4", "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=400&q=80"),
                            Triple("Big Buck Bunny", "magnet:?xt=urn:btih:dd8255ecdc7ca55fb0bbf81323d87062db1f6d1c&dn=Big+Buck+Bunny&tr=udp%3A%2F%2Fexplodie.org%3A6969&ws=https%3A%2F%2Fcommondatastorage.googleapis.com%2Fgtv-videos-bucket%2Fsample%2FBigBuckBunny.mp4", "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?w=400&q=80"),
                            Triple("Tears of Steel", "magnet:?xt=urn:btih:2b87c9da8619e98b577e26e6f41f4f63a0ecb638&dn=Tears+of+Steel&tr=udp%3A%2F%2Fexplodie.org%3A6969&ws=https%3A%2F%2Fcommondatastorage.googleapis.com%2Fgtv-videos-bucket%2Fsample%2FTearsOfSteel.mp4", "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?w=400&q=80")
                        )
                        sampleTorrents.forEach { (name, uri, thumb) ->
                            Surface(
                                color = PrimaryViolet.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryViolet.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .clickable {
                                        magnetInput = uri
                                        parsedMetadata = TorrentParser.parseMagnetUri(uri)
                                        customTitleInput = name
                                        thumbnailUrlInput = thumb
                                    }
                            ) {
                                Text(
                                    text = name,
                                    color = AccentCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Magnet / URI Text Field
                    OutlinedTextField(
                        value = magnetInput,
                        onValueChange = { input ->
                            magnetInput = input
                            val trimmed = input.trim()
                            if (trimmed.startsWith("magnet:?", ignoreCase = true)) {
                                val meta = TorrentParser.parseMagnetUri(trimmed)
                                parsedMetadata = meta
                                if (customTitleInput.isBlank()) {
                                    customTitleInput = meta.displayName
                                }
                                coroutineScope.launch(Dispatchers.IO) {
                                    val autoThumb = TorrentThumbnailExtractor.extractThumbnail(context, trimmed, meta.displayName)
                                    if (autoThumb != null && thumbnailUrlInput.isBlank()) {
                                        withContext(Dispatchers.Main) {
                                            thumbnailUrlInput = autoThumb
                                        }
                                    }
                                }
                            }
                        },
                        placeholder = { Text("magnet:?xt=urn:btih:... or pick .torrent file", fontSize = 13.sp) },
                        label = { Text("Magnet URI or Torrent File") },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (magnetInput.isNotBlank()) {
                                    IconButton(onClick = {
                                        magnetInput = ""
                                        parsedMetadata = null
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        filePickerLauncher.launch(arrayOf("*/*", "application/x-bittorrent"))
                                    },
                                    modifier = Modifier.testTag("btn_pick_torrent_file")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FileOpen,
                                        contentDescription = "Pick .torrent file",
                                        tint = AccentCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryViolet,
                            unfocusedBorderColor = SurfaceElevated,
                            focusedContainerColor = SurfaceElevated.copy(alpha = 0.5f),
                            unfocusedContainerColor = SurfaceElevated.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_torrent_magnet")
                    )

                    // Optional Title Input
                    OutlinedTextField(
                        value = customTitleInput,
                        onValueChange = { customTitleInput = it },
                        placeholder = { Text("Custom Title (optional)", fontSize = 13.sp) },
                        label = { Text("Stream / Torrent Title") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryViolet,
                            unfocusedBorderColor = SurfaceElevated,
                            focusedContainerColor = SurfaceElevated.copy(alpha = 0.5f),
                            unfocusedContainerColor = SurfaceElevated.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_torrent_title")
                    )

                    // Optional Thumbnail Link Input
                    OutlinedTextField(
                        value = thumbnailUrlInput,
                        onValueChange = { thumbnailUrlInput = it },
                        placeholder = { Text("Auto-extracted or enter web/local image URI", fontSize = 13.sp) },
                        label = { Text("Thumbnail Image (Auto-extracted or Custom)") },
                        leadingIcon = {
                            Icon(Icons.Default.Image, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (thumbnailUrlInput.isNotBlank()) {
                                IconButton(onClick = { thumbnailUrlInput = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryViolet,
                            unfocusedBorderColor = SurfaceElevated,
                            focusedContainerColor = SurfaceElevated.copy(alpha = 0.5f),
                            unfocusedContainerColor = SurfaceElevated.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_torrent_thumbnail")
                    )

                    // Live Thumbnail Preview if available
                    if (thumbnailUrlInput.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceElevated.copy(alpha = 0.6f))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp, 40.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DeepObsidian),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = thumbnailUrlInput.trim(),
                                    contentDescription = "Torrent Thumbnail Preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Torrent Video Thumbnail",
                                    color = AccentCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Saved in configuration JSON and displayed on media cards",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Multi-Video File Selector (If parsed metadata contains multiple videos or files)
                    val currentMeta = parsedMetadata
                    if (currentMeta != null && currentMeta.files.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceElevated.copy(alpha = 0.5f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Select Video in Torrent (${currentMeta.files.size} available):",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            currentMeta.files.forEachIndexed { idx, file ->
                                val isSelected = idx == selectedFileIndex
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) PrimaryViolet.copy(alpha = 0.35f) else Color.Transparent)
                                        .clickable { selectedFileIndex = idx }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (file.isVideo) Icons.Default.Movie else Icons.Default.VideoFile,
                                        contentDescription = null,
                                        tint = if (isSelected) AccentCyan else Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = file.fileName,
                                        color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = file.formattedSize,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Action Buttons: Direct Stream & Download
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Stream Button
                        Button(
                            onClick = {
                                if (magnetInput.isNotBlank()) {
                                    val cleanThumb = thumbnailUrlInput.trim().ifBlank { null }
                                    onStreamTorrent?.invoke(
                                        magnetInput.trim(),
                                        customTitleInput.ifBlank { null },
                                        selectedFileIndex,
                                        cleanThumb
                                    )
                                    magnetInput = ""
                                    customTitleInput = ""
                                    thumbnailUrlInput = ""
                                    parsedMetadata = null
                                }
                            },
                            enabled = magnetInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_stream_torrent")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Stream Directly", fontWeight = FontWeight.Bold)
                        }

                        // Download Button
                        OutlinedButton(
                            onClick = {
                                val meta = parsedMetadata ?: if (magnetInput.startsWith("magnet:?", ignoreCase = true)) {
                                    TorrentParser.parseMagnetUri(magnetInput.trim())
                                } else null

                                if (meta != null) {
                                    onStartTorrentDownload?.invoke(meta, selectedFileIndex)
                                }
                            },
                            enabled = magnetInput.isNotBlank(),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("btn_download_torrent")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Active Torrent Downloads Section
        if (downloadStates.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceElevated)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Torrent Downloads (${downloadStates.size})",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    downloadStates.values.forEach { dl ->
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dl.title,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${dl.progressPercentInt}% • ${dl.downloadSpeedFormatted}",
                                    color = AccentCyan,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { dl.progressPercent },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = AccentCyan,
                                trackColor = DeepObsidian
                            )
                        }
                    }
                }
            }
        }

        // Saved Torrent Streams Header
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "Saved Torrent Streams (${torrentStreams.size})",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Saved Streams Grid
        if (torrentStreams.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No saved torrent streams yet",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Paste a magnet link or pick a .torrent file above",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        } else {
            items(torrentStreams, key = { it.id }) { streamItem ->
                MediaCard(
                    item = streamItem,
                    onClick = { onOpenMedia(streamItem) },
                    onInfoClick = { onInfoClick(streamItem) },
                    onFavoriteToggle = { onFavoriteToggle(streamItem) }
                )
            }
        }
    }
}
