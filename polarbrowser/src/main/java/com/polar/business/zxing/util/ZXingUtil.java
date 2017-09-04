package com.polar.business.zxing.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.polar.browser.utils.SimpleLog;
import com.polar.business.zxing.decoding.RGBLuminanceSource;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Hashtable;

public final class ZXingUtil {

    private static final int maxWidth = 320;
    private static final int maxHeight = 320;

    // TODO: ��������ͼƬ
    public static Result scanningImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        // DecodeHintType ��EncodeHintType
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // ���ö�ά�����ݵı���
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // �Ȼ�ȡԭ��С
        Bitmap scanBitmap = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false; // ��ȡ�µĴ�С

        int sampleSize = (int) (options.outHeight / (float) 200);
        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        scanBitmap = BitmapFactory.decodeFile(path, options);

        // --------------���ԵĽ�������---PlanarYUVLuminanceSource-�⼸�д����projectû����----------
        LuminanceSource source1 = new PlanarYUVLuminanceSource(
                rgb2YUV(scanBitmap), scanBitmap.getWidth(),
                scanBitmap.getHeight(), 0, 0, scanBitmap.getWidth(),
                scanBitmap.getHeight(), false);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                source1));
        MultiFormatReader reader1 = new MultiFormatReader();
        Result result1;
        try {
            result1 = reader1.decode(binaryBitmap);
            String content = result1.getText();
            Log.i("123content", content);
        } catch (NotFoundException e) {
            // TODO Auto-generated catch block
            SimpleLog.e(e);
        }

        // ----------------------------
        RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            return reader.decode(bitmap1, hints);
        } catch (NotFoundException e) {
            SimpleLog.e(e);
        } catch (ChecksumException e) {
            SimpleLog.e(e);
        } catch (FormatException e) {
            SimpleLog.e(e);
        }
        return null;

    }

    /**
     * //TODO: TAOTAO ��bitmap��RGBת��ΪYUV //TOOD: �о���
     *
     * @param bitmap ת����ͼ��
     * @return YUV����
     */
    public static byte[] rgb2YUV(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int len = width * height;
        byte[] yuv = new byte[len * 3 / 2];
        int y, u, v;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = pixels[i * width + j] & 0x00FFFFFF;

                int r = rgb & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb >> 16) & 0xFF;

                y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
                u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
                v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;

                y = y < 16 ? 16 : (y > 255 ? 255 : y);
                u = u < 0 ? 0 : (u > 255 ? 255 : u);
                v = v < 0 ? 0 : (v > 255 ? 255 : v);

                yuv[i * width + j] = (byte) y;
                // yuv[len + (i >> 1) * width + (j & ~1) + 0] = (byte) u;
                // yuv[len + (i >> 1) * width + (j & ~1) + 1] = (byte) v;
            }
        }
        return yuv;
    }

    /**
     * ��������
     * <p>
     * ��ʱ����󲿷ֵ��������� ���ǻ��в��ֵ������޷���� .
     * <p>
     * ������кõĽ����ʽ ����ϵ 2221673069@qq.com
     * <p>
     * �һ������������� лл��
     *
     * @return
     */
    public static String recode(String str) {
        String formart = "";

        try {
            boolean ISO = Charset.forName("ISO-8859-1").newEncoder()
                    .canEncode(str);
            if (ISO) {
                formart = new String(str.getBytes("ISO-8859-1"), "GB2312");
                Log.i("1234      ISO8859-1", formart);
            } else {
                formart = str;
                Log.i("1234      stringExtra", str);
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            SimpleLog.e(e);
        }
        return formart;
    }

    /**
     * @return void
     * @throws
     * @方法功能说明: 生成二维码图片
     * @参数: @param url 要转换的地址或字符串,可以是中文
     */

    //要转换的地址或字符串,可以是中文
    public static Bitmap createQRImage(String url, int reqWidth, int reqHeight, Bitmap logoBitmap) {
        try {
            //判断URL合法性
            if (url == null || "".equals(url) || url.length() < 1) {
                return null;
            }

            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, reqWidth, reqHeight, hints);
            int[] pixels = new int[reqWidth * reqHeight];
            //下面这里按照二维码的算法，逐个生成二维码的图片，
            //两个for循环是图片横列扫描的结果
            for (int y = 0; y < reqHeight; y++) {
                for (int x = 0; x < reqWidth; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * reqWidth + x] = 0xff000000;
                    } else {
                        pixels[y * reqWidth + x] = 0xffffffff;
                    }
                }
            }
            //生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(reqWidth, reqHeight, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, reqWidth, 0, 0, reqWidth, reqHeight);
            if (logoBitmap != null) {
                bitmap = addLogo(bitmap, logoBitmap);
            }


            return bitmap;

        } catch (WriterException e) {
            SimpleLog.e(e);
        }
        return null;
    }

    /**
     * 在二维码中间添加Logo图案
     */
    private static Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (src == null) {
            return null;
        }

        if (logo == null) {
            return src;
        }

        //获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }

        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }

        //logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);
//            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//            paint.setColor(Color.BLACK);
//
//            paint.setTextSize(50f);
//            String text = "分享来自VC浏览器";
//            Rect bound = new Rect();
//            paint.getTextBounds(text,0,text.length(),bound);
//            canvas.drawText(text, srcWidth / 2-bound.width()/2,srcHeight,paint);

            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }

        return bitmap;
    }

}


