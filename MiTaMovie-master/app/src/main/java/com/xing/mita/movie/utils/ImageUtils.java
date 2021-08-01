package com.xing.mita.movie.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.xing.mita.movie.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mita
 * @date 2018/10/11
 * @Description
 */
public class ImageUtils {

    /**
     * 加载图片
     *
     * @param context Context
     * @param iv      ImageView
     * @param url     Object
     */
    public static void loadImageWithGifThumbnail(Context context, ImageView iv, Object url) {
        if (context instanceof Activity) {
            if (((Activity) context).isDestroyed()) {
                return;
            }
        }
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .error(new ColorDrawable(Color.parseColor("#564542")));
        Glide.with(context)
                .load(url)
                .thumbnail(Glide.with(context).load(R.drawable.icon_lazyload))
                .apply(options)
                .transition(new DrawableTransitionOptions().crossFade())
                .into(iv);
    }

    /**
     * 加载图片
     *
     * @param activity Activity
     * @param iv       ImageView
     * @param url      Object
     */
    public static void loadImage(Activity activity, ImageView iv, Object url) {
        if (activity.isDestroyed()) {
            return;
        }
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
        Glide.with(activity)
                .load(url)
                .apply(options)
                .transition(new DrawableTransitionOptions().crossFade())
                .into(iv);
    }

    /**
     * 加载圆角图片
     *
     * @param context Context
     * @param iv      ImageView
     * @param url     Object
     */
    public static void loadCornerImage(Context context, ImageView iv, Object url, int radius) {
        if (context instanceof Activity) {
            if (((Activity) context).isDestroyed()) {
                return;
            }
        }
        //设置图片圆角角度
        RoundedCorners roundedCorners = new RoundedCorners(radius);
        RequestOptions options = RequestOptions
                .bitmapTransform(roundedCorners);
        Glide.with(context)
                .load(url)
                .apply(options)
                .transition(new DrawableTransitionOptions().crossFade())
                .into(iv);
    }

    /**
     * 加载视频第一帧
     *
     * @param context Context
     * @param iv      ImageView
     * @param url     Object
     */
    public static void loadVideoThumb(Context context, ImageView iv, Object url) {
        if (context instanceof Activity) {
            if (((Activity) context).isDestroyed()) {
                return;
            }
        }
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions()
                        .transforms(new CenterCrop(),
                                new RoundedCorners(10)))
                .transition(new DrawableTransitionOptions().crossFade())
                .into(iv);
    }

    /**
     * 创建一个二维码
     *
     * @param content 二维码内容
     * @param iv      ImageView
     * @param bmQR    二维码
     * @param bmLogo  图标
     */
    public static void createQRCode(String content, ImageView iv, Bitmap bmQR, Bitmap bmLogo, boolean hasLogo) {
        try {
            int width = iv.getWidth();
            //1,创建实例化对象
            QRCodeWriter writer = new QRCodeWriter();
            //2,设置字符集
            Map<EncodeHintType, String> map = new HashMap<>(16);
            map.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            //3，通过encode方法将内容写入矩阵对象
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, width, map);
            //4，定义一个二维码像素点的数组，向每个像素点中填充颜色
            int[] pixels = new int[width * width];
            //5,往每一像素点中填充颜色（像素没数据则用黑色填充，没有则用彩色填充，不过一般用白色）
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < width; j++) {
                    if (matrix.get(j, i)) {
                        pixels[i * width + j] = 0xff000000;
                    } else {
                        pixels[i * width + j] = 0xffffffff;
                    }
                }
            }
            //6,创建一个指定高度和宽度的空白bitmap对象
            bmQR = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
            //7，将每个像素的颜色填充到bitmap对象
            bmQR.setPixels(pixels, 0, width, 0, 0, width, width);

            if (hasLogo) {
                //8，创建一个bitmap对象用于作为其图标
                bmLogo = BitmapFactory.decodeResource(iv.getContext().getResources(), R.mipmap.icon_logo);
                //9，创建一个方法在二维码上添加一张图片
                if (addLogin(bmQR, bmLogo) != null) {
                    iv.setImageBitmap(addLogin(bmQR, bmLogo));
                }
            } else {
                iv.setImageBitmap(bmQR);
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }


    /**
     * 用于向创建的二维码中添加一个logn
     *
     * @param bmQR   二维码
     * @param bmLogo 图标
     * @return Bitmap
     */
    private static Bitmap addLogin(Bitmap bmQR, Bitmap bmLogo) {
        if (bmQR == null) {
            return null;
        }
        if (bmLogo == null) {
            return bmQR;
        }

        //获取图片的宽高
        int bmQrWidth = bmQR.getWidth();
        int bmQrHeight = bmQR.getHeight();
        int bmLogoWidth = bmLogo.getWidth();
        int bmLogoHeight = bmLogo.getHeight();

        //设置logn的大小为二维码整体大小的1/5
        float scaleLogo = bmQrWidth * 1.0f / 6 / bmLogoWidth;
        Bitmap bitmap = Bitmap.createBitmap(bmQrWidth, bmQrHeight, Bitmap.Config.ARGB_8888);

        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(bmQR, 0, 0, null);
            canvas.scale(scaleLogo, scaleLogo, bmQrWidth >> 1, bmQrHeight >> 1);
            canvas.drawBitmap(bmLogo, (bmQrWidth - bmLogoWidth) >> 1,
                    (bmQrHeight - bmLogoHeight) >> 1, null);

            canvas.save();
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }
        return bitmap;
    }

    /**
     * View转Bitmap
     *
     * @param view View
     * @return Bitmap
     */
    public static Bitmap convertViewToBitmap(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        return view.getDrawingCache();
    }

    /**
     * 压缩图片
     *
     * @param bm Bitmap
     * @return Bitmap
     */
    public static Bitmap compressQuality(Bitmap bm) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bytes = bos.toByteArray();
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * 保存bitmap到本地
     *
     * @param bitmap  Bitmap
     * @param name    String
     * @param context Context
     * @return
     */
    public static boolean saveImg(Context context, Bitmap bitmap, String name) {
        try {
            //图片保存的文件夹名
            String dir = Constant.DIR_IMAGE;
            //已File来构建
            File file = new File(dir);
            //如果不存在  就mkdirs()创建此文件夹
            if (!file.exists()) {
                file.mkdirs();
            }
            //将要保存的图片文件
            File mFile = new File(dir + name);
            if (mFile.exists()) {
                return true;
            }
            //构建输出流
            FileOutputStream outputStream = new FileOutputStream(mFile);
            //compress到输出outputStream
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            //获得图片的uri
            Uri uri = Uri.fromFile(mFile);
            //发送广播通知更新图库，这样系统图库可以找到这张图片
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

}
