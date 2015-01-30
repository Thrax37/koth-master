function explode(d,p)
  local t, ll; t={}; ll=0
  if(#p == 1) then return {p} end
    while true do
      l=string.find(p,d,ll,true)
      if l~=nil then table.insert(t, string.sub(p,ll,l-1)); ll=l+1
      else table.insert(t, string.sub(p,ll)); break
      end
    end
  return t
end

function soldiers(t)
  local s
  s = t["WARLOCKS"] + t["CRUSADERS"] + t["AMAZONS"]
  return s
end

function reserve(t)
  local s
  s = t["GOLD"] - (3 * soldiers(t))
  return s
end

parameters = {"PLAYER", "TOWN", "GOLD", "CORPSES", "WARLOCKS", "CRUSADERS", "AMAZONS", 
"CORSAIRS", "BISHOPS", "NECROMANCERS", "ARCHITECTS", "PEONS", "TEMPLES", "BARRACKS", 
"ESTATES", "PALACES"}

if (#arg < 1) then
  print("5 5 5 10 10 1 3 59")  
else

  args = explode(";", arg[1])
  
  local round = tonumber(args[1])
  local phase = tonumber(args[2])
  local playerID = tonumber(args[3])
  local thisTownID = tonumber(args[4])
  local thisTown = {}
  local towns = {}
  local enemyTowns = {}
  local myTowns = {}
  local output = "W"
  local stock = 200
  local peons = 33
  local bishops = 20
  
  for i=5,#args,1 do
    local town = explode("_", args[i])
    towns[town[2]] = {}
    for key, value in pairs(town) do 
      towns[town[2]][parameters[key]] = tonumber(value)
    end
  end
  
  for i, town in pairs(towns) do 
    if town["PLAYER"] == playerID then
      if town["TOWN"] == thisTownID then thisTown = town end
      table.insert(myTowns, town)
    else
      table.insert(enemyTowns, town)
    end
  end
  
  if phase == 2 then
    local richestTown
    for i, town in pairs(myTowns) do
      if richestTown == nil then richestTown = town end
      if richestTown["GOLD"] > town["GOLD"] then richestTown = town end
    end
    if thisTown["CORSAIRS"] > 0 then output = "S "..richestTown["TOWN"].." "..thisTown["CORSAIRS"] end
  elseif phase == 3 then 
    local necromancers = 0
    local architects = 0
    if thisTown["NECROMANCERS"] < 1 then necromancers = 1 end
    if thisTown["ARCHITECTS"] < 1 then architects = 1 end
    if reserve(thisTown) > stock and thisTown["PEONS"] > peons then output = "R 0 0 0 0 "..math.max(0, math.min(thisTown["BISHOPS"] - bishops, math.min(thisTown["PEONS"] - peons, math.floor((reserve(thisTown) - stock) / 20)))).." "..necromancers.." "..architects end
  elseif phase == 6 then
    local biggestTown
    for i, town in pairs(enemyTowns) do
      if biggestTown == nil then biggestTown = town end
      if soldiers(biggestTown) > soldiers(town) then biggestTown = town end  
    end
    local units = math.min(thisTown["BISHOPS"], math.floor((reserve(thisTown) - stock) / 50))
    if (reserve(thisTown) > stock) then output = "C "..biggestTown["TOWN"].." "..math.floor(units/3).." "..math.floor(units/3).." "..math.floor(units/3) end
  elseif phase == 7 then 
    for i, town in pairs(enemyTowns) do
      if soldiers(town) <= 3 and soldiers(thisTown) >= 9 then output = "A "..town["TOWN"].." 3 3 3"; break end
    end
  elseif phase == 8 then
    if thisTown["CORPSES"] > 0 and thisTown["NECROMANCERS"] > 0 then output = "R "..math.min(thisTown["NECROMANCERS"] * 5, thisTown["CORPSES"]) end
  elseif phase == 9 then
    local smallestTown, poorestTown
    for i, town in pairs(myTowns) do
      if smallestTown == nil then smallestTown = town end
      if poorestTown == nil then poorestTown = town end
      if soldiers(smallestTown) < soldiers(town) then smallestTown = town end  
      if poorestTown["GOLD"] < town["GOLD"] then poorestTown = town end  
      if (soldiers(thisTown) - soldiers(smallestTown) >= 18) then output = "M "..thisTown["TOWN"].." 3 3 3" break end
      if (thisTown["GOLD"] - poorestTown["GOLD"] >= 600) then output = "T "..thisTown["TOWN"].." 300" break end
    end
  elseif phase == 11 then
     if reserve(thisTown) > (stock + 200) then output = "B T" end
  end
  
  print(output)
  
end





