package com.fintech.Util;

import com.fintech.Exception.BadRequestException;
import com.fintech.Model.Entity.User;

import java.util.regex.Pattern;

public class UserAccountValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");


    public static void validateName(String name){
        if (isBlank(name)) {
            throw new BadRequestException("User name is required");
        }

        if (name.length() < 3) {
            throw new BadRequestException("User name must have at least 3 characters");
        }
    }

    public static void validateEmail(String email){
        if (isBlank(email)) {
            throw new BadRequestException("Email is required");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BadRequestException("Invalid email format");
        }
    }

    public static void validatePassword(String password){
        if (isBlank(password)) {
            throw new BadRequestException("Password is required");
        }

        if (password.length() < 8) {
            throw new BadRequestException("Password must have at least 8 characters");
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new BadRequestException("Invalid password format");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
