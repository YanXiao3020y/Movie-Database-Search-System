import sys

args = sys.argv
masterF = open(args[1], "r")

masterLineList = masterF.readlines()
Tj = 0
Ts = 0
lineCnt = 0

for i in range(len(masterLineList)):
    masterLine = masterLineList[i]
    lineCnt += 1
    lineSplit = masterLine.split()
    if len(lineSplit) != 4:
        print("Error with:", lineSplit)
    elif not lineSplit[1].isnumeric() or not lineSplit[3].isnumeric():
        print("Formatting error", lineSplit)
    else:
        Tj += int(lineSplit[1])
        Ts += int(lineSplit[3])
    # masterLine = masterF.readline()
if len(args) == 3:
    slaveF = open(args[2], "r")
    slaveLineList = slaveF.readlines()
    for j in range(len(slaveLineList)):
        slaveLine = slaveLineList[j]
        lineCnt += 1
        lineSplit = slaveLine.split()
        if len(lineSplit) != 4:
            print("Error with:", lineSplit)
        elif not lineSplit[1].isnumeric() or not lineSplit[3].isnumeric():
            print("Formatting error", lineSplit)
        else:
            Tj += int(lineSplit[1])
            Ts += int(lineSplit[3])
        # slaveLine = slaveF.readline()

print("Total line:", lineCnt)
averageTJ_nano = Tj/lineCnt
averageTJ_ms = averageTJ_nano/1000000
averageTS_nano = Ts/lineCnt
averageTS_ms = averageTS_nano/1000000
print("Average TJ (ms):", averageTJ_ms)
print("Average TS (ms):", averageTS_ms)
