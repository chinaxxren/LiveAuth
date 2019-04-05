package auth;

/**
 * Created by anqi on 2019/3/12.
 */
public class Hex2byte {
    public static String bytetohex(byte[] bt) {
        int len = bt.length;
        char[] out = new char[2 * bt.length];
        for (int i = 0, j = 0; j < len; i += 2, j++) {

            char temps1 = (char) ('0' + ((bt[j] & 0xf0) >> 4));
            //System.out.print(temps1);
            char temps2 = (char) ('0' + (bt[j] & 0x0f));
            if (temps1 > '9' || temps1 < '0') {
                switch (temps1 - '9') {
                    case 1:
                        temps1 = 'A';
                        break;
                    case 2:
                        temps1 = 'B';
                        break;
                    case 3:
                        temps1 = 'C';
                        break;
                    case 4:
                        temps1 = 'D';
                        break;
                    case 5:
                        temps1 = 'E';
                        break;
                    case 6:
                        temps1 = 'F';
                        break;
                }
            }
            if (temps2 > '9' || temps2 < '0') {
                switch (temps2 - '9') {
                    case 1:
                        temps2 = 'A';
                        break;
                    case 2:
                        temps2 = 'B';
                        break;
                    case 3:
                        temps2 = 'C';
                        break;
                    case 4:
                        temps2 = 'D';
                        break;
                    case 5:
                        temps2 = 'E';
                        break;
                    case 6:
                        temps2 = 'F';
                        break;
                }
            }
            out[i] = temps1;
            //System.out.print(out[i]);
            out[i + 1] = temps2;
            //System.out.print(out[i+1]);

        }
        //System.out.println();
        return new String(out);

    }

    public static byte[] hextobyte(String hex) {
        byte[] out = new byte[hex.length() / 2];
        //System.out.println("strlen="+hex.length());
        int s = 0, s1 = 0;
        for (int j = 0, i = 0; i < hex.length() / 2; i++, j += 2) {

            if ('0' > hex.charAt(j) || hex.charAt(j) > '9') {
                switch (hex.charAt(j)) {
                    case 'A':
                        s = 10;
                        break;
                    case 'B':
                        s = 11;
                        break;
                    case 'C':
                        s = 12;
                        break;
                    case 'D':
                        s = 13;
                        break;
                    case 'E':
                        s = 14;
                        break;
                    case 'F':
                        s = 15;
                        break;

                }
            } else s = hex.charAt(j) - '0';


            if ('0' > hex.charAt(j + 1) || hex.charAt(j + 1) > '9') {
                switch (hex.charAt(j + 1)) {
                    case 'A':
                        s1 = 10;
                        break;
                    case 'B':
                        s1 = 11;
                        break;
                    case 'C':
                        s1 = 12;
                        break;
                    case 'D':
                        s1 = 13;
                        break;
                    case 'E':
                        s1 = 14;
                        break;
                    case 'F':
                        s1 = 15;
                        break;

                }
            } else s1 = hex.charAt(j + 1) - '0';

            out[i] = (byte) ((s & 0x0000000f) << 4 | (s1 & 0x0000000f));
        }
        return out;
    }

    public static String bytesToHexString(byte[] bArr) {
        StringBuffer sb = new StringBuffer(bArr.length);
        String sTmp;

        for (int i = 0; i < bArr.length; i++) {
            sTmp = Integer.toHexString(0xFF & bArr[i]);
            if (sTmp.length() < 2)
                sb.append(0);
            sb.append(sTmp.toUpperCase());
        }

        return sb.toString();
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
