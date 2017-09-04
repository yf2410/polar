/**
 * @brief     Package com.polar.browser.sync
 * @author    xingxuezhi
 * @since     1.0.0.0
 * @version   1.0.0.0
 * @date      2013-1-10
 */

package com.polar.browser.download_refactor.util;

/** 
 * @file      Base64.java
 * @brief     TODO Pls update here...
 *
 * @author    xingxuezhi
 * @since     1.0.0.0
 * @version   1.0.0.0
 * @date      2013-1-10
 *
 * \if TOSPLATFORM_CONFIDENTIAL_PROPRIETARY
 * ============================================================================\n
 *\n
 *           Copyright (c) 2012 Xing Xuezhi.  All Rights Reserved.\n
 *\n
 * ============================================================================\n
 *\n
 *                              Update History\n
 *\n
 * Author (Name[WorkID]) | Modification | Tracked Id | Description\n
 * --------------------- | ------------ | ---------- | ------------------------\n
 * xingxuezhi[7897]   |  2013-1-10  | <xxxxxxxx> | Initial Created.\n
 *\n
 * \endif
 *
 * <tt>
 *\n
 * Release History:\n
 *\n
 * Author (Name[WorkID]) | ModifyDate | Version | Description \n
 * --------------------- | ---------- | ------- | -----------------------------\n
 * xingxuezhi[7897]   | 2013-1-10 | 1.0.0.0 | Initial created. \n
 *\n
 * </tt>
 */
//=============================================================================
//                                  IMPORT PACKAGES
//=============================================================================
import java.io.ByteArrayOutputStream;
import java.io.IOException;

//=============================================================================
//                                 CLASS DEFINITIONS
//=============================================================================

public class Base64 {

    public static String encode(byte[] data) {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        Base64Encoder encoder = new Base64Encoder();

        try {
            encoder.encode(data, 0, data.length, bOut);
        } catch (IOException e) {
            throw new RuntimeException("exception encoding base64 string: " + e);
        }

        return bOut.toString();
    }

    public static byte[] decode(byte[] data) {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        Base64Encoder encoder = new Base64Encoder();

        try {
            encoder.decode(data, 0, data.length, bOut);
        } catch (IOException e) {
            throw new RuntimeException("exception decoding base64 string: " + e);
        }

        return bOut.toByteArray();
    }

}
