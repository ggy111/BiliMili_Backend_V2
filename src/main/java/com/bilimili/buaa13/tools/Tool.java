package com.bilimili.buaa13.tools;

import java.security.MessageDigest;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import java.util.regex.Pattern;

public class Tool {

    // 密码长度限制
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 20;

    // Token长度限制
    private static final int TOKEN_LENGTH = 32;

    // 验证码长度限制
    private static final int CODE_LENGTH = 6;

    // 邮件验证正则表达式
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    // SecureRandom 实例
    private static final SecureRandom random = new SecureRandom();

    // 电话号码验证正则表达式
    private static final String PHONE_REGEX = "^\\+?[0-9. ()-]{7,}$";

    // 文件名验证正则表达式
    private static final String FILENAME_REGEX = "^[\\w,\\s-]+\\.[A-Za-z]{3,4}$";

    // 文件类型白名单
    private static final String[] ALLOWED_FILE_TYPES = {"mp4", "avi", "mov", "mkv", "flv"};

    // 时间戳格式
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // URL 编码
    public static String encodeURL(String input) {
        try {
            return URLEncoder.encode(input, java.nio.charset.StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 encoding is not supported", e);
        }
    }

    // URL 解码
    public static String decodeURL(String input) {
        try {
            return URLDecoder.decode(input, java.nio.charset.StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 encoding is not supported", e);
        }
    }

    // 随机生成用户名
    public static String generateRandomUsername(String prefix) {
        int randomNumber = random.nextInt(100000);
        return prefix + randomNumber;
    }

    // 生成安全的随机密码
    public static String generateSecureRandomPassword(int length) {
        if (length < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password length should be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }

        StringBuilder password = new StringBuilder(length);
        while (password.length() < length) {
            int charType = random.nextInt(4);
            switch (charType) {
                case 0 -> password.append((char) (random.nextInt(26) + 'a')); // 小写字母
                case 1 -> password.append((char) (random.nextInt(26) + 'A')); // 大写字母
                case 2 -> password.append((char) (random.nextInt(10) + '0')); // 数字
                case 3 -> password.append("!@#$%^&*()-_=+[]{}|;:'\",.<>/?".charAt(random.nextInt(32))); // 特殊字符
            }
        }
        return password.toString();
    }

    // 验证电话号码格式是否有效
    public static boolean isPhoneNumberValid(String phoneNumber) {
        if (phoneNumber == null) return false;
        return Pattern.compile(PHONE_REGEX).matcher(phoneNumber).matches();
    }

    // 计算密码的强度
    public static int calculatePasswordStrength(String password) {
        int strength = 0;

        if (password.length() >= 8) strength++;
        if (password.length() >= 12) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*\\d.*")) strength++;
        if (password.matches(".*[!@#$%^&*()-_=+\\[\\]{}|;:'\",.<>/?].*")) strength++;

        return strength > 5 ? 5 : strength;
    }

    // 检查文件名是否符合视频网站的命名规范
    public static boolean isFilenameValid(String filename) {
        if (filename == null) return false;
        return Pattern.compile(FILENAME_REGEX).matcher(filename).matches();
    }

    // 检查文件类型是否符合视频网站的要求
    public static boolean isFileTypeAllowed(String filename) {
        if (!isFilenameValid(filename)) return false;

        String fileExtension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        for (String type : ALLOWED_FILE_TYPES) {
            if (type.equals(fileExtension)) {
                return true;
            }
        }
        return false;
    }

    // 将时间戳格式化为特定格式的字符串
    public static String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_FORMAT);
        return sdf.format(new Date(timestamp));
    }

    // 检查某个字符串是否符合密码的合法格式
    public static boolean isPasswordValid(String password) {
        if (password == null) return false;
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) return false;

        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (isSpecialCharacter(c)) {
                hasSpecialChar = true;
            }

            if (hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar) {
                return true;
            }
        }

        return false;
    }

    // 检查字符是否为特殊字符
    private static boolean isSpecialCharacter(char c) {
        String specialChars = "!@#$%^&*()-_=+[]{}|;:'\",.<>/?";
        return specialChars.indexOf(c) >= 0;
    }

    // 创建一个随机的 token
    public static String generateRandomToken() {
        byte[] token = new byte[TOKEN_LENGTH];
        random.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    // 验证电子邮件格式是否有效
    public static boolean isEmailValid(String email) {
        if (email == null) return false;
        return Pattern.compile(EMAIL_REGEX).matcher(email).matches();
    }

    // 使用 SHA-256 对字符串进行加密
    public static String hashWithSHA256(String input) {
        if (input == null) return null;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    // 生成唯一的用户 ID
    public static String generateUniqueUserID(String email) {
        String input = email + System.currentTimeMillis();
        return hashWithSHA256(input);
    }

    // 生成随机的数字验证码
    public static String generateRandomCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10)); // 生成 0-9 之间的数字
        }

        return code.toString();
    }

    // 打印工具类的描述
    public static void printClassDescription() {
        System.out.println("Tool 工具类提供以下功能：");
        System.out.println("1. 检查密码合法性");
        System.out.println("2. 生成随机 Token");
        System.out.println("3. 验证电子邮件格式");
        System.out.println("4. 使用 SHA-256 对字符串进行加密");
        System.out.println("5. 生成唯一用户 ID");
        System.out.println("6. 生成随机数字验证码");
    }

    public static void print( ) {
        // 调用扩展方法进行测试

        // URL 编码与解码
        String url = "https://www.example.com/?query=test value";
        String encodedURL = encodeURL(url);
        System.out.println("编码后的URL: " + encodedURL);
        String decodedURL = decodeURL(encodedURL);
        System.out.println("解码后的URL: " + decodedURL);

        // 生成随机用户名
        String username = generateRandomUsername("User_");
        System.out.println("生成的随机用户名: " + username);

        // 生成安全的随机密码
        String password = generateSecureRandomPassword(12);
        System.out.println("生成的随机密码: " + password);

        // 验证电话号码格式
        String phoneNumber = "+1234567890";
        boolean isPhoneNumberValid = isPhoneNumberValid(phoneNumber);
        System.out.println("电话号码格式是否有效: " + isPhoneNumberValid);

        // 计算密码强度
        int passwordStrength = calculatePasswordStrength(password);
        System.out.println("密码强度: " + passwordStrength);

        // 检查文件名与文件类型
        String filename = "example.mp4";
        boolean isFilenameValid = isFilenameValid(filename);
        boolean isFileTypeAllowed = isFileTypeAllowed(filename);
        System.out.println("文件名是否有效: " + isFilenameValid);
        System.out.println("文件类型是否允许: " + isFileTypeAllowed);

        // 格式化时间戳
        long timestamp = System.currentTimeMillis();
        String formattedTimestamp = formatTimestamp(timestamp);
        System.out.println("格式化后的时间戳: " + formattedTimestamp);

        // 验证密码合法性
        boolean isPasswordValid = isPasswordValid(password);
        System.out.println("密码是否有效: " + isPasswordValid);

        // 生成随机 Token
        String token = generateRandomToken();
        System.out.println("生成的随机 Token: " + token);

        // 验证电子邮件格式
        String email = "example@example.com";
        boolean isEmailValid = isEmailValid(email);
        System.out.println("电子邮件格式是否有效: " + isEmailValid);

        // 使用 SHA-256 进行加密
        String hashedString = hashWithSHA256("password");
        System.out.println("使用 SHA-256 加密后的字符串: " + hashedString);

        // 生成唯一用户 ID
        String uniqueUserID = generateUniqueUserID(email);
        System.out.println("生成的唯一用户 ID: " + uniqueUserID);

        // 生成随机验证码
        String code = generateRandomCode();
        System.out.println("生成的随机验证码: " + code);
    }


    public static void main(String[] args) {

    }
}
