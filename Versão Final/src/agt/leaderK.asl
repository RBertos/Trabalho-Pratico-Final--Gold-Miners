// leader agent

{ include("$jacamoJar/templates/common-cartago.asl") }

/*
 * By Joao Leite
 * Based on implementation developed by Rafael Bordini, Jomi Hubner and Maicon Zatelli
 */

winning(none,0).
score(miner1,0).
score(miner2,0).
score(miner3,0).
score(miner4,0).

//the start goal only works after execise j)
//!start.
//+!start <- tweet("a new mining is starting! (posted by jason agent)").
      
+dropped(Value)[source(A)] : score(A,S) & winning(L,SL) & S+Value>SL
   <- -score(A,S);
      +score(A,S+Value);
      -dropped(Value)[source(A)];
      -+winning(A,S+Value);
      .broadcast(tell, winning(A, S+Value));
      .print("Agent ", A, " is now on the lead with the score of ", S+Value).

+dropped(Value)[source(A)] : score(A,S)
   <- -score(A,S);
      +score(A,S+Value);
      -dropped(Value)[source(A)];
      .print("Agent ",A," has dropped ore worth ",Value," points; score is ",S+Value).
