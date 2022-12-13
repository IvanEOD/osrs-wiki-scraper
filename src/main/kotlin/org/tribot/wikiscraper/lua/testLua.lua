
function string:startsWith(value)
    return self:sub(1, #value) == value
end

function table:forEach(method)
    for k, v in pairs(self) do
        method(k, v)
    end
end

local t = {
    test = 1,
}

t:forEach(function(k, v)
    print(k, v)
end)

