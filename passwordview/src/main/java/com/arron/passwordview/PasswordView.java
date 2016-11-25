package com.arron.passwordview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Arron on 2016/11/21 0021.
 * 密码输入框
 */

public class PasswordView extends View {

    private Mode mode; //样式模式
    private int passwordLength;//密码个数
    private long cursorFlashTime;//光标闪动间隔时间
    private int passwordPadding;//每个密码间的间隔
    private int passwordSize;//单个密码大小
    private int borderColor;//边框颜色
    private int borderWidth;//下划线粗细
    private int cursorPosition;//光标位置
    private int cursorWidth;//光标粗细
    private int cursorHeight;//光标长度
    private int cursorColor;//光标颜色
    private boolean isCursorShowing;//光标是否正在显示
    private boolean isCursorEnable;//是否开启光标
    private boolean isInputComplete;//是否输入完毕
    private int cipherTextSize;//密文符号大小
    private static String CIPHER_TEXT = "*"; //密文符号
    private boolean isLoop;//控制循环
    private String[] password;//密码数组
    private InputMethodManager inputManager;
    private PasswordListener passwordListener;
    private Paint paint;
    private Thread thread;

    public PasswordView(Context context) {
        super(context);
    }

    public enum Mode {
        /**
         * 下划线样式
         */
        UNDERLINE(0),

        /**
         * 边框样式
         */
        RECT(1);
        private int mode;

        Mode(int mode) {
            this.mode = mode;
        }

        public int getMode() {
            return this.mode;
        }

        static Mode formMode(int mode) {
            for (Mode m : values()) {
                if (mode == m.mode) {
                    return m;
                }
            }
            throw new IllegalArgumentException();
        }
    }

    /**
     * 当前只支持从xml中构建该控件
     *
     * @param context
     * @param attrs
     */
    public PasswordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        readAttribute(attrs);
    }

    private void readAttribute(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PasswordView);
            mode = Mode.formMode(typedArray.getInteger(R.styleable.PasswordView_mode, Mode.UNDERLINE.getMode()));
            passwordLength = typedArray.getInteger(R.styleable.PasswordView_passwordLength, 4);
            passwordSize = typedArray.getDimensionPixelSize(R.styleable.PasswordView_passwordSize, dp2px(25));
            cursorFlashTime = typedArray.getInteger(R.styleable.PasswordView_cursorFlashTime, 500);
            borderWidth = typedArray.getDimensionPixelSize(R.styleable.PasswordView_borderWidth, dp2px(3));
            borderColor = typedArray.getColor(R.styleable.PasswordView_borderColor, Color.BLACK);
            cursorColor = typedArray.getColor(R.styleable.PasswordView_cursorColor, Color.GRAY);
            isCursorEnable = typedArray.getBoolean(R.styleable.PasswordView_isCursorEnable, true);
            cipherTextSize = typedArray.getDimensionPixelSize(R.styleable.PasswordView_cipherTextSize, sp2px(passwordSize / 2));
            //如果为边框样式，则padding 默认置为0
            if (mode == Mode.UNDERLINE) {
                passwordPadding = typedArray.getDimensionPixelSize(R.styleable.PasswordView_passwordPadding, dp2px(15));
            } else {
                passwordPadding = typedArray.getDimensionPixelSize(R.styleable.PasswordView_passwordPadding, 0);
            }
            typedArray.recycle();
        }
        cursorWidth = dp2px(2);
        cursorHeight = passwordSize / 2;
        password = new String[passwordLength];
        init();
    }

    private void init() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        MyKeyListener MyKeyListener = new MyKeyListener();
        setOnKeyListener(MyKeyListener);
        inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        paint = new Paint();
        paint.setAntiAlias(true);
        thread = new Thread(cursorFlash);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = 0;
        switch (widthMode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                width = passwordSize * passwordLength + passwordPadding * (passwordLength - 1);
                break;
            case MeasureSpec.EXACTLY:
                width = MeasureSpec.getSize(widthMeasureSpec);
                if (passwordSize * passwordLength + passwordPadding * (passwordLength - 1) > width) {
                    passwordSize = width / passwordLength - passwordPadding;
                }
                break;
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = 0;
        switch (heightMode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                height = passwordSize;
                break;
            case MeasureSpec.EXACTLY:
                height = MeasureSpec.getSize(heightMeasureSpec);
                break;
        }
        setMeasuredDimension(width, height);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mode == Mode.UNDERLINE) {
            drawUnderLine(canvas, paint);
        } else {
            drawRect(canvas, paint);
        }
        drawCursor(canvas, paint);
        drawCipherText(canvas, paint);
    }

    class MyKeyListener implements OnKeyListener {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            int action = event.getAction();
            if (action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    /**
                     * 删除操作
                     */
                    if (TextUtils.isEmpty(password[0])) {
                        return true;
                    }
                    delete();
                    if (passwordListener != null) {
                        passwordListener.passwordChange(getPassword(), isInputComplete);
                    }
                    postInvalidate();
                    return true;
                }
                if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                    /**
                     * 只支持数字
                     */
                    if (isInputComplete) {
                        return true;
                    }
                    add((keyCode - 7) + "");
                    if (passwordListener != null) {
                        passwordListener.passwordChange(getPassword(), isInputComplete);
                    }
                    postInvalidate();
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    /**
                     * 确认键
                     */
                    if (passwordListener != null) {
                        passwordListener.passwordEnter(getPassword(), isInputComplete);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 删除
     */
    private void delete() {
        if (cursorPosition > 0) {
            password[cursorPosition - 1] = null;
            cursorPosition--;
        } else if (cursorPosition == 0) {
            password[cursorPosition] = null;
        }
        isInputComplete = false;
    }

    /**
     * 增加
     */
    private void add(String c) {
        if (cursorPosition < passwordLength) {
            password[cursorPosition] = c;
            cursorPosition++;
            if (cursorPosition == passwordLength) {
                isInputComplete = true;
            }
        }
    }

    /**
     * 获取密码
     */
    private String getPassword() {
        StringBuffer stringBuffer = new StringBuffer();
        for (String c : password) {
            if (TextUtils.isEmpty(c)) {
                continue;
            }
            stringBuffer.append(c);
        }
        return stringBuffer.toString();
    }

    /**
     * 绘制密码替代符号
     *
     * @param canvas
     * @param paint
     */
    private void drawCipherText(Canvas canvas, Paint paint) {
        paint.setColor(Color.GRAY);
        paint.setTextSize(cipherTextSize);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);
        Rect r = new Rect();
        canvas.getClipBounds(r);
        int cHeight = r.height();
        paint.getTextBounds(CIPHER_TEXT, 0, CIPHER_TEXT.length(), r);
        float y = cHeight / 2f + r.height() / 2f - r.bottom;
        for (int i = 0; i < password.length; i++) {
            if (!TextUtils.isEmpty(password[i])) {
                canvas.drawText(CIPHER_TEXT,
                        (getPaddingLeft() + passwordSize / 2) + (passwordSize + passwordPadding) * i,
                        getPaddingTop() + y, paint);
            }
        }
    }

    /**
     * 绘制光标
     *
     * @param canvas
     * @param paint
     */
    private void drawCursor(Canvas canvas, Paint paint) {
        paint.setColor(cursorColor);
        paint.setStrokeWidth(cursorWidth);
        paint.setStyle(Paint.Style.FILL);
        if (isCursorShowing && isCursorEnable && !isInputComplete && hasFocus()) {
            canvas.drawLine((getPaddingLeft() + passwordSize / 2) + (passwordSize + passwordPadding) * cursorPosition, getPaddingTop() + (passwordSize - cursorHeight) / 2,
                    (getPaddingLeft() + passwordSize / 2) + (passwordSize + passwordPadding) * cursorPosition, getPaddingTop() + (passwordSize + cursorHeight) / 2,
                    paint);
        }
    }

    /**
     * 绘制密码框下划线
     *
     * @param canvas
     * @param paint
     */
    private void drawUnderLine(Canvas canvas, Paint paint) {
        paint.setColor(borderColor);
        paint.setStrokeWidth(borderWidth);
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < passwordLength; i++) {
            canvas.drawLine(getPaddingLeft() + (passwordSize + passwordPadding) * i, getPaddingTop() + passwordSize,
                    getPaddingLeft() + (passwordSize + passwordPadding) * i + passwordSize, getPaddingTop() + passwordSize,
                    paint);
        }
    }

    private void drawRect(Canvas canvas, Paint paint) {
        paint.setColor(borderColor);
        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.STROKE);
        Rect rect;
        for (int i = 0; i < passwordLength; i++) {
            int startX = getPaddingLeft() + (passwordSize + passwordPadding) * i;
            int startY = getPaddingTop();
            int stopX = getPaddingLeft() + (passwordSize + passwordPadding) * i + passwordSize;
            int stopY = getPaddingTop() + passwordSize;
            rect = new Rect(startX, startY, stopX, stopY);
            canvas.drawRect(rect, paint);
        }
    }

    /**
     * 光标闪动
     */
    Runnable cursorFlash = new Runnable() {
        @Override
        public void run() {
            try {
                while (isLoop) {
                    Thread.sleep(cursorFlashTime);
                    isCursorShowing = !isCursorShowing;
                    postInvalidate();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            /**
             * 弹出软键盘
             */
            requestFocus();
            inputManager.showSoftInput(this, InputMethodManager.SHOW_FORCED);
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isLoop = true;
        thread.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isLoop = false;
    }

    private int dp2px(float dp) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private int sp2px(float spValue) {
        float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER; //输入类型为数字
        return super.onCreateInputConnection(outAttrs);
    }

    public void setPasswordListener(PasswordListener passwordListener) {
        this.passwordListener = passwordListener;
    }

    public void setPasswordSize(int passwordSize) {
        this.passwordSize = passwordSize;
        postInvalidate();
    }

    public void setPasswordLength(int passwordLength) {
        this.passwordLength = passwordLength;
        postInvalidate();
    }

    public void setCursorColor(int cursorColor) {
        this.cursorColor = cursorColor;
        postInvalidate();
    }

    public void setCursorEnable(boolean cursorEnable) {
        isCursorEnable = cursorEnable;
        postInvalidate();
    }

    /**
     * 密码监听者
     */
    public interface PasswordListener {
        /**
         * 输入/删除监听
         *
         * @param password   密码
         * @param isComplete 是否完成
         */
        void passwordChange(String password, boolean isComplete);

        /**
         * 确认键后的回调
         *
         * @param password   密码
         * @param isComplete 是否完成
         */
        void passwordEnter(String password, boolean isComplete);

    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putStringArray("password", password);
        bundle.putInt("cursorPosition", cursorPosition);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            password = bundle.getStringArray("password");
            cursorPosition = bundle.getInt("cursorPosition");
            state = bundle.getParcelable("superState");
        }
        super.onRestoreInstanceState(state);
    }
}
