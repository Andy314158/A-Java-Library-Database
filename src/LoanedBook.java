import java.util.ArrayList;

public class LoanedBook extends Book {

    private ArrayList<Customer> borrowers;
    private ArrayList<Author> authors;

    public LoanedBook(int isbn, String title, int editionNo, int numOfCop, int numLeft) {
        super(isbn, title, editionNo, numOfCop, numLeft,"");
        borrowers = new ArrayList<Customer>();
        authors = new ArrayList<Author>();
    }

    public ArrayList<Customer> getBorrowers() {
        return borrowers;
    }

    public ArrayList<Author> getAuthor() {
        return authors;
    }

}