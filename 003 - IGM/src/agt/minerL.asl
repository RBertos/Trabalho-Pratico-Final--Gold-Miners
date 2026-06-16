// miner agent

{ include("$jacamoJar/templates/common-cartago.asl") }

/*
 * By Joao Leite
 * Based on implementation developed by Rafael Bordini, Jomi Hubner and Maicon Zatelli
 */

/* beliefs */
last_dir(null). // the last movement I did
free.
score(0).

/* rules */
/* this agent program doesn't have any rules */


/* When free, agents wonder around. This is encoded with a plan that executes
 * when agents become free (which happens initially because of the belief "free"
 * above, but can also happen during the execution of the agent (as we will see below).
 *
 * The plan simply gets two random numbers within the scope of the size of the grid
 * (using an internal action jia.random), and then calls the subgoal go_near. Once the
 * agent is near the desired position, if free, it deletes and adds the atom free to
 * its belief base, which will trigger the plan to go to a random location again.
 */

+free : not time_over & gsize(_,W,H) & jia.random(RX,W-1) & jia.random(RY,H-1)
   <-  .print("I chose to go near ", RX, " ", RY, " Coordinates");
       !go_near(RX,RY).
+free : not time_over  // gsize is unknown yet
   <- .wait(100); -+free.

/* When the agent comes to believe it is near the location and it is still free,
 * it updates the atom "free" so that it can trigger the plan to go to a random
 * location again.
 */
+near(X,Y) : free & not time_over <- -+free.



/* The following plans encode how an agent should go to near a location X,Y.
 * Since the location might not be reachable, the plans succeed
 * if the agent is near the location, given by the internal action jia.neighbour,
 * or if the last action was skip, which happens when the destination is not
 * reachable, given by the plan next_step as the result of the call to the
 * internal action jia.get_direction.
 * These plans are only used when exploring the grid, since reaching the
 * exact location is not really important.
 */

+!go_near(X,Y) : free
  <- -near(_,_);
     -last_dir(_);
     !near(X,Y).


+!near(X,Y) : (pos(AgX,AgY) & jia.neighbour(AgX,AgY,X,Y))
   <- .print("I am at ", "(",AgX,",", AgY,")", " which is near (",X,",", Y,")");
      +near(X,Y).

+!near(X,Y) : pos(AgX,AgY) & last_dir(skip)
   <- .print("I am at ", "(",AgX,",", AgY,")", " and I can't get to' (",X,",", Y,")");
      +near(X,Y).

+!near(X,Y) : not near(X,Y)
   <- !next_step(X,Y);
      !near(X,Y).
+!near(X,Y) : true
   <- !near(X,Y).


/* These are the plans to have the agent execute one step in the direction of X,Y.
 * They are used by the plans go_near above and pos below. It uses the internal
 * action jia.get_direction which encodes a search algorithm.
 */

+!next_step(X,Y) : pos(AgX,AgY) // I already know my position
   <- jia.get_direction(AgX, AgY, X, Y, D);
      -+last_dir(D);
      D.
+!next_step(X,Y) : not pos(_,_) // I still do not know my position
   <- !next_step(X,Y).
-!next_step(X,Y) : true  // failure handling -> start again!
   <- -+last_dir(null);
      !next_step(X,Y).


/* The following plans encode how an agent should go to an exact position X,Y.
 * Unlike the plans to go near a position, this one assumes that the
 * position is reachable. If the position is not reachable, it will loop forever.
 */

+!pos(X,Y) : pos(X,Y)
   <- .print("I've reached ",X,"x",Y).
+!pos(X,Y) : not pos(X,Y)
   <- !next_step(X,Y);
      !pos(X,Y).



/* Ore-searching Plans */

/* The following plan encodes how an agent should deal with a newly found ore,
 * when it is not carrying ore and it is free.
 * The first step changes the belief so that the agent no longer believes it is free.
 * Then it adds the belief that there is ore in position X,Y, and
 * prints a message. Finally, it calls a plan to handle that ore.
 */

// perceived ores are included as self beliefs (to not be removed once not seen anymore)
+cell(X,Y,Type,Value) <- +ore(Type,X,Y,Value).

@pcell[atomic]           // atomic: so as not to handle another event until handle ore is initialised
+ore(Type,X,Y,Value)
  :  not time_over & not carrying_gold & free
   <- -free;
      .print("Ore perceived: ",ore(Type,X,Y,Value));
      !init_handle(ore(Type,X,Y,Value)).

// if I see ore and I'm not free but also not carrying ore yet
// (I'm probably going towards one), abort handle(ore) and pick up
// this one which is nearer
@pcell2[atomic]
+ore(Type,X,Y,Value)
  :  not time_over & not carrying_gold & not free &
     .desire(handle(ore(OldType,OldX,OldY,OldValue))) &   // I desire to handle another ore which
     pos(AgX,AgY) &
     jia.dist(X,   Y,   AgX,AgY,DNewG) &
     jia.dist(OldX,OldY,AgX,AgY,DOldG) &
     DNewG < DOldG                        // is farther than the one just perceived
  <- .drop_desire(handle(ore(OldType,OldX,OldY,OldValue)));
     .print("Giving up current ore ",ore(OldType,OldX,OldY,OldValue)," to handle ",ore(Type,X,Y,Value)," which I am seeing!");
     !init_handle(ore(Type,X,Y,Value)).


/* The next plans encode how to handle an ore.
 * The first one drops the desire to be near some location,
 * which could be true if the agent was just randomly moving around looking for ore.
 * The second one simply calls the goal to handle the ore.
 * The third plan is the one that actually results in dealing with the ore.
 * It raises the goal to go to position X,Y, then the goal to pickup the ore,
 * then to go to the position of the depot, and then to drop the ore and remove
 * the belief that there is ore in the original position.
 * Finally, it prints a message and raises a goal to choose another ore.
 * The remaining two plans handle failure.
 */

@pih1[atomic]
+!init_handle(Ore)
  :  .desire(near(_,_))
  <- .print("Dropping near(_,_) desires and intentions to handle ",Ore);
     .drop_desire(near(_,_));
     !init_handle(Ore).
@pih2[atomic]
+!init_handle(Ore)
  :  pos(X,Y)
  <- .print("Going for ",Ore);
     !!handle(Ore). // must use !! to perform "handle" as not atomic

+!handle(ore(Type,X,Y,Value))
  :  not free
  <- .print("Handling ",ore(Type,X,Y,Value)," now.");
     !pos(X,Y);
     !ensure(pick,ore(Type,X,Y,Value));
     ?depot(_,DX,DY);
     !pos(DX,DY);
     !ensure(drop, 0);
     ?score(Z);
     -+score(Z+Value);
     .send(leader, tell, dropped(Value));
     .print("Finish handling ",ore(Type,X,Y,Value));
     !!choose_ore.

// if ensure(pick/drop) failed, pursue another ore
-!handle(G) : G
  <- .print("failed to catch ore ",G);
     .abolish(G); // ignore source
     !!choose_ore.
-!handle(G) : true
  <- .print("failed to handle ",G,", it isn't in the BB anyway");
     !!choose_ore.

/* The next plans deal with picking up and dropping ore. */

+!ensure(pick,_) : pos(X,Y) & ore(Type,X,Y,Value)
  <- pick;
     ?carrying_gold;
     -ore(Type,X,Y,Value).
// fail if no ore there or not carrying_gold after pick!
// handle(G) will "catch" this failure.

+!ensure(drop, _) : carrying_gold & pos(X,Y) & depot(_,X,Y)
  <-  drop.

/* The next plans encode how the agent can choose the next ore
 * to pursue (the closest one to its current position) or,
 * if there is no known ore location, makes the agent believe it is free.
 */
+!choose_ore
  :  not ore(_,_,_,_)
  <- -+free.

// Finished one ore, but others left
// find the closest ore among the known options,
+!choose_ore
  :  ore(_,_,_,_)
  <- .findall(ore(Type,X,Y,Value),ore(Type,X,Y,Value),LG);
     !calc_ore_distance(LG,LD);
     .length(LD,LLD); LLD > 0;
     .print("Ore distances: ",LD,LLD);
     .min(LD,d(_,NewG));
     .print("Next ore is ",NewG);
     !!handle(NewG).
-!choose_ore <- -+free.

+!calc_ore_distance([],[]).
+!calc_ore_distance([ore(Type,GX,GY,Value)|R],[d(D,ore(Type,GX,GY,Value))|RD])
  :  pos(IX,IY)
  <- jia.dist(IX,IY,GX,GY,D);
     !calc_ore_distance(R,RD).
+!calc_ore_distance([_|R],RD)
  <- !calc_ore_distance(R,RD).


+winning(A,S)[source(leader)] : .my_name(A)
  <-  -winning(A,S);
      .print("HAHAHA IM WINNING!").

+winning(A,S)[source(leader)] : true
  <-  -winning(A,S).

+time_left(T)[source(leader)]
  <- -time_left(_);
     +time_left(T);
     .print("Leader says time left is ", T, " seconds").

+time_over[source(leader)]
  <- .drop_all_desires;
     +time_over;
     .abolish(ore(_,_,_,_));
     .abolish(time_left(_));
     .abolish(free);
     .print("Stopping: time is over.").

/* end of a simulation */

+end_of_simulation(S,_) : true
  <- .drop_all_desires;
     .abolish(ore(_,_,_,_));
     .abolish(picked(_));
     -+free;
     .print("-- END ",S," --").
