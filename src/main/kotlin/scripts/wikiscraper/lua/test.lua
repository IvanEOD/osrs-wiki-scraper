function tableEach(t, action)
    for k, v in ipairs(t) do
        action(k, v)
    end
end

function tableConcat(t, operator)
    local result = ''
    tableEach(t, function(k, v)
        result = result .. operator(k, v)
    end)
    return result
end

function formatTemplatesToIncludeString(t)
    return tableConcat(t, function(key, value)
        local returnValue = "{" .. value .. "}"
        if (key ~= #t) then
            returnValue = returnValue .. ","
        end
        return returnValue
    end)
end

myTable = {
    ["myKey"] = "myValue",
    [48] = "2022-12-21 17:33:09",
    ["2022-12-21 17:33:09"] = "myValue",
    [1.0] = 1,
    [true] = "myTrueValue",
    ["something"] = true
}

print(formatTemplatesToIncludeString({ "Infobox Monster", "LocLine" }))