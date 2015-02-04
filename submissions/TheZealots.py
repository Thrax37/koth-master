import sys
from random import *
from operator import itemgetter

(PLAYER, TOWN, GOLD, CORPSES, WARLOCKS, CRUSADERS, AMAZONS, 
CORSAIRS, BISHOPS, NECROMANCERS, ARCHITECTS, PEONS, 
TEMPLES, BARRACKS, ESTATES, PALACES) = range(16)

def getstrength(t):
    return t[WARLOCKS]+t[CRUSADERS]*1.5+t[AMAZONS]/1.5

if len(sys.argv) < 2:
    print 20, 5, 5, 10, 8, 5, 7, 40
else:
    parts = sys.argv[1].split(';')
    turn, phase, me, thistown = [int(parts.pop(0)) for i in range(4)]
    towns = [[int(v) for v in town.split('_')] for town in parts]
    enemy = [t for t in towns if t[PLAYER] != me]
    mytowns = [t for t in towns if t[PLAYER] == me]
    here = [t for t in mytowns if t[TOWN] == thistown][0]
    otherids = [t[TOWN] for t in enemy]
    strength = sorted(enemy, key=getstrength)
    rich = sorted(enemy, key=itemgetter(GOLD))

    output = ''
    if phase == 2:
        output = 'S %s %s' % (rich[-1][TOWN], here[CORSAIRS])
    elif phase == 3:
        Warlocks=Crusaders=Amazons=Corsairs=Bishops=Necromancers=Architects=0
        if here[CORPSES] > 5*here[NECROMANCERS]:
            Necromancers = 1
        if here[PEONS] > 50*here[BISHOPS]:
            Bishops = 1
        if here[WARLOCKS] < strength[0][CRUSADERS]:
            Warlocks = 2
            Amazons = 2
        if here[WARLOCKS] < here[AMAZONS]:
            Warlocks += 1
        if here[GOLD] > 200:
            Architects = 1
        if rich[-1][GOLD] > 100+2*here[GOLD]:
            Corsairs = 1
        output = 'R %s %s %s %s %s %s %s' % (Warlocks,Crusaders,Amazons,Corsairs,Bishops,Necromancers,Architects)
    elif phase == 6:
        if here[GOLD] > 300:
            output = 'C %s %s %s %s' % (strength[0][TOWN],here[GOLD]/300,here[GOLD]/300,here[GOLD]/300)
    elif phase == 7:
        target = strength[0]
        if getstrength(target) < getstrength(here)*3/5:
            output = 'A %s %s %s 0' % (target[TOWN],here[WARLOCKS]*3/4,target[AMAZONS])
    elif phase == 8:
        if here[CORPSES] > 10:
            output = 'R %s' % (here[NECROMANCERS]*5)
    elif phase == 9:
        pass  # move people or gold here
    elif phase == 11 and here[GOLD] > 300:
        output = 'B T'   # Build a temple!

    print output if output else 'W'