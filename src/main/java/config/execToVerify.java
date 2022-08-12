package config;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class execToVerify {
    public static String sha1Verify(String path) throws Exception {
        String line;
        try {
            path = "\""+path+"\"";

            String[] cmd = new String[] { "/bin/sh", "-c","sha1sum "+path };
            Process ps = Runtime.getRuntime().exec(cmd);

            BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));

            if((line = br.readLine()) != null) {
                String[] num = line.split(" ");
                line = num[0];
            }

        } catch (Exception e) {
            throw new Exception("Error:sha1");
        }

        return line;
    }

    public static boolean equal(String sum1,String path2) throws Exception {
        String sum2 = sha1Verify(path2);
        return sum2.equals(sum1);
    }

}
