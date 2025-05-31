package de.raphaelgoetz.buildLite.registry

import com.google.gson.JsonParser
import de.raphaelgoetz.astralis.items.builder.SmartItem
import de.raphaelgoetz.astralis.items.createSmartItem
import de.raphaelgoetz.astralis.items.data.InteractionType
import de.raphaelgoetz.astralis.items.smartItemWithoutMeta
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.profile.PlayerProfile
import java.io.IOException
import java.io.InputStreamReader
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.util.*
import java.util.logging.Level

enum class DisplayURL(val url: String) {
    ITEM_CATEGORY("https://textures.minecraft.net/texture/56330a4a22ff55871fc8c618e421a37733ac1dcab9c8e1a4bb73ae645a4a4e"),
    ITEM_WORLD_1("https://textures.minecraft.net/texture/5a9d914a12c17cccb55899285a066902ba53976807407fcb8696dbe19aef77"),
    GUI_SPAWN("https://textures.minecraft.net/texture/88a1471f37a362dce3582d570e1ba72705c3b42615c8a5a6f1fa6655a1d1f09"),
    GUI_WORLD("https://textures.minecraft.net/texture/140d4e3146c06157c4837106212157cd6600b6ff934af933b1dcab88a7c1efa2"),
    GUI_BACK("https://textures.minecraft.net/texture/44f7bc1fa8217b18b323af841372a3f7c602a435c828faa403d176c6b37b605b"),
    GUI_CLOSE("https://textures.minecraft.net/texture/e6f1898f1e84805694544944f8b49c7007622a2d9b2bb59a278519a68991ac69"),
}

fun Player.getItemWithURL(material: Material, url: String, name: String, description: String = "", interactionType: InteractionType = InteractionType.DISPLAY_CLICK): SmartItem {
    try {
        val categoryTextureURL = URI.create(url).toURL()
        return createSmartItem<SkullMeta>(
            name, Material.PLAYER_HEAD, description, interactionType = interactionType
        ) {
            val newPlayerProfile = Bukkit.createProfile(UUID.randomUUID())
            val playerTextures = newPlayerProfile.textures

            playerTextures.skin = categoryTextureURL
            newPlayerProfile.setTextures(playerTextures)

            playerProfile = newPlayerProfile
        }
    } catch (_: Exception) {
        this.sendMessage("Player Textures couldn't be loaded, so used normal items instead")
        return smartItemWithoutMeta(name, material)
    }
}

fun Player.getItemWithURL(material: Material, url: String, name: String, lore: List<Component>, interactionType: InteractionType = InteractionType.DISPLAY_CLICK): SmartItem {
    try {
        val categoryTextureURL = URI.create(url).toURL()
        return createSmartItem<SkullMeta>(
            name, Material.PLAYER_HEAD, interactionType = interactionType
        ) {
            val newPlayerProfile = Bukkit.createProfile(UUID.randomUUID())
            val playerTextures = newPlayerProfile.textures

            playerTextures.skin = categoryTextureURL
            newPlayerProfile.setTextures(playerTextures)

            playerProfile = newPlayerProfile

            this.lore(lore)
        }
    } catch (_: Exception) {
        this.sendMessage("Player Textures couldn't be loaded, so used normal items instead")
        return smartItemWithoutMeta(name, material)
    }
}
