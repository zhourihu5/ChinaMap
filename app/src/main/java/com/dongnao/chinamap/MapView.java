package com.dongnao.chinamap;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class MapView extends View {
    private static final String TAG = "david";
    private Context context;
    private float scale=1.0f;
    private RectF totalRect;
    private int[] colorArray = new int[]{0xFF239BD7, 0xFF30A9E5, 0xFF80CBF1, 0xFFF00FFF};
    private List<ProviceItem> itemList;
    private Paint paint;
    private ProviceItem select;
    private float mAnimatorValue=0;
    private ArrayList<Path> pathList,dstList;
    private ArrayList<PathMeasure> pathMeasureList;
    public MapView(Context context) {
        super(context);
    }

    public MapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        handleTouch(event.getX(), event.getY());
        return super.onTouchEvent(event);
    }

    private void handleTouch(float x, float y) {
        if (itemList == null) {
            return;
        }
        ProviceItem selectItem = null;
        for (ProviceItem proviceItem : itemList) {
            if(proviceItem.isTouch(x/scale,y/scale)){
                selectItem = proviceItem;
            }
        }
        if (selectItem != null) {
            select = selectItem;
            postInvalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        ??????????????????????????????
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

//        map ?????????  ?????????
        if (totalRect != null) {
            double mapWidth = totalRect.width();
            scale= (float) (width / mapWidth);
        }



        setMeasuredDimension(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height , MeasureSpec.EXACTLY));
    }

    private void init(Context context) {
        this.context = context;
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        pathList = new ArrayList<>();
        dstList = new ArrayList<>();
        pathMeasureList = new ArrayList<>();
        loadThread.start();
        final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mAnimatorValue = (float) valueAnimator.getAnimatedValue();
                Log.i(TAG, "mAnimatorValue: "+mAnimatorValue);
                invalidate();
            }
        });
        valueAnimator.setDuration(2000);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.start();
    }

    public MapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        Log.i(TAG, "onDraw: ");
        if (pathMeasureList != null) {
            canvas.save();
            canvas.scale(scale,scale);
            for (int i=0;i<pathMeasureList.size();i++) {
                dstList.get(i).reset();
                // ???????????????BUG
                dstList.get(i).lineTo(0,0);
                float stop = pathMeasureList.get(i).getLength() * mAnimatorValue;
                pathMeasureList.get(i).getSegment(0, stop, dstList.get(i), true);
                canvas.drawPath(dstList.get(i),paint);
            }
//            for (ProviceItem proviceItem : itemList) {
//                if (proviceItem != select) {
//                    proviceItem.drawItem(canvas, paint, false);
//                }
//            }
//            if (select != null) {
//                select.drawItem(canvas, paint, true);
//            }
        }
    }

    private Thread loadThread=new Thread() {
        @Override
        public void run() {
            InputStream inputStream = context.getResources().openRawResource(R.raw.china);
            List<ProviceItem> list = new ArrayList<>();
            try {

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  //??????DocumentBuilderFactory??????
                DocumentBuilder builder = null; //???factory??????DocumentBuilder??????
                builder = factory.newDocumentBuilder();
                Document doc = builder.parse(inputStream);   //??????????????? ??????Document??????
                Element rootElement = doc.getDocumentElement();
                NodeList items = rootElement.getElementsByTagName("path");
//                ???????????????  ??????
                float left = -1;
                float right = -1;
                float top = -1;
                float bottom = -1;
                for (int i = 0; i < items.getLength(); i++) {
                    Element element = (Element) items.item(i);
                    String pathData = element.getAttribute("android:pathData");
                    Path path = PathParser.createPathFromPathData(pathData);
                    PathMeasure pathMeasure = new PathMeasure();
                    pathMeasure.setPath(path, true);
                    pathMeasureList.add(pathMeasure);
                    Path dst = new Path();
                    dstList.add(dst);
//                    ????????????
                    RectF rect = new RectF();
                    path.computeBounds(rect, true);
                    left = left == -1 ? rect.left : Math.min(left, rect.left);
                    right = right == -1 ? rect.right : Math.max(right, rect.right);
                    top = top == -1 ? rect.top : Math.min(top, rect.top);
                    bottom = bottom == -1 ? rect.bottom : Math.max(bottom, rect.bottom);
                    totalRect = new RectF(left, top, right, bottom);
                }
                handler.sendEmptyMessage(1);

            } catch (Exception e) {
                e.printStackTrace();
            }


        };

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (itemList == null) {
                    return;
                }
                int totalNumber = itemList.size();
                for (int i = 0; i < totalNumber; i++) {
                    int color = Color.WHITE;
                    int flag = i % 4;
                    switch (flag) {
                        case 1:
                            color = colorArray[0];
                            break;
                        case 2:
                            color = colorArray[1];
                            break;
                        case 3:
                            color = colorArray[2];
                            break;
                        default:
                            color = Color.BLACK;
                            break;
                    }
                    itemList.get(i).setDrawColor(color);
                }
                requestLayout();

            }

        };

    };
}
