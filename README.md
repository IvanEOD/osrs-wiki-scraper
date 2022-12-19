# Osrs Wiki Scraper
Osrs Wiki Scraper

<details><summary>OsrsWiki.kt</summary>
<p>

### Create an OsrsWiki instance:
```kotlin
val wiki = OsrsWiki.builder()
    .withCookieManager(customCookieManager)
    .withProxy(customProxy)                
    .withUserAgent(customUserAgent)        
    .build() 
```

 - Optionally set a custom cookie manager.
   - ```.withCookieManager( CookieManager() )```
 - Optionally set a custom proxy.
   - ```.withProxy( Proxy() )```
 - Optionally set a custom user agent.
   - ```.withUserAgent( "Custom User Agent" )```

#### Using the OsrsWiki instance:

 - Get a page title by Item ID:
    - ```wiki.getPageTitleFromId( 995 )``` &#10145; ```"Coins"```
 - Get page titles from Item IDs:
    - ```wiki.getPageTitlesFromIds(11832, 11834, 11836)``` &#10145; ```["Dragon pickaxe", "Dragon pickaxe (or)", "Dragon pickaxe (g)"]```

```kotlin
    wiki.getPageTitleFromId(995)                   // "Coins"
    wiki.getPageTitlesFromIds(11832, 11834, 11836) // [ "Bandos chestplate", "Bandos tassets", "Bandos boots" ]
    wiki.getTitlesInCategory("")
```

</p>
</details>

<details><summary>ScribuntoSession.kt</summary>
<p>

### Creating a Scribunto Session:
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




</details>





















