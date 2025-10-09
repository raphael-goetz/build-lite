package de.raphaelgoetz.buildLite.action

import de.raphaelgoetz.astralis.text.components.adventureText
import de.raphaelgoetz.astralis.ux.color.Colorization
import de.raphaelgoetz.buildLite.PREFIX
import de.raphaelgoetz.buildLite.cache.CacheReview
import de.raphaelgoetz.buildLite.player.checkPermission
import de.raphaelgoetz.buildLite.sql.RecordPlayerReview
import de.raphaelgoetz.buildLite.sql.createSqlPlayerReview
import de.raphaelgoetz.buildLite.sql.deleteSqlPlayerReview
import org.bukkit.entity.Player

fun Player.actionCreateReview(title: String, description: String) {
    if (!checkPermission("build-lite.review.create")) return
    val review = createSqlPlayerReview(title, description)
    if (review == null) {
        sendMessage(adventureText("$PREFIX Failed to create review.") {
            color = Colorization.RED
        })
        return
    }
    CacheReview.append(world, review)
    sendMessage(adventureText("$PREFIX Review '$title' created.") {
        color = Colorization.LIME
    })
}

fun Player.actionDeleteReview(recordPlayerReview: RecordPlayerReview) {
    if (!checkPermission("build-lite.review.delete")) return
    recordPlayerReview.deleteSqlPlayerReview()
    CacheReview.remove(recordPlayerReview)

    sendMessage(adventureText("$PREFIX Review '${recordPlayerReview.title}' deleted.") {
        color = Colorization.LIME
    })
}