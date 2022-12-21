<a name="readme-top"></a>


<br />
<div align="center">
    <a href="https://github.com/IvanEOD/osrs-wiki-scraper/blob/master/README.md">
    </a>
    <h1 align="center">OSRS Wiki Scraper</h1>
    <p align="center">
      An OSRS Wiki scraper in Kotlin designed to easily scrape data <b><u>YOU</u></b> want from the OSRS Wiki.
    </p>
</div>

<details>
    <summary>Table of Contents</summary>
    <ul>
        <li><a href="#about-the-project">About the Project</a>
            <ul>
                <li><a href="#built-with">Built With</a></li>
            </ul>
        </li>
        <li>OsrsWiki
        <ol>
            <li><a href="#osrs-wiki">Usage</a></li>
            <li><a href="#osrs-wiki-code">Code</a></li>
            <li><a href="#osrs-wiki-references">Useful References</a></li>
        </ol>
        </li>
        <li>Scribunto Session
        <ol>
            <li><a href="#osrs-wiki">Usage</a></li>
            <li><a href="#osrs-wiki-code">Code</a></li>
            <li><a href="#osrs-wiki-references">Useful References</a></li>
        </ol>
        </li>
        <li>Lua Builder
        <ul>
            <li><a href="#osrs-wiki">Usage</a></li>
            <li><a href="#osrs-wiki-code">Code</a></li>
            <li><a href="#osrs-wiki-references">Useful References</a></li>
        </ul>
        </li>
        <li>Utility classes
        <ol>
            <li><a href="#osrs-wiki">Usage</a></li>
            <li><a href="#osrs-wiki-code">Code</a></li>
        </ol>
        </li>
    </ul>
</details>

## About the Project

This project includes several useful examples of how to use the scraper to get customized data, but is primarily
intended to provide a framework for programmers to be able to create their own objects and methods to scrape the data
they want.

To be able to efficiently create methods to scrape the data you want you will need a basic understanding
of [Lua][Lua Link], [Kotlin][Kotlin Link], and [MediaWiki][MediaWiki Link].

### Built With

* [![Kotlin][Kotlin Image]][Kotlin Link]
* [![Lua][Lua Image]][Lua Link]
* [![Gradle][Gradle Image]][Gradle Link]
* [![Intellij Idea][Intellij Idea Image]][Intellij Idea Link]


<p align="right">(<a href="#readme-top">back to top</a>)</p>

### OsrsWiki ([OsrsWiki.kt][OsrsWiki.kt Link])

<details><summary>

#### Create an OsrsWiki instance:

</summary>

---

```kotlin
   val wiki = OsrsWiki.builder()
    .withCookieManager(CookieManager())
    .withProxy(Proxy())
    .withUserAgent("Custom User Agent")
    .withScribuntoSessionCount(10)
    .build() 
```

- Optionally set a custom cookie manager.
    - `.withCookieManager( CookieManager() )`


- Optionally set a custom proxy.
    - `.withProxy( Proxy() )`


- Optionally set a custom user agent.
    - `.withUserAgent( "Custom User Agent" )`


- Optionally set the default number of Scribunto sessions used for bulk Scribunto requests.
    - `.withScribuntoSessionCount( 10 )`

---
</details>

<details><summary><h5>Premade methods:</h5></summary>

---

- Get page titles from Item IDs:
    - ```wiki.getItemPageTitlesFromIds(11832, 11834, 11836)```
      &#10145; `["Bandos chestplate", "Bandos tassets", "Bandos boots"]`


- Get page titles from NPC IDs:
    - ```wiki.getNpcPageTitlesFromIds(1399, 2639)``` &#10145; `["King Roald", "Robert The Strong"]`


- Get all Item titles:
    - ```wiki.getAllItemTitles()``` &#10145; `["Abyssal whip", "Abyssal bludgeon", "Abyssal dagger", ...]`


- Get all NPC titles:
    - ```wiki.getAllNpcTitles()``` &#10145; `["Abyssal demon", "Abyssal leech", "Abyssal lurker", ...]`


- Get [ItemDetails][ItemDetails.kt Link]:
    - By name(s)
        - ```wiki.getItemDetails("Bandos chestplate", "Bandos tassets", "Bandos boots")```

          &#10145; `Map<String, List<ItemDetails>>`
    - All items
        - ```wiki.getAllItemDetails()```

          &#10145; `Map<String, List<ItemDetails>>`


- Get [NpcDetails][NpcDetails.kt Link] by NPC Name:
    - ```wiki.getNpcDetails("King Roald", "Robert The Strong")``` &#10145; `Map<String, List<NpcDetails>>`


- Get [MonsterDetails][MonsterDetails.kt Link]:
    - ```wiki.getMonsterDetails("Abyssal demon", "Abyssal leech", "Abyssal lurker")```
      &#10145; `Map<String, List<MonsterDetails>>`
    - ```wiki.getAllMonsterDetails()``` &#10145; `Map<String, List<MonsterDetails>>`


- Get [QuestRequirement][QuestRequirement.kt Link]'s for all quests:
    - ```wiki.getQuestRequirements()``` &#10145; `Map<String, List<QuestRequirement>>`


- Get [VarbitDetails][VarbitDetails.kt Link] for all varbits on the Wiki:
    - ```wiki.getVarbitDetails()``` &#10145; `Map<Int, VarbitDetails>`


- Get [ProductionDetails][ProductionDetails.kt Link] for all items with production data:
    - ```wiki.getProductionDetails()``` &#10145; `Map<String, ProductionDetails>`

---
</details>

### ScribuntoSession.kt ([ScribuntoSession.kt][ScribuntoSession.kt Link])

<details><summary>

#### Creating a Scribunto Session:

</summary>
   <p>

   ```kotlin
   val session = wiki.createScribuntoSession {
    withoutDefaultCode()
    withWikiModule("ModuleName")
    withCode("print('Hello World')")
    withCode {
        /* Use the Lua Builder */
    }
}
   ```

- Optionally disable the default code included in the session, you can add your own code with the `withCode` function.
    - ```.withoutDefaultCode()```
- Optionally set the module the session will use, by default this is `"Var"`.
    - ```.withWikiModule("ModuleName")```
- Optionally add code to the session.
    - ```.withCode("print('Hello World')")```
- Optionally add code to the session.
    - ```.withCode { /* Use the Lua Builder */ }```

  #### Using a Scribunto Session:

  ```kotlin

  ```

</p>
</details>

<details><summary>LuaBuilder.kt</summary>


a

</details>


[OsrsWiki.kt Link]: https://github.com/IvanEOD/osrs-wiki-scraper/blob/master/src/main/kotlin/scripts/wikiscraper/OsrsWiki.kt

[ScribuntoSession.kt Link]: https://github.com/IvanEOD/osrs-wiki-scraper/blob/master/src/main/kotlin/scripts/wikiscraper/lua/ScribuntoSession.kt

[LuaBuilder.kt Link]: https://github.com/IvanEOD/osrs-wiki-scraper/blob/master/src/main/kotlin/scripts/wikiscraper/lua/LuaBuilder.kt

[DropDetails.kt Link]: https://github.com/IvanEOD/osrs-wiki-scraper/blob/master/src/main/kotlin/scripts/wikiscraper/classes/DropDetails.kt

[EquipmentItemInfo.kt Link]: https://github.com/IvanEOD/osrs-wiki-scraper/blob/master/src/main/kotlin/scripts/wikiscraper/classes/EquipmentItemInfo.kt

[ItemBuyLimits.kt Link]: https://github.com/IvanEOD/osrs-wiki-scraper/blob/master/src/main/kotlin/scripts/wikiscraper/classes/ItemBuyLimits.kt

[ItemDetails.kt Link]: https://github.com/IvanEOD/osrs-wiki-scraper/blob/master/src/main/kotlin/scripts/wikiscraper/classes/ItemDetails.kt

[LocationDetails.kt Link]: https://github.com/IvanEOD/osrs-wiki-scraper/blob/master/src/main/kotlin/scripts/wikiscraper/classes/LocationDetails.kt

[QuestRequirement.kt Link]: https://github.com/IvanEOD/osrs-wiki-scraper/blob/master/src/main/kotlin/scripts/wikiscraper/classes/QuestRequirement.kt

[VarbitDetails.kt Link]: https://github.com/IvanEOD/osrs-wiki-scraper/blob/master/src/main/kotlin/scripts/wikiscraper/classes/VarbitDetails.kt

[WikiExchangeData.kt Link]: https://github.com/IvanEOD/osrs-wiki-scraper/blob/master/src/main/kotlin/scripts/wikiscraper/classes/WikiExchangeData.kt

[WikiItemPrice.kt Link]: https://github.com/IvanEOD/osrs-wiki-scraper/blob/master/src/main/kotlin/scripts/wikiscraper/classes/WikiItemPrice.kt

[ProductionDetails.kt Link]: https://github.com/IvanEOD/osrs-wiki-scraper/blob/master/src/main/kotlin/scripts/wikiscraper/classes/ProductionDetails.kt

[NpcDetails.kt Link]: TODO()

[MonsterDetails.kt Link]: TODO()

[Lua Link]: TODO()
[Lua Image]: https://img.shields.io/badge/Lua-5.1-yellowgreen?logo=lua&style=flat
[Kotlin Link]: https://kotlinlang.org/
[Kotlin Image]: https://img.shields.io/badge/Kotlin-1.7.10-yellowgreen.svg?logo=kotlin&style=flat
[Gradle Link]: https://gradle.org/
[Gradle Image]: https://img.shields.io/badge/Gradle-7.3.4-yellowgreen.svg?logo=gradle&style=flat
[Intellij Idea Link]: https://www.jetbrains.com/idea/
[Intellij Idea Image]: https://img.shields.io/badge/Intellij-2022.1-yellowgreen.svg?logo=intellij-idea&style=flat
[MediaWiki Link]: TODO()
[MediaWiki Image]: TODO()