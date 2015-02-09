import sys, re
from random import *
from operator import itemgetter
import cPickle

(PLAYER, TOWN, GOLD, CORPSES, WARLOCKS, CRUSADERS, AMAZONS, 
CORSAIRS, BISHOPS, NECROMANCERS, ARCHITECTS, PEONS, 
TEMPLES, BARRACKS, ESTATES, PALACES) = range(16)

def getfighters(t): return sum(t[WARLOCKS:WARLOCKS+3])
def threat(t): return t[2] + sum(t[4:12])*12 + sum(t[12:16])*200
def spyon(t): return ( t[2] + min(30,t[3])*5 + t[11]*10 + 
    sum(t[12:16])*200 - getfighters(t)*20 )
def needs(t): return [bandits/5+1, t[PEONS]/50+1, t[CORPSES]/5+1, 7]
def wants(t): return [max(0, g-h) for g,h in zip(needs(t), t[7:11])]
def helpcheck(t): return sum(wants(t))

def choose(frequency, picks, span):
    'Return <picks> counts using samples from <frequency> in list <span>'
    choices = [choice(frequency) for i in range(picks)]
    return [choices.count(i) for i in range(span)]

if len(sys.argv) < 2:
    print 5, 15, 10, 20, 3, 4, 7, 36
else:
    parts = sys.argv[1].split(';')
    turn, phase, me, thistown = [int(parts.pop(0)) for i in range(4)]
    towns = [map(int, re.split(r'_', town)) for town in parts]
    # Analysis:
    enemy = [t for t in towns if t[PLAYER] != me]
    mytowns = [t for t in towns if t[PLAYER] == me]
    here = [t for t in mytowns if t[TOWN] == thistown][0]
    otherids = [t[TOWN] for t in enemy]
    fighters = sorted(enemy, key=getfighters)
    rich = sorted(enemy, key=itemgetter(GOLD))
    threats = sorted(enemy, key=threat)
    attractive = sorted(enemy, key=spyon)
    # Useful numbers:
    avgfighters = sum(map(getfighters, enemy)) / len(enemy)
    wages = getfighters(here) + sum(here[CORSAIRS:CORSAIRS+4]) * 2
    outlaws = sum(sum(t[4:12]) for t in towns if t[PLAYER] == -1)
    freetowns = len([t for t in towns if t[PLAYER] != -1])
    bandits = outlaws / freetowns
    # Depends on above
    needhelp = sorted(mytowns, key=helpcheck)
    needhelp.remove(here)

    try:
        plans = cPickle.load(open('Machiavelli.txt', 'rb'))
    except:
        plans = {}
    bribes, raises, gobuild = plans.get(thistown, (0,0,''))

    output = 'W'
    if phase == 2:
        output = 'S %s 100' % rich[-1][TOWN]  # take from the rich ...
    elif phase == 3:
        # Decide strategy here:
        cash = here[GOLD] - wages
        forces = getfighters(here)
        raises = min(here[NECROMANCERS]*5, here[CORPSES], cash/20)
        cash -= raises * 20
        bribes = trainftr = trainextra = 0
        gobuild = ''
        if forces < avgfighters:
            bribes = min(here[BISHOPS], cash/50)
            cash -= bribes * 50
            trainftr = min(max(0, here[PEONS]-30), cash/10)
            cash -= trainftr * 10
        if cash > 200 and turn % 2 == 0:
            gobuild = choice('EEB')
            cash -= 200
        bribes2 = min(here[BISHOPS] - bribes, cash/50)
        cash -= bribes2 * 50
        bribes += bribes2
        trainextra = min(max(0, here[PEONS]-30), cash/50)
        # Write plan to file:
        plans[thistown] = (bribes, raises, gobuild)
        cPickle.dump(plans, open('Machiavelli.txt', 'wb'), -1)

        # Output recruitment decision:
        if trainftr + trainextra:
            getutil = wants(here)
            if sum(getutil) > trainextra:
                utilbias = ( [0]*getutil[0]*3 + [1]*getutil[1]*3 + 
                            [2]*(getutil[2]) + [3]*(getutil[3]) )
                getutil = choose(utilbias, trainextra, 4)
            getftr = choose([0,1,1,2], trainftr, 3)
            getpers = getftr + getutil
            if sum(getutil) < trainextra:
                othernum = trainextra - sum(getutil)
                others = choose([0,1,1,2,3,4,6], othernum, 7)
                getpers = [p+q for p,q in zip(getpers, others)]
            output = 'R %u %u %u %u %u %u %u' % tuple(getpers)
    elif phase == 6:
        if bribes:
            soldiers = choose([0,1,2], bribes, 3)
            target = fighters[-1][TOWN]
            output = 'C %s %s %s %s' % tuple([target] + soldiers)
    elif phase == 7:
        if getfighters(here) > avgfighters * 1.3:
            myarmy = here[WARLOCKS : WARLOCKS+3]
            raiders = sum(myarmy) / 2
            for n in range(raiders):
                force = [min(myarmy[i], n) for i in (0,1,2)]
                if sum(force) >= raiders:
                    break
            for target in attractive[::-1]:
                if raiders > getfighters(target) * 2.5:
                    output = 'A %s %s %s %s' % tuple([target[TOWN]] + force)
                    break
    elif phase == 8:
        if raises:
            output = 'R %s' % raises
    elif phase == 9:
        if needhelp:
            town = needhelp[-1]
            excess = [max(0, g-h) for g,h in zip(here[7:11], needs(here))]
            send = [min(g, h) for g,h in zip(excess, wants(town))]
            if sum(send) > 0:
                output = 'M %u 0 0 0 %u %u %u %u' % tuple([town[TOWN]] + send)
    elif phase == 11:   
        if gobuild:
            output = 'B %s' % gobuild
    print output