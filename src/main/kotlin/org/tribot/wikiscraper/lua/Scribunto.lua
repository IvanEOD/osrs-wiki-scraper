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

function toJson(tbl)
    return mw.text.jsonEncode(tbl)
end
function printReturn(value)
    local response = {}
    response['printReturn'] = value
    print(toJson(response))
end
function parsePagesToList(dplResponse, filter, format)
    local pages = {}
    dplResponse:filter()
    dplResponse:forEach(function(key, value)
        if filter then
            if (filter(value)) then
                pages:insert(value)
            end
        else
            pages:insert(value)
        end
    end)
    --
    --for key, value in pairs(dplResponse) do
    --    if (key ~= "DPL time" and key ~= "Parse time") then
    --        if (filter ~= nil) then
    --            if (filter(value)) then
    --                table.insert(pages, value)
    --            end
    --        else
    --            table.insert(pages, value)
    --        end
    --    end
    --end
    local formatted = {}
    if format then
        for _, page in ipairs(pages) do
            table.insert(formatted, format(page))
        end
        return formatted
    end
    return pages
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

function getPagesInCategory(category)
    return dpl.ask {
        category = category,
        count = 25000,
        ignorecase = true
    }
end

function getPagesInCategories(categories)
    local pages = {}
    for _, category in pairs(categories) do
        local categoryPages = getPagesInCategory(category)
        for _, page in pairs(categoryPages) do
            if tableNotContainsValue(pages, page) == true and page ~= category then
                table.insert(pages, page)
            end
        end
    end
    local returnPages = table.filter(pages, function(key, value, t)
        return value ~= category
                and notStartsWith(value, "Category:")
                and notStartsWith(value, "Template:")
                and notStartsWith(value, "Module:")
                and notStartsWith(value, "File:")
    end)
    return printReturn(toJson(pages))
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






