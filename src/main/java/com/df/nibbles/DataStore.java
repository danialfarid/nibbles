package com.df.nibbles;


import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

public class DataStore {

    public static DataStore INSTANCE = new DataStore();

    protected DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();

    char[] alphanumeric = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

//    public Project createProject() {
//        Entity clazz = new Entity("Class");
//        String id = generateId();
//        clazz.setProperty("id", id);
//        clazz.setProperty("name", "Main");
//        clazz.setProperty("src",
//                "public class Main {\r\n\tpublic static void main(String args[]) {\r\n\t\t\r\n\t}\r\n}");
//        clazz.setProperty("date", new Date());
//        dataStore.put(clazz);
//        return new Project().init(id);
//    }
//
//    private String generateId() {
//        return String.valueOf(alphanumeric[random36()]) + alphanumeric[random36()] +
//                alphanumeric[random36()] + alphanumeric[random36()] +
//                alphanumeric[random36()] + alphanumeric[random36()];
//    }
//
//    protected int random36() {
//        return (int) (36 * Math.random());
//    }
//
//    public List<Clazz> getClasses(String id) {
//        List<Clazz> classes = new ArrayList<>();
//        Filter idFilter = new FilterPredicate("id", FilterOperator.EQUAL, id);
//
//        Query q = new Query("Class").setFilter(idFilter);
//        PreparedQuery pq = dataStore.prepare(q);
//
//        for (Entity result : pq.asIterable()) {
//            String name = (String) result.getProperty("name");
//            String src = (String) result.getProperty("src");
//            classes.add(new Clazz().init(name, src));
//        }
//        return classes;
//    }
//
//    public List<Lib> getLibs(String id) {
//        List<Lib> libs = new ArrayList<>();
//        Filter idFilter = new FilterPredicate("id", FilterOperator.EQUAL, id);
//
//        Query q = new Query("Lib").setFilter(idFilter);
//        PreparedQuery pq = dataStore.prepare(q);
//
//        for (Entity result : pq.asIterable()) {
//            String name = (String) result.getProperty("name");
//            String url = (String) result.getProperty("url");
//            libs.add(new Lib().init(name, url));
//        }
//        return libs;
//    }
//
//    public String createClass(String id, String name) {
//        deleteClass(id, name);
//        Entity clazz = new Entity("Class");
//        clazz.setProperty("id", id);
//        clazz.setProperty("name", name);
//        String[] split = name.split("\\.");
//        String packageName = "";
//        for (int i = 0; i < split.length - 1; i++) {
//            packageName += (i > 0 ? "." : "") + split[i];
//        }
//        String content = packageName.length() > 0 ? "package " + packageName + ";\r\n\r\n" : "";
//        content += "public class " + split[split.length - 1] + " {\r\n\t\r\n}";
//        clazz.setProperty("src", content);
//        clazz.setProperty("date", new Date());
//        dataStore.put(clazz);
//        return content;
//    }
//
//    public String createLib(String id, String name, String url) {
//        deleteLib(id, name);
//        Entity lib = new Entity("Lib");
//        lib.setProperty("id", id);
//        lib.setProperty("name", name);
//        lib.setProperty("url", url);
//        lib.setProperty("date", new Date());
//        dataStore.put(lib);
//        return name;
//    }
//
//    public void updateClass(String id, String name, String src) {
//        Filter filter = CompositeFilterOperator.and(new FilterPredicate("id", FilterOperator.EQUAL, id),
//                new FilterPredicate("name", FilterOperator.EQUAL, name));
//
//        Query q = new Query("Class").setFilter(filter);
//        Entity result = dataStore.prepare(q).asSingleEntity();
//
//        result.setProperty("src", src);
//        dataStore.put(result);
//    }
//
//    public void deleteClass(String id, String name) {
//        Filter filter = CompositeFilterOperator.and(new FilterPredicate("id", FilterOperator.EQUAL, id),
//                new FilterPredicate("name", FilterOperator.EQUAL, name));
//
//        Query q = new Query("Class").setFilter(filter);
//        Iterable<Entity> result = dataStore.prepare(q).asIterable();
//        for (Entity entity : result) {
//            dataStore.delete(entity.getKey());
//        }
//    }
//
//    public void deleteLib(String id, String name) {
//        Filter filter = CompositeFilterOperator.and(new FilterPredicate("id", FilterOperator.EQUAL, id),
//                new FilterPredicate("name", FilterOperator.EQUAL, name));
//
//        Query q = new Query("Lib").setFilter(filter);
//        Iterable<Entity> result = dataStore.prepare(q).asIterable();
//        for (Entity entity : result) {
//            dataStore.delete(entity.getKey());
//        }
//    }
//
//    public boolean getProject(String id) {
//        Filter filter = new FilterPredicate("id", FilterOperator.EQUAL, id);
//
//        Query q = new Query("Class").setFilter(filter);
//        int count = dataStore.prepare(q).countEntities(FetchOptions.Builder.withDefaults());
//        return count > 0;
//    }
}
