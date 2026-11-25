import java.util.*;
import javax.sound.sampled.*;
public final class VerifyAudio {
 public static void main(String[] args) throws Exception {
  Map<String,String[]> files=Map.of("bgm",new String[]{"menu","past","present","future"},"ambience",new String[]{"rain","reactor","terminal"},"sfx",new String[]{"ui_click","ui_open","file_select","file_move","file_blocked","item_pickup","device_repair","puzzle_success","time_travel","core_pickup","ending"});
  int count=0;
  for(var entry:files.entrySet())for(String name:entry.getValue()){
   String path="/assets/audio/"+entry.getKey()+"/"+name+".wav";
   try(var raw=VerifyAudio.class.getResourceAsStream(path)){
    if(raw==null)throw new AssertionError("Missing "+path);
    try(var stream=AudioSystem.getAudioInputStream(new java.io.BufferedInputStream(raw))){
     var format=stream.getFormat();
     if(!format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)||format.getSampleSizeInBits()!=16||format.getSampleRate()!=32000||format.getChannels()!=1)throw new AssertionError(path);
     if(stream.getFrameLength()<100)throw new AssertionError("Empty "+path);
     if(stream.readAllBytes().length!=stream.getFrameLength()*2)throw new AssertionError("Truncated "+path);
     count++;
    }
   }
  }
  System.out.println("Audio decoder/resource checks passed for "+count+" files; no playback device opened.");
 }
}
