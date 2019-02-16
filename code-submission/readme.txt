readme.txt
-------------------------------------------------------
Contents:
1. How to run files to obtain outputs?
2. Structure of code

-------------------------------------------------------
1. Quick description
Please compile the file before running it
Command: javac -cp .:gs-core-1.3.jar:gs-core-1.3-sources.jar:gs-core-1.3-javadoc.jar Main.java

To run, use the command:

Command: java -cp .:gs-core-1.3.jar:gs-core-1.3-sources.jar:gs-core-1.3-javadoc.jar Main "<problem file>" "<backtrack/minconflict>" "<maxstepscount>" "<mrv>"

To be specific:
To run backtrack on a problem with MRV heuristic, 
Command: java -cp .:gs-core-1.3.jar:gs-core-1.3-sources.jar:gs-core-1.3-javadoc.jar Main "<problem file>" "backtrack" "" "mrv"

To run backtrack on a problem without MRV heuristic, 
Command: java -cp .:gs-core-1.3.jar:gs-core-1.3-sources.jar:gs-core-1.3-javadoc.jar Main "<problem file>" "backtrack"

To run minconflicts with 3000 max steps
Command: java -cp .:gs-core-1.3.jar:gs-core-1.3-sources.jar:gs-core-1.3-javadoc.jar Main "<problem file>" "minconflict" "3000"
-----------

Structure of code:
BackTrack:
1. File is first parsed using fileParserforBackTracking() and data is stored in a DataHolder class object.
All the variables are stored as nodes in a graph.
Nodes are connected if there's a contsraint between variables.
2. BackTrack() function is called.
3. One unassigned variable var is picked using pickFromRemaining()
4. Checked if final solution is obtained if above returns null
5. A loop is constructed where each option in domain of var is picked and checked if it's fine to use it 
6. If picked option is viable, it is added to assignment list and a recursive BackTrack call is made.
7. Solution is evaluated using solutionEvaluator()
MinConflicts:
1. File is first parsed using fileParserforMinConflicts() and data is stored in a DataHolder class object.
All the variables are stored as nodes in a graph.
Nodes are connected if there's a contsraint between variables.
2. MinConflicts() function is called.
3. Random assignment of variables is made using randomAssigner().
4. A loop is used where each variable is modified in each iteration. Variables are picked randomly using pickForMinConflicts().
5. Various options in domain are  evaluated. conflicts of each option are evaluated using conflictCounter(). Option with min conflicts is picked.
6. If there are multiple options with same number of conflicts, an option is randomly picked.
7. Solution is evaluated using solutionEvaluator().