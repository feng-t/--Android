package com.hu.connetchess.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hu.connetchess.R;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @program: ConnetChess
 * @description: 棋盘
 * @author: feng-t
 * @email: 137714477@qq.com
 * @github: {@see <a href="https://github.com/feng-t">https://github.com/feng-t</a>}
 * @carete: 2018-08-21
 */
public class ChessBoard extends View {
    private static final String TAG = "ChessBoard";
    private Paint paint = new Paint();
    private int mChessWidth;
    private float LineHeight;//间隔高度
    private int MAX_CHESS = 0;//棋格
    private int WINNUMER = 0;
    private float FLAGS_PROPORTION = 3.0f / 5;//旗子比例
    private Map<Boolean, LinkedList<Point>> Po = new HashMap();
    private int mPintColor = 0xff000000;
    private boolean toYou = true;
    private boolean gameing = true;
    private int winner = 1;//赢家
    private LinkedList<Point> wP = new LinkedList(), bP = new LinkedList();
    private ExecutorService pool = Executors.newSingleThreadExecutor();
    private LinkedList<Point> tempPieces = new LinkedList<>();


    public ChessBoard(Context context) {
        this(context, null);
    }

    public ChessBoard(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChessBoard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAttr(context, attrs);
        init();

    }

    /**
     * 设置自定义属性
     *
     * @param context
     * @param attrs
     */
    private void setAttr(Context context, @Nullable AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ChessBoard);
        MAX_CHESS = ta.getInteger(R.styleable.ChessBoard_max_chess, 10);
        WINNUMER = ta.getInteger(R.styleable.ChessBoard_winnumer, 5);
        ta.recycle();
    }

    private void init() {
        this.setBackgroundColor(0xffb1b1b1);
        paint.setColor(mPintColor);
        paint.setAntiAlias(true);//抗锯齿
        paint.setDither(true);//防抖动
        Po.put(true, wP);
        Po.put(false, bP);
        paint.setTextSize(60f);
        paint.setStyle(Paint.Style.FILL);

    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawText("当前" + (toYou ? "白子" : "黑子") + "下", LineHeight, LineHeight * MAX_CHESS, paint);
        paint.setStyle(Paint.Style.STROKE);

        String start = "重新开始";
        canvas.drawText(start, LineHeight, LineHeight * (MAX_CHESS + 2), paint);
        canvas.drawRect(LineHeight, LineHeight * (MAX_CHESS + 1), LineHeight * 5, (float) (LineHeight * ((float) MAX_CHESS + 2.5)), paint);

        String que = "落在此处";
        canvas.drawText(que, (mChessWidth - (LineHeight * 5)), LineHeight * (MAX_CHESS + 2), paint);
        canvas.drawRect((mChessWidth - (LineHeight * 5)), LineHeight * (MAX_CHESS + 1), mChessWidth - LineHeight, (float) (LineHeight * ((float) MAX_CHESS + 2.5)), paint);


        paint.setStyle(Paint.Style.FILL);
        drawPanin(canvas);
        drawPieces(canvas);
        checkGameover();
        if (!gameing) {

            String gameover = "游戏结束";
            String rq = winner == 1 ? "你赢了" : (winner == 2 ? "你输了~~~" : "平局");
            float gameoverWidth = paint.measureText(gameover);
            float rqWidth = paint.measureText(rq);

            float gameoverx = (getWidth() - gameoverWidth) / 2;
            float rqx = (getWidth() - rqWidth) / 2;
            canvas.drawText(gameover, gameoverx, mChessWidth / 2, paint);
            canvas.drawText(rq, rqx, mChessWidth / 2 + LineHeight * 2, paint);


        }

    }


    /**
     * 绘制棋子
     *
     * @param canvas
     */
    private void drawPieces(Canvas canvas) {
        int white = 0xffffffff;
        int radius = (int) ((1 - FLAGS_PROPORTION) * LineHeight);
        LinkedList<Point> wPiecess = Po.get(true);
        LinkedList<Point> bPiecess = Po.get(false);

        for (Point p : tempPieces) {
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(p.x * LineHeight, p.y * LineHeight, radius, paint);
            paint.setStyle(Paint.Style.FILL);
        }


        paint.setColor(white);
        for (Point p : wPiecess) {
            canvas.drawCircle(p.x * LineHeight, p.y * LineHeight, radius, paint);
        }
        paint.setColor(mPintColor);
        for (Point p : bPiecess) {
            canvas.drawCircle(p.x * LineHeight, p.y * LineHeight, radius, paint);
        }
    }

    //绘制棋盘
    private void drawPanin(Canvas canvas) {
        int w = mChessWidth;
        for (int i = 0; i < MAX_CHESS; i++) {
            int startx = (int) LineHeight;
            int endx = (int) (w - LineHeight);
            int y = (int) (i * LineHeight);
            canvas.drawLine(startx, y, endx, y, paint);
            canvas.drawLine(y, startx, y, endx, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) (event.getX() / LineHeight);
        int y = (int) (event.getY() / LineHeight);
        Point p = new Point(x, y);
        if (event.getY() > LineHeight * (MAX_CHESS + 1) &&
                event.getY() < (LineHeight * ((float) MAX_CHESS + 2.5))) {
            if (event.getX() > LineHeight &&
                    event.getX() < LineHeight * 5 ) {
                tempPieces.clear();
                winner = -1;
                gameing = true;
                bP.clear();
                wP.clear();
                toYou = true;
                invalidate();
                return false;
            }if(!tempPieces.isEmpty()&&event.getX() > (mChessWidth - (LineHeight * 5)) &&
                    event.getX() < (mChessWidth - LineHeight)){

                if (toYou) {
                    wP.add(tempPieces.removeFirst());
                    roBoot();
                }else{
                    bP.add(tempPieces.removeFirst());
                }toYou = !toYou;
                invalidate();
            }
        }

        if (mIsWithinBoard(x, y)) {
            return false;
        }
        if (wP.contains(p) || bP.contains(p)) {
            return false;
        }
        if (!gameing) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE: {
                tempPieces.clear();

                tempPieces.addFirst(p);

                invalidate();
                break;
            }
            case MotionEvent.ACTION_UP:

                break;
        }

        return true;
    }

    private void checkGameover() {
        boolean wWin = checkFive(wP);
        boolean bWin = checkFive(bP);
        if (wWin || bWin) {
            gameing = false;
            winner = wWin ? 1 : 2;
        }
    }

    /**
     * 判断输赢
     */
    private boolean checkFive(LinkedList<Point> p) {
        if (p.size() == 0) {
            return false;
        }
        for (Point pl : p) {
            int x = pl.x;
            int y = pl.y;
            boolean Five = checkHFive(x, y, p);
            if (Five) return true;
            Five = checkVFive(x, y, p);
            if (Five) return true;
            Five = checkLFive(x, y, p);
            if (Five) return true;
            Five = checkFFive(x, y, p);
            if (Five) return true;
        }
        return false;
    }

    /**
     * 横向
     *
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkHFive(int x, int y, LinkedList<Point> points) {
        int count = 1;
        for (int i = 1; i < WINNUMER; i++) {
            //判断是否连成线
            if (points.contains(new Point(x - i, y))) {
                //Log.e(TAG, "checkIsFive: "+);
                count++;
            } else {
                break;
            }
        }
        if (count == WINNUMER) return true;
        for (int i = 1; i < WINNUMER; i++) {
            //判断是否连成线
            if (points.contains(new Point(x + i, y))) {
                //Log.e(TAG, "checkIsFive: "+);
                count++;
            } else {
                break;
            }
        }
        if (count == WINNUMER) return true;
        return false;
    }

    /**
     * 纵向
     *
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkVFive(int x, int y, LinkedList<Point> points) {
        int count = 1;
        for (int i = 1; i < WINNUMER; i++) {
            //判断是否连成线
            if (points.contains(new Point(x, y - i))) {
                //Log.e(TAG, "checkIsFive: "+);
                count++;
            } else {
                break;
            }
        }
        if (count == WINNUMER) return true;
        for (int i = 1; i < WINNUMER; i++) {
            //判断是否连成线
            if (points.contains(new Point(x, y + i))) {
                //Log.e(TAG, "checkIsFive: "+);
                count++;
            } else {
                break;
            }
        }
        if (count == WINNUMER) return true;
        return false;
    }

    /**
     * 左斜
     *
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkLFive(int x, int y, LinkedList<Point> points) {
        int count = 1;
        for (int i = 1; i < WINNUMER; i++) {
            //判断是否连成线
            if (points.contains(new Point(x - i, y - i))) {
                //Log.e(TAG, "checkIsFive: "+);
                count++;
            } else {
                break;
            }
        }
        if (count == WINNUMER) return true;
        for (int i = 1; i < WINNUMER; i++) {
            //判断是否连成线
            if (points.contains(new Point(x + i, y + i))) {
                //Log.e(TAG, "checkIsFive: "+);
                count++;
            } else {
                break;
            }
        }
        if (count == WINNUMER) return true;
        return false;
    }

    /**
     * 右斜
     *
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkFFive(int x, int y, LinkedList<Point> points) {
        int count = 1;
        for (int i = 1; i < WINNUMER; i++) {
            //判断是否连成线
            if (points.contains(new Point(x - i, y + i))) {
                //Log.e(TAG, "checkIsFive: "+);
                count++;
            } else {
                break;
            }
        }
        if (count == WINNUMER) return true;
        for (int i = 1; i < WINNUMER; i++) {
            //判断是否连成线
            if (points.contains(new Point(x + i, y - i))) {
                //Log.e(TAG, "checkIsFive: "+);
                count++;
            } else {
                break;
            }
        }
        if (count == WINNUMER) return true;
        return false;
    }

    private void roBoot() {
        pool.execute(() -> {
            LinkedList<Point> queue = new LinkedList<>();
            Point point = new Point();
            queue.addLast(point);
            while (!queue.isEmpty()) {

            }

        });
    }


    /**
     * 绘制一个正方形
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = Math.min(widthSize, heightSize);
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            width = heightSize;
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            width = widthSize;
        }
        setMeasuredDimension(width, (int) (width * 1.2));
    }

    /**
     * 是否在棋盘外
     *
     * @param x
     * @param y
     * @return
     */
    private boolean mIsWithinBoard(int x, int y) {
        return (x <= 0 || y <= 0 || x >= MAX_CHESS || y >= MAX_CHESS);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mChessWidth = w;
        LineHeight = mChessWidth * 1.0f / MAX_CHESS;//将棋盘分割成MAX_CHESS块
    }
}
