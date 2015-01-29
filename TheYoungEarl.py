import sys, re
from random import *
from operator import itemgetter

(PLAYER, TOWN, GOLD, CORPSES, WARLOCKS, CRUSADERS, AMAZONS, 
CORSAIRS, BISHOPS, NECROMANCERS, ARCHITECTS, PEONS, 
TEMPLES, BARRACKS, ESTATES, PALACES) = range(16)

def getstrength(t): return sum(t[WARLOCKS:WARLOCKS+3])

if len(sys.argv) < 2:
    print 11, 25, 7, 21, 3, 4, 4, 25
else:
    parts = sys.argv[1].split(';')
    turn, phase, me, thistown = [int(parts.pop(0)) for i in range(4)]
    towns = [map(int, re.split(r'_', town)) for town in parts]
    enemy = [t for t in towns if t[PLAYER] != me]
    mytowns = [t for t in towns if t[PLAYER] == me]
    here = [t for t in mytowns if t[TOWN] == thistown][0]
    otherids = [t[TOWN] for t in enemy]
    strength = sorted(enemy, key=getstrength)
    avgstrength = sum(map(getstrength, enemy)) / len(enemy)
    rich = sorted(enemy, key=itemgetter(GOLD))
    costs = getstrength(here) + sum(here[CORSAIRS:CORSAIRS+4]) * 2

    output = 'W'
    if phase == 2:
        output = 'S %s 500' % rich[-1][TOWN]  # take from the rich ...
    elif phase == 3:
        if here[GOLD] > costs + 100 and here[PEONS] > 20:
            output = 'R 1 4 1 2 0 0 0'
    elif phase == 6:
        output = 'C %s 0 %s 0' % (choice(otherids), here[GOLD]/200)
    elif phase == 7:
        target = strength[0]
        if getstrength(target) < getstrength(here)/4:
            if getstrength(here) > avgstrength:
                output = 'A %s 0 %s 0' % (target[TOWN], here[CRUSADERS] / 2)
    elif phase == 8:
        output = 'R %s' % min(20, here[CORPSES], here[GOLD]/40)
    elif phase == 9:
        pass  # move people or gold there if I have a second town ...
    elif phase == 11:
        if here[GOLD] > 250:
            output = 'B E'
    print output