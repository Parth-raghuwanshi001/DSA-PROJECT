# College Management System (DSA in Java)

## Requirements implemented
- Student, faculty and course record classes
- `LinkedList` for storing records
- `ArrayList` for temporary student and faculty search results
- `Stack` for undoing the latest supported record change
- `Queue` for FIFO service requests
- Graph (adjacency map) for course relationships and BFS traversal
- File handling: load at startup and save on exit / save menu
- Unique ID validation and empty queue handling

## Compile and run
Open a terminal in this folder:

```bash
javac CollegeManagementSystem.java
java CollegeManagementSystem
```

The program creates/uses `college_data.txt` in the current working directory.

## Test cases from the project description
1. Add student Aman with ID `ST101`: output confirms the student was added.
2. With no service requests pending, choose Process next service request: `No pending service requests.`
3. Search faculty ID `F999`: `Faculty F999 not found.` is represented by the faculty search message (`Faculty F999 not found.`).
4. Add student Riya, choose Undo, then display students: Riya is removed; Aman remains.

Note: The program uses `ArrayList` explicitly for search results and `LinkedList` for the main records.
