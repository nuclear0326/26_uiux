package timeescape.ui;
import timeescape.core.*;import timeescape.system.*;import java.nio.file.*;import java.util.*;import java.util.concurrent.*;import java.util.concurrent.atomic.*;import javax.swing.*;import java.awt.event.*;
public class VerifyUnread67 {
 static int checks;static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 static GameState initial(){GameState s=new GameState();s.complete("PZ004");s.collect("USB");s.collect("CASE");return s;}
 public static void main(String[] args)throws Exception{
 if(args.length>0){var s=new SaveSlots(Path.of(args[0]),ProgressCatalog.standard()).load(SaveSlots.Slot.AUTO).state();check(!s.isItemUnread("USB")&&s.isItemUnread("CASE"),"separate process restore");System.out.println("PASS child read/unread restore");return;}
 Path dir=Files.createTempDirectory("timeescape-unread67-");SaveSlots slots=new SaveSlots(dir,ProgressCatalog.standard());GameState s=initial();var first=s.snapshot();check(first.unreadItems().equals(Set.of("USB","CASE")),"acquisition records both");s.collect("USB");check(s.snapshot().equals(first),"duplicate acquisition unchanged");
 for(var slot:SaveSlots.Slot.values()){slots.store(slot).save(first);check(slots.load(slot).state().snapshot().equals(first),"round trip "+slot);}
 SaveQueue q=new SaveQueue(slots);AtomicReference<CompletableFuture<Void>> save=new AtomicReference<>();AtomicInteger callbacks=new AtomicInteger();
 SwingUtilities.invokeAndWait(()->{InventoryPanel p=new InventoryPanel(x->{});p.refresh(s,false);check(p.isUnread("USB")&&p.isUnread("CASE"),"dots restored on first render");p.setReadListener(()->{callbacks.incrementAndGet();save.set(q.saveAuto(s.snapshot(),false));});
 JButton usb=null;for(var c:p.getComponents())if(c instanceof JButton b&&"USB".equals(b.getName()))usb=b;
 if(usb==null)throw new AssertionError();usb.dispatchEvent(new MouseEvent(usb,MouseEvent.MOUSE_ENTERED,0,0,5,5,0,false));p.acknowledge("USB");check(callbacks.get()==1&&!p.isUnread("USB")&&p.isUnread("CASE"),"one hover autosave and independent dots");p.refresh(s,false);check(!p.isUnread("USB"),"refresh keeps read");});
 save.get().get(5,TimeUnit.SECONDS);check(!slots.load(SaveSlots.Slot.AUTO).state().isItemUnread("USB"),"auto persists read");check(slots.load(SaveSlots.Slot.ONE).state().isItemUnread("USB"),"manual snapshot independent");
 q.saveManual(SaveSlots.Slot.ONE,s.snapshot()).get(5,TimeUnit.SECONDS);check(!q.load(SaveSlots.Slot.ONE).get(5,TimeUnit.SECONDS).state().isItemUnread("USB"),"manual updated state");check(first.unreadItems().contains("USB"),"snapshot detached");q.close();
 String java=Path.of(System.getProperty("java.home"),"bin","java.exe").toString();Process child=new ProcessBuilder(java,"-Djava.awt.headless=true","-cp",System.getProperty("java.class.path"),VerifyUnread67.class.getName(),dir.toString()).inheritIO().start();check(child.waitFor()==0,"fresh process");
 var legacy=new GameState.Snapshot(1,first.roomId(),first.completed(),first.items(),first.flags(),first.unlockedRooms());SaveStore old=new SaveStore(dir.resolve("legacy"),ProgressCatalog.standard());old.save(legacy);var migrated=old.load().orElseThrow().state();check(migrated.has("USB")&&!migrated.isItemUnread("USB"),"schema1 compatible defaults read");old.save(migrated.snapshot());check(Files.readString(old.file()).contains("schemaVersion=2"),"schema2 rewrite");
 SaveStore recovery=new SaveStore(dir.resolve("recovery"),ProgressCatalog.standard());recovery.save(s.snapshot());recovery.save(s.snapshot());Files.writeString(recovery.file(),Files.readString(recovery.file()).replace("unreadItems=CASE","unreadItems=USB"));var recovered=recovery.load().orElseThrow();check(recovered.recoveredFromBackup()&&recovered.state().isItemUnread("CASE")&&!recovered.state().isItemUnread("USB"),"checksum and backup protect marks");
 s.complete("PZ008");s.collect("CELL");s.collect("MODULE");s.complete("PZ010");check(s.isItemUnread("DEVICE")&&!s.isItemUnread("CASE")&&!s.isItemUnread("CELL"),"reward unread and consumed removed");
 try{GameState.restore(ProgressCatalog.standard(),new GameState.Snapshot(2,first.roomId(),first.completed(),first.items(),first.flags(),first.unlockedRooms(),Set.of("CORE")));throw new AssertionError("invalid accepted");}catch(IllegalArgumentException e){checks++;}
 check(new GameState().snapshot().unreadItems().isEmpty(),"new game clear");System.out.println("PASS "+checks+" unread persistence, all slots, hover autosave, old saves, checksum/backup and process restart checks; no game.");
 }
}
