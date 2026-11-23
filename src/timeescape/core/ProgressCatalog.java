package timeescape.core;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import timeescape.model.Era;

/** Immutable content definitions. Display order never determines prerequisites. */
public final class ProgressCatalog {
    public record Room(String id, Era era, String title, Set<String> exits) {
        public Room { exits = Set.copyOf(exits); }
    }
    public record Item(String id, String room, Set<String> requires, String blockedBy) {
        public Item { requires = Set.copyOf(requires); }
    }
    public record Mission(String id, int order, String title, Set<String> requires,
                          Set<String> completion, String finishedText) {
        public Mission { requires = Set.copyOf(requires); completion = Set.copyOf(completion); }
    }
    public record Step(String id, int order, String name, String room, String mission,
                       String object, String kind, boolean automatic, Set<String> requires,
                       Set<String> items, Set<String> consume, Set<String> grant,
                       Set<String> flags, Set<String> unlock, String prompt, String itemPrompt) {
        public Step {
            requires = Set.copyOf(requires); items = Set.copyOf(items);
            consume = Set.copyOf(consume); grant = Set.copyOf(grant);
            flags = Set.copyOf(flags); unlock = Set.copyOf(unlock);
        }
    }

    private final Map<String, Room> rooms;
    private final Map<String, Item> items;
    private final Map<String, Mission> missions;
    private final Map<String, Step> steps;
    private final String initialRoom;

    public ProgressCatalog(String initialRoom, Collection<Room> rooms, Collection<Item> items,
                           Collection<Mission> missions, Collection<Step> steps) {
        this.initialRoom = initialRoom;
        this.rooms = index(rooms, Room::id);
        this.items = index(items, Item::id);
        this.missions = index(missions, Mission::id);
        this.steps = index(steps, Step::id);
        validate();
    }
    private static <T> Map<String,T> index(Collection<T> values, java.util.function.Function<T,String> key) {
        Map<String,T> map = new LinkedHashMap<>();
        for (T value : values) {
            String id = key.apply(value);
            if (id.isBlank() || map.putIfAbsent(id,value) != null)
                throw new IllegalArgumentException("Duplicate or empty ID: " + id);
        }
        return Collections.unmodifiableMap(map);
    }
    private static void refs(Collection<String> ids, Map<String,?> target, String owner) {
        for (String id : ids) if (!target.containsKey(id))
            throw new IllegalArgumentException(owner + " references unknown ID: " + id);
    }
    private void validate() {
        refs(Set.of(initialRoom),rooms,"initialRoom");
        for (Room room : rooms.values()) refs(room.exits(),rooms,room.id());
        for (Item item : items.values()) {
            if (!item.room().isBlank()) refs(Set.of(item.room()),rooms,item.id());
            refs(item.requires(),steps,item.id());
            if (!item.blockedBy().isBlank()) refs(Set.of(item.blockedBy()),steps,item.id());
        }
        for (Mission mission : missions.values()) {
            refs(mission.requires(),steps,mission.id()); refs(mission.completion(),steps,mission.id());
            if (mission.completion().isEmpty()) throw new IllegalArgumentException("Empty mission: " + mission.id());
        }
        for (Step step : steps.values()) {
            refs(Set.of(step.room()),rooms,step.id());
            if (!step.mission().isBlank()) refs(Set.of(step.mission()),missions,step.id());
            refs(step.requires(),steps,step.id()); refs(step.items(),items,step.id());
            refs(step.consume(),items,step.id()); refs(step.grant(),items,step.id());
            refs(step.unlock(),rooms,step.id());
            if (!step.items().containsAll(step.consume()))
                throw new IllegalArgumentException("Consumed items must be required: " + step.id());
        }
        Set<String> visiting = new HashSet<>(), visited = new HashSet<>();
        for (String id : steps.keySet()) visit(id,visiting,visited);
    }
    private void visit(String id, Set<String> visiting, Set<String> visited) {
        if (visited.contains(id)) return;
        if (!visiting.add(id)) throw new IllegalArgumentException("Prerequisite cycle: " + id);
        for (String dependency : steps.get(id).requires()) visit(dependency,visiting,visited);
        visiting.remove(id); visited.add(id);
    }
    public String initialRoom() { return initialRoom; }
    public Map<String,Room> rooms() { return rooms; }
    public Map<String,Item> items() { return items; }
    public Map<String,Step> steps() { return steps; }
    public List<Mission> missions() {
        return missions.values().stream().sorted(Comparator.comparingInt(Mission::order).thenComparing(Mission::id)).toList();
    }
    public Mission mission(String id) {
        Mission result = missions.get(id);
        if (result == null) throw new IllegalArgumentException("Unknown mission: " + id);
        return result;
    }
    public List<Step> missionSteps(String id) {
        return steps.values().stream().filter(s -> s.mission().equals(id))
            .sorted(Comparator.comparingInt(Step::order).thenComparing(Step::id)).toList();
    }
    public static ProgressCatalog standard() { return DefaultHolder.VALUE; }
    private static final class DefaultHolder {
        static final ProgressCatalog VALUE = readDefault();
    }
    private static ProgressCatalog readDefault() {
        try (InputStream in = ProgressCatalog.class.getResourceAsStream("/assets/progression/game.properties")) {
            if (in == null) throw new IllegalStateException("Missing progression definitions");
            return read(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) { throw new IllegalStateException("Cannot read progression definitions",e); }
    }
    public static ProgressCatalog read(Reader reader) throws IOException {
        Properties p = new Properties(); p.load(reader);
        if (!"1".equals(p.getProperty("schema.version"))) throw new IllegalArgumentException("Unsupported progression schema");
        List<Room> rooms = new ArrayList<>(); List<Item> items = new ArrayList<>();
        List<Mission> missions = new ArrayList<>(); List<Step> steps = new ArrayList<>();
        for (String id : ids(p,"rooms")) {
            String k="room."+id+".";
            rooms.add(new Room(id,Era.valueOf(required(p,k+"era")),required(p,k+"title"),ids(p,k+"exits")));
        }
        for (String id : ids(p,"items")) {
            String k="item."+id+".";
            items.add(new Item(id,p.getProperty(k+"room",""),ids(p,k+"requires"),p.getProperty(k+"blockedBy","")));
        }
        for (String id : ids(p,"missions")) {
            String k="mission."+id+".";
            missions.add(new Mission(id,Integer.parseInt(required(p,k+"order")),required(p,k+"title"),
                ids(p,k+"requires"),ids(p,k+"completion"),required(p,k+"finishedText")));
        }
        for (String id : ids(p,"steps")) {
            String k="step."+id+".";
            steps.add(new Step(id,Integer.parseInt(required(p,k+"order")),required(p,k+"name"),required(p,k+"room"),
                p.getProperty(k+"mission",""),p.getProperty(k+"object",""),required(p,k+"kind"),
                Boolean.parseBoolean(p.getProperty(k+"automatic","false")),ids(p,k+"requires"),ids(p,k+"items"),
                ids(p,k+"consume"),ids(p,k+"grant"),ids(p,k+"flags"),ids(p,k+"unlock"),
                p.getProperty(k+"prompt",""),p.getProperty(k+"itemPrompt","")));
        }
        return new ProgressCatalog(required(p,"initialRoom"),rooms,items,missions,steps);
    }
    private static String required(Properties p, String key) {
        String value=p.getProperty(key);
        if(value==null||value.isBlank()) throw new IllegalArgumentException("Missing field: "+key);
        return value.trim();
    }
    private static Set<String> ids(Properties p,String key) {
        String value=p.getProperty(key,""); Set<String> result=new LinkedHashSet<>();
        if(!value.isBlank()) for(String token:value.split(",",-1)) {
            String id=token.trim();
            if(id.isBlank()||!result.add(id)) throw new IllegalArgumentException("Invalid ID list: "+key);
        }
        return result;
    }
}
