import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

public class customer {

    private String fullname;
    private String lastname;
    private String phone;
    private String passport;
    private String email;
    private LocalDate birthDate; 
    private int age;

    public customer(String fullname, String lastname, String phone, String passport, String email, LocalDate birthDate) {
        this.fullname = fullname;
        this.lastname = lastname;
        this.phone = phone;
        this.passport = passport;
        this.email = email;
        this.birthDate = birthDate;
        this.age = calculateAge(); 
    }

    public String getFullname() {
        return fullname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassport() {
        return passport;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public int getAge() {
        return age;
    }

    private int calculateAge() {
        LocalDate currentDate = LocalDate.now();
        if (birthDate != null) {
            return Period.between(birthDate, currentDate).getYears();
        }
        return 0; 
    }

    // دالة للتحقق من صحة البريد الإلكتروني
    public boolean isValidEmail() {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    // دالة للتحقق من صحة رقم الهاتف
   
    public boolean isValidPhone() {
        String phoneRegex = "^\+?[0-9]{10,15}$"; // يمكنك تعديل النمط حسب الحاجة
        Pattern pattern = Pattern.compile(phoneRegex);
        return pattern.matcher(phone).matches();
    }
}
