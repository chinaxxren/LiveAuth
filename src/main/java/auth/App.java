package auth;

import java.io.UnsupportedEncodingException;

/**
 * Hello world!
 */
public class App {

    public static void main(String[] args) throws UnsupportedEncodingException {
        ConnRoomSig.Conn_Room_Sig.Builder builder = ConnRoomSig.Conn_Room_Sig.newBuilder();
        builder.setStrThirdAccount("5b1f8b49a3149815297a9a56");
        builder.setUint32Groupcode(100008);
        builder.setStrConnedThirdAccount("5b51a84ba3149863b80387b0");
        builder.setUint32ConnedGroupcode(164662);
        long current = System.currentTimeMillis();// 当前时间毫秒数
        int second = (int) (current / 1000);
        builder.setUint32CreateTime(second);
        builder.setUint32ExpireTime(second + 24 * 3600 * 120);
        ConnRoomSig.Conn_Room_Sig conn_room_sig = builder.build();

        //加密内容
        byte[] contents = conn_room_sig.toByteArray();

        //KEY
        byte[] keys = "c2bfcc296d5f3253".getBytes();


        LiveAuth teaUtil = new LiveAuth();
        byte[] ciphertext = teaUtil.encrypt(contents, keys);

        String info = Hex2byte.bytesToHex(ciphertext);
        System.out.println(info.toLowerCase());
    }
}