public class Author {
    private int authorId;
    private String name;
    private String surname;

    public Author(int a, String n, String s) {
        authorId = a;
        name = n;
        surname = s;
    }



    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public int getAuthorId() {
        return authorId;
    }

    public String getName() {
        return name.trim();
    }

    public String getSurname() {
        return surname.trim();
    }
}