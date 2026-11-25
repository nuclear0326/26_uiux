package timeescape.ui;
import java.awt.Font;import javax.swing.UIManager;import javax.swing.plaf.FontUIResource;
/** Bundled role fonts. Static weights avoid synthetic bold and OS dependencies. */
public final class GameFonts {
 private static final Font TITLE=load("NotoSerifKR-SemiBold.otf"),BODY=load("Pretendard-Regular.ttf"),BUTTON=load("Pretendard-SemiBold.ttf");
 private GameFonts(){}
 private static Font load(String name){
  // Windows distribution uses the identical original font files beside the JAR.
  // File loading avoids inflating and copying 30 MB to temporary font files.
  try{
   var location=java.nio.file.Path.of(GameFonts.class.getProtectionDomain().getCodeSource().getLocation().toURI());
   if(java.nio.file.Files.isRegularFile(location)){
    var font=location.getParent().resolve("fonts").resolve(name);
    if(java.nio.file.Files.isRegularFile(font))return Font.createFont(Font.TRUETYPE_FONT,font.toFile());
   }
  }catch(java.net.URISyntaxException|java.io.IOException|java.awt.FontFormatException e){throw new IllegalStateException("Cannot read packaged font "+name,e);}
  try(var in=GameFonts.class.getResourceAsStream("/assets/fonts/"+name)){
  if(in==null)throw new IllegalStateException("Missing font: "+name);
  return Font.createFont(Font.TRUETYPE_FONT,in);
 }catch(Exception e){throw new IllegalStateException("Cannot load bundled font "+name,e);}}
 public static Font title(float size){return TITLE.deriveFont(size);}
 public static Font body(float size){return BODY.deriveFont(Math.max(14f,size));}
 public static Font button(float size){return BUTTON.deriveFont(size);}
 public static void installDefaults(){
  var defaults=UIManager.getDefaults();var keys=new java.util.ArrayList<Object>(defaults.keySet());
  for(Object key:keys)if(defaults.get(key) instanceof Font)UIManager.put(key,new FontUIResource(body(15)));
  for(String key:new String[]{"Button.font","ToggleButton.font","CheckBox.font","RadioButton.font","Menu.font","MenuItem.font","CheckBoxMenuItem.font","RadioButtonMenuItem.font"})UIManager.put(key,new FontUIResource(button(16)));
  UIManager.put("TitledBorder.font",new FontUIResource(title(18)));
 }
}
