// Expanded Gold Miners leader.
// Keeps score and announces the current winner. Gold allocation is distributed
// among miners through broadcast bids.

{ include("$jacamoJar/templates/common-cartago.asl") }

score(1,0).
score(2,0).
score(3,0).
score(4,0).
winning(0,0).

+gold_deposited(Miner,Amount)[source(A)]
  : score(Miner,S) & winning(_,Best) & S+Amount > Best
 <- -score(Miner,S);
    +score(Miner,S+Amount);
    -+winning(Miner,S+Amount);
    .print("Leader: miner",Miner," is winning with ",S+Amount," gold.");
    .broadcast(tell,winning(Miner,S+Amount)).

+gold_deposited(Miner,Amount)[source(A)]
  : score(Miner,S)
 <- -score(Miner,S);
    +score(Miner,S+Amount);
    .print("Leader: miner",Miner," deposited ",Amount," gold; total ",S+Amount,".").

+mission_claimed(X,Y,Miner)[source(A)]
  : true
 <- .print("Leader: miner",Miner," claimed gold at (",X,",",Y,").").

+mission_cancelled(X,Y,Reason)[source(A)]
  : true
 <- .print("Leader: mission for gold at (",X,",",Y,") cancelled: ",Reason,".").
