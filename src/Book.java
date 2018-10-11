import java.util.ArrayList;

public class Book implements Comparable{
    private int isbn;
    private String title;
    private int editionNo;
    private int numOfCop;
    private int numLeft;
    private String authors;



    public Book(int i, String t, int e, int numo, int numl,String authors){
        this.authors=authors;
        isbn = i;
        title = t;
        editionNo = e;
        numOfCop = numo;
        numLeft = numl;
    }

    public int getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public int getEditionNo() {
        return editionNo;
    }

    public int getNumOfCop() {
        return numOfCop;
    }

    public int getNumLeft() {
        return numLeft;
    }

    public void setIsbn(int isbn) {
        this.isbn = isbn;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setEditionNo(int edition_no) {
        this.editionNo = edition_no;
    }

    public void setNumOfCop(int numOfCop) {
        this.numOfCop = numOfCop;
    }

    public void setNumleft(int numLeft) {
        this.numLeft = numLeft;
    }

    public String getAuthors(){
        if(authors.startsWith(", "))
            return authors.substring(2).trim();
        return authors;
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof Book){
            Book b= (Book)o;
         if(this.isbn>b.getIsbn())
             return 1;
         else if (this.isbn<b.getIsbn())
             return -1;
         else return 0;
        }
        return -2;
    }
}
