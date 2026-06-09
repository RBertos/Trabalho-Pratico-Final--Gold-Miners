// Expanded Gold Miners miner.
// All miners share this program; specialization emerges from equipment assigned
// in the Java environment.

//Comando para rodar:
// c:\Users\Bertos\Desktop\Mestrado\Sistemas Multi Agentes\git\Trabalho-Pratico-Final--Gold-Miners\003 - IGM" .\gradlew.bat -p "..\004 - GMExpanded" run

{ include("$jacamoJar/templates/common-cartago.asl") }

last_dir(null).
free.
score(0).
!heartbeat.

/* Exploration */

+free : gsize(_,W,H) & jia_ext.random(RX,W-1) & jia_ext.random(RY,H-1)
 <- .print("Exploring near (",RX,",",RY,").");
    !go_near(RX,RY).

+free
 <- .wait(100);
    -+free.

+near(X,Y) : free
 <- -+free.

+!go_near(X,Y) : free
 <- -near(_,_); -last_dir(_); !near(X,Y).

+!near(X,Y) : pos(AgX,AgY) & jia_ext.neighbour(AgX,AgY,X,Y)
 <- +near(X,Y).

+!near(X,Y) : last_dir(skip)
 <- +near(X,Y).

+!near(X,Y) : free & not near(X,Y)
 <- !next_step(X,Y);
    !near(X,Y).

+!near(X,Y) : free & near(X,Y)
 <- true.

+!near(X,Y) : not free
 <- true.

+!next_step(X,Y) : free & pos(AgX,AgY)
 <- jia_ext.get_direction(AgX,AgY,X,Y,D);
    -+last_dir(D);
    !step_or_stop(D).

+!next_step(X,Y) : not free
 <- true.

+!step_or_stop(skip)
 <- skip.

+!step_or_stop(D)
 <- D.

/* Gold discovery and distributed allocation */

@discover_gold[atomic]
+cell(X,Y,gold)
  : not known_gold(X,Y)
 <- +known_gold(X,Y);
    .print("Gold perceived at (",X,",",Y,").");
    !announce_gold(X,Y);
    !bid_for_gold(X,Y).

+gold_found(X,Y,Finder)[source(A)]
  : not known_gold(X,Y)
 <- +known_gold(X,Y);
    !bid_for_gold(X,Y).

+gold_found(X,Y,Finder)[source(A)]
  : known_gold(X,Y) & not sent_bid(X,Y)
 <- !bid_for_gold(X,Y).

+!announce_gold(X,Y)
  : jia_ext.miner_id(Me)
 <- comm_tick("gold_found");
    .broadcast(tell,gold_found(X,Y,Me)).

+!bid_for_gold(X,Y)
  : not sent_bid(X,Y) & pos(AgX,AgY) & free
 <- jia_ext.miner_id(Me);
    jia_ext.dist(AgX,AgY,X,Y,D);
    +sent_bid(X,Y);
    +bid_for(X,Y,Me,D);
    comm_tick("gold_bid");
    .broadcast(tell,gold_bid(X,Y,Me,D)).

+!bid_for_gold(X,Y)
  : not sent_bid(X,Y) & pos(AgX,AgY)
 <- jia_ext.miner_id(Me);
    jia_ext.dist(AgX,AgY,X,Y,_RealDistance);
    +sent_bid(X,Y);
    +bid_for(X,Y,Me,10000);
    comm_tick("gold_bid");
    .broadcast(tell,gold_bid(X,Y,Me,10000)).

+!bid_for_gold(X,Y)
  : sent_bid(X,Y)
 <- true.

+!bid_for_gold(X,Y)
  : true
 <- .wait(50);
    !bid_for_gold(X,Y).

+gold_bid(X,Y,Miner,D)[source(A)]
  : not bid_for(X,Y,Miner,_)
 <- +bid_for(X,Y,Miner,D);
    !try_claim(X,Y).

+gold_bid(X,Y,Miner,D)[source(A)]
  : true
 <- !try_claim(X,Y).

+bid_for(X,Y,Miner,D)
  : true
 <- !try_claim(X,Y).

+!try_claim(X,Y)
  : bid_for(X,Y,1,D1) & bid_for(X,Y,2,D2) & bid_for(X,Y,3,D3) & bid_for(X,Y,4,D4) & not claimed(X,Y)
 <- jia_ext.best_bid(D1,D2,D3,D4,Winner);
    jia_ext.miner_id(Me);
    !claim_if_winner(X,Y,Winner,Me).

+!try_claim(X,Y)
  : true
 <- true.

@claim_win[atomic]
+!claim_if_winner(X,Y,Me,Me)
  : not claimed(X,Y) & not claiming(X,Y) & free & not engaged
 <- +claiming(X,Y);
    +engaged;
    +claimed(X,Y);
    -free;
    mission("gold");
    comm_tick("mission_claimed");
    .broadcast(tell,mission_claimed(X,Y,Me));
    .print("I am going to gold at (",X,",",Y,").");
    !handle(gold(X,Y)).

+!claim_if_winner(X,Y,Winner,Me)
  : true
 <- true.

+mission_claimed(X,Y,Winner)[source(A)]
  : jia_ext.miner_id(Me) & Winner \== Me
 <- -claimed(X,Y);
    +claimed(X,Y);
    .abolish(bid_for(X,Y,_,_));
    -sent_bid(X,Y);
    .drop_desire(handle(gold(X,Y)));
    .print("Giving up gold at (",X,",",Y,"); miner",Winner," is closer.").

+mission_claimed(X,Y,Winner)[source(A)]
  : jia_ext.miner_id(Winner)
 <- +claimed(X,Y).

+mission_cancelled(X,Y,Reason)[source(A)]
  : claimed(X,Y) | claiming(X,Y)
 <- -claimed(X,Y);
    -claiming(X,Y);
    -engaged;
    -known_gold(X,Y);
    -sent_bid(X,Y);
    .abolish(bid_for(X,Y,_,_)).

+mission_cancelled(X,Y,Reason)[source(A)]
  : true
 <- -known_gold(X,Y);
    -sent_bid(X,Y);
    .abolish(bid_for(X,Y,_,_)).

/* Handling gold */

+!handle(gold(X,Y))
  : true
 <- !pos(X,Y);
    !pick_if_at_target(X,Y).

+!pick_if_at_target(X,Y)
  : pos(X,Y)
 <- pick;
    !after_pick(X,Y).

+!pick_if_at_target(X,Y)
  : not pos(X,Y)
 <- .fail.

+!after_pick(X,Y)
  : carrying_gold
 <- comm_tick("gold_picked");
    .broadcast(tell,gold_picked(X,Y));
    ?depot(_,DX,DY);
    !pos(DX,DY);
    ?cargo(C,_Cap);
    drop;
    ?score(S);
    -+score(S+C);
    comm_tick("gold_deposited");
    jia_ext.miner_id(Me);
    .broadcast(tell,gold_deposited(Me,C));
    .print("Delivered ",C," gold. Local score: ",S+C,".");
    -known_gold(X,Y);
    -engaged;
    -claiming(X,Y);
    -claimed(X,Y);
    -sent_bid(X,Y);
    .abolish(bid_for(X,Y,_,_));
    mission("none");
    !choose_gold.

+!after_pick(X,Y)
  : not carrying_gold
 <- comm_tick("mission_cancelled");
    .broadcast(tell,mission_cancelled(X,Y,no_gold));
    -known_gold(X,Y);
    -engaged;
    -claiming(X,Y);
    -claimed(X,Y);
    -sent_bid(X,Y);
    .abolish(bid_for(X,Y,_,_));
    mission("none");
    !choose_gold.

-!handle(gold(X,Y))
  : true
 <- comm_tick("mission_cancelled");
    .broadcast(tell,mission_cancelled(X,Y,failed));
    -known_gold(X,Y);
    -engaged;
    -claiming(X,Y);
    -claimed(X,Y);
    -sent_bid(X,Y);
    .abolish(bid_for(X,Y,_,_));
    mission("none");
    !choose_gold.

+gold_picked(X,Y)[source(A)]
  : true
 <- -known_gold(X,Y);
    -claiming(X,Y);
    -claimed(X,Y);
    -sent_bid(X,Y);
    .abolish(bid_for(X,Y,_,_)).

+!choose_gold
  : known_gold(X,Y) & not claimed(X,Y)
 <- !bid_for_gold(X,Y).

+!choose_gold
  : not engaged
 <- -engaged;
    -+free.

+!choose_gold
  : engaged
 <- true.

+!heartbeat
  : not engaged
 <- .wait(500);
    skip;
    !choose_gold;
    !heartbeat.

+!heartbeat
  : engaged
 <- .wait(500);
    !heartbeat.

+!reach_base_zone(DX,DY)
  : pos(AgX,AgY) & jia_ext.dist(AgX,AgY,DX,DY,D) & not D > 2
 <- true.

+!reach_base_zone(DX,DY)
  : pos(AgX,AgY)
 <- jia_ext.get_direction(AgX,AgY,DX,DY,Step);
    -+last_dir(Step);
    !step_or_stop(Step);
    !reach_base_zone(DX,DY).

/* Movement */

+!pos(X,Y)
  : pos(X,Y)
 <- true.

+!pos(X,Y)
  : pos(AgX,AgY)
 <- jia_ext.get_direction(AgX,AgY,X,Y,D);
    -+last_dir(D);
    !move_or_fail(D,X,Y).

+!move_or_fail(skip,X,Y)
 <- skip;
    ?pos(X,Y).

+!move_or_fail(D,X,Y)
 <- D;
    !pos(X,Y).

/* Communication from leader */

+winning(Me,S)[source(leader)]
  : jia_ext.miner_id(Me)
 <- -winning(Me,S);
    .print("I am currently winning with ",S," gold.").

+winning(A,S)[source(leader)]
  : true
 <- -winning(A,S).

/* End of simulation */

+end_of_simulation(S,_)
  : true
 <- .drop_all_desires;
    .abolish(known_gold(_,_));
    .abolish(bid_for(_,_,_,_));
    -+free;
    .print("-- END ",S," --").
