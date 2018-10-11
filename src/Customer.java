public class Customer {
    private int customerId;
    private String lastName;
    private String firstName;
    private String city;

    public Customer(int cid, String l,String f, String c){
        customerId = cid;
        lastName = l;
        firstName = f;
        city = c;
    }


    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String surname) {
        this.lastName = surname;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String name) {
        this.firstName = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

}