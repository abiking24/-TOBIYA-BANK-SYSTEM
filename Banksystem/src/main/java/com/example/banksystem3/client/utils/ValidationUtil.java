package com.example.banksystem3.client.utils;

import java.util.regex.Pattern;

public class ValidationUtil {

    // Regex patterns
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+251|0)[0-9]{9}$"); // Matches +251XXXXXXXXX or 0XXXXXXXXX
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern NATIONAL_ID_PATTERN = Pattern.compile("^[0-9]{6,20}$");

    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidNationalId(String nationalId) {
        return nationalId != null && NATIONAL_ID_PATTERN.matcher(nationalId).matches();
    }

    public static boolean isPositiveDouble(String str) {
        if (str == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(str);
            return d > 0;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}