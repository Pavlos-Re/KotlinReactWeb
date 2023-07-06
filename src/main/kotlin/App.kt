import csstype.FontWeight
import csstype.NamedColor
import csstype.px
import emotion.react.css
import kotlinx.coroutines.async
import react.*
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span

suspend fun fetchVideo(id: Int): Video {
    val response = window
        .fetch("https://my-json-server.typicode.com/kotlin-hands-on/kotlinconf-json/videos/$id")
        .await()
        .text()
        .await()
    return Json.decodeFromString(response)
}

suspend fun fetchVideos(): List<Video> = coroutineScope {
    (1..25).map { id ->
        async {
            fetchVideo(id)
        }
    }.awaitAll()
}


val mainScope = MainScope()

val App = FC<Props> {
    // typesafe HTML goes here, starting with the first h1 tag!
    var currentVideo: Video? by useState(null)
    var unwatchedVideos: List<Video> by useState(emptyList())
    var watchedVideos: List<Video> by useState(emptyList())

    useEffectOnce {
        mainScope.launch {
            unwatchedVideos = fetchVideos()
        }
    }

    div {
        h3 {
            +"Videos to watch"
        }
        css {
            paddingLeft = 10.px
            fontWeight = FontWeight.bolder
        }
    }
    div {
        css {
            paddingLeft = 20.px
            color = NamedColor.blue
        }
        VideoList {
            videos = unwatchedVideos
            selectedVideo = currentVideo
            onSelectVideo = { video ->
                currentVideo = video
            }
        }
    }
        div {
            css {
                paddingLeft = 10.px
                fontWeight = FontWeight.bolder
            }
            h3 {
                +"Videos watched"
            }
        }
        div {
            css {
                color = NamedColor.red
                paddingLeft = 20.px
            }
            VideoList {
                videos = watchedVideos
                selectedVideo = currentVideo
                onSelectVideo = { video ->
                    currentVideo = video
                }
            }
        }

        currentVideo?.let { curr ->
            VideoPlayer {
                video = curr
                unwatchedVideo = curr in unwatchedVideos
                onWatchedButtonPressed = {
                    if (video in unwatchedVideos) {
                        unwatchedVideos = unwatchedVideos - video
                        watchedVideos = watchedVideos + video
                    } else {
                        watchedVideos = watchedVideos - video
                        unwatchedVideos = unwatchedVideos + video
                    }
                }
            }
        }
    }

