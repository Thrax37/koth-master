import sys
from random import *
from operator import itemgetter

(PLAYER, TOWN, GOLD, CORPSES, WARLOCKS, CRUSADERS, AMAZONS, CORSAIRS, BISHOPS,
NECROMANCERS, ARCHITECTS, PEONS) = range(12)

def getstrength(t): return sum(t[WARLOCKS:WARLOCKS+3])

if len(sys.argv) < 2:
    print 7, 20, 3, 10, 8, 1, 1, 50
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
        output = 'S %s 3' % rich[-1][TOWN]
    elif phase == 3:
        if here[GOLD] > 200:
            output = 'R 0 1 0 1 0 0 0'
    elif phase == 6:
        if here[GOLD] > 300:
            output = 'C %s 0 1 0' % choice(otherids)
    elif phase == 7:
        target = strength[0]
        if getstrength(target) < getstrength(here)/2:
            output = 'A %s 0 %s 0' % (target[TOWN], here[CRUSADERS] * 3/4)
    elif phase == 8:
        if here[GOLD] > 150 and here[CORPSES] > 0:
            output = 'R 1'
    elif phase == 9:
        pass  # move people or gold here
    elif phase == 11:
        if here[GOLD] > 700:
            output = 'B P'   # Build me a palace

    print output if output else 'W'