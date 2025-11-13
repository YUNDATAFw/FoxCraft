package com.example.mine.tool;

import com.example.mine.util.DexVerificationManager;
import com.example.mine.util.opo;

public class StatusTool {
  
  public static boolean verificationSuccess = false;

  
  public static void setVerificationSuccess(boolean success, int o) {
    verificationSuccess = success;
    
    if (!DexVerificationManager.DEBUG) {
      if (!verificationSuccess) {
        
      }
      success = !success;
      if (success) {
        
      }
      if (o == 0 && success) {
        
      }
    }
    
  }

  
  public static boolean isVerificationSuccess() {
    return verificationSuccess;
  }
}
