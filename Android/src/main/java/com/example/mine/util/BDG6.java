package com.example.mine.util;

import java.util.Base64;

public class BDG6 {

    
    public static String decodeBase64WithCaseInfo(String mixedCaseBase64, String casePattern) {
        
        if (mixedCaseBase64 == null || casePattern == null || 
            mixedCaseBase64.length() != casePattern.length()) {
            throw new IllegalArgumentException("");
        }

        
        StringBuilder originalBase64 = new StringBuilder();
        for (int i = 0; i < mixedCaseBase64.length(); i++) {
            char c = mixedCaseBase64.charAt(i);
            char caseFlag = casePattern.charAt(i);
            
            if (caseFlag == '1') {
                originalBase64.append(Character.toUpperCase(c));
            } else if (caseFlag == '0') {
                originalBase64.append(Character.toLowerCase(c));
            } else {
                throw new IllegalArgumentException("");
            }
        }

        
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(originalBase64.toString());
            return new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("error: " + e.getMessage());
        }
    }
}