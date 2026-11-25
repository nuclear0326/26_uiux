package timeescape.ui;
import java.lang.reflect.*;import java.util.concurrent.*;import javax.swing.*;import java.util.concurrent.atomic.AtomicInteger;
public final class VerifyMenuPreparation81 {
 static int checks;static void check(boolean ok,String text){if(!ok)throw new AssertionError(text);checks++;}
 static Object get(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
 static void set(Object o,String name,Object value)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
 static void invoke(Object o,String name,Class<?> type,Object value)throws Exception{Method m=o.getClass().getDeclaredMethod(name,type);m.setAccessible(true);m.invoke(o,value);}
 public static void main(String[] args)throws Exception{
  AtomicInteger starts=new AtomicInteger();MainMenuPanel[] holder=new MainMenuPanel[1];CompletableFuture<Void> delayed=new CompletableFuture<>();
  SwingUtilities.invokeAndWait(()->{try{
   var p=new MainMenuPanel(new MainMenuPanel.MenuActions(){public void startGame(){starts.incrementAndGet();}public void continueGame(){}public void showSettings(){}public void exitGame(){}});holder[0]=p;
   var atmosphere=get(p,"atmosphere");check(get(atmosphere,"effects")==null,"no cold startup effects loaded");check(get(atmosphere,"preparation")==null,"no worker until requested");
   set(atmosphere,"preparation",delayed);invoke(p,"activate",int.class,0);invoke(p,"activate",int.class,0);
   check((boolean)get(p,"preparingTransition"),"early click waits");check(get(p,"pending")==null&&starts.get()==0,"no animation or game before resources");
  }catch(Exception e){throw new RuntimeException(e);}});
  delayed.complete(null);
  SwingUtilities.invokeAndWait(()->{try{var p=holder[0];check(!(boolean)get(p,"preparingTransition")&&get(p,"pending")!=null,"resumes on EDT");invoke(p,"advance",double.class,MenuStartTimeline.END_AT+1);invoke(p,"advance",double.class,1.0);check(starts.get()==1,"duplicate click completes once");
   var cancelled=new CompletableFuture<Void>();set(get(p,"atmosphere"),"preparation",cancelled);invoke(p,"activate",int.class,0);p.setVisible(false);cancelled.complete(null);
  }catch(Exception e){throw new RuntimeException(e);}});
  SwingUtilities.invokeAndWait(()->{try{check(get(holder[0],"pending")==null&&!(boolean)get(holder[0],"preparingTransition"),"hidden panel cancels deferred transition");}catch(Exception e){throw new RuntimeException(e);}});
  var actual=new MenuAtmosphere(new timeescape.system.MenuImageAssets(),true);var future=actual.prepare();check(future==actual.prepare(),"effects preparation is single flight");future.get(10,TimeUnit.SECONDS);check(get(actual,"effects")!=null,"production effects ready");
  System.out.println("PASS "+checks+" deferred menu, early click, cancellation and effects checks");
 }
}
