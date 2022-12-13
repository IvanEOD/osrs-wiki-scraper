local dpl = require('Module:DPLlua')
local itemStats = require('Module:FetchItemStats')
local plt = require('Module:PageListTools')
local exchange = require('Module:Exchange')
local addcommas = require('Module:Addcommas')._add

local sessionData = {}

-- @handle = tableAny
-- @type = function
function tableAny(t, predicate)
    for k, v in ipairs(t) do
        if (predicate(k, v)) then
            return true
        end
    end
end

-- @handle = tableNone
-- @type = function
function tableNone(t, predicate)
    return not tableAny(t, predicate)
end

-- @handle = tableEach
-- @type = function
function tableEach(t, action)
    for k, v in ipairs(t) do
        action(k, v)
    end
end

-- @handle = tableAll
-- @type = function
function tableAll(t, predicate)
    for k, v in ipairs(t) do
        if (not predicate(k, v)) then
            return false
        end
    end
    return true
end

-- @handle = tableMap
-- @type = function
function tableMap(t, mapper)
    local result = {}
    tableEach(t, function(k, v)
        mapper(k, v, function(key, value)
            result[key] = value
        end)
    end)
    return result
end

-- @handle = tableMapToList
-- @type = function
function tableMapToList(t, mapper)
    local result = {}
    tableEach(t, function(key, value)
        mapper(key, value, function(finalValue)
            table.insert(result, finalValue)
        end)
    end)
    return result
end

-- @handle = tableFilter
-- @type = function
function tableFilter(t, predicate)
    return tableMap(t, function(key, value, callback)
        if (predicate(key, value)) then
            callback(key, value)
        end
    end)
end

-- @handle = tableContainsKey
-- @type = function
function tableContainsKey(t, key)
    tableAny(t, function(k, v)
        return k == key
    end)
end

-- @handle = tableContainsValue
-- @type = function
function tableContainsValue(t, value)
    tableAny(t, function(k, v)
        return v == value
    end)
end

-- @handle = tableContains
-- @type = function
function tableContains(t, value)
    tableAny(t, function(k, v)
        return k == value or v == value
    end)
end

-- @handle = tableContainsAll
-- @type = function
function tableContainsAll(t, values)
    tableAll(values, function(k, v)
        return tableContains(t, v)
    end)
end

-- @handle = tableNotContainsKey
-- @type = function
function tableNotContainsKey(t, key)
    tableNone(t, function(k, v)
        return k == key
    end)
end

-- @handle = tableNotContainsValue
-- @type = function
function tableNotContainsValue(t, value)
    tableNone(t, function(k, v)
        return v == value
    end)
end

-- @handle = tableNotContains
-- @type = function
function tableNotContains(t, value)
    tableNone(t, function(k, v)
        return k == value or v == value
    end)
end

-- @handle = startsWith
-- @type = function
function startsWith(string, prefix)
    return string:sub(1, #prefix) == prefix
end

-- @handle = endsWith
-- @type = function
function endsWith(string, suffix)
    return suffix == "" or string:sub(-#suffix) == suffix
end

-- @handle = replaceAll
-- @type = function
function replaceAll(string, find, replace)
    return string:gsub(find, replace)
end

-- @handle = toJson
-- @type = function
function toJson(tbl)
    return mw.text.jsonEncode(tbl)
end

-- @handle = printReturnError
-- @type = function
function printReturnError(message, value)
    local response = {}
    response['success'] = false
    response['message'] = message
    response['printReturn'] = value
    print(toJson(response))
end


--[[
    @handle = printReturn
    @type = function
    @dependencies = toJson
    @arguments = Any
    @description = Prints the given value as JSON, this is how you get the results of your script.
--]]
function printReturn(value)
    local response = {}
    response['success'] = true
    response['printReturn'] = value
    print(toJson(response))
end



-- @handle = parsePagesToList
-- @type = function
-- @dependencies =
-- @arguments =
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

-- @handle = isSessionLoaded
-- @type = function
-- @dependencies =
-- @arguments =
function isSessionLoaded()
    printReturn(true)
end

-- @handle = loadExchangeData
-- @type = function
-- @dependencies =
-- @arguments =
function loadExchangeData(items, ignoreErrors)
    local itemData = {}

    for _, item in ipairs(items) do


        local title = exchange.checkTitle(item)
        local noErr, loadedData = pcall(mw.loadData, 'Module:Exchange/' .. title)

        if (not noErr) then
            if (not ignoreErrors) then
                printReturnError("Error loading exchange data for item " .. tostring(title), data)
            end
            return
        end

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

        table.insert(itemData, data)
    end
    printReturn(itemData)
end

-- @handle = loadItemData
-- @type = function
-- @dependencies =
-- @arguments =
function loadItemData(items)

end

-- @handle = dplAsk
-- @type = function
-- @dependencies =
-- @arguments =
function dplAsk(query)
    printReturn(dpl.ask(query))
end

-- @handle = dplAskChunked
-- @type = function
-- @dependencies =
-- @arguments =
function dplAskChunked(query, count, offset)
    query['count'] = count
    query['offset'] = offset
    return dpl.ask(query)
end

-- @handle = getPagesInCategory
-- @type = function
-- @dependencies =
-- @arguments =
function getPagesInCategory(category, count, offset)
    local pages = dpl.ask {
        category = category,
        count = count,
        offset = offset,
        ignorecase = true
    }
    return printReturn(pages)
end

-- @handle = getTemplatesOnPage
-- @type = function
-- @dependencies =
-- @arguments =
function getTemplatesOnPage(title)
    printReturn(parsePagesToList(dpl.ask { usedBy = title, }, function(page)
        return startsWith(page, "Template:")
    end, function(page)
        return page:sub(10)
    end))
end

-- @handle = getModulesOnPage
-- @type = function
-- @dependencies =
-- @arguments =
function getModulesOnPage(title)
    printReturn(parsePagesToList(dpl.ask { usedBy = title, }, function(page)
        return startsWith(page, "Module:")
    end, function(page)
        return page:sub(8)
    end))
end

-- @handle = getAllLocationJson
-- @type = function
-- @dependencies =
-- @arguments =
function getAllLocationJson()

end






