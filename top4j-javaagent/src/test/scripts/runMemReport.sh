#!/usr/bin/bash
#
# runMemReport.sh
#

HEAP_SIZE=$1

if [[ -z $HEAP_SIZE ]]
then
	HEAP_SIZE=128
fi

RUN_MEM_TEST=`dirname $0`/runMemTest.sh

# run mem test

${RUN_MEM_TEST} ${HEAP_SIZE} 2>&1 | \

egrep " Rate|GC Overhead|GC Time" | \

gawk ' {

if ($0 ~ /GC Overhead/) {

  gsub("%", "")
  total["gcOverhead"]+=$9
  count["gcOverhead"]++

}

if ($0 ~ /Memory Allocation Rate/) {

  total["memAllocRate"]+=$10
  count["memAllocRate"]++

}

if ($0 ~ /Memory Survivor Rate/) {

  total["memSurvivorRate"]+=$10
  count["memSurvivorRate"]++

}

if ($0 ~ /Memory Promotion Rate/) {

  total["memPromoRate"]+=$10
  count["memPromoRate"]++

}

if ($0 ~ /Mean Nursery GC Time/) {

  gsub("ms", "")
  total["yGCTime"]+=$11
  count["yGCTime"]++

}

if ($0 ~ /Mean Tenured GC Time/) {

  gsub("ms", "")
  total["oGCTime"]+=$11
  count["oGCTime"]++

}


}

END {

#print "Mean GC Overhead = " total["gcOverhead"]/count["gcOverhead"] "%"
#print "Mean Memory Allocation Rate = " total["memAllocRate"]/count["memAllocRate"] " MB/s"
#print "Mean Survivor Survivor Rate = " total["memSurvivorRate"]/count["memSurvivorRate"] " MB/s"
#print "Mean Survivor Promotion Rate = " total["memPromoRate"]/count["memPromoRate"] " MB/s"

meanGCOverhead=total["gcOverhead"]/count["gcOverhead"]
meanMemAllocRate=total["memAllocRate"]/count["memAllocRate"]
meanMemSurvivorRate=total["memSurvivorRate"]/count["memSurvivorRate"]
meanMemPromoRate=total["memPromoRate"]/count["memPromoRate"]
meanYGCTime=total["yGCTime"]/count["yGCTime"]
meanOGCTime=total["oGCTime"]/count["oGCTime"]

print "Mean Memory Allocation Rate, Mean Survivor Survivor Rate, Mean Survivor Promotion Rate, Mean GC Overhead, Mean Nursery GC Time, Mean Tenured GC Time"
print meanMemAllocRate "," meanMemSurvivorRate "," meanMemPromoRate "," meanGCOverhead "," meanYGCTime "," meanOGCTime

} '

exit

INFO  2014-03-14 21:41:30 Top4J JavaAgent: GC Overhead = 2.6812313803376364%
INFO  2014-03-14 21:41:30 Top4J JavaAgent: Mean Nursery GC Time = 9.0ms
INFO  2014-03-14 21:41:30 Top4J JavaAgent: Mean Tenured GC Time = 0.0ms
INFO  2014-03-14 21:41:31 Top4J JavaAgent: Memory Allocation Rate = 4544.242048216198 MB/s
INFO  2014-03-14 21:41:31 Top4J JavaAgent: Memory Survivor Rate = 14.399205561072494 MB/s
INFO  2014-03-14 21:41:31 Top4J JavaAgent: Memory Promotion Rate = 29.830281050226542 MB/s

