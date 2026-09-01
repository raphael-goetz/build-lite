package de.raphaelgoetz.buildLite.help

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta

fun Player.openHelpBook() {
    closeDialog()
    val book = ItemStack(Material.WRITTEN_BOOK)
    val meta = book.itemMeta as BookMeta
    meta.title(Component.text("Build-Lite Guide"))
    meta.author(Component.text("Build-Lite"))
    meta.addPages(
        page(
            "Quick Start",
            "Use the home menu to find worlds, warps, players, creation tools, activity, and settings.\n\n",
            commandLink("Open Home", "/bl"),
            Component.text("\n"),
            commandLink("Browse Worlds", "/bl worlds"),
        ),
        page(
            "Worlds",
            "World cards have two controls:\n\nLeft-click joins.\nRight-click opens World Details.\n\nDetails contains pinning, warps, reviews, build time, and management.",
        ),
        page(
            "World Details",
            "From World Details you can join, pin or unpin, browse warps and reviews, view build time, copy the UUID, create a release, set spawn, or open World Settings when permitted.",
        ),
        page(
            "Creating",
            "The Create menu contains worlds, warps, reviews, banners, and world migration. Entries you cannot use are hidden.\n\n",
            commandLink("Open Create", "/bl create"),
        ),
        page(
            "Building",
            "Build Mode controls whether you may change blocks. Night Vision, review visibility, and fly speed are available under Settings. Press G (Quick Actions) to open Personal Preferences directly.\n\n",
            commandLink("Open Settings", "/bl settings"),
        ),
        page(
            "Warps & Reviews",
            "Warps save useful locations. Reviews attach feedback to the current world. Both can be opened from World Details or commands.\n\n",
            commandLink("Open Warps", "/bl warps"),
            Component.text("\n"),
            commandLink("Open Reviews", "/bl reviews"),
        ),
        page(
            "Banners",
            "The banner tool provides reusable banner patterns for building. Open it from Create or directly with the command below.\n\n",
            commandLink("Open Banners", "/bl banner"),
        ),
        page(
            "Build Time",
            "Time is tracked while you are in Creative mode, Build Mode is enabled, you are active, and you are inside a managed world.\n\n",
            commandLink("My Build Time", "/bl time"),
        ),
        page(
            "Useful Commands",
            "/bl worlds\n/bl warps\n/bl players\n/bl reviews\n/bl time\n/bl create\n/bl settings\n/bl scoreboard\n/bl help",
        ),
        page(
            "Admin",
            "Admins can browse another player's tracked time and migrate existing worlds from the activity and creation menus. Options only appear when permitted.",
        ),
    )
    book.itemMeta = meta
    openBook(book)
}

private fun page(title: String, vararg content: Any): Component {
    var result = Component.empty()
        .append(Component.text(title, NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
        .append(Component.text("\n\n", NamedTextColor.BLACK))

    content.forEach { part ->
        result = result.append(
            when (part) {
                is Component -> part
                else -> Component.text(part.toString(), NamedTextColor.BLACK)
            }
        )
    }
    return result
}

private fun commandLink(label: String, command: String): Component =
    Component.text("› $label", NamedTextColor.BLUE, TextDecoration.UNDERLINED)
        .clickEvent(ClickEvent.runCommand(command))
