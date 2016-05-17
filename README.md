#自定义Android键盘

##由于项目需要临时写了个自定义安全键盘


### 用法：



依赖：
```java

dependencies{
   compile 'com.ziyeyouhu:customizekeyboard:1.0.0'
}
```

初始化  

```java
 private void initMoveKeyBoard() {
         keyboardUtil = new KeyboardUtil(this, rootView, scrollView);
         keyboardUtil.setOtherEdittext(normalEd);
         // monitor the KeyBarod state
         keyboardUtil.setKeyBoardStateChangeListener(new KeyBoardStateListener());
         // monitor the finish or next Key
         keyboardUtil.setInputOverListener(new inputOverListener());
         specialEd.setOnTouchListener(new KeyboardTouchListener(keyboardUtil, KeyboardUtil.INPUTTYPE_ABC, -1));
     }
```

关于键盘输入状态
        
   
```java
     public static int inputType = 1;                   // 默认
     public static final int INPUTTYPE_NUM = 1;         // 数字，右下角 为空
     public static final int INPUTTYPE_NUM_FINISH = 2;  // 数字，右下角 完成
     public static final int INPUTTYPE_NUM_POINT = 3;   // 数字，右下角 为点
     public static final int INPUTTYPE_NUM_X = 4;       // 数字，右下角 为X
     public static final int INPUTTYPE_NUM_NEXT = 5;    // 数字，右下角 为下一个
     public static final int INPUTTYPE_ABC = 6;         // 一般的abc
     public static final int INPUTTYPE_SYMBOL = 7;      // 标点键盘
     public static final int INPUTTYPE_NUM_ABC = 8;     // 数字，右下角 为下一个
```


###效果图如下

<p align="center">
<img src="images/aaa.gif" />
</p>




