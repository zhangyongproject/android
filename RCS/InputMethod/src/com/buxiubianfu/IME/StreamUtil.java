package com.buxiubianfu.IME;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 流工具类
  * @ClassName: StreamUtil
  * @Description: 
  * @author Comsys-linbinghuang
  * @date 2014-11-3 下午4:33:57
  *
  */
public class StreamUtil {
    /**
     * 从输入流读取全部字节
     * 
     * @param is
     * @return
     */
    public static byte[] readBytesFromStream(InputStream is) {
        if (is == null) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int ch = 0;
        try {
            while ((ch = is.read()) != -1) {
                out.write(ch);
            }
            byte[] b = out.toByteArray();
            out.close();
            out = null;
            is.close();
            is = null;
            return b;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
