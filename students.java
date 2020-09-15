/************************************/
/* Students JDBC					*/
/************************************/

import java.io.*;
import java.sql.*;
import oracle.jdbc.driver.*;
import oracle.sql.*;
import java.util.Scanner;

public class students {

    //the constants we will need
    private static final String USER = "\"18079969d\"";
    private static final String PASS = "";
    private static final String DATABASE_URL = "jdbc:oracle:thin:@studora.comp.polyu.edu.hk:1521:dbms";

    public static void main(String args[]) throws SQLException, IOException {

        int userType;

        System.out.println("Welcome to the student course registration system!");

        while(true){

            System.out.println("Please log in.");
            Scanner scan = new Scanner(System.in);
            System.out.println("Enter 1 if you are a student, 2 if you are an administrator. In order to exit type 0 ");

            userType = scan.nextInt();

            if (userType == 1) {
                student();

            } else if	(userType == 2){
                administrator();

            } else if (userType == 0){
                System.out.println("Thanks for using student course registration system. See you next time!");
                break;
            }
            else{
                System.out.println("Invalid input. Please try again.");
            }

        }



    }

    public static void student() throws SQLException {

        String userID;
        while(true){

            Scanner myObj4 = new Scanner(System.in);  // Create a Scanner object
            System.out.println("Enter ID: ");
            userID = myObj4.nextLine();  // Read user input

            // check if a student with such ID exists in the system
            if(studentExists(userID)){
                break;
            }else{
                System.out.println("Sorry! Student with such an ID was not found in the system. Try again.");
            }

        }
        boolean done=false;

        while (!done) {

            System.out.println("System");
            System.out.println("0. QUIT");
            System.out.println("1. List all the courses");
            System.out.println("2. List my courses");
            System.out.println("3. Register a course");
            System.out.println("4. Edit my personal info");

            Scanner myObj = new Scanner(System.in);  // Create a Scanner object
            System.out.println("Enter Choice: ");
            int enterNum = myObj.nextInt();  // Read user input
            switch (enterNum) {
                case 0:
                    System.out.println("BYE!");
                    done = true;
                    break;
                case 1:
                    listAllCourses();
                    break;
                case 2:
                    listMyCourses(userID);
                    break;
                case 3:
                    registerCourse(userID);
                    break;
                case 4:
                    editPersonalInfo(userID);
                    break;
                default:
                    System.out.println("Error! Please type a valid input.");
            }
        }
    }

    public static void listAllCourses() throws SQLException {

        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        OracleConnection conn = (OracleConnection)DriverManager.getConnection(DATABASE_URL, USER, PASS);

        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery(
                "SELECT DISTINCT COURSE_ID, COURSE_TITLE FROM COURSES");
        String namer1;
        while (rset.next())  //true if contains any data on the next(current) line
        {
            namer1 = rset.getString(1);
            if (!rset.wasNull())
            {
                System.out.println(namer1 + " " + rset.getString(2));
            }
        }
        System.out.println();

        rset.close();
        stmt.close();

        //smooth transition to previous menu
        Scanner scan = new Scanner(System.in);  // Create a Scanner object
        System.out.println("Type anything to continue (or just ENTER) ");
        scan.nextLine();

        conn.close();
    }

    public static void listMyCourses(String userID) throws SQLException {

        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        OracleConnection conn = (OracleConnection)DriverManager.getConnection(DATABASE_URL, USER, PASS);

        PreparedStatement preparedStmt = conn.prepareStatement("SELECT DISTINCT COURSES.COURSE_ID,COURSE_TITLE, STAFF_NAME, SECTION FROM ENROLLMENT,COURSES WHERE ENROLLMENT.STUDENT_ID=?");
        preparedStmt.setString(1,userID);
        ResultSet rset = preparedStmt.executeQuery();

        while (rset.next())  //true if contains any data on the next(current) line
        {
            System.out.println(rset.getString(1) + " " + rset.getString(2) + " " + rset.getString(3)+" "+rset.getString(4));
        }
        System.out.println();

        rset.close();
        preparedStmt.close();

        //smooth transition to previous menu
        Scanner scan = new Scanner(System.in);  // Create a Scanner object
        System.out.println("Type anything to continue (or just ENTER) ");
        scan.nextLine();
        conn.close();
    }

    public static void registerCourse(String userID) throws SQLException {

        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        OracleConnection conn = (OracleConnection)DriverManager.getConnection(DATABASE_URL, USER, PASS);
        String courseID;
        boolean done = false;
        while(!done){

            try
            {
                //check if such course is already registered
                while(true){

                    //we get the userID
                    Scanner myObj = new Scanner(System.in);  // Create a Scanner object
                    System.out.println("Enter the course ID ");
                    courseID = myObj.nextLine();  // Read user input
                    if(!courseExists(courseID)){
                        System.out.println("Sorry. Such course does not exist. Please try again.");
                        continue;
                    }

                    // check if a student with such ID exists in the system
                    Statement stmt = conn.createStatement();
                    PreparedStatement preparedStmt = conn.prepareStatement("SELECT DISTINCT COURSE_ID FROM ENROLLMENT WHERE STUDENT=?");
                    preparedStmt.setString(1, userID);
                    ResultSet rset = preparedStmt.executeQuery();
                    boolean found=false;
                    while (rset.next())  //true if contains any data on the next(current) line
                    {
                        String registeredCourseID = rset.getString(1);
                        if (registeredCourseID==courseID)
                        {
                            found=true;
                            break;
                        }
                    }

                    rset.close();
                    preparedStmt.close();

                    if(found){
                        System.out.println("Sorry! Such course is already registered by you. Try again. ");

                    }else{
                        break;
                    }

                }
                // the mysql insert statement
                String query = "insert into ENROLLMENT (STUDENT_ID, COURSE_ID, REG_DATE, GRADE)"
                        + " values (?, ?, CONVERT(VARCHAR(10), getdate(), 111), ?)";

                // create the mysql insert preparedstatement
                PreparedStatement preparedStmt = conn.prepareStatement(query);
                preparedStmt.setString (1, userID);
                preparedStmt.setString (2, courseID);
                preparedStmt.setNull(3, java.sql.Types.INTEGER);

                // execute the preparedstatement
                preparedStmt.execute();

                preparedStmt.close();
                done=true;

            }
            catch (Exception e)
            {
                done=false;
                System.err.println("Got an exception!");
                System.err.println(e.getMessage());

            }
            System.out.println("A course is successfully registered");

            Scanner scan = new Scanner(System.in);
            System.out.println("Type 1 if you want to add one more course, anything else if you want to quit back to previous menu");
            String inp = scan.nextLine();
            if(inp.equals("1")){
                done=false;
            }else{
                done=true;
            }

        }

        conn.close();
    }

    public static void editPersonalInfo(String userID) throws SQLException {
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        OracleConnection conn = (OracleConnection)DriverManager.getConnection(DATABASE_URL, USER, PASS);
        PreparedStatement preparedStmt=null;
        boolean done = false;
        while (!done) {

            try {
                System.out.println("Choose what you would like to edit");
                System.out.println("0. QUIT");
                System.out.println("1. Student ID");
                System.out.println("2. Student name");
                System.out.println("3. Department ");
                System.out.println("4. Address");
                System.out.println("5. Birthdate");
                System.out.println("6. Gender");

                Scanner myObj = new Scanner(System.in);  // Create a Scanner object
                System.out.println("Enter Choice: ");
                int choice = myObj.nextInt();  // Read user input
                switch (choice) {
                    case 0:
                        System.out.println("BYE!");
                        done = true;
                        break;
                    case 1:
                        preparedStmt = conn.prepareStatement("update STUDENTS set STUDENT_ID=? where STUDENT_ID=?");

                        Scanner myObj2 = new Scanner(System.in);  // Create a Scanner object
                        System.out.println("Change student ID to: ");
                        String newID = myObj2.nextLine();  // Read user input

                        // check if a student with such ID exists in the system
                        if(studentExists(newID)){
                            System.out.println("Sorry! Student with such an ID already exists. Please try again. ");
                            continue;
                        }
                        //end of check

                        preparedStmt.setString(1, newID);
                        preparedStmt.setString(2, userID);

                        //update Student ID information in ENROLLMENT table
                        PreparedStatement preparedStmt2 = conn.prepareStatement("update ENROLLMENT set STUDENT_ID=? where STUDENT_ID=?");
                        preparedStmt2.setString(1, newID);
                        preparedStmt2.setString(2, userID);
                        preparedStmt2.execute();
                        preparedStmt2.close();
                        break;
                    case 2:
                        preparedStmt = conn.prepareStatement("update STUDENTS set STUDENT_NAME=? where STUDENT_ID=?");
                        Scanner myObj3 = new Scanner(System.in);
                        System.out.println("Change student name to: ");
                        String studName = myObj3.nextLine();
                        preparedStmt.setString(1, studName);
                        preparedStmt.setString(2, userID);
                        break;
                    case 3:
                        preparedStmt = conn.prepareStatement("update STUDENTS set DEPARTMENT=? where STUDENT_ID=?");
                        Scanner myObj4 = new Scanner(System.in);
                        System.out.println("Change department to (ex. CS):");
                        String department = myObj4.nextLine();
                        preparedStmt.setString(1, department);
                        preparedStmt.setString(2, userID);
                        break;
                    case 4:
                        preparedStmt = conn.prepareStatement("update STUDENTS set ADDRESS=? where STUDENT_ID=?");
                        Scanner myObj5 = new Scanner(System.in);
                        System.out.println("Change address to: ");
                        String address = myObj5.nextLine();
                        preparedStmt.setString(1, address);
                        preparedStmt.setString(2, userID);
                        break;
                    case 5:
                        preparedStmt = conn.prepareStatement("update STUDENTS set BIRTHDATE=? where STUDENT_ID=?");
                        Scanner myObj6 = new Scanner(System.in);
                        System.out.println("Change birthdate to (format: 'YYYY/MM/DD'): ");
                        String birthDate = myObj6.nextLine();
                        preparedStmt.setString(1, birthDate);
                        preparedStmt.setString(2, userID);
                        break;
                    case 6:
                        preparedStmt = conn.prepareStatement("update STUDENTS set GENDER=? where STUDENT_ID=?");
                        Scanner myObj7 = new Scanner(System.in);
                        System.out.println("Change gender to (possible values: MALE,FEMALE): ");
                        String gender = myObj7.nextLine();
                        preparedStmt.setString(1, gender);
                        preparedStmt.setString(2, userID);
                        break;
                    default:
                        System.out.println("Error! Please type a valid input.");
                        continue;
                }
                if (!done) {
                    preparedStmt.execute();
                    preparedStmt.close();

                    System.out.println("Edit is successfully made!");
                    continue;
                }
            }
            catch(Exception e){
                done=false;
                System.err.println("Got an exception!");
                System.err.println(e.getMessage());
            }

        }

        conn.close();
    }

    public static void administrator() throws SQLException {


        while (true) {

            System.out.println("Choose what you want to do: ");
            System.out.println("0. QUIT");
            System.out.println("1. List all the students in the department");
            System.out.println("2. Add new course");
            System.out.println("3. Add new student");
            System.out.println("4. Modify student and course information");
            System.out.println("5. Delete course and student table");
            System.out.println("6. Modify student's grade of the registered courses");
            //System.out.println("7. Top 5 students that have the most courses registered");
            //System.out.println("8. Top 5 students with the highest average grade values");

            Scanner myObj = new Scanner(System.in);  // Create a Scanner object
            System.out.println("Enter Choice: ");
            int enterNum = myObj.nextInt();  // Read user input
            switch (enterNum) {
                case 0:
                    System.out.println("BYE!");
                    break;
                case 1:
                    listAllStudents();
                    break;
                case 2:
                    addNewCourse();
                    break;
                case 3:
                    addNewStudent();
                    break;
                case 4:
                    modifyStudentAndCourseInfo();
                    break;
                case 5:
                    deleteCourseAndStudent();
                    break;
                case 6:
                    modifyStudentGrade();
                    break;
                //case 7:
                //topFiveStudentsCourses();
                //break;
                //case 8:
                //topFiveStudentsGradeHighest();
                //break;
                default:
                    System.out.println("Error! Please type a valid input.");
            }
            if(enterNum==0){
                break;
            }
        }

    }


    public static void listAllStudents() throws SQLException {

        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        OracleConnection conn = (OracleConnection)DriverManager.getConnection(DATABASE_URL, USER, PASS);
        String namer2;

        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery(
                "SELECT DISTINCT STUDENT_ID, STUDENT_NAME, DEPARTMENT, ADDRESS, BIRTHDATE, GENDER FROM STUDENTS");

        while (rset.next())  //true if contains any data on the next(current) line
        {
            namer2 = rset.getString(2);
            if (!rset.wasNull())
            {
                System.out.println(rset.getString(1) + " " + namer2  + " " +
                        rset.getString(3)  + " " + rset.getString(4) + " " + rset.getString(5)
                        + " " + rset.getString(6));
            }
        }
        System.out.println();

        stmt.close();
        rset.close();

        //smooth transition to previous menu
        Scanner scan = new Scanner(System.in);  // Create a Scanner object
        System.out.println("Type anything to continue (or just ENTER) ");
        scan.nextLine();

        conn.close();
    }


    public static void addNewCourse() throws SQLException {

        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        OracleConnection conn = (OracleConnection)DriverManager.getConnection(DATABASE_URL, USER, PASS);
        boolean done = false;
        while(!done){

            try
            {

                Scanner myObj = new Scanner(System.in);  // Create a Scanner object
                System.out.println("Enter course ID: ");
                String courseID = myObj.nextLine();  // Read user input

                System.out.println("Enter course title: ");
                String courseTitle = myObj.nextLine();  // Read user input

                System.out.println("Enter staff name: ");
                String staffName = myObj.nextLine();  // Read user input

                System.out.println("Enter section: ");
                String section = myObj.nextLine();  // Read user input

                // the mysql insert statement
                String query = "insert into COURSES (COURSE_ID, COURSE_TITLE, STAFF_NAME, SECTION)"
                        + " values (?, ?, ?, ?)";

                // create the mysql insert preparedstatement
                PreparedStatement preparedStmt = conn.prepareStatement(query);
                preparedStmt.setString (1, courseID);
                preparedStmt.setString (2, courseTitle);
                preparedStmt.setString (3, staffName);
                preparedStmt.setString (4, section);


                // execute the preparedstatement
                preparedStmt.execute();

                preparedStmt.close();
                done=true;

            }
            catch (Exception e)
            {
                done=false;
                System.err.println("Got an exception!");
                System.err.println(e.getMessage());

            }

            System.out.println("New course is successfully added");

            Scanner scan = new Scanner(System.in);
            System.out.println("Type 1 if you want to add one more course, anything else if you want to quit back to previous menu");
            String inp = scan.nextLine();
            if(inp.equals("1")){
                done=false;
            }else{
                done=true;
            }
        }


        conn.close();
    }

    public static void addNewStudent() throws SQLException {

        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        OracleConnection conn = (OracleConnection)DriverManager.getConnection(DATABASE_URL, USER, PASS);
        boolean done = false;
        while(!done){

            try
            {

                Scanner myObj = new Scanner(System.in);  // Create a Scanner object
                System.out.println("Enter student ID: ");
                String studentID = myObj.nextLine();  // Read user input

                System.out.println("Enter student name: ");
                String studentName = myObj.nextLine();  // Read user input

                System.out.println("Enter department: ");
                String department = myObj.nextLine();  // Read user input

                System.out.println("Enter address: ");
                String address = myObj.nextLine();  // Read user input

                System.out.println("Enter birthdate: ");
                String birthdate = myObj.nextLine();  // Read user input

                System.out.println("Enter gender: ");
                String gender = myObj.nextLine();  // Read user input

                // the mysql insert statement
                String query = "insert into STUDENTS (STUDENT_ID, STUDENT_NAME, DEPARTMENT, ADDRESS, BIRTHDATE, GENDER)"
                        + " values (?, ?, ?, ?, ?, ?)";



                // create the mysql insert preparedstatement
                PreparedStatement preparedStmt = conn.prepareStatement(query);
                preparedStmt.setString (1, studentID);
                preparedStmt.setString (2, studentName);
                preparedStmt.setString (3, department);
                preparedStmt.setString (4, address);
                preparedStmt.setString (5, birthdate);
                preparedStmt.setString (6, gender);


                // execute the preparedstatement
                preparedStmt.execute();


                preparedStmt.close();


            }
            catch (Exception e)
            {

                System.err.println("Got an exception!");
                System.err.println(e.getMessage());

            }

            System.out.println("New student is successfully added");

            Scanner scan = new Scanner(System.in);
            System.out.println("Type 1 if you want to add one more student, anything else if you want to quit back to previous menu");
            String inp = scan.nextLine();
            if(inp.equals("1")){
                done=false;
            }else{
                done=true;
            }
        }
        conn.close();
    }

    public static void modifyStudentAndCourseInfo() throws SQLException {
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        OracleConnection conn = (OracleConnection)DriverManager.getConnection(DATABASE_URL, USER, PASS);
        boolean done = false;
        while (!done) {
            System.out.println("What do you want to do? : ");
            System.out.println("0. Quit");
            System.out.println("1. Modify course information");
            System.out.println("2. Modify student information");

            Scanner myObj = new Scanner(System.in);  // Create a Scanner object
            System.out.println("Select the table to be modified(1 or 2): ");
            int choice = myObj.nextInt();  // Read user input

            if (choice == 1) {
                while (true) {
                    System.out.println("Input the ID of the course you want change information about: ");
                    String courseID = myObj.nextLine();

                    if (!courseExists(courseID)) {
                        System.out.println("Sorry! Course with such an ID does not exist. Please try again. ");
                        continue;
                    }
                    editCourseInfo(courseID);
                }

            } else if (choice == 2) {

                while (true) {
                    System.out.println("Input the ID of the student you want change information about: ");
                    String userID = myObj.nextLine();
                    //check if such student exists in the system
                    if (!studentExists(userID)) {
                        System.out.println("Sorry! Student with such an ID does not exist. Please try again. ");
                        continue;
                    }

                    editPersonalInfo(userID);
                }

            } else if (choice == 0) {
                System.out.println("Thanks for using student course registration system. See you next time!");
                break;
            } else {
                System.out.println("Invalid input. Please try again.");
            }
        }
        conn.close();
    }


    public static void editCourseInfo(String courseID) throws SQLException {

        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        OracleConnection conn = (OracleConnection)DriverManager.getConnection(DATABASE_URL, USER, PASS);
        PreparedStatement preparedStmt=null;
        Statement stmt;
        boolean done = false;
        while (!done) {

            try {
                System.out.println("Choose what you would like to edit");
                System.out.println("0. QUIT");
                System.out.println("1. Course ID");
                System.out.println("2. Course title");
                System.out.println("3. Staff name ");
                System.out.println("4. Section");

                Scanner myObj = new Scanner(System.in);  // Create a Scanner object
                System.out.println("Enter Choice: ");
                int choice = myObj.nextInt();  // Read user input
                switch (choice) {
                    case 0:
                        System.out.println("BYE!");
                        done = true;
                        break;
                    case 1:
                        preparedStmt = conn.prepareStatement("update COURSES set COURSE_ID=? where COURSE_ID=?");
                        System.out.println("Change course ID to: ");
                        String newID = myObj.nextLine();

                        // check if a student with such ID exists in the system
                        stmt = conn.createStatement();
                        ResultSet rset = stmt.executeQuery("SELECT DISTINCT COURSE_ID FROM COURSES");
                        boolean found=false;
                        while (rset.next())  //true if contains any data on the next(current) line
                        {
                            String existingCourseID = rset.getString(1);
                            if (existingCourseID.equals(newID))
                            {
                                found=true;
                                break;
                            }
                        }

                        rset.close();
                        stmt.close();

                        if(found){
                            System.out.println("Sorry! Student with such an ID already exists. Please try again. ");
                            continue;
                        }
                        //end of check

                        preparedStmt.setString(1, newID);
                        preparedStmt.setString(2, courseID);

                        //update Student ID information in ENROLLMENT table
                        PreparedStatement preparedStmt2 = conn.prepareStatement("update ENROLLMENT set COURSE_ID=? where COURSE_ID=?");
                        preparedStmt2.setString(1, newID);
                        preparedStmt2.setString(2, courseID);
                        preparedStmt2.execute();
                        preparedStmt2.close();
                        break;
                    case 2:
                        preparedStmt = conn.prepareStatement("update COURSES set COURSE_TITLE=? where COURSE_ID=?");
                        System.out.println("Change course title to (ex. Fundamentals of Computing): ");
                        String courseTitle = myObj.nextLine();
                        preparedStmt.setString(1, courseTitle);
                        preparedStmt.setString(2, courseID);
                        break;
                    case 3:
                        preparedStmt = conn.prepareStatement("update COURSES set STAFF_NAME=? where COURSE_ID=?");
                        System.out.println("Change staff name to (ex. Ka Chun K. LEE):");
                        String staffName = myObj.nextLine();
                        preparedStmt.setString(1, staffName);
                        preparedStmt.setString(2, courseID);
                        break;
                    case 4:
                        preparedStmt = conn.prepareStatement("update COURSES set SECTION=? where COURSE_ID=?");
                        System.out.println("Change section to: ");
                        String section = myObj.nextLine();
                        preparedStmt.setString(1, section);
                        preparedStmt.setString(2, courseID);
                        break;
                    default:
                        System.out.println("Error! Please type a valid input.");
                        continue;
                }
                if (!done) {
                    preparedStmt.execute();
                    preparedStmt.close();

                    System.out.println("Edit is successfully made!");
                    continue;
                }
            }
            catch(Exception e){
                done=false;
                System.err.println("Got an exception!");
                System.err.println(e.getMessage());
            }

        }

        conn.close();
    }

    public static void deleteCourseAndStudent() throws SQLException {

        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        OracleConnection conn = (OracleConnection)DriverManager.getConnection(DATABASE_URL, USER, PASS);
        boolean done = false;
        String studentID = null;
        PreparedStatement preparedStmt=null;
        while (!done) {
            try {

                System.out.println("What do you want to do? : ");
                System.out.println("0. Quit");
                System.out.println("1. Delete a course");
                System.out.println("2. Delete a student\n");

                Scanner myObj = new Scanner(System.in);  // Create a Scanner object
                int choice = myObj.nextInt();  // Read user input

                if (choice == 1) {

                        System.out.println("Input the ID of the course you want to delete: ");
                        Scanner myObj2 = new Scanner(System.in);
                        String courseID = myObj2.nextLine();

                        if (!courseExists(courseID)) {
                            System.out.println("Sorry! Course with such an ID does not exist. Please try again. ");
                            continue;
                        }

                        //deleting item from course list
                        preparedStmt = conn.prepareStatement("DELETE FROM COURSES WHERE COURSE_ID = ?");
                        preparedStmt.setString(1, courseID);
                        preparedStmt.execute();
                        preparedStmt.close();

                        //deleting item from registered courses list
                        PreparedStatement preparedStmt2 = conn.prepareStatement("DELETE FROM ENROLLMENT WHERE COURSE_ID = ?");
                        preparedStmt2.setString(1, courseID);
                        preparedStmt2.execute();
                        preparedStmt2.close();

                        System.out.println("Deleted ");


                } else if (choice == 2) {

                    while (true) {
                        System.out.println("Input the ID of the student you want to delete: ");
                        studentID = myObj.nextLine();
                        //check if such student exists in the system
                        if (!studentExists(studentID)) {
                            System.out.println("Sorry! Student with such an ID does not exist. Please try again. ");
                            continue;
                        }
                        preparedStmt = conn.prepareStatement("DELETE FROM STUDENTS WHERE STUDENT_ID = ?");
                        preparedStmt.setString(1, studentID);
                        preparedStmt.execute();
                        preparedStmt.close();
                    }

                } else if (choice == 0) {
                    System.out.println("Thanks for using student course registration system. See you next time!");
                    break;
                } else {
                    System.out.println("Invalid input. Please try again.");
                }

                //deleting item from student list
                preparedStmt = conn.prepareStatement("DELETE FROM STUDENTS WHERE STUDENT_ID = ?");
                preparedStmt.setString(1, studentID);
                preparedStmt.execute();
                preparedStmt.close();

                //deleting item from registered courses list
                preparedStmt = conn.prepareStatement("DELETE FROM ENROLLMENT WHERE STUDENT_ID = ?");
                preparedStmt.setString(1, studentID);
                preparedStmt.execute();
                preparedStmt.close();

            } catch(Exception e) {
                done = false;
                System.err.println("Got an exception!");
                System.err.println(e.getMessage());
            }

        }
        conn.close();
    }


    public static void modifyStudentGrade() throws SQLException {

        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        OracleConnection conn = (OracleConnection)DriverManager.getConnection(DATABASE_URL, USER, PASS);
        boolean done = false;
        while (!done) {

            try {

                System.out.println("Input information about the student and the course the grade of which you want to modify. ");
                System.out.println("Enter the student's ID: ");
                Scanner myObj = new Scanner(System.in);  // Create a Scanner object
                String studentID = myObj.nextLine();  // Read user input

                System.out.println("Enter the course ID: ");
                String courseID = myObj.nextLine();  // Read user input

                System.out.println("Enter the new grade ");
                int grade = myObj.nextInt();  // Read user input

                PreparedStatement preparedStmt = conn.prepareStatement("update ENROLLMENT set GRADE=? where STUDENT_ID=? and COURSE_ID=?");
                preparedStmt.setInt(1, grade);
                preparedStmt.setString(2, studentID);
                preparedStmt.setString(3, courseID);

                preparedStmt.execute();
                preparedStmt.close();

            }
            catch(Exception e)
            {
                done=false;
                System.err.println("Got an exception!");
                System.err.println(e.getMessage());
            }

        }
        System.out.println("Edit is successfully made!");

        conn.close();
    }



    /*public void topFiveStudentsCourses() {

    }

    public void topFiveStudentsGradeHighest() {

    }*/

    public static boolean studentExists(String newID) throws SQLException {
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        OracleConnection conn2 = (OracleConnection)DriverManager.getConnection(DATABASE_URL, USER, PASS);
        // check if a student with such ID exists in the system
        Statement stmt = conn2.createStatement();
        ResultSet rset = stmt.executeQuery("SELECT DISTINCT STUDENT_ID FROM STUDENTS");
        boolean found=false;
        while (rset.next())  //true if contains any data on the next(current) line
        {
            String studID = rset.getString(1);
            if (newID.equals(studID))
            {
                found=true;
                break;
            }
        }

        rset.close();
        stmt.close();

        conn2.close();
        return found;
        //end of check
    }

    public static boolean courseExists(String courseID) throws SQLException {
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        OracleConnection conn2 = (OracleConnection)DriverManager.getConnection(DATABASE_URL, USER, PASS);
        // check if a student with such ID exists in the system
        Statement stmt = conn2.createStatement();
        ResultSet rset = stmt.executeQuery("SELECT DISTINCT COURSE_ID FROM COURSES");
        boolean found=false;
        while (rset.next())  //true if contains any data on the next(current) line
        {
            String existingCourseID = rset.getString(1);
            if (existingCourseID.equals(courseID))
            {
                found=true;
                break;
            }
        }

        rset.close();
        stmt.close();

        conn2.close();
        return found;
        //end of check
    }


}



