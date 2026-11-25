import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * College Management System
 * DSA concepts: ArrayList, LinkedList, Stack, Queue, Graph and File Handling.
 *
 * Compile: javac CollegeManagementSystem.java
 * Run:     java CollegeManagementSystem
 *
 * Data is stored in college_data.txt in the current working directory.
 */
public class CollegeManagementSystem {
    private static final String DATA_FILE = "college_data.txt";

    // LinkedList is used for the main student, faculty and course records.
    private final LinkedList<Student> students = new LinkedList<>();
    private final LinkedList<Faculty> facultyMembers = new LinkedList<>();
    private final LinkedList<Course> courses = new LinkedList<>();

    // Stack stores inverse operations. Undo is available for the latest supported change.
    private final Stack<UndoAction> undoStack = new Stack<>();

    // Queue stores service requests in FIFO order.
    private final Queue<ServiceRequest> serviceQueue = new LinkedList<>();

    // Graph: each course ID is a vertex; related courses are connected by edges.
    private final Map<String, Set<String>> courseGraph = new LinkedHashMap<>();

    private final Scanner scanner = new Scanner(System.in);

    // ----------------------------- MODEL CLASSES -----------------------------

    static class Student {
        String id, name, department;
        Student(String id, String name, String department) {
            this.id = id; this.name = name; this.department = department;
        }
        @Override public String toString() {
            return "ID: " + id + " | Name: " + name + " | Department: " + department;
        }
    }

    static class Faculty {
        String id, name, department;
        Faculty(String id, String name, String department) {
            this.id = id; this.name = name; this.department = department;
        }
        @Override public String toString() {
            return "ID: " + id + " | Name: " + name + " | Department: " + department;
        }
    }

    static class Course {
        String id, name, facultyId;
        Course(String id, String name, String facultyId) {
            this.id = id; this.name = name; this.facultyId = facultyId;
        }
        @Override public String toString() {
            return "ID: " + id + " | Course: " + name + " | Faculty ID: " + facultyId;
        }
    }

    static class ServiceRequest {
        String requestId, studentId, description;
        ServiceRequest(String requestId, String studentId, String description) {
            this.requestId = requestId; this.studentId = studentId; this.description = description;
        }
        @Override public String toString() {
            return "Request: " + requestId + " | Student: " + studentId + " | " + description;
        }
    }

    interface UndoAction {
        String description();
        void undo();
    }

    // ------------------------------- STARTUP --------------------------------

    public static void main(String[] args) {
        CollegeManagementSystem app = new CollegeManagementSystem();
        app.loadData();
        app.runMenu();
        app.saveData();
        System.out.println("Data saved. Thank you for using College Management System.");
    }

    private void runMenu() {
        while (true) {
            System.out.println("\n========== COLLEGE MANAGEMENT SYSTEM ==========");
            System.out.println("1. Add student");
            System.out.println("2. Display/search students");
            System.out.println("3. Add faculty");
            System.out.println("4. Display/search faculty");
            System.out.println("5. Add course");
            System.out.println("6. Display courses");
            System.out.println("7. Connect two related courses (Graph)");
            System.out.println("8. Display/search course relationships");
            System.out.println("9. Add service request");
            System.out.println("10. Process next service request");
            System.out.println("11. Display pending service requests");
            System.out.println("12. Undo latest record change");
            System.out.println("13. Save data now");
            System.out.println("0. Save and exit");
            int choice = readInt("Enter choice: ");

            switch (choice) {
                case 1: addStudent(); break;
                case 2: displayStudents(); break;
                case 3: addFaculty(); break;
                case 4: displayFaculty(); break;
                case 5: addCourse(); break;
                case 6: displayCourses(); break;
                case 7: connectCourses(); break;
                case 8: displayCourseGraph(); break;
                case 9: addServiceRequest(); break;
                case 10: processNextRequest(); break;
                case 11: displayServiceQueue(); break;
                case 12: undoLastChange(); break;
                case 13: saveData(); System.out.println("Data saved."); break;
                case 0: saveData(); return;
                default: System.out.println("Invalid choice. Please enter a number from 0 to 13.");
            }
        }
    }

    // ---------------------------- STUDENT METHODS ----------------------------

    private void addStudent() {
        String id = readNonEmpty("Enter student ID: ").toUpperCase();
        if (studentById(id) != null || facultyById(id) != null || courseById(id) != null) {
            System.out.println("This ID already exists. IDs must be unique.");
            return;
        }
        String name = readNonEmpty("Enter student name: ");
        String department = readNonEmpty("Enter department: ");
        Student student = new Student(id, name, department);
        students.add(student);
        undoStack.push(new UndoAction() {
            public String description() { return "Add student " + student.name; }
            public void undo() { students.remove(student); }
        });
        System.out.println("Student added. ID: " + id + " | " + name);
    }

    private void displayStudents() {
        if (students.isEmpty()) { System.out.println("No student records found."); return; }
        System.out.println("\n----------- STUDENT LIST -----------");
        for (Student s : students) System.out.println(s);
        String search = readLine("Enter student ID/name to search, or press Enter to return: ").trim();
        if (!search.isEmpty()) {
            // ArrayList is used as a temporary collection of search results.
            ArrayList<Student> matches = new ArrayList<>();
            for (Student s : students) {
                if (s.id.equalsIgnoreCase(search) || s.name.equalsIgnoreCase(search)) matches.add(s);
            }
            if (matches.isEmpty()) System.out.println("No matching student found.");
            else for (Student match : matches) System.out.println("Found: " + match);
        }
    }

    // ----------------------------- FACULTY METHODS ----------------------------

    private void addFaculty() {
        String id = readNonEmpty("Enter faculty ID: ").toUpperCase();
        if (studentById(id) != null || facultyById(id) != null || courseById(id) != null) {
            System.out.println("This ID already exists. IDs must be unique.");
            return;
        }
        String name = readNonEmpty("Enter faculty name: ");
        String department = readNonEmpty("Enter department: ");
        Faculty member = new Faculty(id, name, department);
        facultyMembers.add(member);
        undoStack.push(new UndoAction() {
            public String description() { return "Add faculty " + member.name; }
            public void undo() { facultyMembers.remove(member); }
        });
        System.out.println("Faculty added. ID: " + id + " | " + name);
    }

    private void displayFaculty() {
        if (facultyMembers.isEmpty()) { System.out.println("No faculty records found."); return; }
        System.out.println("\n----------- FACULTY LIST -----------");
        for (Faculty f : facultyMembers) System.out.println(f);
        String search = readLine("Enter faculty ID/name to search, or press Enter to return: ").trim();
        if (!search.isEmpty()) {
            // ArrayList is also used for faculty search results.
            ArrayList<Faculty> matches = new ArrayList<>();
            for (Faculty f : facultyMembers) {
                if (f.id.equalsIgnoreCase(search) || f.name.equalsIgnoreCase(search)) matches.add(f);
            }
            if (matches.isEmpty()) System.out.println("Faculty " + search + " not found.");
            else for (Faculty match : matches) System.out.println("Found: " + match);
        }
    }

    // ------------------------------ COURSE METHODS ---------------------------

    private void addCourse() {
        String id = readNonEmpty("Enter course ID: ").toUpperCase();
        if (studentById(id) != null || facultyById(id) != null || courseById(id) != null) {
            System.out.println("This ID already exists. IDs must be unique.");
            return;
        }
        String name = readNonEmpty("Enter course name: ");
        String facultyId = readNonEmpty("Enter faculty ID assigned to this course: ").toUpperCase();
        if (facultyById(facultyId) == null) {
            System.out.println("Faculty " + facultyId + " not found. Add the faculty first.");
            return;
        }
        Course course = new Course(id, name, facultyId);
        courses.add(course);
        courseGraph.putIfAbsent(id, new LinkedHashSet<>());
        undoStack.push(new UndoAction() {
            public String description() { return "Add course " + course.name; }
            public void undo() {
                courses.remove(course);
                courseGraph.remove(id);
                for (Set<String> neighbours : courseGraph.values()) neighbours.remove(id);
            }
        });
        System.out.println("Course added. ID: " + id + " | " + name);
    }

    private void displayCourses() {
        if (courses.isEmpty()) { System.out.println("No course records found."); return; }
        System.out.println("\n----------- COURSE LIST -----------");
        for (Course c : courses) System.out.println(c);
    }

    // ------------------------------- GRAPH -----------------------------------

    private void connectCourses() {
        if (courses.size() < 2) {
            System.out.println("Add at least two courses before connecting them."); return;
        }
        String first = readNonEmpty("Enter first course ID: ").toUpperCase();
        String second = readNonEmpty("Enter second course ID: ").toUpperCase();
        if (courseById(first) == null || courseById(second) == null) {
            System.out.println("One or both course IDs were not found."); return;
        }
        if (first.equals(second)) { System.out.println("A course cannot be connected to itself."); return; }
        boolean existed = courseGraph.get(first).contains(second);
        courseGraph.get(first).add(second);
        courseGraph.get(second).add(first);
        if (!existed) {
            undoStack.push(new UndoAction() {
                public String description() { return "Connect courses " + first + " and " + second; }
                public void undo() {
                    courseGraph.getOrDefault(first, Collections.emptySet()).remove(second);
                    courseGraph.getOrDefault(second, Collections.emptySet()).remove(first);
                }
            });
        }
        System.out.println("Course relationship saved.");
    }

    private void displayCourseGraph() {
        if (courseGraph.isEmpty()) { System.out.println("No courses available in graph."); return; }
        System.out.println("\n------ COURSE RELATIONSHIP GRAPH ------");
        for (String id : courseGraph.keySet()) {
            Course c = courseById(id);
            System.out.print((c == null ? id : c.name + " (" + id + ")") + " -> ");
            Set<String> neighbours = courseGraph.get(id);
            if (neighbours.isEmpty()) System.out.print("No connected courses");
            else {
                for (String neighbour : neighbours) {
                    Course linked = courseById(neighbour);
                    System.out.print((linked == null ? neighbour : linked.name + " (" + neighbour + ")") + "  ");
                }
            }
            System.out.println();
        }

        String start = readLine("Enter a course ID to search connected courses (or Enter to return): ").trim().toUpperCase();
        if (!start.isEmpty()) {
            if (!courseGraph.containsKey(start)) { System.out.println("Course " + start + " not found."); return; }
            // Breadth-first search (BFS) using a queue.
            Set<String> visited = new LinkedHashSet<>();
            Queue<String> queue = new LinkedList<>();
            queue.add(start); visited.add(start);
            System.out.print("Reachable courses: ");
            while (!queue.isEmpty()) {
                String current = queue.remove();
                System.out.print(current + " ");
                for (String next : courseGraph.getOrDefault(current, Collections.emptySet())) {
                    if (visited.add(next)) queue.add(next);
                }
            }
            System.out.println();
        }
    }

    // ---------------------------- SERVICE QUEUE ------------------------------

    private void addServiceRequest() {
        String requestId = "SR" + (System.currentTimeMillis() % 1000000);
        String studentId = readNonEmpty("Enter student ID: ").toUpperCase();
        if (studentById(studentId) == null) {
            System.out.println("Student " + studentId + " not found."); return;
        }
        String description = readNonEmpty("Enter service request: ");
        serviceQueue.add(new ServiceRequest(requestId, studentId, description));
        System.out.println("Service request added to queue. Request ID: " + requestId);
    }

    private void processNextRequest() {
        if (serviceQueue.isEmpty()) {
            System.out.println("No pending service requests."); return;
        }
        System.out.println("Processed: " + serviceQueue.remove());
    }

    private void displayServiceQueue() {
        if (serviceQueue.isEmpty()) { System.out.println("No pending service requests."); return; }
        System.out.println("\n------ PENDING SERVICE REQUESTS (FIFO) ------");
        for (ServiceRequest request : serviceQueue) System.out.println(request);
    }

    // --------------------------------- UNDO ----------------------------------

    private void undoLastChange() {
        if (undoStack.isEmpty()) {
            System.out.println("No record changes available to undo."); return;
        }
        UndoAction action = undoStack.pop();
        action.undo();
        System.out.println("Last operation undone: " + action.description());
    }

    // ------------------------------ FILE HANDLING ----------------------------

    private void loadData() {
        Path path = Paths.get(DATA_FILE);
        if (!Files.exists(path)) {
            System.out.println("No saved data file found. Starting with empty records.");
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\\t", -1);
                if (p.length == 0) continue;
                switch (p[0]) {
                    case "STUDENT":
                        if (p.length == 4) students.add(new Student(decode(p[1]), decode(p[2]), decode(p[3])));
                        break;
                    case "FACULTY":
                        if (p.length == 4) facultyMembers.add(new Faculty(decode(p[1]), decode(p[2]), decode(p[3])));
                        break;
                    case "COURSE":
                        if (p.length == 4) {
                            Course c = new Course(decode(p[1]), decode(p[2]), decode(p[3]));
                            courses.add(c); courseGraph.putIfAbsent(c.id, new LinkedHashSet<>());
                        }
                        break;
                    case "EDGE":
                        if (p.length == 3) {
                            String a = decode(p[1]), b = decode(p[2]);
                            courseGraph.putIfAbsent(a, new LinkedHashSet<>());
                            courseGraph.putIfAbsent(b, new LinkedHashSet<>());
                            courseGraph.get(a).add(b); courseGraph.get(b).add(a);
                        }
                        break;
                    default: break; // Ignore unknown or malformed record types.
                }
            }
            System.out.println("Saved records loaded successfully.");
        } catch (IOException e) {
            System.out.println("Could not load data: " + e.getMessage());
        }
    }

    private void saveData() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(DATA_FILE))) {
            for (Student s : students)
                writer.write("STUDENT\t" + encode(s.id) + "\t" + encode(s.name) + "\t" + encode(s.department) + "\n");
            for (Faculty f : facultyMembers)
                writer.write("FACULTY\t" + encode(f.id) + "\t" + encode(f.name) + "\t" + encode(f.department) + "\n");
            for (Course c : courses)
                writer.write("COURSE\t" + encode(c.id) + "\t" + encode(c.name) + "\t" + encode(c.facultyId) + "\n");
            for (String a : courseGraph.keySet()) {
                for (String b : courseGraph.get(a)) {
                    if (a.compareTo(b) < 0)
                        writer.write("EDGE\t" + encode(a) + "\t" + encode(b) + "\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Could not save data: " + e.getMessage());
        }
    }

    // Base64 keeps tabs/newlines in names from breaking the text-file format.
    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
    private static String decode(String value) {
        return new String(Base64.getDecoder().decode(value), java.nio.charset.StandardCharsets.UTF_8);
    }

    // ------------------------------ HELPERS ----------------------------------

    private Student studentById(String id) {
        for (Student s : students) if (s.id.equalsIgnoreCase(id)) return s;
        return null;
    }
    private Faculty facultyById(String id) {
        for (Faculty f : facultyMembers) if (f.id.equalsIgnoreCase(id)) return f;
        return null;
    }
    private Course courseById(String id) {
        for (Course c : courses) if (c.id.equalsIgnoreCase(id)) return c;
        return null;
    }
    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
    private String readNonEmpty(String prompt) {
        while (true) {
            String value = readLine(prompt).trim();
            if (!value.isEmpty()) return value;
            System.out.println("Input cannot be empty. Please try again.");
        }
    }
    private int readInt(String prompt) {
        while (true) {
            String value = readLine(prompt).trim();
            try { return Integer.parseInt(value); }
            catch (NumberFormatException e) { System.out.println("Please enter a valid number."); }
        }
    }
}
