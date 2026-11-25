package timeescape.system;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** UTF-8 scenes with [scene.id] headings and --- page separators. */
public final class DialogueCatalog {
    private final Map<String,List<String>> scenes;
    private DialogueCatalog(Map<String,List<String>> scenes){this.scenes=Map.copyOf(scenes);}
    public List<String> scene(String id){
        List<String> result=scenes.get(id);
        if(result==null)throw new IllegalArgumentException("Unknown dialogue scene: "+id);
        return result;
    }
    public static DialogueCatalog parse(Reader input)throws IOException{
        Map<String,List<String>> scenes=new LinkedHashMap<>();
        String id=null;List<String> pages=new ArrayList<>();StringBuilder page=new StringBuilder();
        BufferedReader reader=new BufferedReader(input);String line;int number=0;
        while((line=reader.readLine())!=null){
            if(++number==1&&line.startsWith("\uFEFF"))line=line.substring(1);
            String trimmed=line.trim();
            if(trimmed.startsWith("#")||trimmed.isEmpty())continue;
            if(trimmed.startsWith("[")&&trimmed.endsWith("]")){
                if(id!=null){addPage(pages,page);scenes.put(id,List.copyOf(pages));}
                id=trimmed.substring(1,trimmed.length()-1).trim();
                if(!id.matches("[a-zA-Z0-9_.-]+")||scenes.containsKey(id))throw new IOException("Invalid/duplicate dialogue scene at line "+number);
                pages=new ArrayList<>();
            }else if(trimmed.equals("---")){
                if(id==null)throw new IOException("Missing dialogue heading at line "+number);
                addPage(pages,page);
            }else{
                if(id==null)throw new IOException("Missing dialogue heading at line "+number);
                if(page.length()>0)page.append('\n');page.append(line.strip());
            }
        }
        if(id!=null){addPage(pages,page);scenes.put(id,List.copyOf(pages));}
        if(scenes.isEmpty())throw new IOException("Empty dialogue file");
        return new DialogueCatalog(scenes);
    }
    private static void addPage(List<String> pages,StringBuilder page)throws IOException{
        if(page.isEmpty())throw new IOException("Empty dialogue page");
        pages.add(page.toString());page.setLength(0);
    }
    public static DialogueCatalog standard(){return Holder.VALUE;}
    private static final class Holder {static final DialogueCatalog VALUE=load();}
    private static DialogueCatalog load(){
        try{
            Path location=Path.of(DialogueCatalog.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if(Files.isRegularFile(location)){
                Path external=location.getParent().resolve("dialogue/ko.txt");
                if(Files.isRegularFile(external)){
                    try(Reader r=Files.newBufferedReader(external,StandardCharsets.UTF_8)){
                        DialogueCatalog catalog=parse(r);catalog.scene("intro.start");return catalog;
                    }catch(IOException|IllegalArgumentException e){System.err.println("Invalid external dialogue, using bundled text: "+e.getMessage());}
                }
            }
        }catch(java.net.URISyntaxException e){System.err.println("Cannot locate external dialogue: "+e.getMessage());}
        try(InputStream in=DialogueCatalog.class.getResourceAsStream("/assets/dialogue/ko.txt")){
            if(in==null)throw new IOException("Missing bundled dialogue");
            return parse(new InputStreamReader(in,StandardCharsets.UTF_8));
        }catch(IOException e){throw new IllegalStateException("Cannot load dialogue",e);}
    }
}
