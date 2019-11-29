package com.stomhong.library;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class KeyboardUtil {

    private Context mContext;
    private int widthPixels;
    private Activity mActivity;
    private PpKeyBoardView keyboardView;
    public static Keyboard abcKeyboard;// 字母键盘
    public static Keyboard symbolKeyboard;// 字母键盘
    public static Keyboard numKeyboard;// 数字键盘
    public static Keyboard keyboard;//提供给keyboardView 进行画

    public boolean isupper = false;// 是否大写
    public boolean isShow = false;
    InputFinishListener inputOver;
    KeyBoardStateChangeListener keyBoardStateChangeListener;
    private View layoutView;
    private View keyBoardLayout;

    // 开始输入的键盘状态设置
    public static int inputType = 1;// 默认
    public static final int INPUTTYPE_NUM = 1; // 数字，右下角 为空
    public static final int INPUTTYPE_NUM_FINISH = 2;// 数字，右下角 完成
    public static final int INPUTTYPE_NUM_POINT = 3; // 数字，右下角 为点
    public static final int INPUTTYPE_NUM_X = 4; // 数字，右下角 为X
    public static final int INPUTTYPE_NUM_NEXT = 5; // 数字，右下角 为下一个

    public static final int INPUTTYPE_ABC = 6;// 一般的abc
    public static final int INPUTTYPE_SYMBOL = 7;// 标点键盘
    public static final int INPUTTYPE_NUM_ABC = 8; // 数字，右下角 为下一个

    public static final int KEYBOARD_SHOW = 1;
    public static final int KEYBOARD_HIDE = 2;

    private EditText ed;
    private Handler mHandler;
    private Handler showHandler;
    private ScrollView sv_main;
    private View root_view;
    private int scrollTo = 0;
    private KeyboardUtil mKeyboardUtil;
    private TextView keyboard_tips_tv;
    private static final float TIPS_MARGIN_W = 0.0407f;
    private View inflaterView;
    private ImageView mIVClose;

    /**
     * 最新构造方法，现在都用这个
     *
     * @param ctx
     * @param rootView rootView 需要是LinearLayout,以适应键盘
     */
    public KeyboardUtil(Context ctx, LinearLayout rootView, ScrollView scrollView) {
        this.mContext = ctx;
        this.mActivity = (Activity) mContext;
        widthPixels = mContext.getResources().getDisplayMetrics().widthPixels;
        initKeyBoardView(rootView);
        initScrollHandler(rootView, scrollView);
        mKeyboardUtil = this;
    }

    /**
     * 弹框类，用这个
     *
     * @param view 是弹框的inflaterView
     */
    public KeyboardUtil(View view, Context ctx, LinearLayout root_View, ScrollView scrollView) {
        this(ctx, root_View, scrollView);
        this.inflaterView = view;
    }

    //设置监听事件
    public void setInputOverListener(InputFinishListener listener) {
        this.inputOver = listener;
    }

    public static Keyboard getKeyBoardType() {
        return keyboard;
    }

    private void initKeyBoardView(LinearLayout rootView) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        keyBoardLayout = inflater.inflate(R.layout.input, null);

        keyBoardLayout.setVisibility(View.GONE);
        keyBoardLayout.setBackgroundColor(mActivity.getResources().getColor(R.color.product_list_bac));
        initLayoutHeight((LinearLayout) keyBoardLayout);
        this.layoutView = keyBoardLayout;
        rootView.addView(keyBoardLayout);

        if (keyBoardLayout != null && keyBoardLayout.getVisibility() == View.VISIBLE)
            Log.d("KeyboardUtil", "visible");
    }

    public void initLayoutHeight(LinearLayout layoutView) {
        LinearLayout.LayoutParams keyboard_layoutlLayoutParams = (LinearLayout.LayoutParams) layoutView
                .getLayoutParams();
        RelativeLayout TopLayout = (RelativeLayout) layoutView.findViewById(R.id.keyboard_view_top_rl);
        mIVClose = (ImageView) layoutView.findViewById(R.id.iv_close);
        mIVClose.setOnClickListener(new finishListener());
//        keyboard_tips_tv = (TextView) layoutView.findViewById(R.id.keyboard_tips_tv);
//        TextView keyboard_view_finish = (TextView) layoutView.findViewById(R.id.keyboard_view_finish);
//        setMargins(keyboard_tips_tv, (int) (widthPixels * TIPS_MARGIN_W), 0, 0, 0);
//        keyboard_tips_tv.setVisibility(View.VISIBLE);
//        setMargins(keyboard_view_finish, 0, 0, (int) (widthPixels * TIPS_MARGIN_W), 0);
//        keyboard_view_finish.setOnClickListener(new finishListener());
        if (keyboard_layoutlLayoutParams == null) {
            int height = (int) (mActivity.getResources().getDisplayMetrics().heightPixels * SIZE.KEYBOARY_H);
            layoutView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, height));
        } else {
            keyboard_layoutlLayoutParams.height = (int) (mActivity.getResources().getDisplayMetrics().heightPixels * SIZE.KEYBOARY_H);
        }

        LinearLayout.LayoutParams TopLayoutParams = (LinearLayout.LayoutParams) TopLayout
                .getLayoutParams();

        if (TopLayoutParams == null) {
            int height = (int) (mActivity.getResources().getDisplayMetrics().heightPixels * SIZE.KEYBOARY_T_H);
            TopLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, height));
        } else {
            TopLayoutParams.height = (int) (mActivity.getResources().getDisplayMetrics().heightPixels * SIZE.KEYBOARY_T_H);
        }
    }

    private void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view
                    .getLayoutParams();
            layoutParams.setMargins(left, top, right, bottom);
        } else if (view.getLayoutParams() instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view
                    .getLayoutParams();
            layoutParams.setMargins(left, top, right, bottom);
        }
    }

    public boolean setKeyBoardCursorNew(EditText edit) {
        this.ed = edit;
        boolean flag = false;

        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();// isOpen若返回true，则表示输入法打开
        if (isOpen) {
//			((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
            if (imm.hideSoftInputFromWindow(edit.getWindowToken(), 0))
                flag = true;
        }

//		act.getWindow().setSoftInputMode(
//				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        String methodName = null;
        if (currentVersion >= 16) {
            // 4.2
            methodName = "setShowSoftInputOnFocus";
        } else if (currentVersion >= 14) {
            // 4.0
            methodName = "setSoftInputShownOnFocus";
        }

        if (methodName == null) {
            edit.setInputType(InputType.TYPE_NULL);
        } else {
            Class<EditText> cls = EditText.class;
            Method setShowSoftInputOnFocus;
            try {
                setShowSoftInputOnFocus = cls.getMethod(methodName,
                        boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(edit, false);
            } catch (NoSuchMethodException e) {
                edit.setInputType(InputType.TYPE_NULL);
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    public void hideSystemKeyBoard() {
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(keyBoardLayout.getWindowToken(), 0);
    }

    public void hideAllKeyBoard() {
        hideSystemKeyBoard();
        hideKeyboardLayout();
    }


    public int getInputType() {
        return this.inputType;
    }

    public boolean getKeyboardState() {
        return this.isShow;
    }

    public EditText getEd() {
        return ed;
    }


    //初始化滑动handler
    @SuppressLint("HandlerLeak")
    private void initScrollHandler(View rootView, ScrollView scrollView) {
        this.sv_main = scrollView;
        this.root_view = rootView;
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == ed.getId()) {
                    if (sv_main != null)
                        sv_main.smoothScrollTo(0, scrollTo);
                }
            }
        };
    }

    //滑动监听
    private void keyBoardScroll(final EditText editText, int scorllTo) {
        this.scrollTo = scorllTo;
        ViewTreeObserver vto_bighexagon = root_view.getViewTreeObserver();
        vto_bighexagon.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Message msg = new Message();
                msg.what = editText.getId();
                mHandler.sendMessageDelayed(msg, 500);
                // // 防止多次促发
                root_view.getViewTreeObserver()
                        .removeGlobalOnLayoutListener(this);
            }
        });
    }

    //设置一些不需要使用这个键盘的edittext,解决切换问题
    public void setOtherEdittext(EditText... edittexts) {
        for (EditText editText : edittexts) {
            editText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        //防止没有隐藏键盘的情况出现
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hideKeyboardLayout();
                            }
                        }, 300);
                        ed = (EditText) v;
                        hideKeyboardLayout();
                    }
                    return false;
                }
            });
        }
    }

    class finishListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            hideKeyboardLayout();
        }
    }


    private OnKeyboardActionListener listener = new OnKeyboardActionListener() {
        @Override
        public void swipeUp() {
        }

        @Override
        public void swipeRight() {
        }

        @Override
        public void swipeLeft() {
        }

        @Override
        public void swipeDown() {
        }

        @Override
        public void onText(CharSequence text) {
            if (ed == null)
                return;
            Editable editable = ed.getText();
//			if (editable.length()>=20)
//				return;
            int start = ed.getSelectionStart();
            int end = ed.getSelectionEnd();
            String temp = editable.subSequence(0, start) + text.toString() + editable.subSequence(start, editable.length());
            ed.setText(temp);
            Editable etext = ed.getText();
            Selection.setSelection(etext, start + 1);
        }

        @Override
        public void onRelease(int primaryCode) {
            if (inputType != KeyboardUtil.INPUTTYPE_NUM_ABC
                    && (primaryCode == Keyboard.KEYCODE_SHIFT)) {
                keyboardView.setPreviewEnabled(true);
            }
        }

        @Override
        public void onPress(int primaryCode) {
            if (inputType == KeyboardUtil.INPUTTYPE_NUM_ABC ||
                    inputType == KeyboardUtil.INPUTTYPE_NUM ||
                    inputType == KeyboardUtil.INPUTTYPE_NUM_POINT ||
                    inputType == KeyboardUtil.INPUTTYPE_NUM_FINISH ||
                    inputType == KeyboardUtil.INPUTTYPE_NUM_NEXT ||
                    inputType == KeyboardUtil.INPUTTYPE_NUM_X) {
                keyboardView.setPreviewEnabled(false);
                return;
            }
            if (primaryCode == Keyboard.KEYCODE_SHIFT
                    || primaryCode == Keyboard.KEYCODE_DELETE
                    || primaryCode == 123123
                    || primaryCode == 456456
                    || primaryCode == 789789
                    || primaryCode == 32) {
                keyboardView.setPreviewEnabled(false);
                return;
            }
            keyboardView.setPreviewEnabled(true);
            return;
        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            Editable editable = ed.getText();
            int start = ed.getSelectionStart();
            if (primaryCode == Keyboard.KEYCODE_CANCEL) {// 收起
                hideKeyboardLayout();
                if (inputOver != null)
                    inputOver.inputHasOver(primaryCode, ed);
            } else if (primaryCode == Keyboard.KEYCODE_DELETE) {// 回退
                if (editable != null && editable.length() > 0) {
                    if (start > 0) {
                        editable.delete(start - 1, start);
                    }
                }
            } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {// 大小写切换

                changeKey();
                keyboardView.setKeyboard(abcKeyboard);

            } else if (primaryCode == Keyboard.KEYCODE_DONE) {// 完成 and 下一个
                if (keyboardView.getRightType() == 4) {
                    hideKeyboardLayout();
                    if (inputOver != null)
                        inputOver.inputHasOver(keyboardView.getRightType(), ed);
                } else if (keyboardView.getRightType() == 5) {
                    // 下一个监听

                    if (inputOver != null)
                        inputOver.inputHasOver(keyboardView.getRightType(), ed);
                }
            } else if (primaryCode == 0) {
                // 空白键
            } else if (primaryCode == 123123) {//转换数字键盘
                isupper = false;
                showKeyBoardLayout(ed, INPUTTYPE_NUM_ABC, -1);
            } else if (primaryCode == 456456) {//转换字母键盘
                isupper = false;
                showKeyBoardLayout(ed, INPUTTYPE_ABC, -1);
            } else if (primaryCode == 789789) {//转换字符键盘
                isupper = false;
                showKeyBoardLayout(ed, INPUTTYPE_SYMBOL, -1);
            } else if (primaryCode == 741741) {
                showKeyBoardLayout(ed, INPUTTYPE_ABC, -1);
            } else {
                editable.insert(start, Character.toString((char) primaryCode));
            }
        }
    };

    /**
     * 键盘大小写切换
     */
    private void changeKey() {
        List<Key> keylist = abcKeyboard.getKeys();
        if (isupper) {// 大写切小写
            isupper = false;
            for (Key key : keylist) {
                if (key.label != null && isword(key.label.toString())) {
                    key.label = key.label.toString().toLowerCase();
                    key.codes[0] = key.codes[0] + 32;
                }
            }
        } else {// 小写切大写
            isupper = true;
            for (Key key : keylist) {
                if (key.label != null && isword(key.label.toString())) {
                    key.label = key.label.toString().toUpperCase();
                    key.codes[0] = key.codes[0] - 32;
                }
            }
        }
    }

    public void showKeyboard() {
        if (keyboardView != null) {
            keyboardView.setVisibility(View.GONE);
        }
        initInputType();
        isShow = true;
        keyboardView.setVisibility(View.VISIBLE);
    }

    private void initKeyBoard(int keyBoardViewID) {
        mActivity = (Activity) mContext;
        if (inflaterView != null) {
            keyboardView = (PpKeyBoardView) inflaterView.findViewById(keyBoardViewID);
        } else {
            keyboardView = (PpKeyBoardView) mActivity
                    .findViewById(keyBoardViewID);
        }
        keyboardView.setEnabled(true);
        keyboardView.setOnKeyboardActionListener(listener);
        keyboardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    return true;
                }
                return false;
            }
        });
    }

    private Key getCodes(int i) {
        return keyboardView.getKeyboard().getKeys().get(i);
    }

    private void initInputType() {
        if (inputType == INPUTTYPE_NUM) {
            // isnum = true;
            initKeyBoard(R.id.keyboard_view);
            keyboardView.setPreviewEnabled(false);
            numKeyboard = new Keyboard(mContext, R.xml.symbols);
            setMyKeyBoard(numKeyboard);
        } else if (inputType == INPUTTYPE_NUM_FINISH) {
            initKeyBoard(R.id.keyboard_view);
            keyboardView.setPreviewEnabled(false);
            numKeyboard = new Keyboard(mContext, R.xml.symbols_finish);
            setMyKeyBoard(numKeyboard);
        } else if (inputType == INPUTTYPE_NUM_POINT) {
            initKeyBoard(R.id.keyboard_view);
            keyboardView.setPreviewEnabled(false);
            numKeyboard = new Keyboard(mContext, R.xml.symbols_point);
            setMyKeyBoard(numKeyboard);
        } else if (inputType == INPUTTYPE_NUM_X) {
            initKeyBoard(R.id.keyboard_view);
            keyboardView.setPreviewEnabled(false);
            numKeyboard = new Keyboard(mContext, R.xml.symbols_x);
            setMyKeyBoard(numKeyboard);
        } else if (inputType == INPUTTYPE_NUM_NEXT) {
            initKeyBoard(R.id.keyboard_view);
            keyboardView.setPreviewEnabled(false);
            numKeyboard = new Keyboard(mContext, R.xml.symbols_next);
            setMyKeyBoard(numKeyboard);
        } else if (inputType == INPUTTYPE_ABC) {
            // isnum = false;
            initKeyBoard(R.id.keyboard_view_abc_sym);
            keyboardView.setPreviewEnabled(true);
            abcKeyboard = new Keyboard(mContext, R.xml.symbols_abc);
            setMyKeyBoard(abcKeyboard);
        } else if (inputType == INPUTTYPE_SYMBOL) {
            initKeyBoard(R.id.keyboard_view_abc_sym);
            keyboardView.setPreviewEnabled(true);
            symbolKeyboard = new Keyboard(mContext, R.xml.symbols_symbol);
            setMyKeyBoard(symbolKeyboard);
        } else if (inputType == INPUTTYPE_NUM_ABC) {
            initKeyBoard(R.id.keyboard_view);
            keyboardView.setPreviewEnabled(false);
            numKeyboard = new Keyboard(mContext, R.xml.symbols_num_abc);
            setMyKeyBoard(numKeyboard);
        }

    }

    private void setMyKeyBoard(Keyboard newkeyboard) {
        keyboard = newkeyboard;
        keyboardView.setKeyboard(newkeyboard);
    }

    //新的隐藏方法
    public void hideKeyboardLayout() {
        if (getKeyboardState() == true) {
            if (keyBoardLayout != null)
                keyBoardLayout.setVisibility(View.GONE);
            if (keyBoardStateChangeListener != null)
                keyBoardStateChangeListener.KeyBoardStateChange(KEYBOARD_HIDE, ed);
            isShow = false;
            hideKeyboard();
            ed = null;
        }
    }

    /**
     * @param editText
     * @param keyBoardType 类型
     * @param scrollTo     滑动到某个位置,可以是大于等于0的数，其他数不滑动
     */
    //新的show方法
    public void showKeyBoardLayout(final EditText editText, int keyBoardType, int scrollTo) {
        if (editText.equals(ed) && getKeyboardState() == true && this.inputType == keyBoardType)
            return;

        this.inputType = keyBoardType;
        this.scrollTo = scrollTo;
        //TODO
        if (keyBoardLayout != null && keyBoardLayout.getVisibility() == View.VISIBLE)
            Log.d("KeyboardUtil", "visible");

        if (setKeyBoardCursorNew(editText)) {
            showHandler = new Handler();
            showHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    show(editText);
                }
            }, 400);
        } else {
            //直接显示
            show(editText);
        }
    }

    private void show(EditText editText) {
        this.ed = editText;
        if (keyBoardLayout != null)
            keyBoardLayout.setVisibility(View.VISIBLE);
        showKeyboard();
        if (keyBoardStateChangeListener != null)
            keyBoardStateChangeListener.KeyBoardStateChange(KEYBOARD_SHOW, editText);
        //用于滑动
        if (scrollTo >= 0) {
            keyBoardScroll(editText, scrollTo);
        }
    }

    private void hideKeyboard() {
        isShow = false;
        if (keyboardView != null) {
            int visibility = keyboardView.getVisibility();
            if (visibility == View.VISIBLE) {
                keyboardView.setVisibility(View.INVISIBLE);
            }
        }
        if (layoutView != null) {
            layoutView.setVisibility(View.GONE);
        }
    }

    private boolean isword(String str) {
        String wordstr = "abcdefghijklmnopqrstuvwxyz";
        if (wordstr.indexOf(str.toLowerCase()) > -1) {
            return true;
        }
        return false;
    }

    /**
     * @description:TODO 输入监听
     */
    public interface InputFinishListener {
        public void inputHasOver(int onclickType, EditText editText);
    }

    /**
     * 监听键盘变化
     */
    public interface KeyBoardStateChangeListener {
        public void KeyBoardStateChange(int state, EditText editText);
    }

    public void setKeyBoardStateChangeListener(KeyBoardStateChangeListener listener) {
        this.keyBoardStateChangeListener = listener;
    }

}
