local dpl = require('Module:DPLlua')
local exchange = require('Module:Exchange')

local Item = {}
Item.__index = Item


function Item.new(title)
    local self = setmetatable({}, Item)
    self.title = title

    local loaded = dpl.ask({
        title = title,
        include = "{Infobox Item}, {Infobox Bonuses}",
        casesensitive = false
    })

    self.info = loaded[1]["include"]["Infobox Item"]
    self.bonuses = loaded[1]["include"]["Infobox Bonuses"]
    self:loadExchangeData()
    return self
end

function Item:determineVersions()
    for k, v in pairs(self.info) do
        print(k .." = ".. v)
    end
end

function Item:loadExchangeData()

    local checkedTitle = exchange.checkedTitle(self.title)
    local noError, loadedData = pcall(mw.loadData, "Module:Exchange/"..self.title)

    if not noError then
        self.exchangeData = nil
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

    self.exchangeData = data
end