package auth;

import java.io.ByteArrayOutputStream;
import java.util.Random;

public class LiveAuth {
    // 指向当前的明文块
    private byte[] plain;
    // 这指向前面一个明文块
    private byte[] prePlain;
    // 输出的密文或者明文
    private byte[] out;
    // 当前加密的密文位置和上一次加密的密文块位置，他们相差8
    private int crypt;
    // 当前处理的加密解密块的位置
    private int pos;
    // 填充数
    private int padding;
    // 密钥
    private byte[] key;
    // 这个表示当前解密开始的位置，之所以要这么一个变量是为了避免当解密到最后时
    // 后面已经没有数据，这时候就会出错，这个变量就是用来判断这种情况免得出错
    private int contextStart;
    // 随机数对象
    private static Random random = new Random();
    // 字节输出流
    private ByteArrayOutputStream baos;

    /**
     * 构造函数
     */
    public LiveAuth() {
        baos = new ByteArrayOutputStream(8);
    }

    /**
     * 把字节数组从offset开始的len个字节转换成一个unsigned int， 因为java里面没有unsigned，所以unsigned
     * int使用long表示的， 如果len大于8，则认为len等于8。如果len小于8，则高位填0 <br>
     * (edited by notxx) 改变了算法, 性能稍微好一点. 在我的机器上测试10000次, 原始算法花费18s, 这个算法花费12s.
     *
     * @param in     字节数组.
     * @param offset 从哪里开始转换.
     * @param len    转换长度, 如果len超过8则忽略后面的
     * @return
     */
    private static long getUnsignedInt(byte[] in, int offset, int len) {
        long ret = 0;
        int end = 0;
        if (len > 8)
            end = offset + 8;
        else
            end = offset + len;
        for (int i = offset; i < end; i++) {
            ret <<= 8;
            ret |= in[i] & 0xff;
        }
        return (ret & 0xffffffffl) | (ret >>> 32);
    }

    /**
     * 加密
     *
     * @param in     明文字节数组
     * @param offset 开始加密的偏移
     * @param len    加密长度
     * @param k      密钥
     * @return 密文字节数组
     */
    public byte[] encrypt(byte[] in, int offset, int len, byte[] k) {
        // 检查密钥
        if (k == null)
            return in;

        plain = new byte[8];
        prePlain = new byte[8];
        pos = 1;
        padding = 0;
        crypt = 0;
        this.key = k;

        // 计算头部填充字节数
        pos = (len + 0x0A) % 8;

        if (pos != 0)
            pos = 8 - pos;

        out = new byte[len * 2 + 1];
        // 这里的操作把pos存到了plain的第一个字节里面
        // 0xF8后面三位是空的，正好留给pos，因为pos是0到7的值，表示文本开始的字节位置
        plain[0] = (byte) ((rand() & 0xF8) | pos);
        // 这里用随机产生的数填充plain[1]到plain[pos]之间的内容
        for (int i = 1; i <= pos; i++)
            plain[i] = (byte) (rand() & 0xFF);
        pos++;
        // 这个就是prePlain，第一个8字节块当然没有prePlain，所以我们做一个全0的给第一个8字节块
        for (int i = 0; i < 8; i++)
            prePlain[i] = 0x0;

        // 继续填充2个字节的随机数，这个过程中如果满了8字节就加密之
        padding = 1;
        while (padding <= 2) {
            if (pos < 8) {
                plain[pos++] = (byte) (rand() & 0xFF);
                padding++;
            }
            if (pos == 8) {
                encrypt8Bytes();
            }
        }

        // 头部填充完了，这里开始填真正的明文了，也是满了8字节就加密，一直到明文读完
        int i = offset;
        while (len > 0) {
            if (pos < 8) {
                plain[pos++] = in[i++];
                len--;
            }
            if (pos == 8) {
                encrypt8Bytes();
            }

        }

        // 最后填上0，以保证是8字节的倍数
        padding = 1;
        while (padding <= 7) {
            if (pos < 8) {
                plain[pos++] = 0x0;
                padding++;
            }
            if (pos == 8) {
                encrypt8Bytes();
            }
        }

        byte[] ciphertext = new byte[crypt];
        System.arraycopy(out, 0, ciphertext, 0, crypt);
        return ciphertext;
    }

    /**
     * @param in 需要加密的明文
     * @param k  密钥
     * @return Message 密文
     * @paraminLen 明文长度
     */
    public byte[] encrypt(byte[] in, byte[] k) {
        return encrypt(in, 0, in.length, k);
    }

    /**
     * 加密一个8字节块
     *
     * @param in 明文字节数组
     * @return 密文字节数组
     */
    private byte[] encipher(byte[] in) {
        // 迭代次数，16次
        int loop = 0x10;
        // 得到明文和密钥的各个部分，注意java没有无符号类型，所以为了表示一个无符号的整数
        // 我们用了long，这个long的前32位是全0的，我们通过这种方式模拟无符号整数，后面用到的long也都是一样的
        // 而且为了保证前32位为0，需要和0xFFFFFFFF做一下位与
        long y = getUnsignedInt(in, 0, 4);
        long z = getUnsignedInt(in, 4, 4);
        long a = getUnsignedInt(key, 0, 4);
        long b = getUnsignedInt(key, 4, 4);
        long c = getUnsignedInt(key, 8, 4);
        long d = getUnsignedInt(key, 12, 4);
        // 这是算法的一些控制变量，为什么delta是0x9E3779B9呢？
        // 这个数是TEA算法的delta，实际是就是(sqr(5) - 1) * 2^31 (根号5，减1，再乘2的31次方)
        long sum = 0;
        long delta = 0x9E3779B9;
        delta &= 0xFFFFFFFFL;

        // 开始迭代了，乱七八糟的，我也看不懂，反正和DES之类的差不多，都是这样倒来倒去
        while (loop-- > 0) {
            sum += delta;
            sum &= 0xFFFFFFFFL;
            y += ((z << 4) + a) ^ (z + sum) ^ ((z >>> 5) + b);
            y &= 0xFFFFFFFFL;
            z += ((y << 4) + c) ^ (y + sum) ^ ((y >>> 5) + d);
            z &= 0xFFFFFFFFL;
        }

        // 最后，我们输出密文，因为我用的long，所以需要强制转换一下变成int
        baos.reset();
        writeInt((int) y);
        writeInt((int) z);
        return baos.toByteArray();
    }

    /**
     * 写入一个整型到输出流，高字节优先
     *
     * @param t
     */
    private void writeInt(int t) {
        baos.write(t >>> 24);
        baos.write(t >>> 16);
        baos.write(t >>> 8);
        baos.write(t);
    }

    // for(j = 0; j < 8; j++)  /*CBC XOR */
    //  src_buf[j]^=iv_buf[j];

    // OI_TeaEncryptECB_1(src_buf, pKey, pOutBuf);

    // src_i =0;
    // iv_buf =pOutBuf;
    // *pOutBufLen +=8;
    // pOutBuf +=8;

    private void encrypt8Bytes() {
        for (pos = 0; pos < 8; pos++)
            plain[pos] ^= prePlain[pos];

        byte[] crypted = encipher(plain);
        System.arraycopy(crypted, 0, out, crypt, 8);
        System.arraycopy(crypted, 0, prePlain, 0, 8);
        crypt += 8;
        pos = 0;
    }

    /**
     * 这是个随机因子产生器，用来填充头部的，如果为了调试，可以用一个固定值
     * 随机因子可以使相同的明文每次加密出来的密文都不一样
     *
     * @return 随机因子
     */
    private int rand() {
        return random.nextInt();
    }
}