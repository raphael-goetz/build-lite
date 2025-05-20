package de.raphaelgoetz.buildLite.question

import de.raphaelgoetz.astralis.event.listen
import de.raphaelgoetz.astralis.event.unregister
import de.raphaelgoetz.astralis.schedule.doLater
import de.raphaelgoetz.astralis.schedule.doNow
import de.raphaelgoetz.astralis.schedule.time.TaskTimeTypes
import de.raphaelgoetz.astralis.text.communication.CommunicationType
import de.raphaelgoetz.astralis.text.components.AdventureMessage
import de.raphaelgoetz.astralis.text.components.adventureMessage
import de.raphaelgoetz.astralis.text.components.adventureText
import de.raphaelgoetz.astralis.text.sendText
import de.raphaelgoetz.astralis.text.translation.getValue
import de.raphaelgoetz.astralis.text.translation.sendTransText
import de.raphaelgoetz.buildLite.store.BuildPlayer
import de.raphaelgoetz.buildLite.store.BuildServer
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.bukkit.event.Listener

fun BuildPlayer.askNewWorldCreate(server: BuildServer) {

    val question =
        adventureMessage(player.locale().getValue("question.world.create")) {
            type = CommunicationType.INFO
            onHoverText(adventureText(player.locale().getValue("question.world.create.hover")) {
                type = CommunicationType.DEBUG
            })
        }

    val nothing = adventureMessage(player.locale().getValue("question.world.create.timeout")) {
        type = CommunicationType.ALERT
    }

    player.interrogate_new(question, nothing) { _, isAnswered, event ->

        if (!isAnswered) return@interrogate_new
        if (event == null) return@interrogate_new

        event.isCancelled = true
        val message = MiniMessage.miniMessage().serialize(event.message())
        val name = message.replace(Regex("\\W"), "")

        doNow {
            server.createWorld(name)

            player.sendTransText("question.world.create.success") {
                type = CommunicationType.SUCCESS
            }
        }
    }
}

inline fun Player.interrogate_new(
    questionMessage: AdventureMessage = adventureMessage("Please type your answer in the chat!") {
        type = CommunicationType.INFO
    },
    timeoutMessage: AdventureMessage = adventureMessage("You didn't answer!") {
        type = CommunicationType.ALERT
    },
    timeout: Long = 60,
    timeTypes: TaskTimeTypes = TaskTimeTypes.SECONDS,
    crossinline onAnswerReceived: (player: Player, answered: Boolean, chatEvent: AsyncChatEvent?) -> Unit
) {

    var answered = false
    var message: Component? = null
    this.sendText(questionMessage)
    lateinit var event: Listener

    event = this.listen<AsyncChatEvent> { asyncChatEvent ->
        event.unregister()
        message = asyncChatEvent.message()
        doNow {
            answered = true
            onAnswerReceived.invoke(this@interrogate_new, answered, asyncChatEvent)
        }
    }

    doLater(timeout, timeTypes) {
        event.unregister()
        if (message != null) return@doLater
        this.sendText(timeoutMessage)
        doNow {
            answered = false
            onAnswerReceived.invoke(this@interrogate_new, answered, null)
        }
    }
}