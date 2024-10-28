import java.util.Scanner;
import java.time.LocalDate;
import java.util.regex.Pattern;

public class hello{
    public static void main(String []args){
        //customer
        LocalDate birthDate = LocalDate.of(2003, 7, 23); 
        customer customer = new customer("ahmed", "rashed", "0945555555", "ar5363", "ahmedrashed@gmail.com", birthDate);
        System.out.println(customer.getAge());

        
    }
}