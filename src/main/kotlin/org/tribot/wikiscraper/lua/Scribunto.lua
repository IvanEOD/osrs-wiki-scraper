local dpl = require('Module:DPLlua')
local itemStats = require('Module:FetchItemStats')
local plt = require('Module:PageListTools')

local function getTimestamp()
    return os.date().format("%Y-%m-%d %H:%M:%S")
end

local function getTimeFromString(dateString)
    return os.time {
        year = dateString:sub(1, 4),
        month = dateString:sub(6, 7),
        day = dateString:sub(9, 10),
        hour = dateString:sub(12, 13), min = dateString:sub(15, 16), sec = dateString:sub(18, 19)
    }
end

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
    tableAny(t, function(k, v) return k == key end)
end

function tableContainsValue(t, value)
    tableAny(t, function(k, v) return v == value end)
end

function tableContains(t, value)
    tableAny(t, function(k, v) return k == value or v == value end)
end

function tableContainsAll(t, values)
    tableAll(values, function(k, v) return tableContains(t, v) end)
end

function tableNotContainsKey(t, key)
    tableNone(t, function(k, v) return k == key end)
end

function tableNotContainsValue(t, value)
    tableNone(t, function(k, v) return v == value end)
end

function tableNotContains(t, value)
    tableNone(t, function(k, v) return k == value or v == value end)
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

function printReturn(value)
    local response = {}
    response['printReturn'] = value
    print(toJson(response))
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
function dplAsk(query)
    printReturn(dpl.ask(query))
end

function dplAskChunked(query, count, offset)
    query['count'] = count
    query['offset'] = offset
    return dpl.ask(query)
end

function getPagesInCategory(category, count, offset)
    local pages = dpl.ask {
        category = category,
        count = count,
        offset = offset,
        ignorecase = true
    }
    return printReturn(pages)
end


function getTemplatesOnPage(title)
    printReturn(parsePagesToList(dpl.ask { usedBy = title, }, function(page)
        return startsWith(page, "Template:")
    end, function(page)
        return page:sub(10)
    end))
end

function getModulesOnPage(title)
    printReturn(parsePagesToList(dpl.ask { usedBy = title, }, function(page)
        return startsWith(page, "Module:")
    end, function(page)
        return page:sub(8)
    end))
end

function getAllLocationJson()

end






