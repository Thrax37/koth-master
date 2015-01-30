$BONUS = 1.5
class Town
  
  attr_accessor :player, :town, :gold, :corpses, :warlocks, :crusaders, :amazons, 
    :corsairs, :bishops, :necromancers, :architects, :peons, :temples, :barracks, 
    :estates, :palaces
  
  def initialize(arg)
    args = arg.split("_")
    @player = args[0].to_i
    @town = args[1].to_i
    @gold = args[2].to_i
    @corpses = args[3].to_i
    @warlocks = args[4].to_i
    @crusaders = args[5].to_i
    @amazons = args[6].to_i
    @corsairs = args[7].to_i
    @bishops = args[8].to_i
    @necromancers = args[9].to_i
    @architects = args[10].to_i
    @peons = args[11].to_i
    @temples = args[12].to_i
    @barracks = args[13].to_i
    @estates = args[14].to_i
    @palaces = args[15].to_i
  end  
  def soldiers
    @warlocks + @crusaders + @amazons
  end 
  def units
    self.soldiers + @corsairs + @bishops + @necromancers + @architects
  end  
  def citizens
    self.units + @peons
  end  
  def buildings
    @temples + @barracks + @estates + @palaces
  end
  def cash
    @gold - (self.units * 2) - 50
  end
  def flesh
    @peons - (self.units * 2 / 5)
  end
end

def stronger(aW, aC, aA, dW, dC, dA)
  aW - [[0, aW - dW].max, dA].min + ([[0, aW - dW].max, dA].min * $BONUS) + aC - [[0, aC - dC].max, dW].min + ([[0, aC - dC].max, dW].min * $BONUS) + aA - [[0, aA - dA].max, dC].min + ([[0, aA - dA].max, dC].min * $BONUS)  > dW - [[0, dW - aW].max, aA].min + ([[0, dW - aW].max, aA].min * $BONUS) + dC - [[0, dC - aC].max, aW].min + ([[0, dC - aC].max, aW].min * $BONUS) + dA - [[0, dA - aA].max, aC].min + ([[0, dA - aA].max, aC].min * $BONUS) 
end

if ARGV.size < 1 
  puts "12 12 12 8 2 2 2 50"
else
  args = ARGV[0].split(";")
  
  round = args[0].to_i
  phase = args[1].to_i
  thisPlayer = args[2].to_i
  thisTownId = args[3].to_i
  towns, myTowns, enemyTowns = Array.new, Array.new, Array.new
  
  args[4..-1].each {|arg|arg.split(";").each {|t|towns.push(Town.new(t))}}
  towns.each {|town|town.player == thisPlayer ? myTowns.push(town) : enemyTowns.push(town)}
  thisTown = towns[towns.index{|t|t.town == thisTownId}]
  strongestTown = enemyTowns.sort{|x,y|y.soldiers<=>x.soldiers}.fetch(0)  
  weakestTown = enemyTowns.sort{|x,y|x.soldiers<=>y.soldiers}.fetch(0)  
  baseTown = myTowns.sort{|x,y|y.peons<=>x.peons}.fetch(0)    
  
  case phase
  when 2
    puts "S " + strongestTown.town.to_s + " " + thisTown.corsairs.to_s
  when 3
    if (thisTown.cash > 90)
      recruits = [[(thisTown.cash - 90) / 10, thisTown.flesh].min / 5, 0].max
      puts "R " + recruits.to_s + " " + recruits.to_s + " " + (recruits*3).to_s + " " + [0, 2 - thisTown.corsairs].max.to_s + " " + [0, 2 - thisTown.bishops].max.to_s + " " + [0, 2 - thisTown.necromancers].max.to_s + " " + [0, 2 - thisTown.architects].max.to_s
    else
      puts "W"
    end
  when 6
    converts = [[thisTown.cash / 50, thisTown.bishops].min / 5, 0].max
    puts "C " + strongestTown.town.to_s + " " + (converts*3).to_s + " " + converts.to_s + " " + converts.to_s
  when 7
    if round > 10
      if stronger(thisTown.warlocks / 6, thisTown.crusaders / 6, thisTown.amazons / 6, strongestTown.warlocks, strongestTown.crusaders, strongestTown.amazons)
        puts "A " + strongestTown.town.to_s + " " +  (thisTown.warlocks/4).to_s + " " + (thisTown.crusaders/4).to_s + " " + (thisTown.amazons/4).to_s
      elsif stronger(thisTown.warlocks / 6, thisTown.crusaders / 6, thisTown.amazons / 6, weakestTown.warlocks, weakestTown.crusaders, weakestTown.amazons)
        puts "A " + weakestTown.town.to_s + " " +  (thisTown.warlocks/4).to_s + " " + (thisTown.crusaders/4).to_s + " " + (thisTown.amazons/4).to_s
      else
        puts "W"
      end
    else
      puts "W"
    end
  when 8
    puts "R 10"
  when 9
    if thisTown.town != baseTown.town
      if thisTown.soldiers > 0 
        puts "M " + baseTown.town.to_s + " " + thisTown.warlocks.to_s + " " + thisTown.crusaders.to_s + " " + thisTown.amazons.to_s
      else
        puts "T " + baseTown.town.to_s + " " + thisTown.cash
      end
    else
      puts "W"
    end
  when 11
    if thisTown.town == baseTown.town and thisTown.cash > 200 
      puts "B B" 
    else
      puts "W"
    end
  else
    puts "W"
  end
end

