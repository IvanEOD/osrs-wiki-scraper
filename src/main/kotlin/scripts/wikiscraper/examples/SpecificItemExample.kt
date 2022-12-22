package scripts.wikiscraper.examples

import scripts.wikiscraper.OsrsWiki
import scripts.wikiscraper.query.getItemDetails


/* Written by IvanEOD 12/22/2022, at 12:20 AM */
fun main() {

    val wiki = OsrsWiki.builder().build()

    val itemData = wiki.getItemDetails("Abyssal whip")

    itemData.forEach {
        it.debug("")
    }

}

/*

Item: Abyssal whip [4151]
    Image: https://oldschool.runescape.wiki/images/Abyssal_whip.png
    Members: true
    Alchable: true
    Equippable: true
    Tradeable: true
    Stackable: false
    Stacks in bank: true
    Placeholder: true
    Quest: []
    Destroy: [Drop]
    Options: [Wield]
    Edible: false
    Examine: A weapon from the Abyss.
    Value: 120001
    High alch value: 72000
    Low alch value: 48000
    Weight: 0.453
    Can sell on exchange: true
    Release: 15 February 2005
    Update: Ghosts Ahoy and Slayer Update
    Last update: 15 February 2005
    Equipment item info:
        Slot: weapon
        Two-handed: false
        Combat style: Whip
        Stab (Attack): 0
        Slash (Attack): 82
        Crush (Attack): 0
        Magic (Attack): 0
        Range (Attack): 0
        Stab (Defence): 0
        Slash (Defence): 0
        Crush (Defence): 0
        Magic (Defence): 0
        Range (Defence): 0
        Strength: 82
        Ranged Strength: 0
        Magic Damage: 0
        Prayer: 0
        Attack Range: 1
        Speed: 4
    Exchange Data:
        Exchange Name: Abyssal whip
        Exchange id: 4151
        Data Graph Link: http://services.runescape.com/m=itemdb_oldschool/viewitem.ws?obj=4151
        Price: 1412647
        Last price: 1424297
        Price change: 0.008
        Price date: Wed Dec 21 13:53:22 EST 2022
        Last price date: Wed Dec 21 13:53:22 EST 2022
        Buy limit: 70
        Exchange volume: 5738
        Members: true
        Icon: Abyssal whip.png
        Examine: A weapon from the Abyss.
        Value: 120001
        High alch: 72000
        Low alch: 48000

 */