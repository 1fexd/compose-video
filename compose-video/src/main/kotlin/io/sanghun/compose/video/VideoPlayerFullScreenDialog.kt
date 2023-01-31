/*
 * Copyright 2023 Dora Lee
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sanghun.compose.video

import android.content.pm.ActivityInfo
import android.widget.ImageButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.util.RepeatModeUtil
import io.sanghun.compose.video.controller.VideoPlayerControllerConfig
import io.sanghun.compose.video.controller.applyToExoPlayerView
import io.sanghun.compose.video.util.findActivity
import io.sanghun.compose.video.util.setFullScreen

/**
 * ExoPlayer does not support full screen views by default.
 * So create a full screen modal that wraps the Compose Dialog.
 *
 * Delegate all functions of the video controller that were used just before
 * the full screen to the video controller managed by that component.
 * Conversely, if the full screen dismissed, it will restore all the functions it delegated
 * for synchronization with the video controller on the full screen and the video controller on the previous screen.
 *
 * @param player Exoplayer instance.
 * @param currentPlayerView [StyledPlayerView] instance currently in use for playback.
 * @param fullScreenPlayerView Callback to return all features to existing video player controller.
 * @param controllerConfig Player controller config. You can customize the Video Player Controller UI.
 * @param repeatMode Sets the content repeat mode.
 * @param onDismissRequest Callback that occurs when modals are closed.
 * @param securePolicy Policy on setting [android.view.WindowManager.LayoutParams.FLAG_SECURE] on a full screen dialog window.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun VideoPlayerFullScreenDialog(
    player: ExoPlayer,
    currentPlayerView: StyledPlayerView,
    fullScreenPlayerView: StyledPlayerView.() -> Unit,
    controllerConfig: VideoPlayerControllerConfig,
    repeatMode: RepeatMode,
    onDismissRequest: () -> Unit,
    securePolicy: SecureFlagPolicy,
) {
    val context = LocalContext.current
    val fullScreenPlayerView = remember {
        StyledPlayerView(context)
            .also(fullScreenPlayerView)
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            securePolicy = securePolicy,
            decorFitsSystemWindows = true,
        ),
    ) {
        LaunchedEffect(Unit) {
            StyledPlayerView.switchTargetView(player, currentPlayerView, fullScreenPlayerView)

            val currentActivity = context.findActivity()
            currentActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            currentActivity.setFullScreen(true)
        }

        LaunchedEffect(controllerConfig) {
            controllerConfig.applyToExoPlayerView(fullScreenPlayerView) {
                if (!it) {
                    onDismissRequest()
                }
            }
            fullScreenPlayerView.findViewById<ImageButton>(com.google.android.exoplayer2.ui.R.id.exo_fullscreen)
                .performClick()
        }

        LaunchedEffect(controllerConfig, repeatMode) {
            fullScreenPlayerView.setRepeatToggleModes(
                if (controllerConfig.showRepeatModeButton) {
                    RepeatModeUtil.REPEAT_TOGGLE_MODE_ALL or RepeatModeUtil.REPEAT_TOGGLE_MODE_ONE
                } else {
                    RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE
                },
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            VideoPlayerSurface(
                defaultPlayerView = fullScreenPlayerView,
                player = player,
                usePlayerController = true,
                handleLifecycle = true,
                autoDispose = false,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(),
            )
        }
    }
}
