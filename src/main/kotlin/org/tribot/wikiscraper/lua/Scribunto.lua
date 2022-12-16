local dpl = require('Module:DPLlua')
local itemStats = require('Module:FetchItemStats')
local exchange = require('Module:Exchange')

function tableAny(t, predicate)
    for k, v in ipairs(t) do
        if (predicate(k, v)) then
            return true
        end
    end
end

function tableNone(t, predicate)
    return not tableAny(t, predicate)
end

function tableEach(t, action)
    for k, v in ipairs(t) do
        action(k, v)
    end
end

function tableAll(t, predicate)
    for k, v in ipairs(t) do
        if (not predicate(k, v)) then
            return false
        end
    end
    return true
end

function tableMap(t, mapper)
    local result = {}
    tableEach(t, function(k, v)
        mapper(k, v, function(key, value)
            result[key] = value
        end)
    end)
    return result
end

function tableMapToList(t, mapper)
    local result = {}
    tableEach(t, function(key, value)
        mapper(key, value, function(finalValue)
            table.insert(result, finalValue)
        end)
    end)
    return result
end

function tableFilter(t, predicate)
    return tableMap(t, function(key, value, callback)
        if (predicate(key, value)) then
            callback(key, value)
        end
    end)
end

function tableContainsKey(t, key)
    tableAny(t, function(k, v)
        return k == key
    end)
end

function tableContainsValue(t, value)
    tableAny(t, function(k, v)
        return v == value
    end)
end

function tableContains(t, value)
    tableAny(t, function(k, v)
        return k == value or v == value
    end)
end

function tableContainsAll(t, values)
    tableAll(values, function(k, v)
        return tableContains(t, v)
    end)
end

function tableNotContainsKey(t, key)
    tableNone(t, function(k, v)
        return k == key
    end)
end

function tableNotContainsValue(t, value)
    tableNone(t, function(k, v)
        return v == value
    end)
end

function tableNotContains(t, value)
    tableNone(t, function(k, v)
        return k == value or v == value
    end)
end

function startsWith(string, prefix)
    return string:sub(1, #prefix) == prefix
end

function endsWith(string, suffix)
    return suffix == "" or string:sub(-#suffix) == suffix
end

function replaceAll(string, find, replace)
    return string:gsub(find, replace)
end

function toJson(tbl)
    return mw.text.jsonEncode(tbl)
end

function printReturnError(message, value)
    local response = {}
    response['success'] = false
    response['message'] = message
    response['printReturn'] = value
    print(toJson(response))
end

function printReturn(value)
    local response = {}
    response['success'] = true
    response['printReturn'] = value
    print(toJson(response))
end

function dplAsk(query, printResults)
    local results = dpl.ask(query)
    if (printResults) then
        printReturn(results)
    end
    return results
end

function smwAsk(query, printResults)
    local results = mw.smw.ask(query)
    if (printResults) then
        printReturn(results)
    end
    return results
end

function parsePagesToList(dplResponse, filter, format)
    return tableMapToList(dplResponse, function(_, value, callback)
        local page = value
        if (filter == nil or filter(page)) then
            if (format) then
                page = format(page)
            end
            callback(page)
        end
    end)
end

function isSessionLoaded()
    printReturn(true)
end

function loadExchangeData(items, ignoreErrors, printResults)
    local itemData = {}

    for _, item in ipairs(items) do
        local title = exchange.checkTitle(item)
        local noErr, loadedData = pcall(mw.loadData, 'Module:Exchange/' .. title)

        if (not noErr) then
            if (not ignoreErrors) then
                printReturnError("Error loading exchange data for item " .. tostring(title), data)
            end
        end

        if noErr then

            local data = {}
            data.id = loadedData.itemId
            data.icon = loadedData.icon
            data.name = loadedData.item
            data.value = loadedData.value
            data.buyLimit = loadedData.limit
            data.members = loadedData.members
            data.category = loadedData.category
            data.examine = loadedData.examine
            data.highAlch = loadedData.hialch
            data.lowAlch = loadedData.lowalch
            data.price = exchange.loadBulkData(title, 'price')
            data.date = exchange.loadBulkData('%LAST_UPDATE_F%', 'price')
            data.last = exchange.loadBulkData(title, 'lastPrice')
            data.lastDate = exchange.loadBulkData('%LAST_UPDATE_F%', 'lastPrice')
            data.volume = exchange.loadBulkData(title, 'volume')

            local changeperday = require('Module:ChangePerDay')._change

            if data.last then
                data.link = 'http://services.runescape.com/m=itemdb_oldschool/viewitem.ws?obj=' .. data.id
                data.change = math.abs(changeperday({ data.price, data.last, data.date, data.lastDate }))
            end
            itemData[item] = data
        end
    end
    if (printResults ~= nil and printResults) then
        printReturn(itemData)
    end
    return itemData
end

function loadItemData(items, printResults)
    local itemData = {}
    for _, item in pairs(items) do
        local loaded = dpl.ask({
            title = item,
            include = "{Infobox Item}, {Infobox Bonuses}"
        })
        local exchangeData = loadExchangeData({item}, true)

        local data = {
            title = loaded[1].title,
            info = loaded[1]["include"]["Infobox Item"],
            bonuses = loaded[1]["include"]["Infobox Bonuses"],
            exchangeData = exchangeData and exchangeData[item] or {}
        }
        itemData[item] = data
    end
    if (printResults ~= nil and printResults) then
        printReturn(itemData)
    end
    return itemData
end

function dplAskChunked(query, count, offset, printResults)
    query['count'] = count
    query['offset'] = offset
    local response = dpl.ask(query)
    if (printResults ~= nil and printResults) then
        printReturn(response)
    end
    return response
end

function getPagesInCategory(category, count, offset, printResults)
    local pages = dpl.ask {
        category = category,
        count = count,
        offset = offset,
        ignorecase = true
    }
    if (printResults ~= nil and printResults) then
        printReturn(pages)
    end
    return pages
end

function getTemplatesOnPage(title, printResults)
    local result = parsePagesToList(dpl.ask { usedBy = title, }, function(page)
        return startsWith(page, "Template:")
    end, function(page)
        return page:sub(10)
    end)
    if (printResults ~= nil and printResults) then
        printReturn(result)
    end
    return result
end

function getModulesOnPage(title, printResults)
    local result = parsePagesToList(dpl.ask { usedBy = title, }, function(page)
        return startsWith(page, "Module:")
    end, function(page)
        return page:sub(8)
    end)
    if (printResults ~= nil and printResults) then
        printReturn(result)
    end
    return result
end

function loadTitlesWithLocationData(limit, offset, printResults)
    local results = mw.smw.ask {
        "[[Location JSON::+]]",
        "limit = " .. limit,
        "offset = " .. offset,
    }
    if (printResults ~= nil and printResults) then
        printReturn(results)
    end
    return results
end

function loadLocationData(limit, offset, printResults)
    local results = mw.smw.ask {
        "[[Location JSON::+]]",
        "?Location JSON",
        "limit = " .. limit,
        "offset = " .. offset
    }
    if (printResults) then
        printReturn(results)
    end
    return results
end