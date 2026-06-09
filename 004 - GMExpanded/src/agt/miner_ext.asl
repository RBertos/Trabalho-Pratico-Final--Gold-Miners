// Expanded Gold Miners miner.
// All miners share this program; specialization emerges from equipment assigned
// in the Java environment.

// Run from terminal:
// Set-Location "c:/Users/Bertos/Desktop/Mestrado/Sistemas Multi Agentes/git/Trabalho-Pratico-Final--Gold-Miners/003 - IGM"; ./gradlew.bat -p "../004 - GMExpanded" run

{ include("$jacamoJar/templates/common-cartago.asl") }

last_dir(null).
free.
score(0).
!heartbeat.

cargo_space :- cargo(C,Cap) & C < Cap.
is_collector :- equipped(carrinho).
is_collector :- equipped(mochila).
is_explorer :- equipped(lanterna) & not is_collector.

/* Exploration */

+free : gsize(_,W,H) & jia_ext.random(RX,W) & jia_ext.random(RY,H)
 <- !go_near(RX,RY).

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

@pick_gold_here[atomic]
+cell(X,Y,gold)
  : pos(X,Y) & cargo_space & free & not engaged
 <- jia_ext.miner_id(Me);
   .print("miner",Me,": gold under me at (",X,",",Y,") - picking now.");
    !pick_here(X,Y).

@discover_gold[atomic]
+cell(X,Y,gold)
  : not known_gold(X,Y) & not pos(X,Y)
 <- jia_ext.miner_id(Me);
   +known_gold(X,Y);
  .print("miner",Me,": gold perceived at (",X,",",Y,").");
  !prepare_bids(X,Y);
  !announce_gold(X,Y);
  !bid_for_gold(X,Y);
  !resolve_claim(X,Y).

+gold_found(X,Y,Finder)[source(A)]
  : not known_gold(X,Y)
 <- +known_gold(X,Y);
    !prepare_bids(X,Y);
    !bid_for_gold(X,Y);
    !resolve_claim(X,Y).

+gold_found(X,Y,Finder)[source(A)]
  : known_gold(X,Y) & not sent_bid(X,Y)
 <- !prepare_bids(X,Y);
    !bid_for_gold(X,Y);
    !resolve_claim(X,Y).

+!announce_gold(X,Y)
  : jia_ext.miner_id(Me)
 <- comm_tick("gold_found");
    .broadcast(tell,gold_found(X,Y,Me)).

+!prepare_bids(X,Y)
  : not bids_ready(X,Y)
 <- +bids_ready(X,Y);
    .abolish(bid_for(X,Y,_,_));
    +bid_for(X,Y,1,10000);
    +bid_for(X,Y,2,10000);
    +bid_for(X,Y,3,10000);
    +bid_for(X,Y,4,10000).

+!prepare_bids(X,Y)
  : bids_ready(X,Y)
 <- true.

+!resolve_claim(X,Y)
  : bid_for(X,Y,1,_) & bid_for(X,Y,2,_) & bid_for(X,Y,3,_) & bid_for(X,Y,4,_) & not claimed(X,Y)
 <- .wait(80);
    !try_claim(X,Y).

+!resolve_claim(X,Y)
  : true
 <- true.

+!bid_for_gold(X,Y)
  : is_collector & not sent_bid(X,Y) & pos(AgX,AgY) & free
 <- jia_ext.miner_id(Me);
    jia_ext.dist(AgX,AgY,X,Y,D);
    !set_bid(X,Y,Me,D);
    +sent_bid(X,Y);
    comm_tick("gold_bid");
    .broadcast(tell,gold_bid(X,Y,Me,D)).

+!bid_for_gold(X,Y)
  : not is_collector & not sent_bid(X,Y)
 <- +sent_bid(X,Y).

+!bid_for_gold(X,Y)
  : sent_bid(X,Y)
 <- true.

+!bid_for_gold(X,Y)
  : true
 <- true.

+!set_bid(X,Y,Miner,D)
  : bid_for(X,Y,Miner,Old)
 <- -bid_for(X,Y,Miner,Old);
    +bid_for(X,Y,Miner,D).

+!set_bid(X,Y,Miner,D)
  : not bid_for(X,Y,Miner,_)
 <- +bid_for(X,Y,Miner,D).

+gold_bid(X,Y,Miner,D)[source(A)]
  : true
 <- !set_bid(X,Y,Miner,D);
    !resolve_claim(X,Y).

+bid_for(X,Y,Miner,D)
  : true
 <- true.

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
  : is_collector & not claimed(X,Y) & not claiming(X,Y) & free & not engaged
 <- +claiming(X,Y);
    .abolish(target(_,_));
    +target(X,Y);
    +engaged;
    +claimed(X,Y);
    -free;
    mission("gold");
    comm_tick("mission_claimed");
    .broadcast(tell,mission_claimed(X,Y,Me));
    .print("miner",Me,": going to gold at (",X,",",Y,").");
    !handle(gold(X,Y)).

+!claim_if_winner(X,Y,Winner,Me)
  : true
 <- true.

+mission_claimed(X,Y,Winner)[source(A)]
  : jia_ext.miner_id(Me) & Winner \== Me & target(X,Y)
 <- -claiming(X,Y);
    .abolish(target(_,_));
    -engaged;
    -+free;
    mission("none");
    -claimed(X,Y);
    +claimed(X,Y);
    .abolish(bid_for(X,Y,_,_));
    -bids_ready(X,Y);
    -sent_bid(X,Y);
    .drop_desire(handle(gold(X,Y)));
    .print("miner",Me,": giving up gold at (",X,",",Y,"); miner",Winner," is closer.");
    !choose_gold.

+mission_claimed(X,Y,Winner)[source(A)]
  : jia_ext.miner_id(Me) & Winner \== Me
 <- .abolish(bid_for(X,Y,_,_));
   -bids_ready(X,Y);
   -sent_bid(X,Y).

+mission_claimed(X,Y,Winner)[source(A)]
  : jia_ext.miner_id(Winner)
 <- +claimed(X,Y).

+mission_cancelled(X,Y,Reason)[source(A)]
  : claimed(X,Y) | claiming(X,Y)
 <- -claimed(X,Y);
    -claiming(X,Y);
    .abolish(target(_,_));
    -engaged;
    -known_gold(X,Y);
    -sent_bid(X,Y);
    -bids_ready(X,Y);
    .abolish(bid_for(X,Y,_,_)).

+mission_cancelled(X,Y,Reason)[source(A)]
  : true
 <- -known_gold(X,Y);
    -sent_bid(X,Y);
   -bids_ready(X,Y);
    .abolish(bid_for(X,Y,_,_)).

/* Handling gold */

+!pick_here(X,Y)
  : cargo_space
 <- -free;
    +engaged;
    .abolish(target(_,_));
    +target(X,Y);
    +claiming(X,Y);
    +known_gold(X,Y);
    +claimed(X,Y);
    mission("gold");
    !pos(X,Y);
    pick;
    !after_pick(X,Y).

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
    !vacate_depot;
    ?score(S);
    -+score(S+C);
    comm_tick("gold_deposited");
    jia_ext.miner_id(Me);
    .broadcast(tell,gold_deposited(Me,C));
    .print("miner",Me,": delivered ",C," gold. Local score: ",S+C,".");
    .abolish(target(_,_));
    -known_gold(X,Y);
    -engaged;
    -claiming(X,Y);
    -claimed(X,Y);
    -sent_bid(X,Y);
    -bids_ready(X,Y);
    .abolish(bid_for(X,Y,_,_));
    mission("none");
    !choose_gold.

+!after_pick(X,Y)
  : not carrying_gold
 <- comm_tick("mission_cancelled");
    .broadcast(tell,mission_cancelled(X,Y,no_gold));
    .abolish(target(_,_));
    -known_gold(X,Y);
    -engaged;
    -claiming(X,Y);
    -claimed(X,Y);
    -sent_bid(X,Y);
    -bids_ready(X,Y);
    .abolish(bid_for(X,Y,_,_));
    mission("none");
    !choose_gold.

-!handle(gold(X,Y))
  : true
 <- comm_tick("mission_cancelled");
    .broadcast(tell,mission_cancelled(X,Y,failed));
    .abolish(target(_,_));
    -known_gold(X,Y);
    -engaged;
    -claiming(X,Y);
    -claimed(X,Y);
    -sent_bid(X,Y);
    -bids_ready(X,Y);
    .abolish(bid_for(X,Y,_,_));
    mission("none");
    !choose_gold.

+gold_picked(X,Y)[source(A)]
  : target(X,Y) & not carrying_gold
 <- .abolish(target(_,_));
    -engaged;
    -+free.

+gold_picked(X,Y)[source(A)]
  : not carrying_gold
 <- -known_gold(X,Y);
    -claiming(X,Y);
    -claimed(X,Y);
    -sent_bid(X,Y);
   -bids_ready(X,Y);
    .abolish(bid_for(X,Y,_,_)).

+!choose_gold
  : false
 <- true.

+!choose_gold
  : not engaged & not carrying_gold
 <- -engaged;
    -+free.

+!choose_gold
  : engaged
 <- true.

+!choose_gold
  : carrying_gold
 <- true.

+!vacate_depot
  : pos(AgX,AgY) & depot(_,DX,DY) & AgX == DX & AgY == DY
 <- right;
    !vacate_depot_left;
    !vacate_depot_up;
    !vacate_depot_down.

+!vacate_depot_left
  : pos(AgX,AgY) & depot(_,DX,DY) & AgX == DX & AgY == DY
 <- left.

+!vacate_depot_left
  : true
 <- true.

+!vacate_depot_up
  : pos(AgX,AgY) & depot(_,DX,DY) & AgX == DX & AgY == DY
 <- up.

+!vacate_depot_up
  : true
 <- true.

+!vacate_depot_down
  : pos(AgX,AgY) & depot(_,DX,DY) & AgX == DX & AgY == DY
 <- down.

+!vacate_depot_down
  : true
 <- true.

+!vacate_depot
  : true
 <- true.

+!heartbeat
  : not engaged
 <- .wait(500);
    !choose_gold;
    !heartbeat.

+!heartbeat
  : engaged & not carrying_gold & not .desire(handle(gold(_,_)))
 <- .abolish(claiming(_,_));
    -engaged;
    mission("none");
    -+free;
    !choose_gold;
    .wait(200);
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
 <- !pos_retry(X,Y,AgX,AgY,0).

+!pos_retry(X,Y,CurX,CurY,Blocked)
  : pos(X,Y)
 <- true.

+!pos_retry(X,Y,CurX,CurY,2)
  : not pos(X,Y)
 <- !recover_blocked(X,Y);
    .fail.

+!pos_retry(X,Y,CurX,CurY,Blocked)
  : pos(AgX,AgY)
 <- jia_ext.get_direction(AgX,AgY,X,Y,D);
    -+last_dir(D);
    !step_or_stop(D);
    !after_step(X,Y,AgX,AgY,Blocked).

+!after_step(X,Y,OldX,OldY,Blocked)
  : pos(NewX,NewY) & NewX \== OldX
 <- !pos_retry(X,Y,NewX,NewY,0).

+!after_step(X,Y,OldX,OldY,Blocked)
  : pos(NewX,NewY) & NewX == OldX & NewY \== OldY
 <- !pos_retry(X,Y,NewX,NewY,0).

+!after_step(X,Y,OldX,OldY,0)
  : pos(OldX,OldY)
 <- !escape_step;
    !pos_retry(X,Y,OldX,OldY,1).

+!after_step(X,Y,OldX,OldY,1)
  : pos(OldX,OldY)
 <- !escape_step;
    !pos_retry(X,Y,OldX,OldY,2).

+!after_step(X,Y,OldX,OldY,2)
  : pos(OldX,OldY)
 <- !recover_blocked(X,Y);
    .fail.

+!recover_blocked(X,Y)
  : engaged & target(X,Y)
 <- jia_ext.miner_id(Me);
    .print("miner",Me,": blocked >2 ticks toward (",X,",",Y,") - replanning.");
    comm_tick("mission_cancelled");
    .broadcast(tell,mission_cancelled(X,Y,blocked));
    .drop_desire(handle(gold(X,Y)));
    .abolish(target(_,_));
    -known_gold(X,Y);
    -claiming(X,Y);
    -claimed(X,Y);
    -sent_bid(X,Y);
    -bids_ready(X,Y);
    .abolish(bid_for(X,Y,_,_));
    -engaged;
    mission("none");
    !escape_step;
    -+free.

+!recover_blocked(X,Y)
  : true
 <- !escape_step;
    -+free.

+!escape_step
 <- jia_ext.random(R,4);
    !dir_from_random(R,D);
    !step_or_stop(D).

+!dir_from_random(0,left)
 <- true.

+!dir_from_random(1,right)
 <- true.

+!dir_from_random(2,up)
 <- true.

+!dir_from_random(3,down)
 <- true.

/* Communication from leader */

+winning(Me,S)[source(leader)]
  : jia_ext.miner_id(Me)
 <- -winning(Me,S);
    .print("miner",Me,": currently winning with ",S," gold.").

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
