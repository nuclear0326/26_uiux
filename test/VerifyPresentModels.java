import timeescape.puzzle.*;import timeescape.puzzle.present.*;import java.util.*;
public class VerifyPresentModels {
 static int checks;static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 static void compare(LegacyPresentPuzzleModel old,PresentPuzzle model){
  check(old.id.equals(model.id())&&old.size()==model.size()&&(old.id.equals("PZ004")?model.title().equals("전원 켜기 (임시)"):old.title().equals(model.title()))&&old.instruction().equals(model.instruction())&&old.solved()==model.solved(),"metadata/result "+old.id);
  for(int i=0;i<old.size();i++)check(old.value(i)==model.value(i)&&old.label(i).equals(model.label(i)),"control "+old.id+":"+i);
  if(model instanceof PowerDistributionPuzzle p)check(old.power()==p.power()&&model.status().equals("출력 "+old.power()+" / 16  ·  필요한 회로만 연결하세요."),"power/status");
 }
 public static void main(String[] args){String[] ids={"PZ004","PZ010","PZ011"};int states=0;
 for(String id:ids){int mod=id.equals("PZ004")?2:id.equals("PZ006")?10:4;PresentPuzzle fresh=PresentPuzzles.create(id);int combinations=(int)Math.pow(mod,fresh.size());
  for(int code=0;code<combinations;code++){
   for(int next=-1;next<fresh.size();next++){var old=new LegacyPresentPuzzleModel(id);var model=PresentPuzzles.create(id);int digits=code;for(int i=0;i<model.size();i++){int clicks=digits%mod;digits/=mod;for(int j=0;j<clicks;j++){old.turn(i);model.turn(i);}}if(next>=0){old.turn(next);model.turn(next);}compare(old,model);}
   states++;
  }
  var one=PresentPuzzles.create(id);one.turn(0);compare(new LegacyPresentPuzzleModel(id),PresentPuzzles.create(id));check(one!=PresentPuzzles.create(id),"independent sessions");
  for(int invalid:new int[]{-1,one.size()}){int[] before=new int[one.size()];for(int i=0;i<before.length;i++)before[i]=one.value(i);try{one.turn(invalid);throw new AssertionError("invalid accepted");}catch(IllegalArgumentException expected){checks++;}for(int i=0;i<before.length;i++)check(one.value(i)==before[i],"invalid input unchanged");}
  PresentPuzzleModel adapter=new PresentPuzzleModel(id);var model=PresentPuzzles.create(id);for(int k=0;k<50;k++){int i=k%model.size();adapter.turn(i);model.turn(i);check(adapter.solved()==model.solved()&&adapter.value(i)==model.value(i)&&adapter.label(i).equals(model.label(i)),"compatibility facade");}
 }
 try{PresentPuzzles.create("missing");throw new AssertionError("unknown accepted");}catch(IllegalArgumentException expected){checks++;}
 System.out.println("PASS "+checks+" old/new model comparisons across "+states+" control combinations and each next action; no game");
 }
}
