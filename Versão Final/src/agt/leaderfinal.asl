// leader agent for team scoring

{ include("$jacamoJar/templates/common-cartago.asl") }

winning(none,0).
team_score(red,0).
team_score(blue,0).
team_score(green,0).

+dropped(Value)[source(miner1)] <- !add_team_score(red,Value).
+dropped(Value)[source(miner2)] <- !add_team_score(red,Value).
+dropped(Value)[source(miner3)] <- !add_team_score(red,Value).

+dropped(Value)[source(miner4)] <- !add_team_score(blue,Value).
+dropped(Value)[source(miner5)] <- !add_team_score(blue,Value).
+dropped(Value)[source(miner6)] <- !add_team_score(blue,Value).

+dropped(Value)[source(miner7)] <- !add_team_score(green,Value).
+dropped(Value)[source(miner8)] <- !add_team_score(green,Value).
+dropped(Value)[source(miner9)] <- !add_team_score(green,Value).

+!add_team_score(Team,Value)
  :  team_score(Team,S) & winning(_,Best) & S+Value>Best
  <- -team_score(Team,S);
     +team_score(Team,S+Value);
     -+winning(Team,S+Value);
     .broadcast(tell, winning(Team,S+Value));
     .print("Team ",Team," is now leading with ",S+Value," points.").

+!add_team_score(Team,Value)
  :  team_score(Team,S)
  <- -team_score(Team,S);
     +team_score(Team,S+Value);
     .print("Team ",Team," scored ",Value," points; total is ",S+Value).

+time_left(0)
   <- !announce_time_over.

+time_left(T) : T > 0
   <- !announce_time_left(T);
      .print("Time left: ", T, " seconds").

+time_over
   <- !announce_time_over.

+!announce_time_left(T)
   <- .broadcast(tell, time_left(T)).

+!announce_time_over
   <- .broadcast(tell, time_over);
      .print("Time is over!");
      !announce_final_score.

+!announce_final_score
  :  final_score_announced
  <- true.

+!announce_final_score
  :  team_score(red,R) & team_score(blue,B) & team_score(green,G) & R > B & R > G
  <- +final_score_announced;
     .print("Final score -> red: ",R,", blue: ",B,", green: ",G);
     .print("Winner: red team with ",R," points.").

+!announce_final_score
  :  team_score(red,R) & team_score(blue,B) & team_score(green,G) & B > R & B > G
  <- +final_score_announced;
     .print("Final score -> red: ",R,", blue: ",B,", green: ",G);
     .print("Winner: blue team with ",B," points.").

+!announce_final_score
  :  team_score(red,R) & team_score(blue,B) & team_score(green,G) & G > R & G > B
  <- +final_score_announced;
     .print("Final score -> red: ",R,", blue: ",B,", green: ",G);
     .print("Winner: green team with ",G," points.").

+!announce_final_score
  :  team_score(red,R) & team_score(blue,B) & team_score(green,G)
  <- +final_score_announced;
     .print("Final score -> red: ",R,", blue: ",B,", green: ",G);
     .print("The match ended in a tie.").
