package scripts.wikiscraper.examples

import scripts.wikiscraper.OsrsWiki
import scripts.wikiscraper.query.getMonsterDetails


/* Written by IvanEOD 12/22/2022, at 12:14 AM */

fun main() {

    val wiki = OsrsWiki.builder().build()

    val manData = wiki.getMonsterDetails("Man")

    manData.forEach { (title, list) ->
        println("Title: $title")
        list.forEach { details ->
            details.debug("    ")
        }
    }
}

/*

Title: Man
    Monster: Man (Light blue) [[3110]]
        Image: https://oldschool.runescape.wiki/images/Man_%28red%29.png
        Members: false
        Level: 2
        Size: 1
        Examine: One of Gielinor's many citizens.
        XP Bonus: 0.0
        Max Hit: [1]
        Aggressive: false
        Poisonous: false
        Attack Style: [[[Crush]]]
        Attack Speed: 4
        Respawn: 25
        Slayer Level: null
        Slayer XP: null
        Hitpoints Level: 7
        Attack Level: 1
        Strength Level: 1
        Defence Level: 1
        Ranged Level: 1
        Mage Level: 1
        Attack Bonus: 0
        Strength Bonus: 0
        Magic Attack Bonus: 0
        Magic Strength Bonus: 0
        Range Attack Bonus: 0
        Range Strength Bonus: 0
        Stab Defence Bonus: -21
        Slash Defence Bonus: -21
        Crush Defence Bonus: -21
        Magic Defence Bonus: -21
        Range Defence Bonus: -21
        Drop Version: Regular
        Immune Poison: false
        Immune Venom: false
        Immune Cannon: false
        Immune Thrall: false
        Release: Thu Jan 04 00:00:00 EST 2001
        Update: Runescape beta is now online!
        Removal Update:
        Last Update: Thu Jan 04 00:00:00 EST 2001
    Monster: Man (Shayzien) [[3106, 6818, 6987, 8858]]
        Image: https://oldschool.runescape.wiki/images/Man.png
        Members: false
        Level: 2
        Size: 1
        Examine: One of Gielinor's many citizens.
        XP Bonus: 0.0
        Max Hit: [1]
        Aggressive: false
        Poisonous: false
        Attack Style: [[[Crush]]]
        Attack Speed: 4
        Respawn: 25
        Slayer Level: null
        Slayer XP: null
        Hitpoints Level: 7
        Attack Level: 1
        Strength Level: 1
        Defence Level: 1
        Ranged Level: 1
        Mage Level: 1
        Attack Bonus: 0
        Strength Bonus: 0
        Magic Attack Bonus: 0
        Magic Strength Bonus: 0
        Range Attack Bonus: 0
        Range Strength Bonus: 0
        Stab Defence Bonus: -21
        Slash Defence Bonus: -21
        Crush Defence Bonus: -21
        Magic Defence Bonus: -21
        Range Defence Bonus: -21
        Drop Version: Regular
        Immune Poison: false
        Immune Venom: false
        Immune Cannon: false
        Immune Thrall: false
        Release: Thu Jan 04 00:00:00 EST 2001
        Update: Runescape beta is now online!
        Removal Update:
        Last Update: Thu Jan 04 00:00:00 EST 2001
    Monster: Man (Blue Moon Inn) [[11058]]
        Image: https://oldschool.runescape.wiki/images/Man_%28backpack%29.png
        Members: false
        Level: 2
        Size: 1
        Examine: One of Gielinor's many citizens.
        XP Bonus: 0.0
        Max Hit: [1]
        Aggressive: false
        Poisonous: false
        Attack Style: [[[Crush]]]
        Attack Speed: 4
        Respawn: 25
        Slayer Level: null
        Slayer XP: null
        Hitpoints Level: 7
        Attack Level: 1
        Strength Level: 1
        Defence Level: 1
        Ranged Level: 1
        Mage Level: 1
        Attack Bonus: 0
        Strength Bonus: 0
        Magic Attack Bonus: 0
        Magic Strength Bonus: 0
        Range Attack Bonus: 0
        Range Strength Bonus: 0
        Stab Defence Bonus: -21
        Slash Defence Bonus: -21
        Crush Defence Bonus: -21
        Magic Defence Bonus: -21
        Range Defence Bonus: -21
        Drop Version: Regular
        Immune Poison: false
        Immune Venom: false
        Immune Cannon: false
        Immune Thrall: false
        Release: Thu Jan 04 00:00:00 EST 2001
        Update: Runescape beta is now online!
        Removal Update:
        Last Update: Thu Jan 04 00:00:00 EST 2001
    Monster: Man (Blue) [[3652]]
        Image: https://oldschool.runescape.wiki/images/Man_%28backpack%29.png
        Members: false
        Level: 2
        Size: 1
        Examine: One of Gielinor's many citizens.
        XP Bonus: 0.0
        Max Hit: [1]
        Aggressive: false
        Poisonous: false
        Attack Style: [[[Crush]]]
        Attack Speed: 4
        Respawn: 25
        Slayer Level: null
        Slayer XP: null
        Hitpoints Level: 7
        Attack Level: 1
        Strength Level: 1
        Defence Level: 1
        Ranged Level: 1
        Mage Level: 1
        Attack Bonus: 0
        Strength Bonus: 0
        Magic Attack Bonus: 0
        Magic Strength Bonus: 0
        Range Attack Bonus: 0
        Range Strength Bonus: 0
        Stab Defence Bonus: -21
        Slash Defence Bonus: -21
        Crush Defence Bonus: -21
        Magic Defence Bonus: -21
        Range Defence Bonus: -21
        Drop Version: Regular
        Immune Poison: false
        Immune Venom: false
        Immune Cannon: false
        Immune Thrall: false
        Release: Thu Jan 04 00:00:00 EST 2001
        Update: Runescape beta is now online!
        Removal Update:
        Last Update: Thu Jan 04 00:00:00 EST 2001
    Monster: Man (Black) [[3261]]
        Image: https://oldschool.runescape.wiki/images/Man_%28Karamja%29.png
        Members: false
        Level: 2
        Size: 1
        Examine: One of Gielinor's many citizens.
        XP Bonus: 0.0
        Max Hit: [1]
        Aggressive: false
        Poisonous: false
        Attack Style: [[[Crush]]]
        Attack Speed: 4
        Respawn: null
        Slayer Level: null
        Slayer XP: null
        Hitpoints Level: 7
        Attack Level: 1
        Strength Level: 1
        Defence Level: 1
        Ranged Level: 1
        Mage Level: 1
        Attack Bonus: 0
        Strength Bonus: 0
        Magic Attack Bonus: 0
        Magic Strength Bonus: 0
        Range Attack Bonus: 0
        Range Strength Bonus: 0
        Stab Defence Bonus: -21
        Slash Defence Bonus: -21
        Crush Defence Bonus: -21
        Magic Defence Bonus: -21
        Range Defence Bonus: -21
        Drop Version: Regular
        Immune Poison: false
        Immune Venom: false
        Immune Cannon: false
        Immune Thrall: false
        Release: Thu Jan 04 00:00:00 EST 2001
        Update: Runescape beta is now online!
        Removal Update:
        Last Update: Thu Jan 04 00:00:00 EST 2001
    Monster: Man (Green) [[3108, 6989]]
        Image: https://oldschool.runescape.wiki/images/Man_%28Shayzien%29.png
        Members: false
        Level: 2
        Size: 1
        Examine: One of Gielinor's many citizens.
        XP Bonus: 0.0
        Max Hit: [1]
        Aggressive: false
        Poisonous: false
        Attack Style: [[[Crush]]]
        Attack Speed: 4
        Respawn: 25
        Slayer Level: null
        Slayer XP: null
        Hitpoints Level: 7
        Attack Level: 1
        Strength Level: 1
        Defence Level: 1
        Ranged Level: 1
        Mage Level: 1
        Attack Bonus: 0
        Strength Bonus: 0
        Magic Attack Bonus: 0
        Magic Strength Bonus: 0
        Range Attack Bonus: 0
        Range Strength Bonus: 0
        Stab Defence Bonus: -21
        Slash Defence Bonus: -21
        Crush Defence Bonus: -21
        Magic Defence Bonus: -21
        Range Defence Bonus: -21
        Drop Version: Regular
        Immune Poison: false
        Immune Venom: false
        Immune Cannon: false
        Immune Thrall: false
        Release: Thu Jan 04 00:00:00 EST 2001
        Update: Runescape beta is now online!
        Removal Update:
        Last Update: Thu Jan 04 00:00:00 EST 2001
    Monster: Man (Red) [[3265]]
        Image: https://oldschool.runescape.wiki/images/Man_%28blue%29.png
        Members: false
        Level: 2
        Size: 1
        Examine: One of Gielinor's many citizens.
        XP Bonus: 0.0
        Max Hit: [1]
        Aggressive: false
        Poisonous: false
        Attack Style: [[[Crush]]]
        Attack Speed: 4
        Respawn: 25
        Slayer Level: null
        Slayer XP: null
        Hitpoints Level: 7
        Attack Level: 1
        Strength Level: 1
        Defence Level: 1
        Ranged Level: 1
        Mage Level: 1
        Attack Bonus: 0
        Strength Bonus: 0
        Magic Attack Bonus: 0
        Magic Strength Bonus: 0
        Range Attack Bonus: 0
        Range Strength Bonus: 0
        Stab Defence Bonus: -21
        Slash Defence Bonus: -21
        Crush Defence Bonus: -21
        Magic Defence Bonus: -21
        Range Defence Bonus: -21
        Drop Version: Regular
        Immune Poison: false
        Immune Venom: false
        Immune Cannon: false
        Immune Thrall: false
        Release: Thu Jan 04 00:00:00 EST 2001
        Update: Runescape beta is now online!
        Removal Update:
        Last Update: Thu Jan 04 00:00:00 EST 2001
    Monster: Man (Backpack) [[3107, 6988]]
        Image: https://oldschool.runescape.wiki/images/Man_%28cavalier%29.png
        Members: false
        Level: 2
        Size: 1
        Examine: One of Gielinor's many citizens.
        XP Bonus: 0.0
        Max Hit: [1]
        Aggressive: false
        Poisonous: false
        Attack Style: [[[Crush]]]
        Attack Speed: 4
        Respawn: 25
        Slayer Level: null
        Slayer XP: null
        Hitpoints Level: 7
        Attack Level: 1
        Strength Level: 1
        Defence Level: 1
        Ranged Level: 1
        Mage Level: 1
        Attack Bonus: 0
        Strength Bonus: 0
        Magic Attack Bonus: 0
        Magic Strength Bonus: 0
        Range Attack Bonus: 0
        Range Strength Bonus: 0
        Stab Defence Bonus: -21
        Slash Defence Bonus: -21
        Crush Defence Bonus: -21
        Magic Defence Bonus: -21
        Range Defence Bonus: -21
        Drop Version: Regular
        Immune Poison: false
        Immune Venom: false
        Immune Cannon: false
        Immune Thrall: false
        Release: Thu Jan 04 00:00:00 EST 2001
        Update: Runescape beta is now online!
        Removal Update:
        Last Update: Thu Jan 04 00:00:00 EST 2001
    Monster: Man (Cavalier) [[3298]]
        Image: https://oldschool.runescape.wiki/images/Man_%28pink%29.png
        Members: false
        Level: 2
        Size: 1
        Examine: One of Gielinor's many citizens.
        XP Bonus: 0.0
        Max Hit: [1]
        Aggressive: false
        Poisonous: false
        Attack Style: [[[Crush]]]
        Attack Speed: 4
        Respawn: 25
        Slayer Level: null
        Slayer XP: null
        Hitpoints Level: 7
        Attack Level: 1
        Strength Level: 1
        Defence Level: 1
        Ranged Level: 1
        Mage Level: 1
        Attack Bonus: 0
        Strength Bonus: 0
        Magic Attack Bonus: 0
        Magic Strength Bonus: 0
        Range Attack Bonus: 0
        Range Strength Bonus: 0
        Stab Defence Bonus: -21
        Slash Defence Bonus: -21
        Crush Defence Bonus: -21
        Magic Defence Bonus: -21
        Range Defence Bonus: -21
        Drop Version: Regular
        Immune Poison: false
        Immune Venom: false
        Immune Cannon: false
        Immune Thrall: false
        Release: Thu Jan 04 00:00:00 EST 2001
        Update: Runescape beta is now online!
        Removal Update:
        Last Update: Thu Jan 04 00:00:00 EST 2001
    Monster: Man (Pink) [[3109, 6815]]
        Image: https://oldschool.runescape.wiki/images/Man_%28black%29.png
        Members: false
        Level: 2
        Size: 1
        Examine: One of Gielinor's many citizens.
        XP Bonus: 0.0
        Max Hit: [1]
        Aggressive: false
        Poisonous: false
        Attack Style: [[[Crush]]]
        Attack Speed: 4
        Respawn: 25
        Slayer Level: null
        Slayer XP: null
        Hitpoints Level: 7
        Attack Level: 1
        Strength Level: 1
        Defence Level: 1
        Ranged Level: 1
        Mage Level: 1
        Attack Bonus: 0
        Strength Bonus: 0
        Magic Attack Bonus: 0
        Magic Strength Bonus: 0
        Range Attack Bonus: 0
        Range Strength Bonus: 0
        Stab Defence Bonus: -21
        Slash Defence Bonus: -21
        Crush Defence Bonus: -21
        Magic Defence Bonus: -21
        Range Defence Bonus: -21
        Drop Version: Cavalier
        Immune Poison: false
        Immune Venom: false
        Immune Cannon: false
        Immune Thrall: false
        Release: Thu Jan 04 00:00:00 EST 2001
        Update: Runescape beta is now online!
        Removal Update:
        Last Update: Thu Jan 04 00:00:00 EST 2001
    Monster: Man (Backpack (Musa Point)) [[3264]]
        Image: https://oldschool.runescape.wiki/images/Man_%28light_blue%29.png
        Members: false
        Level: 2
        Size: 1
        Examine: One of Gielinor's many citizens.
        XP Bonus: 0.0
        Max Hit: [1]
        Aggressive: false
        Poisonous: false
        Attack Style: [[[Crush]]]
        Attack Speed: 4
        Respawn: 25
        Slayer Level: null
        Slayer XP: null
        Hitpoints Level: 7
        Attack Level: 1
        Strength Level: 1
        Defence Level: 1
        Ranged Level: 1
        Mage Level: 1
        Attack Bonus: 0
        Strength Bonus: 0
        Magic Attack Bonus: 0
        Magic Strength Bonus: 0
        Range Attack Bonus: 0
        Range Strength Bonus: 0
        Stab Defence Bonus: -21
        Slash Defence Bonus: -21
        Crush Defence Bonus: -21
        Magic Defence Bonus: -21
        Range Defence Bonus: -21
        Drop Version:
        Immune Poison: false
        Immune Venom: false
        Immune Cannon: false
        Immune Thrall: false
        Release: Thu Jan 04 00:00:00 EST 2001
        Update: Runescape beta is now online!
        Removal Update:
        Last Update: Thu Jan 04 00:00:00 EST 2001



 */