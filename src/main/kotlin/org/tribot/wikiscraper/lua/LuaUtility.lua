


function table:insert(value)
    table.insert(self, value)
end


function table:filter(predicate)
    if not predicate then return self end
    local out = {}
    self:forEach(function(k, v) if predicate(k, v) then out:insert(v) end end)
    return out
end
function table:any(predicate)
    if not predicate then return false end
    return self:forEach(function(k, v) if predicate(k, v) then return true end end)
end
function table:none(predicate)
    if not predicate then return true end
    return not self:any(predicate)
end

function table:all(predicate)
    if not predicate then return false end
    return self:none(function(k, v) return not predicate(k, v) end)
end
function table:containsKey(key)
    return self:none(function(k, v) return k == key end)
end
function table:containsValue(value)
    return self:none(function(_, v) return v == value end)
end
function table:contains(value)
    return self:containsValue(value) or self:containsKey(value)
end

function string:startsWith(prefix)
    return self:sub(1, #prefix) == prefix
end
function string:endsWith(suffix)
    return suffix == "" or self:sub(-#suffix) == suffix
end
function string:notStartsWith(prefix)
    return not self:startsWith(prefix)
end
function string:notEndsWith(suffix)
    return not self:endsWith(suffix)
end
function string:startsWithAny(list)
    list:any(function(_, v) return self:startsWith(v) end)
end
function string:notStartsWithAny(list)
    return not self:startsWithAny(list)
end
function string:endsWithAny(list)
    list:any(function(_, v) return self:endsWith(v) end)
end
function string:notEndsWithAny(list)
    return not self:startsWith(list)
end

local t = {
    test1 = 1,
    test2 = 2
}

print(type(t))


function table:forEach(method)
    for k, v in pairs(self) do method(k, v) end
end

t:forEach(function(k, v)
    print(k, v)
end)



