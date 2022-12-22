<a name="readme-top"></a>


<br />
<div align="center">
    <a href="https://github.com/IvanEOD/osrs-wiki-scraper/blob/master/README.md">
    </a>
    <h1 align="center">OSRS Wiki Scraper</h1>

---
<p align="center">
      An OSRS Wiki scraper in Kotlin designed to easily scrape data <b><u>YOU</u></b> want from the OSRS Wiki.
    </p>
</div>

---

<details>
    <summary>Table of Contents</summary>
    <ul>
        <li><a href="#about-the-project">About the Project</a>
            <ul>
                <li><a href="#built-with">Built With</a></li>
            </ul>
        </li>
        <li><a href="#osrswiki-osrswikikt">OsrsWiki</a>
        <ul>
            <li><a href="#osrs-wiki">OsrsWiki Builder</a> </li>
            <li><a href="#osrs-wiki">Premade data parsing methods</a></li>
            <li><a href="#osrs-wiki">Standard data parsing methods</a></li>
            <li><a href="#osrs-wiki-code">Code</a></li>
        </ul>
        </li>
        <li><a href="#scribunto-session-scribuntosessionkt">Scribunto Session</a>
        <ul>
            <li><a href="#osrs-wiki">Usage</a></li>
            <li><a href="#osrs-wiki-code">Code</a></li>
        </ul>
        </li>
        <li><a href="#lua-builder-luabuilderkt">Lua Builder</a>
        <ul>
            <li><a href="#osrs-wiki">Usage</a></li>
            <li><a href="#osrs-wiki-code">Code</a></li>
        </ul>
        </li>
        <li><a href="#utility-classes">Utility classes</a>
        <ul>
            <li><a href="#osrs-wiki">Usage</a></li>
            <li><a href="#osrs-wiki-code">Code</a></li>
        </ul>
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

## OsrsWiki ([OsrsWiki.kt][OsrsWiki.kt Link])

<details><summary>

###### The OsrsWiki class is the main class of the project. It provides methods to scrape data from the [OSRS Wiki][OsrsWiki Link].

</summary>

<details><summary>

#### &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; OsrsWiki Builder:

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

<details><summary>

#### &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Premade data parsing methods:

</summary>

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


<details><summary>

#### &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Standard data parsing methods:

</summary>

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

- Get all template names present on a page:
  - ```kotlin
       wiki.getNamesOfTemplatesOnPage("Baby chinchompa") // List<String>
    ```

- Get all uses of a template across the entire Wiki:
  - ```kotlin
       wiki.getAllTemplateUses("Infobox Item") // Map<String, List<JsonObject>>
    ```
  
- Get all data for specified template(s) on a page: 
  - ```kotlin
       wiki.getTemplateDataOnPage("Baby chinchompa", "Infobox Item", "Infobox Bonuses") // Map<String, List<JsonObject>>
    ``` 
    
- Get all data for all templates on a page:
  - ```kotlin
       wiki.getAllTemplateDataOnPage("Baby chinchompa") // Map<String, List<JsonObject>>
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


- MediaWiki Semantic Search: 
  - ```kotlin
    val query = listOf(
        "[[Location JSON::+]]",
        "?#-=title",
        "?Production JSON",
    )
    val response = wiki.smwAsk(query) // JsonElement
    ```

  
---
<p align="right">(<a href="#readme-top">back to top</a>)</p>
</details>
</details>

## Scribunto Session ([ScribuntoSession.kt][ScribuntoSession.kt Link])

<details><summary>

###### The Scribunto Session connects to the [MediaWiki API][MediaWiki Link] and allows for the execution of [Lua scripts][Lua Link] on the Wiki.

</summary>

#### _Why is that useful?_
* Executing custom [Lua][Lua Link] scripts on the Wiki.
* Loading data from the Wiki Lua modules.
* Using the [DPL3][DPL3 Documentation Link] query language to query the Wiki.
* Controlling the format and the volume of the data returned by the Wiki.

<details><summary>

#### &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Creating a Scribunto Session:

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
    - ```kotlin
      .withoutDefaultCode()
      ```
- Optionally set the module the session will use, by default this is `"Var"`, for no particular reason other than being a small module.
    - ```kotlin
      .withWikiModule("ModuleName")
      ```
- Optionally add code to persist in the session.
    - ```kotlin
      .withCode("print('Hello World')")
      .withCode { /* Use the Lua Builder */ }
      ```
    - See [LuaBuilder.kt][LuaBuilder.kt Link] for more information on the Lua Builder.

</p>

---
<p align="right">(<a href="#readme-top">back to top</a>)</p>
</details>

<details><summary>

#### &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Using a Scribunto Session:

</summary>

---

- Send a request with a string of Lua code:
  - ```kotlin
      session.sendRequest("print(\"Hello World\"") // Pair<Boolean, JsonElement>
    ```
- Send a request with a [LuaBuilder][LuaBuilder.kt Link] instance:
  - ```kotlin
      session.sendRequest {
        /* Use the Lua Builder */
      }
      // Pair<Boolean, JsonElement>
    ``` 
- Send a request with the first parameter being `true` and it will automatically refresh the Scribunto Session:
  - ```kotlin
      session.sendRequest(true, "print(\"Hello World\"") // Pair<Boolean, JsonElement>
      session.sendRequest(true) {
            /* Use the Lua Builder */
      }    
      // Pair<Boolean, JsonElement>
    ``` 
    
- The return value from the `sendRequest` function is a `Pair<Boolean, JsonElement>` where the first value is whether or not the request was successful and the second value is the response from the Wiki `print` return field.


- To get a value back from the wiki use the Lua `print` function.


- The default Lua code provided includes a method to return values called ``printReturn`` and will return the input value as a JSON string.
  - ```json
    {
        "success": true,
        "message": "Only present if success is false",
        "printReturn": "{\"json\": \"value\"}"        
    }
    ``` 

- The session uses the same `Session ID` for each request. The wiki will continue to add the code the requests to the session until the session is refreshed or the session expires.


- The session will automatically refresh if the session expires or if the session is refreshed manually.


- If the session has failed too many requests since the last refresh it will automatically refresh.


- The session can be refreshed manually:
  - ```kotlin
      session.refresh() 
    ```
     
 

---
<p align="right">(<a href="#readme-top">back to top</a>)</p>
</details>

</details>

## Lua Builder ([LuaBuilder.kt][LuaBuilder.kt Link])

<details><summary>

###### The Lua Builder is a [DSL][Kotlin DSL Link] for easily creating [Lua][Lua Link] code from [Kotlin][Kotlin Link]. It is not intended to be a full Lua interpreter or converter, but rather a tool to make it easier to create Lua code.

</summary>

---

- You can create a [LuaScope][LuaScope Link] instance with the `lua` function:
  - ```kotlin
      lua {
        /* Use the Lua Builder */
      }
    ``` 
    
- The [LuaScope][LuaScope Link] will convert values to a [Lua][Lua Link] representation.


- The supported value types are:
  - ``String``
  - ``Number``
  - ``Date``
  - ``Boolean``
  - ``Map<*, *>`` (``*`` values may be any of the above types)
  - ``Iterable<*>`` (``*`` values may be any of the above types)


- To set a key's value use <code>\`=\`</code> like <code>"key" \`=\` "value"</code>.


- There are two types of LuaScope with slight differences.
  - The [LuaGlobalScope][LuaGlobalScope.kt Link]
    - This is the default scope and only allows `String` keys.
    - These values allow the use of ".local()" to prepend the key with "local" making it a local variable.
      - ```"myValue".local()``` will output ``local myValue``
 

  - The [LuaTableScope][LuaTableScope.kt Link]
    - This scope allows `String`, `Number`, `Boolean`, and `Date` keys.
    - These values can not use `.local()` because they are values in a table.

<table>
    
    
<tr>
  <th align="center">Kotlin</th>
  <th align="center">Lua Output</th> 
</tr> 
    
    
<tr>
    
<td>

```kotlin
"myValue" `=` "value"
```
</td>
    
<td>

```lua
myValue = "value"
```
</td>
    
</tr>
    
<tr>
    
<td style="white-space: nowrap"><pre lang="kt">"myValue".local() `=` "value"</pre>
</td>
    
<td>

```lua
local myValue = "value"
```
</td>
    
</tr>

<tr>
    
<td>

```kotlin
"myModule" `=` require("ModuleName")
```
</td>
    
<td>

```lua
myModule = require("ModuleName")
```
</td>
    
</tr>
    
<tr>
    
<td>

```kotlin
+"print('This code is just added as is to the Lua script')"
```
</td>
    
<td>

```lua
print('This code is just added as is to the Lua script')
```
</td>
    
</tr>
    
<tr>
    
<td>

```kotlin
"myTable" `=` {
    "myKey" `=` "myValue"
    48 `=` Date()
    Date() `=` "myValue"
    1.0 `=` 1
    true `=` "myTrueValue"
    "something" `=` true
    "myListInLua" `=` listOf("a", "b", "c")
    "myMapInLua" `=` mapOf("a" to "b", "c" to "d")
  }
```

Inside the brackets is [LuaTableScope][LuaTableScope.kt Link] which allows values other than ``String`` to be keys.
</td>
    
<td>

```lua
myTable = { 
    ["myKey"] = "myValue",
    [48] = "2022-12-21 17:33:09",
    ["2022-12-21 17:33:09"] = "myValue",
    [1.0] = 1,
    [true] = "myTrueValue",
    ["something"] = true,
    ["myListInLua"] = {"a", "b", "c"},
    ["myMapInLua"] = {
      ["a"] = "b", 
      ["c"] = "d"
    }
}
```
</td>
    
</tr>
    
</table>

---
<p align="right">(<a href="#readme-top">back to top</a>)</p>
</details>

## Utility Classes

<details><summary>

###### Some notable classes used by and made available by the scraper.

</summary>

---

- ### Versioned Map ([VersionedMap.kt][VersionedMap.kt Link])

<details><summary>

###### This is used for templates from the wiki to parse versioned data and determine images and page references within the value.

</summary>
 
  - The best way to obtain this is by calling ``.toVersionedMap()`` on a `JsonObject` received from the wiki.
    ```kotlin
        val versionedMap = jsonObject.toVersionedMap()
      ```
  
<div align="left">
<div align="left" style="width: min-content; margin-left: 4%">
<div style="width: max-content">

- The <code>VersionedMap</code> will create a <code>TemplatePropertyData</code> for each key:

<div style="width: min-content">
<div style="width: min-content">
<div align="left">

```kotlin
data class TemplatePropertyData(
  val name: String,
  val key: String,
  val isWikiKey: Boolean,
  val version: Int,
  val value: String
)
```
</div>
</div>

- Example Template Data:
<div style="width: min-content">
<div style="width: min-content">
<div align="left">

```json
{
  "id1" : 111,
  "id2" : 222,
  "id3" : 333
}
```
</div>
</div>

- Would create these property data classes:
<div style="width: min-content">
<div align="left" style="width: min-content">

```kotlin
TemplatePropertyData(name="id1", key="id", isWikiKey=true, version=1, value="111")
TemplatePropertyData(name="id2", key="id", isWikiKey=true, version=2, value="222")
TemplatePropertyData(name="id3", key="id", isWikiKey=true, version=3, value="333")
```

</div>
</div>
</div>
</div>
</div>
</div>
</div>

* You can check how many versions a template has with ``versionedMap.versions``
* By default, getting a property without the version will return `Version 0`.</br>
* `Version 0` is all values combined, or in a single versioned property, the value itself.</br>
* You can also use the original key if you know it and are expecting it.</br> 
* ``id3`` will work the same as ``["id", 3]``
* If a template has multiple versions, some values may be the same across all versions, and will not have a versioned key.
* So if a version of a key is requested that does not exist, it will return the first or only value available.
* You can get a full map of a specific version, or a list containing a map for each individual version.


```kotlin
val versionCount = versionedMap.versions    // 3

val id = versionedMap["id"]                 // "111, 222, 333"
val id1 = versionedMap["id", 1]             // "111"
val id2 = versionedMap["id", 2]             // "222"
val id5 = versionedMap["id", 5]             // "111"

val version2 = versionedMap.getVersion(2)   // Map<String, String>
val allVersions = versionedMap.getIndividualVersions()  // List<Map<String, String>>
```


</details>
<p align="right">(<a href="#readme-top">back to top</a>)</p>

- ### TitleQueue ([TitleQueue.kt][TitleQueue.kt Link])

<details><summary>

###### This is used for efficiently scraping data by titles in bulk.

</summary>

---

 - If the response is too long the Wiki will return an error, if this happens you may need to lower the chunk size.


 - Create a new queue with the list of titles and the chunk size. (The default size is 100)
   - ```kotlin
     val titles = wiki.getAll
     val queue = TitleQueue(titles, 50)
     ```

 - Then call ``queue.execute { /* Your code here */ }`` to execute the queue.
   - The block inside the execute function is suspending.
   - The parameter passed to the block is a list of titles to be processed.
   - The block should only return titles that failed to be processed and will be re-added to the queue.
 
   - ```kotlin
     val processedResults = mutableMapOf<String, String>()
     queue.execute { titlesChunk ->
       // Process the titles here adding any data to your results, and returning any failed titles.
       // No data is returned from execute.
     }
     ```

---
<p align="right">(<a href="#readme-top">back to top</a>)</p>
</details>



<details><summary>

## Useful References

</summary>

<details><summary>

###### Some useful references to assist in using this project.

</summary>

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
<p align="right">(<a href="#readme-top">back to top</a>)</p>
</details>


</details>
</details>


[OsrsWiki.kt Link]: https://github.com/IvanEOD/osrs-wiki-scraper/blob/master/src/main/kotlin/scripts/wikiscraper/OsrsWiki.kt
[ScribuntoSession.kt Link]: https://github.com/IvanEOD/osrs-wiki-scraper/blob/master/src/main/kotlin/scripts/wikiscraper/lua/ScribuntoSession.kt
[LuaBuilder.kt Link]: https://github.com/IvanEOD/osrs-wiki-scraper/blob/master/src/main/kotlin/scripts/wikiscraper/lua/LuaBuilder.kt
[LuaScope Link]: https://github.com/IvanEOD/osrs-wiki-scraper/blob/master/src/main/kotlin/scripts/wikiscraper/lua/LuaBuilder.kt
[LuaGlobalScope.kt Link]: https://github.com/IvanEOD/osrs-wiki-scraper/blob/master/src/main/kotlin/scripts/wikiscraper/lua/LuaBuilder.kt
[LuaTableScope.kt Link]: https://github.com/IvanEOD/osrs-wiki-scraper/blob/master/src/main/kotlin/scripts/wikiscraper/lua/LuaBuilder.kt
[VersionedMap.kt Link]: https://github.com/IvanEOD/osrs-wiki-scraper/blob/master/src/main/kotlin/scripts/wikiscraper/utility/VersionedMap.kt
[TitleQueue.kt Link]: https://github.com/IvanEOD/osrs-wiki-scraper/blob/master/src/main/kotlin/scripts/wikiscraper/utility/TitleQueue.kt
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
[Kotlin DSL Link]: https://docs.gradle.org/current/userguide/kotlin_dsl.html
[OsrsWiki Link]: https://oldschool.runescape.wiki/
