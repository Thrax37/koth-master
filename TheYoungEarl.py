import sys, re
from random import *
from operator import itemgetter

(PLAYER, TOWN, GOLD, CORPSES, WARLOCKS, CRUSADERS, AMAZONS, 
CORSAIRS, BISHOPS, NECROMANCERS, ARCHITECTS, PEONS, 
TEMPLES, BARRACKS, ESTATES, PALACES) = range(16)
OUTLAW = -1

def seqalloc(seq, num):
    outseq = []
    totalw = float(sum(seq))
    for weight in seq:
        share = int(round(num * weight / totalw)) if weight else 0
        outseq.append(share)
        totalw -= weight
        num -= share
    return outseq

def getstrength(t): return sum(t[WARLOCKS:AMAZONS+1])
def calcprofit(t): return t[GOLD] + 20*t[PEONS] + 200*t[ESTATES]
def getincome(t): return 5*t[PEONS] + 2*(t[PEONS]+t[CORSAIRS])*t[ESTATES]

if len(sys.argv) < 2:
    print 5,20,5,   12,4,1,13,   40
else:
    try:
        output = 'W'

        parts = sys.argv[1].split(';')
        turn, phase, me, thistown = [int(parts.pop(0)) for i in range(4)]
        # Catagorize towns:
        towns = [map(int, re.split(r'_', town)) for town in parts]
        outlaws = [t for t in towns if t[PLAYER] == OUTLAW]
        mytowns = [t for t in towns if t[PLAYER] == me]
        enemy = [t for t in towns if t[PLAYER] not in (me, OUTLAW)]
        # Situation:
        here = [t for t in mytowns if t[TOWN] == thistown][0]
        strength = sorted(enemy, key=getstrength)
        strengthoutlaw = sorted(outlaws, key=getstrength)
        profenemy = sorted(enemy, key=calcprofit)
        profoutlaw = sorted(outlaws, key=calcprofit)
        rich = outlaws + sorted(enemy, key=itemgetter(GOLD))
        bandits = sum(sum(t[4:12]) for t in outlaws)
        # Planning:
        siblings = [t for t in mytowns if t != here]
        bodytowns = [t for t in mytowns if t[CORPSES] > 0]
        myincome = getincome(here)
        isrich = all(t[GOLD] < here[GOLD]*1.5 for t in enemy)
        wages = getstrength(here) + sum(here[CORSAIRS:ARCHITECTS+1]) * 2
        newpeons = (turn % 5 == 1) if turn > 1 else 0
        theft = 10 * max(0, bandits/len(enemy+mytowns) - here[CORSAIRS]*5)

        if phase == 2:
            output = 'S %u %u' % (rich[-1][TOWN], here[CORSAIRS])

        elif phase == 3:
            cash = max(0, here[GOLD] - wages)
            if theft < myincome:
                if here[ARCHITECTS] and cash > 200:
                    cash -= 200
                cash = max(0, cash - 150)
            # must haves:
            cors = (bandits/len(mytowns+enemy)) / 5 + 1
            cors = cors if cors < 25 else 0
            bish = here[PEONS]/50 + 1
            necro = here[CORPSES]/20 + 1
            require = [cors, bish, necro, 0]
            need = [0]*3 + [max(0, p-q) for p,q in zip(require, here[7:11])]
            if sum(need)*22 < cash:
                train = need
            else:
                train = seqalloc(need, cash/22)
            now = [p+q for p,q in zip(here[4:11], train)]
            cash -= sum(p*q for p,q in zip(train[3:], [12,20,20,15]))
            cash -= sum(train)*2   # new wages
            # nice to have:
            raises = min(here[CORPSES]/4+1, now[5]*5, cash/20)
            cash -= raises * 20
            archreq = max(0, 13 - here[ARCHITECTS])
            if theft < myincome and archreq * 17 < cash:
                train[6] += archreq
                cash -= archreq * 17
                if cash > 200:
                    cash -= 200
            newbish = max(0, cash/50 - now[4])
            if newpeons:
                bishreq = (myincome - 200 - wages)/50 * 12/10
                newbish += max(0, bishreq - (now[4]+newbish))
            newbish = min(newbish, cash/22)
            train[4] += newbish
            cash -= newbish * 22

            if sum(train) > 0:
                output = 'R %u %u %u %u %u %u %u' % tuple(train)

        elif phase == 6:
            cash = here[GOLD]
            if theft < myincome:
                if here[ARCHITECTS] and cash > 200:
                    cash -= 200
                cash = max(0, cash - 150)
            raises = min(here[CORPSES]/4+1, here[NECROMANCERS]*5, cash/20)
            cash -= raises * 20
            count = min(cash/50, here[BISHOPS])
            if count > 0:
                target = (strengthoutlaw + strength)[-1]
                count = min(count, sum(target[4:7]))
                ftrs = seqalloc(target[4:7], count)
                output = 'C %u %u %u %u' % tuple([target[TOWN]] + ftrs)

        elif phase == 7:
            force = getstrength(here)
            target = None
            if not enemy or force > getstrength(strength[-1])*2:
                for town in profenemy[::-1]+profoutlaw[::-1]:
                    if force > getstrength(town)*4:
                        target = town
                        break
            if target:
                raiders = seqalloc(here[4:7], force/2)
                output = 'A %u %u %u %u' % tuple([target[TOWN]] + raiders)

        elif phase == 8:
            cash = here[GOLD]
            if theft < myincome:
                if here[ARCHITECTS] and cash > 200:
                    cash -= 200
                cash = max(0, cash - 150)
            raises = min(here[CORPSES]/4+1, here[NECROMANCERS]*5, cash/20)
            if raises > 0:
                output = 'R %s' % raises

        elif phase == 9:
            if here[CORPSES] == 0 and bodytowns:
                dest = max(bodytowns, key=itemgetter(CORPSES))
                necro = min(here[NECROMANCERS], dest[CORPSES]/20+1)
                output = 'M %u 0 0 0 0 0 %u 0' % (dest[TOWN], necro)
            elif siblings and theft < 100 and isrich:
                given = min(here[GOLD]/33, here[CORSAIRS]*200)
                dest = max(siblings, key=getincome)
                output = 'T %u %u' % (dest[TOWN], given)

        elif phase == 11:
            if here[ARCHITECTS] and here[GOLD] >= 200:
                output = 'B E'
    except:
        pass
    print output