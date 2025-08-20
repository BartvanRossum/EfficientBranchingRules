# Efficient Branching Rules for Optimizing Range and Order-Based Objective Functions
Repository containing code and instances for the paper Efficient Branching Rules for Optimizing Range and Order-Based Objective Functions by Bart van Rossum, Rui Chen, and Andrea Lodi.

# Prerequisites.
The algorithms are implemented in Java, and CPLEX 22.1.0 is used to solve all (integer) linear programs. Most scripts require a single number as command line argument to specify the set-up to be used.

# Fair Capacitated Vehicle Routing Problem.
The code used to conduct the experiments in Section 4 and Section 6 can be found in the folder [CVRP](/src/CVRP), while all instances are stored in [dataCVRP](dataCVRP). The scripts in [scripts](/src/CVRP/scripts) correspond to the following parts of the paper:
<ul>
  <li> MainTwoIndex: Section 4.3 </li>
  <li> MainRangeBranching: Section 4.4 </li>
  <li> MainRangeBranchingTSP and MainRangeTSP: Section 4.5</li>
  <li> MainGini: Section 6.4</li>
  <li> MainOrderRange: Section 6.4</li>
</ul>

# Fair Generalized Assignment Problem.
The code used to conduct the experiments in Section 5 can be found in the folder [GAP](/src/GAP), while all instances are stored in [dataGAP](dataGAP). The script MainGap is used to generate the results in Section 5.4.
