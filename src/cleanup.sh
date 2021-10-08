#!/bin/bash


# Change this to your netid
netid=mbk190001

#
# Root directory of your project
PROJDIR=/home/013/m/mb/mbk190001/6378/Testing

#
# Directory where the config file is located on your local system
CONFIGLOCAL=$PROJDIR/config.txt

n=0

cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" | sed -e "s/\r$//" |

(
    read i
    echo $i
    
    while [ $n -lt $i ]
    
    do
        read line
	    host=$( echo $line | awk '{ print $2 }' )
	
	    echo $netid "Cleanup on" $host
        ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -l "$netid" "$host" "killall -u $netid " &
        sleep 1

        n=$(( n + 1 ))
    done

    
   
)

echo "Cleanup complete"