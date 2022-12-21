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
            <li><a href="#osrs-wiki">OsrsWiki Builder</a> </li>
            <li><a href="#osrs-wiki">Premade data parsing methods</a></li>
            <li><a href="#osrs-wiki">Standard data parsing methods</a></li>
            <li><a href="#osrs-wiki-code">Code</a></li>
        </ol>
        </li>
        <li>Scribunto Session
        <ol>
            <li><a href="#osrs-wiki">Usage</a></li>
            <li><a href="#osrs-wiki-code">Code</a></li>
        </ol>
        </li>
        <li>Lua Builder
        <ul>
            <li><a href="#osrs-wiki">Usage</a></li>
            <li><a href="#osrs-wiki-code">Code</a></li>
        </ul>
        </li>
        <li>Utility classes
        <ol>
            <li><a href="#osrs-wiki">Usage</a></li>
            <li><a href="#osrs-wiki-code">Code</a></li>
        </ol>
        </li>
        <li><a href="#useful-references">Useful References</a></li>
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

#### OsrsWiki Builder:

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
    - ```kotlin
      .withCookieManager( CookieManager() )
      ```


- Optionally set a custom proxy.
    - ```kotlin
      .withProxy( Proxy() )
      ```


- Optionally set a custom user agent.
    - ```kotlin
      .withUserAgent( "Custom User Agent" )
        ```


- Optionally set the default number of Scribunto sessions used for bulk Scribunto requests.
    - ```kotlin
      .withScribuntoSessionCount( 10 )
      ```


---
<p align="right">(<a href="#readme-top">back to top</a>)</p>
</details>

<details><summary><h5>Premade data parsing methods:</h5></summary>

---

- Get page titles from Item IDs:
    - ```kotlin
      wiki.getItemPageTitlesFromIds(11832, 11834, 11836) // ["Bandos chestplate", "Bandos tassets", "Bandos boots"]
      ```

- Get page titles from NPC IDs:
    - ```kotlin
       wiki.getNpcPageTitlesFromIds(1399, 2639) // ["King Roald", "Robert The Strong"]
        ```

- Get all Item titles:
    - ```kotlin 
      wiki.getAllItemTitles() // ["Abyssal whip", "Abyssal bludgeon", "Abyssal dagger", ...]
      ```

- Get all NPC titles:
    - ```kotlin 
      wiki.getAllNpcTitles() // ["Abyssal demon", "Abyssal leech", "Abyssal lurker", ...]
      ```

- Get [ItemDetails][ItemDetails.kt Link] by name(s) or all:
    - ```kotlin
      wiki.getItemDetails("Bandos chestplate", "Bandos tassets", "Bandos boots") // Map<String, List<ItemDetails>>
      wiki.getAllItemDetails() // Map<String, List<ItemDetails>>
      ```

- Get [NpcDetails][NpcDetails.kt Link] by name(s) or all:
    - ```kotlin
      wiki.getNpcDetails("King Roald", "Robert The Strong") // Map<String, List<NpcDetails>>
      wiki.getAllNpcDetails() // Map<String, List<NpcDetails>>
      ```

- Get [MonsterDetails][MonsterDetails.kt Link] by name(s) or all:
    - ```kotlin
      wiki.getMonsterDetails("Abyssal demon", "Abyssal leech", "Abyssal lurker") // Map<String, List<MonsterDetails>>
      wiki.getAllMonsterDetails() // Map<String, List<MonsterDetails>>
      ```

- Get [QuestRequirement][QuestRequirement.kt Link]'s for all quests:
    - ```kotlin 
      wiki.getQuestRequirements() // Map<String, List<QuestRequirement>>
        ```

- Get [VarbitDetails][VarbitDetails.kt Link] for all varbits on the Wiki:
    - ```kotlin 
      wiki.getVarbitDetails() // Map<Int, VarbitDetails>
        ```

- Get [ProductionDetails][ProductionDetails.kt Link] for all items with production data:
    - ```kotlin 
      wiki.getProductionDetails() // Map<String, ProductionDetails>
        ```

- Get [ItemPrice][WikiItemPrice.kt Link] for Item ID:
    - ```kotlin 
      wiki.getItemPrice(11832) // WikiItemPrice?
        ```

- Get all [LocLineDetails][LocLineDetails.kt Link]:
    - ```kotlin 
      wiki.getAllLocLineDetails() // Map<String, List<LocLineDetails>>
        ```   
- Get Slayer Monsters and their Task IDs:
    - ```kotlin 
      wiki.getSlayerMonstersAndTaskIds() // Map<String, Int>
        ```

- Get Slayer Masters that assign task:
    - ```kotlin 
      wiki.getSlayerMastersThatAssign("Ghouls") // ["Mazchna", "Vannaka"]
        ```

---
<p align="right">(<a href="#readme-top">back to top</a>)</p>
</details>


<details><summary><h5>Standard data parsing methods:</h5></summary>

---

- Get all titles in a category:
  - ```kotlin
      wiki.getTitlesInCategory("Items", "Monsters") // List<String>
      ```

- Get all titles using any _**(one or more)**_ of the specified template(s):
  - ```kotlin
       wiki.getAllTitlesUsingTemplate("Infobox Item", "Infobox Bonuses") // List<String>
    ```
    
- Get all titles using **all of** the specified template(s):
  - ```kotlin
       wiki.getAllTitlesUsingTheseTemplates("Infobox Item", "Infobox Bonuses") // List<String>
    ```

- Get all templates present on a page:
  - ```kotlin
       wiki.getTemplatesOnPage("Baby chinchompa") // List<String>
    ```

- Get all uses of a template across the entire Wiki:
  - ```kotlin
       wiki.getAllTemplateUses("Infobox Item") // Map<String, List<JsonObject>>
    ```

- Get all titles in categories with revisions since a specified date:
  - ```kotlin
      val threeDaysAgo = Date.from(Instant.now().minus(3, ChronoUnit.DAYS)) 
      wiki.getAllTitlesWithRevisionsSince(threeDaysAgo, "Items") // List<String>
      ```


- Get last revision timestamp for title(s):
  - ```kotlin
      wiki.getLastRevisionTimestamp("Baby chinchompa", "Black chinchompa") // Map<String, String>
      wiki.getLastRevisionTimestamp(listOf("Baby chinchompa", "Black chinchompa")) // Map<String, String>
      ```


- Dynamic Page List (DPL3) query:
  - ```kotlin
    val query = mapOf(
        "category" to "Items",
        "count" to 10,
        "include" to "{Infobox Item}",    
    )
    val response = wiki.dplAsk(query) // JsonElement
    ```
  - Further explanation on DPL3 queries can be found below in [ScribuntoSession][ScribuntoSession.kt Link] and [DPL3 Documentation][DPL3 Documentation Link] 





---
<p align="right">(<a href="#readme-top">back to top</a>)</p>
</details>

### ScribuntoSession.kt ([ScribuntoSession.kt][ScribuntoSession.kt Link])

<details><summary>

#### Creating a Scribunto Session:

</summary>

---

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

---

</details>

<details><summary><h5>LuaBuilder.kt</h5></summary>

---

---

</details>



<details><summary><h5>Useful References:</h5></summary>

---

* Scribunto
  * [OSRS Wiki - API Sandbox (Scribunto) ](https://oldschool.runescape.wiki/w/Special:ApiSandbox#action=scribunto-console&format=json&title=Var&question=print(%22test%22))
  * [Scribunto Libraries](https://www.mediawiki.org/wiki/Extension:Scribunto/Lua_reference_manual#Scribunto_libraries)
 

* Dynamic Page List (DPL)
  * [OSRS Wiki DPL Module](https://oldschool.runescape.wiki/w/Module:DPLlua)
  * [OSRS Wiki Page List Tools Module](https://oldschool.runescape.wiki/w/Module:PageListTools)
  * [DPL3 Manual](https://followthescore.org/dpldemo/index.php?title=DPL:Manual)
  * [DPL3 Manual - General Usage and Invocation Syntax](https://followthescore.org/dpldemo/index.php?title=DPL:Manual_-_General_Usage_and_Invocation_Syntax)
  * [DPL3 Manual - DPL Parameters: Criteria for page selection](https://followthescore.org/dpldemo/index.php?title=DPL:Manual_-_DPL_parameters:_Criteria_for_page_selection)
  * [Fandom DPL3 Extension - Parameters: Controlling output format](https://help.fandom.com/wiki/Extension:DPL3/Parameters:_Controlling_output_format)

* Semantic Scribunto
  * [OSRS Wiki Semantic Search Playground](https://oldschool.runescape.wiki/w/Special:Ask?#search)
  * [Semantic Media Wiki GitHub](https://github.com/SemanticMediaWiki/SemanticScribunto/) 

---

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

[LocLineDetails.kt Link]: TODO()

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

[DPL3 Documentation Link]: https://www.mediawiki.org/wiki/Extension:DynamicPageList_(DPL)