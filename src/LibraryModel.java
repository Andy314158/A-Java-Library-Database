import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.*;

public class LibraryModel {


    private JFrame dialogParent;
    private Connection connection;


    public LibraryModel(JFrame parent, String userid, String password) {
        dialogParent = parent;

        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql:" + "//db.ecs.vuw.ac.nz/" + userid + "_jdbc";
            connection = DriverManager.getConnection(url, userid, password);
            System.out.println("Connected to database: " + url);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        System.out.print("DATABASE CONNECT");
    }



    public String bookLookup(int isbn) {

        String query = "SELECT isbn, authorid, authorseqno, title FROM book_author NATURAL JOIN book WHERE isbn="+ isbn + "ORDER BY authorseqno";

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            String title = "";

            ArrayList<String> authors = new ArrayList<String>();
            ArrayList<Integer> authorIDs = new ArrayList<Integer>();

            while(rs.next()){
                authorIDs.add(rs.getInt("authorid"));
                title=rs.getString("title");
            }

                for (Integer i : authorIDs) {
                query = "SELECT authorid, name FROM author WHERE authorid=" + i;
                rs = statement.executeQuery(query);
                rs.next();
                authors.add(rs.getString("name"));
            }

            statement.close();
            String s = "Book Title: "+title+"\n"+"Author/s: ";
            for(String name:authors){
                s+=", "+name.trim();
            }
            return s;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Failed Book LookUp query";
    }


    public String showCatalogue() {

        String query = ""
                + "SELECT DISTINCT title, edition_no, numofcop, numleft, isbn "
                + "FROM book "
                + "ORDER BY isbn";

        try {
            ArrayList<Book> books = new ArrayList<Book>();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            String title;
            int edNo;
            int copies;
            int numL;
            int isbn;

            String result = "Catalogue"+"\n";
            while (rs.next()) {
                title = rs.getString("title");
                edNo = rs.getInt("edition_no");
                copies = rs.getInt("numofcop");
                numL = rs.getInt("numleft");
                isbn = rs.getInt("isbn");

                query = ""
                        + "SELECT surname "
                        + "FROM book_author "
                        + "NATURAL JOIN author "
                        + "WHERE isbn = " + isbn
                        + " ORDER BY authorseqno";

                statement = connection.createStatement();
                ResultSet rs2 = statement.executeQuery(query);
                String authors = "";
                while(rs2.next())
                    authors+=", "+rs2.getString("surname").trim();

                books.add(new Book(isbn, title, edNo, copies, numL,authors));

            }
            statement.close();
            rs.close();
            for(Book book: books){
                result = result + ""
                        + book.getIsbn() + ": " + book.getTitle() + "\n"
                        + "\tEdition: " + book.getEditionNo() + " - Number of copies: " + book.getNumOfCop() + " - Copies left: " + book.getNumLeft()+ "\n"
                        + "\tAuthors: "+book.getAuthors()+"\n";

                }
                return result + "\n";
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Failed Catalogue Query";
    }




    public String showLoanedBooks() {
        String query = ""
                + "SELECT isbn, customerid "
                + "FROM cust_book "
                + "ORDER By isbn";

        try {
            ArrayList<LoanedBook> loanedBooks = new ArrayList<LoanedBook>();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);


            if(!rs.isBeforeFirst()){
                return "No books loaned";
            } else{
                while(rs.next()){
                    int isbn = rs.getInt("isbn");
                    int customerId = rs.getInt("customerid");
                    boolean preAdded = false;
                    for(LoanedBook storedBook: loanedBooks){
                        if(storedBook.getIsbn() == isbn){
                            storedBook.getBorrowers().add(new Customer(customerId, "No LastName", "No FirstName", "no City"));
                            preAdded = true;
                            break;
                        }
                    }
                    if(preAdded == false){
                        LoanedBook book = new LoanedBook(isbn, "no title", -1, -1, -1);
                        book.getBorrowers().add(new Customer(customerId, "No LastName", "No FirstName", "no City"));
                        loanedBooks.add(book);
                    }


                }
            }
            statement.close();
            rs.close();
            for(LoanedBook loanedBook : loanedBooks){
                query = ""
                        + "SELECT title, edition_no, numofcop, numleft "
                        + "FROM book "
                        + "WHERE isbn = " + loanedBook.getIsbn();
                statement = connection.createStatement();
                rs = statement.executeQuery(query);
                if(!rs.isBeforeFirst()){
                    return "No books from loaned books exist in books";
                } else{
                    while(rs.next()){
                        loanedBook.setTitle(rs.getString("title"));
                        loanedBook.setEditionNo(rs.getInt("edition_no"));
                        loanedBook.setNumOfCop(rs.getInt("numofcop"));
                        loanedBook.setNumleft(rs.getInt("numleft"));
                    }
                }
                query = ""
                        + "SELECT authorid, name, surname "
                        + "FROM author "
                        + "NATURAL JOIN book_author "
                        + "WHERE isbn = " + loanedBook.getIsbn()
                        + " ORDER By authorseqno";
                statement = connection.createStatement();
                rs = statement.executeQuery(query);
                while(rs.next()){
                    int authorId = rs.getInt("authorid");
                    String name = rs.getString("name");
                    String surname = rs.getString("surname");
                    loanedBook.getAuthor().add(new Author(authorId,name,surname));
                }
                for(Customer borrower: loanedBook.getBorrowers()){
                    query = ""
                            + "SELECT l_name, f_name, city "
                            + "FROM customer "
                            + "WHERE customerid = " + borrower.getCustomerId();
                    statement = connection.createStatement();
                    rs = statement.executeQuery(query);
                    while(rs.next()){
                        borrower.setLastName(rs.getString("l_name"));
                        borrower.setFirstName(rs.getString("f_name"));
                        borrower.setCity(rs.getString("city"));
                    }
                }

            }
            String result = "Show Loaned books: \n";
            for(LoanedBook loanedBook: loanedBooks){
                result = result + "\n"
                        + loanedBook.getIsbn() + ": " + loanedBook.getTitle() + "\n"
                        + "\tEdition: "  + loanedBook.getEditionNo() + " - Number of copies: " + loanedBook.getNumOfCop() + " - Copies left: " + loanedBook.getNumLeft() + "\n"
                        + "\tAuthors: ";
                for(Author author : loanedBook.getAuthor()){
                    result = result + author.getSurname().trim() + ", ";
                }

                result = result + "\n\tBorrowers:\n";
                for(Customer borrower : loanedBook.getBorrowers()){
                    result = result + "\t\t" + borrower.getCustomerId() + ": " + borrower.getLastName().trim() + ", " + borrower.getFirstName().trim() + " - " + borrower.getCity() + "\n";
                }
            }
            return result;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Failed ShowAuthor Query";
    }





    public String showAuthor(int authorID) {
        String query = ""
                + "SELECT name, surname "
                + "FROM author "
                + "WHERE authorid = " + authorID;

        String result="Show Author:\n";

        try {

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            if (!rs.isBeforeFirst()) {
                return "invalid authorID";
            }
            else {
                while (rs.next()) {
                    result+=(rs.getString("name")).trim()+" ";
                    result+=(rs.getString("surname")).trim()+"\n";
                }
            }

            statement.close();
            rs.close();

            query = ""
                    + "SELECT isbn, title "
                    + "FROM book_author "
                    + "NATURAL JOIN book "
                    + "WHERE authorid = " + authorID
                    + " ORDER BY isbn";
            statement = connection.createStatement();
            rs = statement.executeQuery(query);

            result += "\tBooks Written:" + "\n";

            while (rs.next()) {
                result = result + "\t\t" + rs.getString("isbn").trim() + " - " + rs.getString("title") + "\n";
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Failed ShowAuthor Query";
    }





    public String showAllAuthors() {
        String query = ""
                + "SELECT authorid, name, surname "
                + "FROM author "
                + "ORDER BY authorid";

        try {
            ArrayList<Author> authors = new ArrayList<Author>();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            String result = "Show All Authors:\n";

            while (rs.next()) {
                result+= rs.getString("name").trim();
                result+= rs.getString("surname").trim()+"\n";
            }
            statement.close();
            rs.close();
            return result;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Failed ShowAuthor Query";
    }





    public String showCustomer(int customerID) {
        String query = ""
                + "SELECT f_name, l_name, city "
                + "FROM customer "
                + "WHERE customerid = " + customerID;

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            String result = "Show Customer:\n";
            if (!rs.isBeforeFirst()) {
                return "invalid customerID";
            } else {
                while (rs.next()) {
                    result+=(rs.getString("f_name")).trim()+" ";
                    result+=(rs.getString("l_name")).trim()+"\n";
                }
            }
            statement.close();
            rs.close();

            query = ""
                    + "SELECT isbn, title "
                    + "FROM cust_book "
                    + "NATURAL JOIN book "
                    + "WHERE customerid = " + customerID
                    + " ORDER BY isbn";

            statement = connection.createStatement();
            rs = statement.executeQuery(query);

            if (!rs.isBeforeFirst()) {
                result = result + "\t(no books borrowed)";
            } else {
                result = result + "\tBooks Borrowed:" + "\n";
                while (rs.next()) {
                    result = result + "\t\t" + rs.getString("isbn").trim() + " - " + rs.getString("title").trim() + "\n";
                }
            }

            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Failed ShowCustomer Query";
    }





    public String showAllCustomers() {
        String query = ""
                + "SELECT customerid, l_name, f_name, city "
                + "FROM customer "
                + "ORDER BY customerid";

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            String result = "Show All Customers:\n";

            while (rs.next()) {
                result+= rs.getString("f_name").trim()+" ";
                result+= rs.getString("l_name").trim()+", City: ";
                result+= rs.getString("city")+"\n";


            }
            statement.close();
            rs.close();
            return result;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Failed ShowAuthor Query";
    }





    public String borrowBook(int isbn, int customerID, int day, int month, int year) {
        String query = ""
                + "SELECT l_name, f_name "
                + "FROM customer "
                + "WHERE customerid = " + customerID;

        try {
            String result="Name: ";
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            if (!rs.isBeforeFirst()) {
                return "No such customer ID : " + customerID;
            } else {
                while (rs.next()) {
                    result+= rs.getString("l_name").trim()+" ";
                    result+= rs.getString("f_name").trim()+"\n";
                }
            }
            statement.close();
            rs.close();

            query = ""
                    + "SELECT title "
                    + "FROM book "
                    + "WHERE isbn = " + isbn + " "
                    + "AND numleft > 0";
            statement = connection.createStatement();
            rs = statement.executeQuery(query);
            String bookTitle = "";

            if (!rs.isBeforeFirst()) {
                return "Book not available for booking: " + isbn;
            } else {
                while (rs.next()) {
                    bookTitle = rs.getString("title");
                }
            }
            statement.close();
            rs.close();

            query = ""
                    + "SELECT * "
                    + "FROM cust_book "
                    + "WHERE customerid = " + customerID + " "
                    + "AND isbn = " + isbn;

            statement = connection.createStatement();
            rs = statement.executeQuery(query);

            if (rs.isBeforeFirst()) {
                return "Book: " + isbn + ", is already being borrowed by the customer";
            }
            statement.close();
            rs.close();


            String update = ""
                    + "INSERT INTO cust_book (isbn, duedate, customerid) "
                    + "VALUES ('" + isbn + "', '" + year + "-" + month + "-" + day + "', '" + customerID + "')";
            statement = connection.createStatement();

            int updateResult = statement.executeUpdate(update);

            if (updateResult != 1) {
                return "Could not correctly borrow book";
            } else {
                statement.close();

                update = ""
                        + "UPDATE book SET numleft = numleft-1 "
                        + "WHERE isbn = " + isbn;
                statement = connection.createStatement();

                updateResult = statement.executeUpdate(update);
                if (updateResult != 1) {
                    return "Could not alter No of Books borrowed.";
                } else {
                    return result + "Borrow Book:\n"
                            + "\tBook: " + isbn + " (" + bookTitle.trim() + ")\n"
                            + "\tDue Date: " + day + "," + month + "," + year;
                }

            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Failed borrowBook Query";
    }





    public String returnBook(int isbn, int customerid) {
        String query = ""
                + "SELECT * "
                + "FROM cust_book "
                + "WHERE isbn = " + isbn + " "
                + "AND customerid = " + customerid;

        try {

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            if (!rs.isBeforeFirst()) {
                return "Book (isbn =" + isbn + ") Book not borrowed by customerid: " + customerid;
            }
            statement.close();
            rs.close();

            query = ""
                    + "SELECT * "
                    + "FROM book "
                    + "WHERE isbn = " + isbn + " ";

            statement = connection.createStatement();
            rs = statement.executeQuery(query);

            if (!rs.isBeforeFirst()) {
                return "Book does not exist in the book table";
            }
            statement.close();
            rs.close();

            String delete = ""
                    + "DELETE "
                    + "FROM cust_book "
                    + "WHERE isbn = " + isbn + " "
                    + "AND customerid = " + customerid;

            statement = connection.createStatement();
            int deleteResult = statement.executeUpdate(delete);

            if (deleteResult != 1) {
                return "could not successfully delete booking from cust_book table ";
            }

            statement.close();
            String update = ""
                    + "UPDATE book SET numleft = numleft+1 "
                    + "WHERE isbn = " + isbn;
            statement = connection.createStatement();
            int updateResult = statement.executeUpdate(update);

            if (updateResult != 1) {
                return "Could not alter the number of available books";
            } else {
                return "Book return:\n"
                        + "\tBook: " + isbn + " returned by " + customerid;

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Failed returnBook Query";
    }




    public void closeDBConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public String deleteCus(int customerID) {
        return "Failed delete Customer Query";
    }


    public String deleteAuthor(int authorID) {
        return "Failed delete Author Query";
    }

    public String deleteBook(int isbn) {
    return "";
    }
}
