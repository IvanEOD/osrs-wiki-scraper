# Osrs Wiki Scraper


### OsrsWiki ([OsrsWiki.kt][OsrsWiki.kt Link])

<details><summary>

#### Create an OsrsWiki instance:
</summary>

---
```kotlin
   val wiki = OsrsWiki.builder()
                 .withCookieManager( CookieManager() )
                 .withProxy( Proxy() )                
                 .withUserAgent( "Custom User Agent" )    
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
     - ```wiki.getItemPageTitlesFromIds(11832, 11834, 11836)``` &#10145; `["Bandos chestplate", "Bandos tassets", "Bandos boots"]`


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
       - ```wiki.getMonsterDetails("Abyssal demon", "Abyssal leech", "Abyssal lurker")``` &#10145; `Map<String, List<MonsterDetails>>`
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

