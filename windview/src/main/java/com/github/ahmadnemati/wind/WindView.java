package com.github.ahmadnemati.wind;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by َAhmad Nemati on 12/19/2016.
 */

public class WindView extends View {
    private static final String TAG = WindView.class.getSimpleName();
    private Rect rect = new Rect();
    private Path path;
    private Matrix matrix;
    private Paint paint;
    private int primaryTextColor;
    private String WindDirectionText;
    private long startTime = 0;
    private PathEffect pathEffect;
    private Bitmap smallPoleBitmap;
    private Bitmap bigPoleBitmap;
    private Bitmap smallBladeBitmap;
    private Bitmap bigBladeBitmap;
    private Bitmap trendBitmap;
    private Bitmap barometerBitmap;
    private float rotation;
    private int bigPoleX;
    private int smallPoleX;
    private String windText = "Wind";
    private String windName;
    private String windSpeedText;
    private String barometerText = "Barometer";
    private int poleBottomY;
    private int windTextX;
    private int windTextY;
    private float windSpeed;
    private String windSpeedUnit;
    private boolean animationEnable = true;
    private boolean animationBaroMeterEnable = false;
    private float pressurePaddingTop;
    private double senterOfPressureLine;
    private double pressureLineSize;
    private int scale;
    private float barometerTickSpacing;
    private double lineSpace;
    private float pressure;
    private String pressureText;
    private float lineSize;
    private float curSize;
    private int pressureTextY;
    private String pressureUnit;
    private int trendType;
    private int labelFontSize;
    private int numericFontSize;
    private Typeface typeface;

    public WindView(Context context) {
        super(context);
        setupView();
    }

    public WindView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public WindView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public WindView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray obtainStyledAttributes = getContext().obtainStyledAttributes(attrs, R.styleable.WindView);
        pressure = obtainStyledAttributes.getFloat(R.styleable.WindView_pressure, -1.0f);
        windTextX = obtainStyledAttributes.getDimensionPixelSize(R.styleable.WindView_windTextX, 242);
        windTextY = obtainStyledAttributes.getDimensionPixelSize(R.styleable.WindView_windTextY, 44);
        pressureTextY = obtainStyledAttributes.getDimensionPixelSize(R.styleable.WindView_pressureTextY, 8);
        labelFontSize = obtainStyledAttributes.getDimensionPixelSize(R.styleable.WindView_labelFontSize, 18);
        numericFontSize = obtainStyledAttributes.getDimensionPixelSize(R.styleable.WindView_numericFontSize, 14);
        barometerTickSpacing = (float) obtainStyledAttributes.getDimensionPixelSize(R.styleable.WindView_barometerTickSpacing, 9);
        bigPoleX = obtainStyledAttributes.getDimensionPixelSize(R.styleable.WindView_bigPoleX, 48);
        smallPoleX = obtainStyledAttributes.getDimensionPixelSize(R.styleable.WindView_smallPoleX, 98);
        poleBottomY = obtainStyledAttributes.getDimensionPixelSize(R.styleable.WindView_poleBottomY, 118);
        scale = getScale(20);
        obtainStyledAttributes.recycle();
        setupView();
    }

    private void setupView() {
        Resources resources = getContext().getResources();
        primaryTextColor = resources.getColor(R.color.text_color);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        smallPoleBitmap = BitmapFactory.decodeResource(resources, R.drawable.smallpole);
        bigPoleBitmap = BitmapFactory.decodeResource(resources, R.drawable.bigpole);
        smallBladeBitmap = BitmapFactory.decodeResource(resources, R.drawable.smallblade);
        bigBladeBitmap = BitmapFactory.decodeResource(resources, R.drawable.bigblade);
        barometerBitmap = BitmapFactory.decodeResource(resources, R.drawable.barometer);
        matrix = new Matrix();
        WindDirectionText = "";
        rotation = 0.0f;
        lineSpace = toPixel(1d);
        pressureLineSize = lineSpace + (barometerTickSpacing);
        senterOfPressureLine = (9d * pressureLineSize) + lineSpace;
        pressurePaddingTop = getPaddingTop();
        pathEffect = new DashPathEffect(new float[]{4f, 4f}, 0f);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initCanvas(canvas);

    }

    private void initCanvas(Canvas canvas) {
        boolean enable = false;
        if (bigPoleBitmap != null && smallBladeBitmap != null && bigBladeBitmap != null) {
            paint.setColor(primaryTextColor);
            canvas.drawBitmap(bigPoleBitmap, bigPoleX, (poleBottomY - bigPoleBitmap.getHeight()), paint);
            canvas.drawBitmap(smallPoleBitmap, smallPoleX, (poleBottomY - smallPoleBitmap.getHeight()), paint);
            if (animationEnable) {
                startTimes();
            }
            int width = bigPoleX + (bigPoleBitmap.getWidth() / 2);
            int height = (poleBottomY - bigPoleBitmap.getHeight()) - 4;
            rect.left = width - (bigBladeBitmap.getWidth() / 2);
            rect.top = height - (bigBladeBitmap.getHeight() / 2);
            rect.bottom = (bigBladeBitmap.getHeight() / 2) + height;
            matrix.reset();
            matrix.setRotate(rotation, ((float) bigBladeBitmap.getWidth()) / 2.0f, (bigBladeBitmap.getHeight()) / 2.0f);
            matrix.postTranslate((float) (width - (bigBladeBitmap.getWidth() / 2)), (height - (bigBladeBitmap.getHeight() / 2)));
            canvas.drawBitmap(bigBladeBitmap, matrix, paint);
            width = smallPoleX + (smallPoleBitmap.getWidth() / 2);
            int height2 = (poleBottomY - smallPoleBitmap.getHeight()) - 4;
            rect.right = (smallBladeBitmap.getWidth() / 2) + width;
            rect.bottom = Math.max(rect.bottom, height + (smallBladeBitmap.getHeight() / 2));
            matrix.reset();
            matrix.setRotate(rotation, ((float) smallBladeBitmap.getWidth()) / 2.0f, ((float) smallBladeBitmap.getHeight()) / 2.0f);
            matrix.postTranslate((float) (width - (smallBladeBitmap.getWidth() / 2)), (float) (height2 - (smallBladeBitmap.getHeight() / 2)));
            canvas.drawBitmap(smallBladeBitmap, matrix, paint);
            drawWind(canvas);


        }
    }

    private void drawWind(Canvas canvas) {
        paint.setTextSize((float) labelFontSize);
        paint.setStyle(Paint.Style.FILL);
        paint.setPathEffect(null);
        float width = (((float) smallPoleX) + (((float) smallBladeBitmap.getWidth()) / 2.0f)) + ((float) windTextX);
        canvas.drawText(windText, width, (float) windTextY, paint);
        if (windSpeed < 0.0f) {
            paint.setTextSize((float) labelFontSize);
            canvas.drawText(windName, width, (float) (windTextY + labelFontSize), paint);
        } else if (!stringValid(windSpeedText)) {
            paint.setTextSize((float) numericFontSize);
            canvas.drawText(windSpeedText, width, (float) (windTextY + numericFontSize), paint);
            if (!stringValid(windName)) {
                paint.setTextSize((float) labelFontSize);
                canvas.drawText(windName, width + (((float) (windSpeedText.length() * numericFontSize)) / 2.0f), (float) (windTextY + numericFontSize), paint);
            }
        }
    }


    public void start() {
        animationEnable = true;
        invalidate();
    }

    public void stop() {
        animationEnable = false;
    }


    private int getScale(int i) {
        return (int) ((getContext().getResources().getDisplayMetrics().density * ((float) i)) + 0.5f);
    }

    private void startTimes() {
        long nanoTime = System.nanoTime();
        float f = ((float) (nanoTime - startTime)) / 1000000.0f;
        startTime = nanoTime;
        rotation = ((float) ((Math.sqrt((double) windSpeed) * ((double) f)) / 20.0d)) + rotation;
        rotation %= 360.0f;
    }

    private boolean stringValid(String str) {
        return str == null || str.trim().length() == 0 || str.trim().equalsIgnoreCase("null");
    }

    public Typeface getTypeface() {
        return typeface;
    }

    public void setTypeface(Typeface typeface) {
        this.typeface = typeface;
        paint.setTypeface(typeface);
    }

    private double toPixel(double d) {
        return (getContext().getResources().getDisplayMetrics().density) * d;
    }
}
