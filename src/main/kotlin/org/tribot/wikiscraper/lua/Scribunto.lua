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
        hour = dateString:sub(12, 13),
        min = dateString:sub(15, 16),
        sec = dateString:sub(18, 19)
    }
end

function dplAskUses(uses, count, offset, include)
    return dpl.ask {
        namespace = '',
        uses = uses,
        count = count,
        offset = offset,
        include = include,
        ignorecase = true
    }
end

function dplAskCategory(category, count, offset, include)
    return dpl.ask {
        namespace = '',
        category = category,
        count = count,
        offset = offset,
        include = include,
        ignorecase = true
    }
end

function getAllLocationJson()

end

function getTemplatesOnPage(title)
    return dpl.ask {
        namespace = '',
        usedBy = title,
        ignorecase = true,
    }
end


function getPagesInCategories(categories)
    local pages = {}
    for _, category in pairs(categories) do
        local categoryPages = dplAskCategory(category, 25000, 0, 'title')
        for _, page in pairs(categoryPages) do
            table.insert(pages, page.title)
        end
    end
    return pages
end

function toJson(tbl) return mw.text.jsonEncode(tbl) end
function getPagesInCategoriesJson(categories) return toJson(getPagesInCategories(categories)) end
function isSessionLoaded() return true end

function printReturn(value)
    local response = {}
    response['printReturn'] = value
    print(mw.text.jsonEncode(response))
end

--{{#dpl:
--          |titlematch=Baby chinchompa
--|include={Infobox Item}.templatevalues
--|format=__NOTOC__,
--}}

function getAllTemplates(title)
    local templates = dpl.ask {
        title = title,
        include = '{Infobox Item}.templatevalues',
    }
    return templates
end



--Template:Chinchompas
--Template:CiteTwitter
--Template:Ctg
--Template:External
--Template:Hastranscript
--Template:Hastranscript/t
--Template:Hatnote
--Template:Infobox Item
--Template:Infobox NPC
--Template:Mes
--Template:Multi Infobox
--Template:Navbox
--Template:Otheruses
--Template:Pets
--Template:Plink
--Template:Reflist
--Template:Subject changes
--Template:Subject changes footer
--Template:Subject changes header
--Template:Synced switch
--Template:UD
--Template:Wp