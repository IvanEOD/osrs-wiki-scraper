# Osrs Wiki Scraper


<details><summary>OsrsWiki.kt</summary>
<p>

### Create an OsrsWiki instance:
```kotlin
val wiki = OsrsWiki.builder()
    .withCookieManager(customCookieManager)   // Optionally set a custom cookie manager
    .withProxy(customProxy)                   // Optionally set a custom proxy
    .withUserAgent(customUserAgent)           // Optionally set a custom user agent
    .build() 
```

```kotlin
    wiki.getPageTitleFromId(995)                   // "Coins"
    wiki.getPageTitlesFromIds(11832, 11834, 11836) // [ "Bandos chestplate", "Bandos tassets", "Bandos boots" ]
    wiki.getTitlesInCategory("")
```

</p>
</details>