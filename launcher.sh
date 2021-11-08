#!/bin/bash

# Change this to your netid
netid=mbk190001

# Root directory of your project
PROJDIR=/home/013/m/mb/mbk190001/6378/Proj2

# Directory where the config file is located on your local system
CONFIGLOCAL=$PROJDIR/config.txt
CONFIG=$PROJDIR/config.txt

# Directory your java classes are in
BINDIR=$PROJDIR

# Your main project class
PROG=Main

n=0

cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" | sed -e "s/\r$//" |
(
    echo "Launching project"
    read i
    echo "Total nodes:  " $i

    while read line 
    do
        if [ $n -lt $i ]
	then
	host=$( echo $line | awk '{ print $2 }' )
	
	nodeId=$( echo $line | awk '{ print $3 }' )
	echo $netid "Running on" $host "with" $nodeId $n
        
    #gnome-terminal -- "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host java -cp $BINDIR $PROG $p; exec bash" &
    ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -l "$netid" "$host" "cd ; cd $PROJDIR; java Main $n $CONFIG " &
	fi
        n=$(( n + 1 ))
    done
   
)
