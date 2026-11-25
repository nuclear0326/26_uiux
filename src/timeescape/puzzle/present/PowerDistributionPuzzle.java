package timeescape.puzzle.present;
public final class PowerDistributionPuzzle extends CyclicValuesPuzzle {
 public PowerDistributionPuzzle(){super("PZ004",2,new int[5]);}
 public String title(){return "전원 켜기 (임시)";}
 public String instruction(){return "스위치를 켜거나 끄며 전력 합계를 정확히 16으로 맞추세요. 잘못된 합계로 확인해도 다시 조절할 수 있습니다.";}
 private static final int[] POWER={2,3,5,7,11};
 public int power(){int sum=0;for(int i=0;i<size();i++)sum+=POWER[i]*value(i);return sum;}
 public String label(int i){return POWER[i]+" 전력: "+(value(i)==1?"ON":"OFF");}
 @Override public boolean solved(){return power()==16;}
 @Override public String status(){return "출력 "+power()+" / 16  ·  필요한 회로만 연결하세요.";}
}
