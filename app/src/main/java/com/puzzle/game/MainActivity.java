package com.puzzle.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    private List<PuzzlePiece> pieces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RelativeLayout layout = findViewById(R.id.layout);
        ImageView imageView = findViewById(R.id.imageView);


        imageView.post(new Runnable() {
            @Override
            public void run() {
                pieces = splitImage();
                TouchListener touchListener = new TouchListener();
                for(PuzzlePiece piece : pieces) {
                    piece.setOnTouchListener(touchListener);
                    layout.addView(piece);
                }
            }
        });
    }

    /**
     * row and collumn is configured.
     * @return
     */
    public List<PuzzlePiece> splitImage() {
        int rows = 3;
        int cols = 3;
        int piecesNumber = rows * cols;
        ImageView imageView = findViewById(R.id.imageView);
        int[] dimensions = getBitmapPositionInsideImageView(imageView);
        int scaledBitmapLeft = dimensions[0];
        int scaledBitmapTop = dimensions[1];
        int scaledBitmapWidth = dimensions[2];
        int scaledBitmapHeight = dimensions[3];

        int croppedImageWidth = scaledBitmapWidth - 2 * Math.abs(scaledBitmapLeft);
        int croppedImageHeight = scaledBitmapHeight - 2 * Math.abs(scaledBitmapTop);

        ArrayList<PuzzlePiece> pieces = new ArrayList<>(piecesNumber);
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledBitmapWidth,
                scaledBitmapHeight, true);
        Bitmap croppedBitmap = Bitmap.createBitmap(scaledBitmap, Math.abs(scaledBitmapLeft),
                Math.abs(scaledBitmapTop), croppedImageWidth, croppedImageHeight);

        int pieceWidth = croppedImageWidth / cols;
        int pieceHeight = croppedImageHeight / rows;

        int yCoord = 0;
        for (int row = 0; row < rows; row++) {
            int xCoord = 0;
            for (int col = 0; col < cols; col++) {
                // calculate offset for each piece
                int offsetX = 0;
                int offsetY = 0;
                if (col > 0) {
                    offsetX = pieceWidth / 3;
                }
                if (row > 0) {
                    offsetY = pieceHeight / 3;
                }

                // apply the offset to each piece
                Bitmap pieceBitmap = Bitmap.createBitmap(croppedBitmap, xCoord - offsetX, yCoord - offsetY, pieceWidth + offsetX, pieceHeight + offsetY);
                PuzzlePiece piece = new PuzzlePiece(getApplicationContext());
                piece.setImageBitmap(pieceBitmap);
                piece.xCoord = xCoord - offsetX + imageView.getLeft();
                piece.yCoord = yCoord - offsetY + imageView.getTop();
                piece.pieceWidth = pieceWidth + offsetX;
                piece.pieceHeight = pieceHeight + offsetY;

                // this bitmap will hold our final puzzle piece image
                Bitmap puzzlePiece = Bitmap.createBitmap(pieceWidth + offsetX, pieceHeight + offsetY, Bitmap.Config.ARGB_8888);

                // draw path
                int bumpSize = pieceHeight / 4;
                Canvas canvas = new Canvas(puzzlePiece);
                Path path = new Path();
                path.moveTo(offsetX, offsetY);
                if (row == 0) {
                    // top side piece
                    path.lineTo(pieceBitmap.getWidth(), offsetY);
                } else {
                    // top bump
                    path.lineTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 3, offsetY);
                    path.cubicTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 6,
                            offsetY - bumpSize,
                            offsetX + (pieceBitmap.getWidth() - offsetX) / 6 * 5,
                            offsetY - bumpSize,
                            offsetX + (pieceBitmap.getWidth() - offsetX) / 3 * 2, offsetY);
                    path.lineTo(pieceBitmap.getWidth(), offsetY);
                }

                if (col == cols - 1) {
                    // right side piece
                    path.lineTo(pieceBitmap.getWidth(), pieceBitmap.getHeight());
                } else {
                    // right bump
                    path.lineTo(pieceBitmap.getWidth(), offsetY + (pieceBitmap.getHeight() - offsetY) / 3);
                    path.cubicTo(pieceBitmap.getWidth() - bumpSize,offsetY + (pieceBitmap.getHeight() - offsetY) / 6, pieceBitmap.getWidth() - bumpSize, offsetY + (pieceBitmap.getHeight() - offsetY) / 6 * 5, pieceBitmap.getWidth(), offsetY + (pieceBitmap.getHeight() - offsetY) / 3 * 2);
                    path.lineTo(pieceBitmap.getWidth(), pieceBitmap.getHeight());
                }

                if (row == rows - 1) {
                    // bottom side piece
                    path.lineTo(offsetX, pieceBitmap.getHeight());
                } else {
                    // bottom bump
                    path.lineTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 3 * 2, pieceBitmap.getHeight());
                    path.cubicTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 6 * 5,pieceBitmap.getHeight() - bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX) / 6, pieceBitmap.getHeight() - bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX) / 3, pieceBitmap.getHeight());
                    path.lineTo(offsetX, pieceBitmap.getHeight());
                }

                if (col == 0) {
                    // left side piece
                    path.close();
                } else {
                    // left bump
                    path.lineTo(offsetX, offsetY + (pieceBitmap.getHeight() - offsetY) / 3 * 2);
                    path.cubicTo(offsetX - bumpSize, offsetY + (pieceBitmap.getHeight() - offsetY) / 6 * 5, offsetX - bumpSize, offsetY + (pieceBitmap.getHeight() - offsetY) / 6, offsetX, offsetY + (pieceBitmap.getHeight() - offsetY) / 3);
                    path.close();
                }

                // mask the piece
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setColor(0XFF000000);
                paint.setStyle(Paint.Style.FILL);

                canvas.drawPath(path, paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(pieceBitmap, 0, 0, paint);

                Paint border = new Paint();
                border.setColor(0X80FFFFFF);
                border.setStyle(Paint.Style.STROKE);
                border.setStrokeWidth(8.0f);
                canvas.drawPath(path, border);

                // draw a black border
                border = new Paint();
                border.setColor(0X80000000);
                border.setStyle(Paint.Style.STROKE);
                border.setStrokeWidth(3.0f);
                canvas.drawPath(path, border);

                // set the resulting bitmap to the piece
                piece.setImageBitmap(puzzlePiece);

                pieces.add(piece);
                xCoord += pieceWidth;
            }
            yCoord += pieceHeight;
        }

        return pieces;
    }

    private int[] getBitmapPositionInsideImageView(ImageView imageView) {
        int[] ret = new int[4];
        if (imageView == null && imageView.getDrawable() == null) {
            return ret;
        }

        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        float scaleX = f[Matrix.MSCALE_X];
        float scaleY = f[Matrix.MSCALE_Y];

        Drawable d = imageView.getDrawable();
        int origH = d.getIntrinsicHeight();
        int origW = d.getIntrinsicWidth();

        int actH = Math.round(origH * scaleY);
        int actW = Math.round(origW * scaleX);

        ret[2] = actW;
        ret[3] = actH;

        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - actH) / 2;
        int left = (int) (imgViewW - actW) / 2;

        ret[0] = left;
        ret[1] = top;
        return ret;
    }
}
