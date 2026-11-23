package timeescape.puzzle;
import timeescape.puzzle.present.*;
/** Compatibility facade for existing callers. Rules live only in the five concrete models. */
public final class PresentPuzzleModel {
 public final String id;private final PresentPuzzle delegate;
 public PresentPuzzleModel(String id){this.id=id;delegate=PresentPuzzles.create(id);}
 public int size(){return delegate.size();}public int value(int i){return delegate.value(i);}
 public void turn(int i){delegate.turn(i);}public boolean solved(){return delegate.solved();}
 public String title(){return delegate.title();}public String instruction(){return delegate.instruction();}
 public String label(int i){return delegate.label(i);}
 public int power(){if(delegate instanceof PowerDistributionPuzzle p)return p.power();throw new IllegalStateException("Power is only available for the power puzzle");}
}
