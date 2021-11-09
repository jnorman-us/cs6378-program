# CS6378 Project 2
Library implementation to simulate a distributed system of nodes arranged in communicative topology. Coded in Java.

Team Members:
-------------  
Joseph Norman  
Madison King  
Alinta Wang  


Set up:  
-------  
Have Java classes in the project directory.  
Have config.txt in the project directory.  
Have launcher.sh and cleanup.sh in a 'launcher' directory.  

launcher.sh + cleanup.sh  
Change netid and location of project directory, as well as location of config file.  
May need to chmod launcher and cleanup permissions to execute.
If there is a windows/linux problem with running the .sh files, run `sed -i -e 's/\r$//' *.sh` on launcher and cleanup .sh files.


Compile:  
--------  
In the project directory, run:  
`javac *.java`


Run:  
----  
In directory of launcher.sh, run:  
`./launcher.sh`  


Cleanup:  
--------  
In directory of cleanup.sh, run:  
`./cleanup.sh`